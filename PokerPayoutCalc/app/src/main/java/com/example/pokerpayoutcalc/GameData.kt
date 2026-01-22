package com.example.pokerpayoutcalc

import android.os.Parcelable
import kotlinx.parcelize.Parcelize // Add this import

@Parcelize // Add this annotation
// Data class to hold all game-related information that will be stored in Firestore
data class GameData(
    val players: List<Player> = emptyList(),
    val houseBalance: Int = 0,
    val isGameEnded: Boolean = false
) : Parcelable // Implement Parcelable