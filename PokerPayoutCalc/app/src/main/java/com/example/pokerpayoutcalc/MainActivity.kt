package com.example.pokerpayoutcalc

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

// Remember to import all your constants from Constants.kt if you have them.
// For example:
// import com.example.pokerpayoutcalc.Constants.RESULT_EXTRA_NEW_PLAYER
// import com.example.pokerpayoutcalc.Constants.RESULT_EXTRA_HOUSE_BALANCE_OUT
// ... and so on for all constants you use.
// If all constants are in Constants.kt, you can use:
// import com.example.pokerpayoutcalc.Constants.*


class MainActivity : AppCompatActivity(), GameDataChangeListener { // <-- Implement GameDataChangeListener

    private lateinit var textViewTitle: TextView
    private lateinit var recyclerViewPlayers: RecyclerView
    private lateinit var buttonAddPlayer: Button
    private lateinit var buttonEndGame: Button
    // Removed textViewNoPlayersYet as it's not in your current XML

    private lateinit var playerRepository: PlayerRepository // Correct instantiation below

    private var currentGameData: GameData = GameData()

    private val combinedList = mutableListOf<ListItem>()
    private lateinit var adapter: PlayerAdapter

    // ActivityResultLaunchers (these remain largely the same, they interact with other activities)
    private val addPlayerActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val newPlayer: Player? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                data?.getParcelableExtra(RESULT_EXTRA_NEW_PLAYER, Player::class.java)
            } else {
                @Suppress("DEPRECATION")
                data?.getParcelableExtra(RESULT_EXTRA_NEW_PLAYER) as? Player
            }
            val updatedHouseBalance = data?.getIntExtra(RESULT_EXTRA_HOUSE_BALANCE_OUT, currentGameData.houseBalance) ?: currentGameData.houseBalance

            newPlayer?.let { player ->
                val updatedPlayers = currentGameData.players.toMutableList()
                updatedPlayers.add(player)
                val newGameData = GameData(updatedPlayers, updatedHouseBalance, isGameEnded = false)
                // Save data via repository; the listener in MainActivity will update the UI
                playerRepository.saveGameData(newGameData)
                Toast.makeText(this, "${player.name} added!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val rebuyActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val updatedPlayer: Player? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                data?.getParcelableExtra(RESULT_EXTRA_PLAYER_UPDATED, Player::class.java)
            } else {
                @Suppress("DEPRECATION")
                data?.getParcelableExtra(RESULT_EXTRA_PLAYER_UPDATED) as? Player
            }
            val updatedHouseBalance = data?.getIntExtra(RESULT_EXTRA_HOUSE_BALANCE_UPDATED, currentGameData.houseBalance) ?: currentGameData.houseBalance

            updatedPlayer?.let { player ->
                val updatedPlayers = currentGameData.players.toMutableList()
                val index = updatedPlayers.indexOfFirst { it.id == player.id }
                if (index != -1) {
                    updatedPlayers[index] = player
                    val newGameData = GameData(updatedPlayers, updatedHouseBalance, isGameEnded = false)
                    playerRepository.saveGameData(newGameData)
                    Toast.makeText(this, "${player.name} rebuy updated!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error: Player not found for rebuy update.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private val playerInfoActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val updatedPlayer: Player? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                data?.getParcelableExtra(RESULT_PLAYER_UPDATED_FROM_INFO, Player::class.java)
            } else {
                @Suppress("DEPRECATION")
                data?.getParcelableExtra(RESULT_PLAYER_UPDATED_FROM_INFO) as? Player
            }
            val updatedHouseBalance = data?.getIntExtra(RESULT_HOUSE_BALANCE_UPDATED_FROM_INFO, currentGameData.houseBalance) ?: currentGameData.houseBalance

            updatedPlayer?.let { player ->
                val updatedPlayers = currentGameData.players.toMutableList()
                val index = updatedPlayers.indexOfFirst { it.id == player.id }
                if (index != -1) {
                    updatedPlayers[index] = player
                    val newGameData = GameData(updatedPlayers, updatedHouseBalance, isGameEnded = false)
                    playerRepository.saveGameData(newGameData)
                    Toast.makeText(this, "${player.name} info updated!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error: Player not found for info update.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private val cashoutActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val updatedPlayer: Player? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                data?.getParcelableExtra(RESULT_PLAYER_UPDATED_FROM_CASHOUT, Player::class.java)
            } else {
                @Suppress("DEPRECATION")
                data?.getParcelableExtra(RESULT_PLAYER_UPDATED_FROM_CASHOUT) as? Player
            }
            val updatedHouseBalance = data?.getIntExtra(RESULT_HOUSE_BALANCE_UPDATED_FROM_CASHOUT, currentGameData.houseBalance) ?: currentGameData.houseBalance
            val cashoutMessage = data?.getStringExtra("cashout_message") ?: "Cashout processed."

            updatedPlayer?.let { player ->
                val updatedPlayers = currentGameData.players.toMutableList()
                val index = updatedPlayers.indexOfFirst { it.id == player.id }
                if (index != -1) {
                    updatedPlayers[index] = player
                    val newGameData = GameData(updatedPlayers, updatedHouseBalance, isGameEnded = false)
                    playerRepository.saveGameData(newGameData)
                    Toast.makeText(this, "${player.name} cashed out!", Toast.LENGTH_SHORT).show()

                    val resultIntent = Intent(this, CashoutResultActivity::class.java).apply {
                        putExtra("cashout_message", cashoutMessage)
                    }
                    startActivity(resultIntent)

                } else {
                    Toast.makeText(this, "Error: Player not found for cashout update.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private val endGameActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                val data: Intent? = result.data
                val endedGameData: GameData? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    data?.getParcelableExtra(RESULT_EXTRA_GAME_ENDED_DATA, GameData::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    data?.getParcelableExtra(RESULT_EXTRA_GAME_ENDED_DATA) as? GameData
                }

                endedGameData?.let { gameData ->
                    playerRepository.saveGameData(gameData) // Listener will pick up this save
                    Toast.makeText(this, "Game results saved.", Toast.LENGTH_SHORT).show()
                } ?: run {
                    Toast.makeText(this, "Error: Game data not returned from End Game Activity.", Toast.LENGTH_LONG).show()
                }
            }
            RESULT_CODE_NEW_GAME -> {
                // When "New Game" is chosen in EndGameActivity, it calls clearAllData
                playerRepository.clearAllData(
                    onSuccess = {
                        // After clearing, the Firestore listener in PlayerRepository will trigger onGameDataChanged with empty data
                        // No need to manually reset currentGameData here
                        Toast.makeText(this, "New game started! All data cleared.", Toast.LENGTH_LONG).show()
                    },
                    onFailure = { e ->
                        Toast.makeText(this, "Error starting new game: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                )
            }
            Activity.RESULT_CANCELED -> {
                Toast.makeText(this, "End game cancelled.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views with the correct IDs from your activity_main.xml
        textViewTitle = findViewById(R.id.textViewTitle)
        recyclerViewPlayers = findViewById(R.id.recyclerViewPlayers)
        buttonAddPlayer = findViewById(R.id.buttonAddPlayer)
        buttonEndGame = findViewById(R.id.buttonEndGame)
        // textViewNoPlayersYet is removed, its functionality is implicitly handled by RecyclerView visibility

        // Initialize PlayerRepository (Now correct with no-arg constructor)
        playerRepository = PlayerRepository()
        // Set the MainActivity as the listener for game data changes
        playerRepository.setGameDataChangeListener(this)

        setupRecyclerView()
        setupListeners()
        // Initial UI update will happen when onGameDataChanged is first called by the repository
    }

    override fun onResume() {
        super.onResume()
        // Re-attach the listener on resume to ensure real-time updates
        playerRepository.setGameDataChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        // Remove the listener on pause to prevent memory leaks and unnecessary updates
        playerRepository.removeGameDataChangeListener()
    }

    // --- GameDataChangeListener Interface Methods ---
    // This method will be called by PlayerRepository whenever game data changes in Firestore
    override fun onGameDataChanged(gameData: GameData) {
        currentGameData = gameData
        updateUI() // Update UI based on the new game data
        Log.d("MainActivity", "GameData updated via listener: $currentGameData")
    }

    // This method will be called by PlayerRepository if there's an error with the listener
    override fun onError(e: Exception) {
        Log.e("MainActivity", "Error listening to GameData: ${e.message}", e)
        Toast.makeText(this, "Error loading game data: ${e.message}", Toast.LENGTH_LONG).show()
    }
    // --- End GameDataChangeListener Interface Methods ---


    private fun setupRecyclerView() {
        adapter = PlayerAdapter(combinedList,
            onRebuyClick = { player ->
                val intent = Intent(this, RebuyActivity::class.java).apply {
                    putExtra(EXTRA_PLAYER, player)
                    putExtra(RESULT_EXTRA_HOUSE_BALANCE_UPDATED, currentGameData.houseBalance)
                }
                rebuyActivityResultLauncher.launch(intent)
            },
            onInfoClick = { player ->
                val intent = Intent(this, PlayerInfoActivity::class.java).apply {
                    putExtra(EXTRA_PLAYER_INFO, player)
                    putExtra(EXTRA_HOUSE_BALANCE_INFO, currentGameData.houseBalance)
                }
                playerInfoActivityResultLauncher.launch(intent)
            },
            onCashoutClick = { player ->
                val intent = Intent(this, CashoutActivity::class.java).apply {
                    putExtra(EXTRA_PLAYER_CASHOUT, player)
                    putExtra(EXTRA_HOUSE_BALANCE_CASHOUT_IN, currentGameData.houseBalance)
                }
                cashoutActivityResultLauncher.launch(intent)
            }
        )
        recyclerViewPlayers.layoutManager = LinearLayoutManager(this)
        recyclerViewPlayers.adapter = adapter
    }

    private fun setupListeners() {
        buttonAddPlayer.setOnClickListener {
            val intent = Intent(this, AddPlayerActivity::class.java).apply {
                putExtra(EXTRA_HOUSE_BALANCE_IN, currentGameData.houseBalance)
            }
            addPlayerActivityResultLauncher.launch(intent)
        }

        buttonEndGame.setOnClickListener {
            showEndGameConfirmationDialog()
        }
    }

    private fun updateUI() {
        // Clear and repopulate combinedList for the RecyclerView
        combinedList.clear()
        combinedList.add(House(balance = currentGameData.houseBalance)) // <-- Corrected House constructor call using named argument

        // Sort players based on the new logic using isVip and isPlaying
        val sortedPlayers = currentGameData.players.sortedWith(compareBy<Player> { player ->
            // Priority 1: VIP players
            if (player.isVip) 0
            // Priority 2: Non-VIP players who are still playing
            else if (player.isPlaying) 1
            // Priority 3: Players who are no longer playing (isPlaying == false)
            else 2
        }.thenBy { it.name.lowercase(Locale.ROOT) }) // Secondary sort by name for consistent order within groups

        combinedList.addAll(sortedPlayers)

        adapter.notifyDataSetChanged()

        // Update TextViews with current data
        textViewTitle.text = String.format(Locale.US, "Current Players: %d", currentGameData.players.size)

        // Show/hide RecyclerView and enable/disable End Game button based on player list
        if (currentGameData.players.isEmpty()) {
            recyclerViewPlayers.visibility = View.GONE
            buttonEndGame.isEnabled = false // Disable End Game if no players
            // You might want to display a Toast or a temporary message here if the list is empty
            // Toast.makeText(this, "No players yet. Click 'Add Player' to begin!", Toast.LENGTH_SHORT).show()
        } else {
            recyclerViewPlayers.visibility = View.VISIBLE
            buttonEndGame.isEnabled = true // Enable End Game if players exist
        }
    }

    // NEW FUNCTION: Checks if all players are cashed out (isPlaying == false)
    private fun areAllPlayersCashedOut(): Boolean {
        // If there are no players, it's considered "cashed out" for the purpose of ending the game.
        // However, showEndGameConfirmationDialog already checks for empty players.
        if (currentGameData.players.isEmpty()) {
            return true
        }
        return currentGameData.players.all { !it.isPlaying }
    }


    private fun showEndGameConfirmationDialog() {
        if (currentGameData.players.isEmpty()) {
            Toast.makeText(this, "Cannot end game with no players.", Toast.LENGTH_SHORT).show()
            return
        }

        // NEW CHECK: Verify all players are cashed out
        if (!areAllPlayersCashedOut()) {
            Toast.makeText(this, "All players must be cashed out before game ends.", Toast.LENGTH_LONG).show()
            return // Stop here if not all players are cashed out
        }

        AlertDialog.Builder(this)
            .setTitle("End Current Game?")
            .setMessage("Are you sure you want to end the current game and calculate payouts? This will update player balances and house balance.")
            .setPositiveButton("YES") { dialog, _ ->
                val intent = Intent(this, EndGameActivity::class.java).apply {
                    putParcelableArrayListExtra(EXTRA_ALL_PLAYERS_END_GAME, ArrayList(currentGameData.players))
                    putExtra(EXTRA_HOUSE_BALANCE_END_GAME, currentGameData.houseBalance)
                }
                endGameActivityResultLauncher.launch(intent)
                dialog.dismiss()
            }
            .setNegativeButton("CANCEL") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}