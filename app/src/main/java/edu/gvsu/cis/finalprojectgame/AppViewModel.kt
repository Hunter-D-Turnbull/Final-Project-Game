package edu.gvsu.cis.finalprojectgame

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import edu.gvsu.cis.finalprojectgame.ui.theme.CardBlack
import edu.gvsu.cis.finalprojectgame.ui.theme.CardRed
import edu.gvsu.cis.finalprojectgame.ui.theme.MyGreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.EmailAuthProvider

enum class Suit(val value: String) {
    Heart("♥"),
    Diamond("♦"),
    Club("♣"),
    Spade("♠")
}

enum class Rank(val value: String) {
    Ace("A"),
    Two("2"),
    Three("3"),
    Four("4"),
    Five("5"),
    Six("6"),
    Seven("7"),
    Eight("8"),
    Nine("9"),
    Ten("10"),
    Jack("J"),
    Queen("Q"),
    King("K")
}

data class Hand(
    val hand: List<CardClass?>,
    val score: Int
)

data class RemoteGame(
    val id: String = "",
    val finalPoints: Int = 0,
    val totalHands: Int = 0,
    val timestamp: Long = 0
)

data class RemoteHand(
    val handNumber: Int = 0,
    val result: String = "",
    val pointsChange: Int = 0
)

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val _deck = MutableStateFlow(listOf<CardClass?>())
    val deck = _deck.asStateFlow()
    private val _dealerHand = MutableStateFlow(listOf<CardClass?>())
    val dealerHand = _dealerHand.asStateFlow()
    private val _gameInProgress = MutableStateFlow(false)
    val gameInProgress = _gameInProgress.asStateFlow()
    private val _cardsDealt = MutableStateFlow(0)
    val cardsDealt = _cardsDealt.asStateFlow()
    private val _currentBackgroundColor = MutableStateFlow(MyGreen)
    val currentBackgroundColor = _currentBackgroundColor.asStateFlow()
    private val _currentHandTotal = MutableStateFlow(0)
    val currentHandTotal = _currentHandTotal.asStateFlow()
    private val _currentDealerTotal = MutableStateFlow(0)
    val currentDealerTotal = _currentDealerTotal.asStateFlow()
    private val _playerTurnOver = MutableStateFlow(false)
    val playerTurnOver = _playerTurnOver.asStateFlow()
    private val _currentDeck = MutableStateFlow(listOf<CardClass?>())
    val currentDeck = _currentDeck.asStateFlow()
    private val _currentPoints = MutableStateFlow(1000)
    val currentPoints = _currentPoints.asStateFlow()
    private val _numHands = MutableStateFlow(1)
    val numHands = _numHands.asStateFlow()
    private val _totalHandsPlayed = MutableStateFlow(1)
    val totalHandsPlayed = _totalHandsPlayed.asStateFlow()
    private val _allHands = MutableStateFlow(listOf<Hand>())
    val allHands = _allHands.asStateFlow()
    private val _currentHandIndex = MutableStateFlow(0)
    val currentHandIndex = _currentHandIndex.asStateFlow()
    private val _selectedGameId = MutableStateFlow<Int?>(null)
    private val _currentBet = MutableStateFlow(500)
    val currentBet = _currentBet.asStateFlow()
    val _hasPlayed = MutableStateFlow(false)
    val hasPlayed = _hasPlayed.asStateFlow()
    val selectedGameId = _selectedGameId.asStateFlow()
    private var currentGameId: Int? = null
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val _isUserSignedIn = MutableStateFlow(auth.currentUser != null)
    val isUserSignedIn = _isUserSignedIn.asStateFlow()
    private var currentRemoteGameId: String? = null
    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        _isUserSignedIn.value = firebaseAuth.currentUser != null
    }
    private val _remoteGames = MutableStateFlow<List<RemoteGame>>(emptyList())
    val remoteGames = _remoteGames.asStateFlow()
    private val _remoteHands = MutableStateFlow<List<RemoteHand>>(emptyList())
    val remoteHands = _remoteHands.asStateFlow()
    val currentUser get() = auth.currentUser
    private val _selectedRemoteGameId = MutableStateFlow<String?>(null)
    val selectedRemoteGameId = _selectedRemoteGameId.asStateFlow()
    val dao: GameDao =
        (app as MyRoomApplication).myDB.getDao()

    val games = dao.getAllGames()

    init {
        auth.addAuthStateListener(authListener)
    }

    fun signUp(
        email: String,
        password: String,
        name: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val uid = result.user?.uid ?: throw Exception("User creation failed")

                val userData = mapOf(
                    "name" to name,
                    "email" to email
                )

                firestore.collection("users")
                    .document(uid)
                    .set(userData)
                    .await()

                onResult(true, null)

            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    fun signIn(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun changePassword(
        oldPassword: String,
        newPassword: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val user = auth.currentUser
        val email = user?.email

        if (user == null || email == null) {
            onResult(false, "User not logged in")
            return
        }

        viewModelScope.launch {
            try {
                // Re-authenticate with old password
                val credential = EmailAuthProvider.getCredential(email, oldPassword)
                user.reauthenticate(credential).await()

                // Update to new password
                user.updatePassword(newPassword).await()

                onResult(true, null)

            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    fun fetchRemoteGames() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("users")
                    .document(uid)
                    .collection("history")
                    .get()
                    .await()

                val games = snapshot.documents.map { doc ->
                    RemoteGame(
                        id = doc.id,
                        finalPoints = doc.getLong("finalPoints")?.toInt() ?: 0,
                        totalHands = doc.getLong("totalHands")?.toInt() ?: 0,
                        timestamp = doc.getLong("timestamp") ?: 0
                    )
                }.sortedByDescending { it.timestamp }

                _remoteGames.value = games

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fetchRemoteHands(gameId: String) {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("users")
                    .document(uid)
                    .collection("history")
                    .document(gameId)
                    .collection("hands")
                    .get()
                    .await()

                val hands = snapshot.documents.map { doc ->
                    RemoteHand(
                        handNumber = doc.getLong("handNumber")?.toInt() ?: 0,
                        pointsChange = doc.getLong("pointsChange")?.toInt() ?: 0,
                        result = doc.getString("result") ?: "UNKNOWN"
                    )
                }.sortedBy { it.handNumber }

                _remoteHands.value = hands

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun saveHand(hand: Hand) {
        val gameId = currentGameId ?: return

        val result = when {
            hand.score > 21 -> "LOSS"
            else -> "WIN" // or add push logic later
        }

        viewModelScope.launch(Dispatchers.IO) {
            dao.insertHand(
                HandEntity(
                    gameOwnerId = gameId,
                    handNumber = _totalHandsPlayed.value,
                    result = result,
                    pointsChange = _currentBet.value
                )
            )
        }

        // Update number of hands in ViewModel
        _numHands.update { it + 1 }
    }

    fun startRemoteGameSession() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val gameData = mapOf(
                    "totalHands" to 0,
                    "finalPoints" to _currentPoints.value,
                    "timestamp" to System.currentTimeMillis()
                )

                val docRef = firestore.collection("users")
                    .document(uid)
                    .collection("history")
                    .add(gameData)
                    .await()

                currentRemoteGameId = docRef.id

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun endRemoteGameSession() {
        val uid = auth.currentUser?.uid ?: return
        val gameId = currentRemoteGameId ?: return

        viewModelScope.launch {
            try {
                val updatedGame = mapOf(
                    "totalHands" to _totalHandsPlayed.value,
                    "finalPoints" to _currentPoints.value
                )

                firestore.collection("users")
                    .document(uid)
                    .collection("history")
                    .document(gameId)
                    .update(updatedGame)
                    .await()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        currentRemoteGameId = null
    }

    fun saveRemoteHand(hand: Hand) {
        val uid = auth.currentUser?.uid ?: return
        val gameId = currentRemoteGameId ?: return

        val result = when {
            hand.score > 21 -> "LOSS"
            else -> "WIN"
        }

        viewModelScope.launch {
            try {
                val handData = mapOf(
                    "handNumber" to _totalHandsPlayed.value,
                    "result" to result,
                    "pointsChange" to _currentBet.value
                )

                firestore.collection("users")
                    .document(uid)
                    .collection("history")
                    .document(gameId)
                    .collection("hands")
                    .add(handData)
                    .await()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun startNewGameSession() {
        viewModelScope.launch(Dispatchers.IO) {
            val gameId = dao.insertGame(
                GameEntity(
                    totalHands = 0,
                    finalPoints = _currentPoints.value
                )
            ).toInt()

            currentGameId = gameId
        }

        _allHands.value = emptyList()
        _numHands.value = 0
    }

    fun endGameSession() {
        val gameId = currentGameId ?: return

        viewModelScope.launch(Dispatchers.IO) {
            val updatedGame = GameEntity(
                gameId = gameId,
                totalHands = _totalHandsPlayed.value,
                finalPoints = _currentPoints.value
            )
            dao.updateGame(updatedGame)
        }

        currentGameId = null
    }

    fun createDeck() {
        _deck.value = emptyList()
        for (suit in Suit.entries) {
            for (rank in Rank.entries) {
                val value = when (rank) {
                    Rank.Jack, Rank.Queen, Rank.King -> 10
                    Rank.Ace -> 11
                    else -> rank.value.toInt()}
                val color = if (suit == Suit.Heart || suit == Suit.Diamond) CardRed else CardBlack
                _deck.update {
                    it + CardClass(rank.value, value, suit.value, color)
                }
            }
        }
    }

    fun shuffleDeck() {
        _deck.value = _deck.value.shuffled()
    }

    fun drawCard(): CardClass? {
        val card = _currentDeck.value.firstOrNull()
        if (card != null) {
            _currentDeck.update { it.drop(1) }
            _cardsDealt.update { it + 1 }
        }
        return card
    }

    fun calculateScore(cards: List<CardClass?>): Int {
        return cards.sumOf { it?.value ?: 0 }
    }

    fun updateHand(index: Int, newCards: List<CardClass?>) {
        val newHands = _allHands.value.toMutableList()
        newHands[index] = Hand(newCards, calculateScore(newCards))
        _allHands.value = newHands
        _currentHandTotal.value = newHands[_currentHandIndex.value].score
    }

    fun getCurrentHand(): Hand? {
        val index = _currentHandIndex.value
        return _allHands.value.getOrNull(index)
    }

    fun startGame() {
        startNewGameSession()
        startRemoteGameSession()
        _hasPlayed.value = false
        _gameInProgress.value = true
        _dealerHand.value = emptyList()
        _allHands.value = listOf(Hand(emptyList(), 0))
        _currentHandIndex.value = 0
        _numHands.value = 1
        _totalHandsPlayed.value = 1
        _playerTurnOver.value = false
        _cardsDealt.value = 0
        _currentPoints.value = 1000
        _currentBet.value = 500

        createDeck()
        shuffleDeck()
        _currentDeck.value = _deck.value

        repeat(2) {
            val card = drawCard()
            val current = _allHands.value[0].hand + card
            updateHand(0, current)
        }
        repeat(2) {
            _dealerHand.update { it + drawCard() }
        }
        addDealerHand()
        checkIfPlayerDone()
    }

    fun playAgain() {
        if (_currentBet.value > _currentPoints.value) {
            _currentBet.value = _currentPoints.value
        }
        _hasPlayed.value = false
        _dealerHand.value = emptyList()
        _allHands.value = listOf(Hand(emptyList(), 0))
        _currentHandIndex.value = 0
        _numHands.value = 1
        _totalHandsPlayed.value += 1
        _playerTurnOver.value = false
        _cardsDealt.value = 0

        shuffleDeck()
        _currentDeck.value = _deck.value

        repeat(2) {
            val card = drawCard()
            val current = _allHands.value[0].hand + card
            updateHand(0, current)
        }
        repeat(2) {
            _dealerHand.update { it + drawCard() }
        }
        addDealerHand()
        checkIfPlayerDone()
    }

    fun endGame() {
        endGameSession()
        endRemoteGameSession()
        _gameInProgress.value = false
    }

    fun checkIfPlayerDone() {
        if (_currentHandTotal.value >= 21) {
            if (_currentHandIndex.value < _numHands.value - 1) {
                _currentHandIndex.update { it + 1 }
                _currentHandTotal.value = getCurrentHand()?.score ?: 0
            } else {
                _playerTurnOver.value = true
                dealerTurn()
            }
        }
    }

    fun addDealerHand() {
        _currentDealerTotal.value = _dealerHand.value.sumOf { it?.value ?: 0 }
    }

    fun updateBackground(newColor: Color) {
        _currentBackgroundColor.value = newColor
    }

    fun hit() {
        _hasPlayed.value = true
        if (_playerTurnOver.value) return
        val currentHand = getCurrentHand() ?: return
        val updatedHand = currentHand.hand + drawCard()
        updateHand(_currentHandIndex.value, updatedHand)
        checkIfPlayerDone()
    }

    fun stand() {
        _hasPlayed.value = true
        if (_playerTurnOver.value) return

        if (_currentHandIndex.value < _numHands.value - 1) {
            _currentHandIndex.update { it + 1 }
            _currentHandTotal.value = getCurrentHand()?.score ?: 0
        } else {
            _playerTurnOver.value = true
            dealerTurn()
        }
    }

    fun doubleDown() {
        _hasPlayed.value = true
        if (_playerTurnOver.value) return
        val currentHand = getCurrentHand() ?: return
        val updatedHand = currentHand.hand + drawCard()
        _currentBet.value *= 2

        updateHand(_currentHandIndex.value, updatedHand)

        if (_currentHandTotal.value > 21) {
            _currentPoints.update { it - _currentBet.value }
        }

        if (_currentHandIndex.value < _numHands.value - 1) {
            _currentHandIndex.update { it + 1 }
            _currentHandTotal.value = getCurrentHand()?.score ?: 0
        } else {
            _playerTurnOver.value = true
            dealerTurn()
        }
    }

    fun split() {
        _hasPlayed.value = true
        if (_playerTurnOver.value) return

        val hand = getCurrentHand()?.hand ?: return
        if (hand.size != 2 || hand[0]?.value != hand[1]?.value) return

        val firstHand = listOf(hand[0], drawCard())
        val secondHand = listOf(hand[1], drawCard())

        _allHands.value = listOf(
            Hand(firstHand, calculateScore(firstHand)),
            Hand(secondHand, calculateScore(secondHand))
        )

        _numHands.value = 2
        _currentHandIndex.value = 0
        _currentHandTotal.value = _allHands.value[0].score
    }

    fun dealerTurn() {
        addDealerHand()
        while (_currentDealerTotal.value < 17) {
            _dealerHand.update { it + drawCard() }
            addDealerHand()
        }

        val dealerScore = _currentDealerTotal.value

        // Update player points
        _allHands.value.forEach { hand ->
            val playerScore = hand.score
            when {
                playerScore > 21 -> _currentPoints.update { it - _currentBet.value }
                dealerScore > 21 -> _currentPoints.update { it + _currentBet.value }
                dealerScore > playerScore -> _currentPoints.update { it - _currentBet.value }
                dealerScore < playerScore -> _currentPoints.update { it + _currentBet.value }
            }
        }

        // Save the game and all hands once, after the dealer is done
        _allHands.value.forEach { hand ->
            saveHand(hand)
            saveRemoteHand(hand)
        }
    }

    fun currentHandIndexPlusOne() {
        _currentHandIndex.update { value -> value + 1 }
    }

    fun currentHandIndexMinusOne() {
        _currentHandIndex.update { value -> value - 1 }
    }

    fun selectGame(gameId: Int) {
        _selectedGameId.value = gameId
    }

    fun selectRemoteGame(gameId: String) {
        _selectedRemoteGameId.value = gameId
    }

    fun plustwentyfive() {
        if ((_currentBet.value + 25) < _currentPoints.value) {
            _currentBet.value += 25
        }
    }

    fun plushundred() {
        if ((_currentBet.value + 100) < _currentPoints.value) {
            _currentBet.value += 100
        }
    }

    fun minustwentyfive() {
        if ((_currentBet.value - 25) > 0) {
            _currentBet.value -= 25
        }
    }

    fun minushundred() {
        if ((_currentBet.value - 100) > 0) {
            _currentBet.value -= 100
        }
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authListener)
    }
}
