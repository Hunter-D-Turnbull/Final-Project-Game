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

    val currentUser get() = auth.currentUser
    val dao: GameDao =
        (app as MyRoomApplication).myDB.getDao()

    val games = dao.getAllGames()

    fun saveGame(finalPoints: Int, totalHands: Int, hands: List<Hand>) {
        viewModelScope.launch(Dispatchers.IO) {
            // Insert single game
            val gameId = dao.insertGame(
                GameEntity(
                    totalHands = totalHands,
                    finalPoints = finalPoints
                )
            ).toInt()

            // Insert each hand linked to that game
            hands.forEachIndexed { index, hand ->
                val result = when {
                    hand.score > 21 -> "LOSS"
                    else -> "WIN"
                }

                dao.insertHand(
                    HandEntity(
                        gameOwnerId = gameId,
                        handNumber = index + 1,
                        result = result,
                        pointsChange = hand.score
                    )
                )
            }
        }
    }

    fun signUp(email: String, password: String, name: String, onResult: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener

                    // Save user name in Firestore
                    val userData = mapOf(
                        "name" to name,
                        "email" to email
                    )

                    firestore.collection("users")
                        .document(uid)
                        .set(userData)
                        .addOnSuccessListener { onResult(true, null) }
                        .addOnFailureListener { onResult(false, it.message) }

                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun signIn(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun signOut() {
        auth.signOut()
    }

    fun saveGameToFirestore(finalPoints: Int, totalHands: Int, hands: List<Hand>) {
        val uid = auth.currentUser?.uid ?: return

        val gameData = mapOf(
            "finalPoints" to finalPoints,
            "totalHands" to totalHands,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("users")
            .document(uid)
            .collection("history")
            .add(gameData)
            .addOnSuccessListener { docRef ->

                hands.forEachIndexed { index, hand ->
                    val handData = mapOf(
                        "handNumber" to index + 1,
                        "score" to hand.score
                    )

                    docRef.collection("hands").add(handData)
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
                    handNumber = _numHands.value + 1,
                    result = result,
                    pointsChange = hand.score
                )
            )
        }

        // Update number of hands in ViewModel
        _numHands.update { it + 1 }
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
                totalHands = _numHands.value,
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
        _hasPlayed.value = false
        _gameInProgress.value = true
        _dealerHand.value = emptyList()
        _allHands.value = listOf(Hand(emptyList(), 0))
        _currentHandIndex.value = 0
        _numHands.value = 1
        _playerTurnOver.value = false
        _cardsDealt.value = 0
        _currentPoints.value = 1000

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
}
