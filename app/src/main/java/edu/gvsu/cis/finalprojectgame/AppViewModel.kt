package edu.gvsu.cis.finalprojectgame

import androidx.compose.runtime.collectAsState
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
    val hand: List<CardClass?>
)

class AppViewModel : ViewModel () {
    private val _deck = MutableStateFlow(listOf<CardClass?>())
    val deck = _deck.asStateFlow()

    private val _dealerHand = MutableStateFlow(listOf<CardClass?>())
    val dealerHand = _dealerHand.asStateFlow()

    private val _playerHand = MutableStateFlow(listOf<CardClass?>())
    val playerHand = _playerHand.asStateFlow()

    private val _gameInProgress = MutableStateFlow<Boolean>(false)
    val gameInProgress = _gameInProgress.asStateFlow()

    private val _cardsDealt = MutableStateFlow<Int>(0)
    val cardsDealt = _cardsDealt.asStateFlow()

    private val _currentBackgroundColor = MutableStateFlow(MyGreen)
    val currentBackgroundColor = _currentBackgroundColor.asStateFlow()

    private val _currentHandTotal = MutableStateFlow<Int>(0)
    val currentHandTotal = _currentHandTotal.asStateFlow()

    private val _currentDealerTotal = MutableStateFlow<Int>(0)
    val currentDealerTotal = _currentHandTotal.asStateFlow()

    private val _playerTurnOver = MutableStateFlow<Boolean>(false)
    val playerTurnOver = _playerTurnOver.asStateFlow()

    private val _currentDeck = MutableStateFlow(listOf<CardClass?>())
    val currentDeck = _currentDeck.asStateFlow()

    private val _currentPoints = MutableStateFlow<Int>(1000)
    val currentPoints = _currentPoints.asStateFlow()

    private val _numHands = MutableStateFlow<Int>(1)
    val numHands = _numHands.asStateFlow()

    private val _allHands = MutableStateFlow(listOf<Hand?>())
    val allHands = _allHands.asStateFlow()

    fun createDeck() {
        for (suit in Suit.entries) {
            for (rank in Rank.entries) {
                if (rank.value == "J" || rank.value == "Q" || rank.value == "K"){
                    if (suit.value == "♥" || suit.value == "♦") {
                        _deck.update {deck -> deck + CardClass(rank.value, 10, suit.value, CardRed)}
                    } else {
                        _deck.update {deck -> deck + CardClass(rank.value, 10, suit.value, CardBlack)}
                    }
                } else if (rank.value == "A") {
                    if (suit.value == "♥" || suit.value == "♦") {
                        _deck.update {deck -> deck + CardClass(rank.value, 11, suit.value, CardRed)}
                    } else {
                        _deck.update {deck -> deck + CardClass(rank.value, 11, suit.value, CardBlack)}
                    }
                } else {
                    if (suit.value == "♥" || suit.value == "♦") {
                        _deck.update {deck -> deck + CardClass(rank.value, rank.value.toInt(), suit.value, CardRed)}
                    } else {
                        _deck.update {deck -> deck + CardClass(rank.value, rank.value.toInt(), suit.value, CardBlack)}
                    }
                }
            }
        }
    }

    fun shuffleDeck() {
        val shuffledDeck = _deck.value.shuffled()
        _deck.value = shuffledDeck
    }

