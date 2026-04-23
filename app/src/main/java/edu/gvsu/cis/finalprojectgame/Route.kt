package edu.gvsu.cis.finalprojectgame

import kotlinx.serialization.Serializable

@Serializable
sealed class GameRoute {

    @Serializable
    data object ToMainMenuScreen

    @Serializable
    data object ToGameScreen

    @Serializable
    data object ToSettingsScreen

    @Serializable
    data object ToColorSelectorScreen

    @Serializable
    data object ToPostGameScreen

    @Serializable
    data object ToGameDetailScreen

    @Serializable
    data object toSignUpScreen

    @Serializable
    data object toLoginScreen

    @Serializable
    data object ToAccountScreen

    @Serializable
    data object ToPasswordChange
}