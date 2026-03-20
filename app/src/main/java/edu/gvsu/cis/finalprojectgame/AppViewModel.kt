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
            var currentDeck = _deck.value
            repeat(2) {
                _playerHand.update { playerHand -> playerHand + currentDeck[_cardsDealt.value] }
                currentDeck = currentDeck.drop(_cardsDealt.value)
                _cardsDealt.update { cardsDealt -> cardsDealt + 1 }
            }
            repeat(2) {
                _dealerHand.update { dealerHand -> dealerHand + currentDeck[_cardsDealt.value] }
                currentDeck = currentDeck.drop(_cardsDealt.value)
                _cardsDealt.update { cardsDealt -> cardsDealt + 1 }
            }
        } else {
            _dealerHand.value = listOf<CardClass?>()
            _playerHand.value = listOf<CardClass?>()
            _cardsDealt.update {0}
            shuffleDeck()
            var currentDeck = _deck.value
            repeat(2) {
                _playerHand.update { playerHand -> playerHand + currentDeck[_cardsDealt.value] }
                currentDeck = currentDeck.drop(_cardsDealt.value)
                _cardsDealt.update { cardsDealt -> cardsDealt + 1 }
            }
            repeat(2) {
                _dealerHand.update { dealerHand -> dealerHand + currentDeck[_cardsDealt.value] }
                currentDeck = currentDeck.drop(_cardsDealt.value)
                _cardsDealt.update { cardsDealt -> cardsDealt + 1 }
            }
        }
    }

    fun updateBackground(newColor: Color) {
        _currentBackgroundColor.update {newColor}
    }
}
