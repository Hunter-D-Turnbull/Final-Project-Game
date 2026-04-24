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
    private val _dealerHand = MutableStateFlow(listOf<CardClass?>())
    val dealerHand = _dealerHand.asStateFlow()
    private val _gameInProgress = MutableStateFlow(false)
    val gameInProgress = _gameInProgress.asStateFlow()
    private val _cardsDealt = MutableStateFlow(0)
    private val _currentBackgroundColor = MutableStateFlow(MyGreen)
    val currentBackgroundColor = _currentBackgroundColor.asStateFlow()
    private val _currentHandTotal = MutableStateFlow(0)
    private val _currentDealerTotal = MutableStateFlow(0)
    private val _playerTurnOver = MutableStateFlow(false)
    val playerTurnOver = _playerTurnOver.asStateFlow()
    private val _currentDeck = MutableStateFlow(listOf<CardClass?>())
    private val _currentPoints = MutableStateFlow(1000)
    val currentPoints = _currentPoints.asStateFlow()
    private val _numHands = MutableStateFlow(1)
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
    private val _gamesPlayed = MutableStateFlow(0)
    val gamesPlayed = _gamesPlayed.asStateFlow()

    private val _totalWins = MutableStateFlow(0)
    val totalWins = _totalWins.asStateFlow()
    private val _unlockedAchievements = MutableStateFlow<Set<String>>(emptySet())
    val unlockedAchievements = _unlockedAchievements.asStateFlow()
    private val _leaderboard = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val leaderboard = _leaderboard.asStateFlow()
    private val _betLocked = MutableStateFlow(false)
    val betLocked = _betLocked.asStateFlow()
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

    fun saveHand(hand: Hand, handNumber: Int) {
        val gameId = currentGameId ?: return
        val dealerScore = _currentDealerTotal.value

        val result = when {
            hand.score > 21 -> "LOSS"
            dealerScore > 21 -> "WIN"
            hand.score > dealerScore -> "WIN"
            hand.score < dealerScore -> "LOSS"
            else -> "PUSH"
        }

        viewModelScope.launch(Dispatchers.IO) {
            dao.insertHand(
                HandEntity(
                    gameOwnerId = gameId,
                    handNumber = handNumber,
                    result = result,
                    pointsChange = _currentBet.value
                )
            )
        }

        _numHands.update { it + 1 }
    }

    suspend fun startRemoteGameSession() {
        val uid = auth.currentUser?.uid ?: return

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

    fun endRemoteGameSession() {
        val uid = auth.currentUser?.uid ?: return
        val gameId = currentRemoteGameId ?: return

        viewModelScope.launch {
            try {
                val updatedGame = mapOf(
                    "totalHands" to (_totalHandsPlayed.value - 1),
                    "finalPoints" to _currentPoints.value
                )

                firestore.collection("users")
                    .document(uid)
                    .collection("history")
                    .document(gameId)
                    .update(updatedGame)
                    .await()
                currentRemoteGameId = null

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun saveRemoteHand(hand: Hand, handNumber: Int) {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            // Wait up to ~1 second for gameId to be ready
            var attempts = 0
            while (currentRemoteGameId == null && attempts < 10) {
                kotlinx.coroutines.delay(100)
                attempts++
            }

            val gameId = currentRemoteGameId
            if (gameId == null) {
                println("Still no gameId after waiting — skipping save")
                return@launch
            }
            val dealerScore = _currentDealerTotal.value

            val result = when {
                hand.score > 21 -> "LOSS"
                dealerScore > 21 -> "WIN"
                hand.score > dealerScore -> "WIN"
                hand.score < dealerScore -> "LOSS"
                else -> "PUSH"
            }

            try {
                val handData = mapOf(
                    "handNumber" to handNumber,
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
                totalHands = (_totalHandsPlayed.value - 1),
                finalPoints = _currentPoints.value
            )
            dao.updateGame(updatedGame)
        }

        currentGameId = null
    }

    fun clearAllRemoteHistory() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val historyRef = firestore.collection("users")
                    .document(uid)
                    .collection("history")

                val gamesSnapshot = historyRef.get().await()

                for (gameDoc in gamesSnapshot.documents) {

                    val handsRef = gameDoc.reference.collection("hands")
                    val handsSnapshot = handsRef.get().await()

                    for (handDoc in handsSnapshot.documents) {
                        handDoc.reference.delete().await()
                    }

                    gameDoc.reference.delete().await()
                }

                // Clear UI immediately
                _remoteGames.value = emptyList()
                _remoteHands.value = emptyList()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearAllLocalHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteAllGames()
        }
    }

    fun fetchStats() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val doc = firestore.collection("users")
                    .document(uid)
                    .collection("stats")
                    .document("global")
                    .get()
                    .await()

                _gamesPlayed.value = doc.getLong("gamesPlayed")?.toInt() ?: 0
                _totalWins.value = doc.getLong("totalWins")?.toInt() ?: 0

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateStats(gamesDelta: Int = 0, winsDelta: Int = 0) {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val ref = firestore.collection("users")
                    .document(uid)
                    .collection("stats")
                    .document("global")

                firestore.runTransaction { transaction ->
                    val snapshot = transaction.get(ref)

                    val currentGames = snapshot.getLong("gamesPlayed") ?: 0
                    val currentWins = snapshot.getLong("totalWins") ?: 0

                    transaction.set(
                        ref,
                        mapOf(
                            "gamesPlayed" to currentGames + gamesDelta,
                            "totalWins" to currentWins + winsDelta
                        )
                    )
                }.await()

                // update local cache
                _gamesPlayed.value += gamesDelta
                _totalWins.value += winsDelta

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun unlockAchievement(achievementId: String) {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val ref = firestore.collection("users")
                    .document(uid)
                    .collection("achievements")
                    .document(achievementId)

                val doc = ref.get().await()
                if (doc.exists()) return@launch

                ref.set(
                    mapOf(
                        "unlocked" to true,
                        "timestamp" to System.currentTimeMillis()
                    )
                ).await()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun fetchUnlockedAchievements() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("users")
                    .document(uid)
                    .collection("achievements")
                    .get()
                    .await()

                _unlockedAchievements.value =
                    snapshot.documents.map { it.id }.toSet()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun submitScoreToLeaderboard() {
        val uid = auth.currentUser?.uid ?: return
        val score = _currentPoints.value

        viewModelScope.launch {
            try {
                // Get user name
                val userDoc = firestore.collection("users")
                    .document(uid)
                    .get()
                    .await()

                val name = userDoc.getString("name") ?: "No Name"

                val entry = mapOf(
                    "uid" to uid,
                    "name" to name,
                    "score" to score,
                    "timestamp" to System.currentTimeMillis()
                )

                firestore.collection("leaderboard")
                    .add(entry)
                    .await()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fetchLeaderboard() {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("leaderboard")
                    .orderBy("score", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(10)
                    .get()
                    .await()

                val list = snapshot.documents.map { doc ->
                    LeaderboardEntry(
                        name = doc.getString("name") ?: "No Name",
                        score = doc.getLong("score")?.toInt() ?: 0
                    )
                }

                _leaderboard.value = list

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteAccount(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val user = auth.currentUser

        if (user == null || user.email != email) {
            onResult(false, "Invalid user")
            return
        }

        viewModelScope.launch {
            try {
                val credential = EmailAuthProvider.getCredential(email, password)
                user.reauthenticate(credential).await()

                val uid = user.uid

                val leaderboardSnapshot = firestore.collection("leaderboard")
                    .whereEqualTo("uid", uid)
                    .get()
                    .await()

                for (doc in leaderboardSnapshot.documents) {
                    firestore.collection("leaderboard")
                        .document(doc.id)
                        .update("name", "No Name")
                        .await()
                }

                val userDocRef = firestore.collection("users").document(uid)

                // Delete subcollections manually
                val history = userDocRef.collection("history").get().await()
                for (game in history.documents) {
                    val hands = game.reference.collection("hands").get().await()
                    for (hand in hands.documents) {
                        hand.reference.delete().await()
                    }
                    game.reference.delete().await()
                }

                val statsDoc = userDocRef.collection("meta").document("stats")
                statsDoc.delete().await()

                // Delete main user document
                userDocRef.delete().await()

                user.delete().await()

                onResult(true, null)

            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
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
        var total = 0
        var aceCount = 0

        for (card in cards) {
            val value = card?.value ?: 0
            total += value

            if (value == 11) {
                aceCount++
            }
        }

        // Adjust Aces from 11 → 1 if bust
        while (total > 21 && aceCount > 0) {
            total -= 10
            aceCount--
        }

        return total
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

    fun lockBetAndDeal() {
        _betLocked.value = true

        _dealerHand.value = emptyList()
        _allHands.value = listOf(Hand(emptyList(), 0))
        _currentHandIndex.value = 0
        _numHands.value = 1
        _playerTurnOver.value = false
        _cardsDealt.value = 0

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

    fun startGame() {
        updateStats(gamesDelta = 1)

        when (_gamesPlayed.value + 1) {
            1 -> unlockAchievement("play_1")
            5 -> unlockAchievement("play_5")
            10 -> unlockAchievement("play_10")
        }

        startNewGameSession()
        viewModelScope.launch { startRemoteGameSession() }
        startGameLogic()
    }

    fun startGameLogic() {
        _hasPlayed.value = false
        _gameInProgress.value = true
        _dealerHand.value = emptyList()
        _allHands.value = emptyList()
        _currentHandIndex.value = 0
        _numHands.value = 1
        _totalHandsPlayed.value = 1
        _playerTurnOver.value = false
        _cardsDealt.value = 0
        _currentPoints.value = 1000
        _currentBet.value = 500
        _betLocked.value = false
        createDeck()
        shuffleDeck()
        _currentDeck.value = _deck.value
    }

    fun playAgain() {
        if (_currentBet.value > _currentPoints.value) {
            _currentBet.value = _currentPoints.value
        }
        _hasPlayed.value = false
        _dealerHand.value = emptyList()
        _allHands.value = emptyList()
        _currentHandIndex.value = 0
        _numHands.value = 1
        _playerTurnOver.value = false
        _cardsDealt.value = 0
        _betLocked.value = false
        shuffleDeck()
        _currentDeck.value = _deck.value
    }

    fun endGame() {
        submitScoreToLeaderboard()
        endGameSession()
        endRemoteGameSession()
        _gameInProgress.value = false
    }

    fun checkIfPlayerDone() {
        val currentScore = getCurrentHand()?.score ?: 0
        if (currentScore >= 21) {
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
        _currentDealerTotal.value = calculateScore(_dealerHand.value)
    }

    fun updateBackground(newColor: Color) {
        _currentBackgroundColor.value = newColor
    }

    fun hit() {
        if (_currentBet.value == _currentPoints.value) {
            unlockAchievement("all_in")
        }
        unlockAchievement("hit")
        _hasPlayed.value = true
        if (_playerTurnOver.value) return
        val currentHand = getCurrentHand() ?: return
        val updatedHand = currentHand.hand + drawCard()
        updateHand(_currentHandIndex.value, updatedHand)
        checkIfPlayerDone()
    }

    fun stand() {
        if (_currentBet.value == _currentPoints.value) {
            unlockAchievement("all_in")
        }
        unlockAchievement("stand")
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
        if (_currentBet.value == _currentPoints.value) {
            unlockAchievement("all_in")
        }
        unlockAchievement("double_down")
        _hasPlayed.value = true
        if (_playerTurnOver.value) return

        val currentHand = getCurrentHand() ?: return
        val updatedHand = currentHand.hand + drawCard()
        _currentBet.value *= 2

        updateHand(_currentHandIndex.value, updatedHand)

        if (_currentHandIndex.value < _numHands.value - 1) {
            _currentHandIndex.update { it + 1 }
            _currentHandTotal.value = getCurrentHand()?.score ?: 0
        } else {
            _playerTurnOver.value = true
            dealerTurn()
        }
    }

    fun split() {
        if (_playerTurnOver.value) return

        val hand = getCurrentHand()?.hand ?: return
        if (hand.size != 2 || hand[0]?.value != hand[1]?.value) return
        if (_currentBet.value == _currentPoints.value) {
            unlockAchievement("all_in")
        }
        unlockAchievement("split")

        val firstHand = listOf(hand[0], drawCard())
        val secondHand = listOf(hand[1], drawCard())

        _allHands.value = listOf(
            Hand(firstHand, calculateScore(firstHand)),
            Hand(secondHand, calculateScore(secondHand))
        )

        _numHands.value = 2
        _currentHandIndex.value = 0
        _currentHandTotal.value = _allHands.value[0].score
        checkIfPlayerDone()
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

            val isWin = when {
                playerScore > 21 -> false
                dealerScore > 21 -> true
                playerScore > dealerScore -> true
                playerScore < dealerScore -> false
                else -> false // push counts as not a win
            }

            // Update points
            when {
                playerScore > 21 -> _currentPoints.update { it - _currentBet.value }
                dealerScore > 21 -> _currentPoints.update { it + _currentBet.value }
                dealerScore > playerScore -> _currentPoints.update { it - _currentBet.value }
                dealerScore < playerScore -> _currentPoints.update { it + _currentBet.value }
            }

            if (isWin) {
                updateStats(winsDelta = 1)

                when (_totalWins.value + 1) {
                    1 -> unlockAchievement("win_1")
                    5 -> unlockAchievement("win_5")
                    10 -> unlockAchievement("win_10")
                }
            }

            // Blackjack achievement
            if (hand.hand.size == 2 && playerScore == 21) {
                unlockAchievement("blackjack")
            }
        }
        if (_currentPoints.value >= 2500) {
            unlockAchievement("points_2500")
        }
        if (_currentPoints.value >= 5000) {
            unlockAchievement("points_5000")
        }
        if (_currentPoints.value <= 0) {
            unlockAchievement("points_0")
        }

        // Save the game and all hands once, after the dealer is done
        _allHands.value.forEach { hand ->
            val handNumber = _totalHandsPlayed.value
            saveHand(hand, handNumber)
            saveRemoteHand(hand, handNumber)
            _totalHandsPlayed.update { it + 1 }
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
        if ((_currentBet.value + 25) <= _currentPoints.value) {
            _currentBet.value += 25
        }
    }

    fun plushundred() {
        if ((_currentBet.value + 100) <= _currentPoints.value) {
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
