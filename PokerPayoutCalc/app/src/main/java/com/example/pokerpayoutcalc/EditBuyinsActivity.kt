package com.example.pokerpayoutcalc

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

// Prosledjivanje podataka (constants remain the same)
const val EXTRA_PLAYER_EDIT_BUYINS = "com.example.pokerpayoutcalc.PLAYER_EDIT_BUYINS"
const val EXTRA_HOUSE_BALANCE_IN_EDIT = "com.example.pokerpayoutcalc.HOUSE_BALANCE_IN_EDIT"
const val RESULT_EXTRA_PLAYER_UPDATED_FROM_EDIT = "com.example.pokerpayoutcalc.PLAYER_UPDATED_FROM_EDIT"
const val RESULT_EXTRA_HOUSE_BALANCE_UPDATED_FROM_EDIT = "com.example.pokerpayoutcalc.HOUSE_BALANCE_UPDATED_FROM_EDIT"

class EditBuyinsActivity : AppCompatActivity() {

    private lateinit var textViewEditBuyinsPlayerName: TextView
    private lateinit var recyclerViewLoanBuyins: RecyclerView
    private lateinit var recyclerViewCashBuyins: RecyclerView
    private lateinit var buttonEditBuyinsBack: Button

    private var currentPlayer: Player? = null
    private var currentHouseBalance: Int = 0

    private lateinit var loanBuyinsAdapter: BuyinsAdapter
    private lateinit var cashBuyinsAdapter: BuyinsAdapter

    // --- REMOVED: playerRepository declaration ---
    // private lateinit var playerRepository: PlayerRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_buyins)

        textViewEditBuyinsPlayerName = findViewById(R.id.textViewEditBuyinsPlayerName)
        recyclerViewLoanBuyins = findViewById(R.id.recyclerViewLoanBuyins)
        recyclerViewCashBuyins = findViewById(R.id.recyclerViewCashBuyins)
        buttonEditBuyinsBack = findViewById(R.id.buttonEditBuyinsBack)

        // --- REMOVED: playerRepository initialization ---
        // playerRepository = PlayerRepository(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            currentPlayer = intent.getParcelableExtra(EXTRA_PLAYER_EDIT_BUYINS, Player::class.java)
        } else {
            @Suppress("DEPRECATION")
            currentPlayer = intent.getParcelableExtra(EXTRA_PLAYER_EDIT_BUYINS) as? Player
        }
        currentHouseBalance = intent.getIntExtra(EXTRA_HOUSE_BALANCE_IN_EDIT, 0)

        currentPlayer?.let { player ->
            textViewEditBuyinsPlayerName.text = player.name

            recyclerViewLoanBuyins.layoutManager = LinearLayoutManager(this)
            loanBuyinsAdapter = BuyinsAdapter(player.loanBuyins, "Loan") { position, value ->
                showConfirmationDialog("Delete Loan Buyin?", "Are you sure you want to delete this loan of $value?", {
                    // Update the player object's lists directly
                    player.loanBuyins.removeAt(position)
                    player.sumLoanBuyin -= value

                    loanBuyinsAdapter.notifyItemRemoved(position)
                    // No need to notifyItemRangeChanged for simple deletions usually
                    // loanBuyinsAdapter.notifyItemRangeChanged(position, player.loanBuyins.size) // This might be problematic if size changes

                    Toast.makeText(this, "Loan of $value deleted. Will be saved on return.", Toast.LENGTH_SHORT).show()
                })
            }
            recyclerViewLoanBuyins.adapter = loanBuyinsAdapter

            recyclerViewCashBuyins.layoutManager = LinearLayoutManager(this)
            cashBuyinsAdapter = BuyinsAdapter(player.cashBuyins, "Cash") { position, value ->
                showConfirmationDialog("Delete Cash Buyin?", "Are you sure you want to delete this cash buyin of $value?", {
                    // Update the player object's lists directly
                    player.cashBuyins.removeAt(position)
                    player.sumCashBuyin -= value
                    currentHouseBalance -= value // Update house balance locally

                    cashBuyinsAdapter.notifyItemRemoved(position)
                    // No need to notifyItemRangeChanged
                    // cashBuyinsAdapter.notifyItemRangeChanged(position, player.cashBuyins.size)

                    Toast.makeText(this, "Cash buyin of $value deleted. House balance updated. Will be saved on return.", Toast.LENGTH_SHORT).show()
                })
            }
            recyclerViewCashBuyins.adapter = cashBuyinsAdapter

        } ?: run {
            Toast.makeText(this, "Error: Player data not found!", Toast.LENGTH_LONG).show()
            finish()
        }

        // listener
        buttonEditBuyinsBack.setOnClickListener {
            setResultAndFinish()
        }
    }

    override fun onBackPressed() {
        setResultAndFinish()
        super.onBackPressed()
    }

    // confirm dijalog (remains the same)
    private fun showConfirmationDialog(title: String, message: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("YES") { dialog, _ ->
                onConfirm()
                dialog.dismiss()
            }
            .setNegativeButton("BACK") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    // --- REMOVED: saveAllPlayerDataFromEditActivity() ---
    // This logic is now handled by MainActivity when it receives the updated player and house balance.
    /*
    private fun saveAllPlayerDataFromEditActivity() {
        currentPlayer?.let { player ->
            val allPlayers = playerRepository.loadPlayers()
            val indexToUpdate = allPlayers.indexOfFirst { it.name == player.name }
            if (indexToUpdate != -1) {
                allPlayers[indexToUpdate] = player
            } else {
                Toast.makeText(this, "Warning: Player not found in full list for re-save.", Toast.LENGTH_SHORT).show()
            }
            playerRepository.savePlayersAndHouseBalance(allPlayers, currentHouseBalance)
        }
    }
    */

    // postavi rezultate (remains the same)
    private fun setResultAndFinish() {
        val resultIntent = Intent().apply {
            putExtra(RESULT_EXTRA_PLAYER_UPDATED_FROM_EDIT, currentPlayer)
            putExtra(RESULT_EXTRA_HOUSE_BALANCE_UPDATED_FROM_EDIT, currentHouseBalance)
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}