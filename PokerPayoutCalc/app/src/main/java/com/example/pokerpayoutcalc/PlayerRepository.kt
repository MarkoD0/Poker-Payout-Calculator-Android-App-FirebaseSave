package com.example.pokerpayoutcalc

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.android.gms.tasks.Task

interface GameDataChangeListener {
    fun onGameDataChanged(gameData: GameData)
    fun onError(e: Exception)
}

class PlayerRepository {

    private val TAG = "PlayerRepository"
    private val firestore: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = Firebase.auth
    private val GAME_COLLECTION = "poker_games"
    private val GAME_DOCUMENT_ID = "current_game" // Fixed ID for the single game

    private var listenerRegistration: ListenerRegistration? = null
    private var gameDataChangeListener: GameDataChangeListener? = null

    init {
        // Authenticate anonymously when the repository is created
        // This is important because Firestore security rules usually require authentication.
        // Even for a shared single game, anonymous auth lets you control read/write access.
        signInAnonymously()
    }

    private fun signInAnonymously() {
        auth.signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Anonymous sign-in successful.")
                    // If successfully signed in, start listening for data
                    startListeningForGameData()
                } else {
                    Log.w(TAG, "Anonymous sign-in failed.", task.exception)
                    gameDataChangeListener?.onError(task.exception ?: Exception("Anonymous sign-in failed"))
                }
            }
    }

    // Set the listener for real-time updates
    fun setGameDataChangeListener(listener: GameDataChangeListener) {
        this.gameDataChangeListener = listener
        // If already signed in, ensure listener is active
        if (auth.currentUser != null) {
            startListeningForGameData()
        }
    }

    // Remove the listener when it's no longer needed (e.g., in onPause/onDestroy)
    fun removeGameDataChangeListener() {
        listenerRegistration?.remove()
        listenerRegistration = null
        this.gameDataChangeListener = null
        Log.d(TAG, "Firestore listener removed.")
    }

    private fun startListeningForGameData() {
        // Remove previous listener if exists to avoid duplicates
        listenerRegistration?.remove()

        listenerRegistration = firestore.collection(GAME_COLLECTION)
            .document(GAME_DOCUMENT_ID)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    gameDataChangeListener?.onError(e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val gameData = snapshot.toObject(GameData::class.java)
                    gameData?.let {
                        Log.d(TAG, "Current game data: ${it.players.size} players, House: ${it.houseBalance}, Game Ended: ${it.isGameEnded}")
                        gameDataChangeListener?.onGameDataChanged(it)
                    } ?: run {
                        Log.w(TAG, "GameData document exists but is null or malformed.")
                        // If document exists but cannot be parsed, create a default one
                        gameDataChangeListener?.onGameDataChanged(GameData())
                    }
                } else {
                    Log.d(TAG, "Current game data: null. Document might not exist, creating default.")
                    // Document doesn't exist, provide default game data
                    gameDataChangeListener?.onGameDataChanged(GameData())
                    // Optionally, you can also write an empty document to create it
                    // saveGameData(GameData(), {}).addOnFailureListener { Log.e(TAG, "Failed to create initial document", it) }
                }
            }
    }

    // Saves all game data to a single Firestore document
    fun saveGameData(gameData: GameData,
                     onSuccess: () -> Unit = {},
                     onFailure: (Exception) -> Unit = {}): Task<Void> {
        return firestore.collection(GAME_COLLECTION)
            .document(GAME_DOCUMENT_ID)
            .set(gameData) // set() will create or overwrite the document
            .addOnSuccessListener {
                Log.d(TAG, "Game data successfully saved.")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error saving game data", e)
                onFailure(e)
            }
    }

    // Clears all data by deleting the document or resetting its fields
    fun clearAllData(onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}): Task<Void> {
        val emptyGameData = GameData() // Create a default empty game state
        return firestore.collection(GAME_COLLECTION)
            .document(GAME_DOCUMENT_ID)
            .set(emptyGameData) // Overwrite with empty data
            .addOnSuccessListener {
                Log.d(TAG, "All game data cleared/reset.")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error clearing game data", e)
                onFailure(e)
            }
        // Alternatively, to actually delete the document:
        // return firestore.collection(GAME_COLLECTION).document(GAME_DOCUMENT_ID).delete()
        // But setting to empty is often safer for a single shared game document.
    }
}