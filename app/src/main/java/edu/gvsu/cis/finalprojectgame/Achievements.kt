package edu.gvsu.cis.finalprojectgame

data class Achievement(
    val id: String,
    val title: String,
    val description: String
)

val achievementsList = listOf(
    Achievement("play_1", "Getting Started", "Play 1 game"),
    Achievement("play_5", "Regular", "Play 5 games"),
    Achievement("play_10", "Addicted", "Play 10 games"),

    Achievement("points_2500", "Stacking Chips", "Reach 2500 points"),
    Achievement("points_5000", "High Roller", "Reach 5000 points"),
    Achievement("points_0", "Broke", "Reach 0 points"),

    Achievement("win_1", "First Win", "Win 1 hand"),
    Achievement("win_5", "On a Roll", "Win 5 hands"),
    Achievement("win_10", "Unstoppable", "Win 10 hands"),

    Achievement("blackjack", "Blackjack!", "Get a 21"),

    Achievement("all_in", "All In", "Bet all your points"),
    Achievement("double_down", "Double Down", "Use double down"),
    Achievement("split", "Split Decision", "Split a hand"),
    Achievement("stand", "Stand Firm", "Stand on a hand"),
    Achievement("hit", "Hit Me", "Hit a card")
)