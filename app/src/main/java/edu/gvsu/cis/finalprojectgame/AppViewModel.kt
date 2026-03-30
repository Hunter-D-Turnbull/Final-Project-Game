package edu.gvsu.cis.finalprojectgame

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import edu.gvsu.cis.finalprojectgame.ui.theme.CardBlack
import edu.gvsu.cis.finalprojectgame.ui.theme.CardRed
import edu.gvsu.cis.finalprojectgame.ui.theme.MyGreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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

class AppViewModel : ViewModel() {
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
        _gameInProgress.value = true
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
        if (_playerTurnOver.value) return
        val currentHand = getCurrentHand() ?: return
        val updatedHand = currentHand.hand + drawCard()
        updateHand(_currentHandIndex.value, updatedHand)
        checkIfPlayerDone()
    }

    fun stand() {
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
        if (_playerTurnOver.value) return
        val currentHand = getCurrentHand() ?: return
        val updatedHand = currentHand.hand + drawCard()

        updateHand(_currentHandIndex.value, updatedHand)

        if (_currentHandTotal.value > 21) {
            _currentPoints.update { it - 1000 }
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
        _allHands.value.forEach { hand ->
            val playerScore = hand.score
            when {
                playerScore > 21 -> _currentPoints.update { it - 500 }
                dealerScore > 21 -> _currentPoints.update { it + 500 }
                dealerScore > playerScore -> _currentPoints.update { it - 500 }
                dealerScore < playerScore -> _currentPoints.update { it + 500 }
            }
        }
    }

    fun currentHandIndexPlusOne() {
        _currentHandIndex.update { value -> value + 1 }
    }
    fun currentHandIndexMinusOne() {
        _currentHandIndex.update { value -> value - 1 }
    }
}