    fun startGame() {
        if (!_gameInProgress.value) {
            _gameInProgress.update { true }
            createDeck()
            shuffleDeck()
            _currentDeck.update { _deck.value }
            repeat(2) {
                _playerHand.update { playerHand -> playerHand + _currentDeck.value[0] }
                _currentDeck.update {currentDeck -> currentDeck.drop(1)}
                _cardsDealt.update { cardsDealt -> cardsDealt + 1 }
            }
            addPlayerHand()
            repeat(2) {
                _dealerHand.update { dealerHand -> dealerHand + _currentDeck.value[0] }
                _currentDeck.update {currentDeck -> currentDeck.drop(1)}
                _cardsDealt.update { cardsDealt -> cardsDealt + 1 }
            }
            checkIfPlayerDone()
        } else {
            _dealerHand.value = listOf<CardClass?>()
            _playerHand.value = listOf<CardClass?>()
            _cardsDealt.update {0}
            _playerTurnOver.update { false }
            shuffleDeck()
            _currentDeck.update { _deck.value }
            repeat(2) {
                _playerHand.update { playerHand -> playerHand + _currentDeck.value[0] }
                _currentDeck.update {currentDeck -> currentDeck.drop(1)}
                _cardsDealt.update { cardsDealt -> cardsDealt + 1 }
            }
            addPlayerHand()
            repeat(2) {
                _dealerHand.update { dealerHand -> dealerHand + _currentDeck.value[0] }
                _currentDeck.update {currentDeck -> currentDeck.drop(1)}
                _cardsDealt.update { cardsDealt -> cardsDealt + 1 }
            }
            checkIfPlayerDone()
        }
    }

    fun checkIfPlayerDone() {
        if (_currentHandTotal.value == 21) {
            _playerTurnOver.update { true }
            dealerTurn()
        } else if (_currentHandTotal.value > 21) {
            _playerTurnOver.update { true }
            _currentPoints.update { currentPoints -> currentPoints - 500 }
        } else {
            _playerTurnOver.update { false }
        }
    }

    fun addPlayerHand() {
        _currentHandTotal.update { 0 }
        val currHand = _playerHand.value
        currHand.forEach { card -> _currentHandTotal.update{currentTotal -> currentTotal + card!!.value}}
    }

    fun addDealerHand() {
        _currentDealerTotal.update { 0 }
        val currHand = _dealerHand.value
        currHand.forEach { card -> _currentDealerTotal.update{currentTotal -> currentTotal + card!!.value}}
    }

    fun updateBackground(newColor: Color) {
        _currentBackgroundColor.update {newColor}
    }

    fun hit() {
        if (!_playerTurnOver.value) {
            _playerHand.update { playerHand -> playerHand + _currentDeck.value[0] }
            _currentDeck.update { currentDeck -> currentDeck.drop(1) }
            _cardsDealt.update { cardsDealt -> cardsDealt + 1 }
            addPlayerHand()
            checkIfPlayerDone()
        }
    }

    fun dealerTurn() {
        addDealerHand()
        if (_currentDealerTotal.value < 17) {
            _dealerHand.update { dealerHand -> dealerHand + _currentDeck.value[0] }
            _currentDeck.update {currentDeck -> currentDeck.drop(1)}
            _cardsDealt.update { cardsDealt -> cardsDealt + 1 }
            dealerTurn()
        } else {
            if (_currentDealerTotal.value > _currentHandTotal.value && _currentDealerTotal.value < 22) {
                _currentPoints.update { currentPoints -> currentPoints - 500 }
            } else if (_currentDealerTotal.value == _currentHandTotal.value) {
                _currentPoints.update { currentPoints -> currentPoints + 0}
            } else {
                _currentPoints.update { currentPoints -> currentPoints + 500 }
            }
        }
    }

    fun stand() {
        if (!_playerTurnOver.value) {
            addPlayerHand()
            _playerTurnOver.update { true }
            dealerTurn()
        }
    }

    fun doubleDown() {
        if (!_playerTurnOver.value) {
            _playerHand.update { playerHand -> playerHand + _currentDeck.value[0] }
            _currentDeck.update { currentDeck -> currentDeck.drop(1) }
            _cardsDealt.update { cardsDealt -> cardsDealt + 1 }
            addPlayerHand()
            if (_currentHandTotal.value < 22) {
                _playerTurnOver.update { true }
                dealerTurn()
            } else {
                _playerTurnOver.update { true }
                _currentPoints.update { currentPoints -> currentPoints - 1000 }
            }
        }
    }

    fun split() {
        if (!_playerTurnOver.value) {
            _numHands.update { numHands -> numHands + 1 }
            var tempHand = _playerHand.value
            repeat(2) {
                _allHands.update { allHands -> allHands + Hand(listOf(tempHand[0]))}
                tempHand.drop(1)
            }
        }
    }
}
