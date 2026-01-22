// com/example/pokerpayoutcalc/Player.kt
package com.example.pokerpayoutcalc

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID // Import UUID for generating unique IDs

sealed interface ListItem {
    val name: String
}

@Parcelize
data class Player(
    val id: String = UUID.randomUUID().toString(), // ADDED: Unique ID for each player
    override val name: String = "", // Kept as required, but should ideally have a default or be provided on creation
    var balance: Int = 0,
    var sumCashBuyin: Int = 0,
    val cashBuyins: MutableList<Int> = mutableListOf(),
    var sumLoanBuyin: Int = 0,
    val loanBuyins: MutableList<Int> = mutableListOf(),
    var isVip: Boolean = false,
    var isPlaying: Boolean = true
) : ListItem, Parcelable

@Parcelize
data class House(
    override val name: String = "HOUSE",
    var balance: Int = 0
) : ListItem, Parcelable