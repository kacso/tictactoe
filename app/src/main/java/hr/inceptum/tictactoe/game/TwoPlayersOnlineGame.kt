package hr.inceptum.tictactoe.game

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import hr.inceptum.tictactoe.GAME_COLLECTION
import hr.inceptum.tictactoe.USER_COLLECTION
import hr.inceptum.tictactoe.game.IGameUtil.Companion.BOARD_HEIGHT
import hr.inceptum.tictactoe.game.IGameUtil.Companion.BOARD_WIDTH
import hr.inceptum.tictactoe.game.IGameUtil.Companion.PLAYER_1
import hr.inceptum.tictactoe.game.IGameUtil.Companion.PLAYER_2
import hr.inceptum.tictactoe.models.Game
import hr.inceptum.tictactoe.models.UserDocument

class TwoPlayersOnlineGame(
    private val gameListener: GameListener,
    private val currentUser: FirebaseUser,
    private val db: FirebaseFirestore,
    private val gameId: String
) : IGameUtil {
    override var currentPlayer: Int? = null
    private var board = Array(BOARD_WIDTH * BOARD_HEIGHT) { 0 }
    private var isFinished = false
    private var players: Pair<UserDocument, UserDocument> = Pair(UserDocument(), UserDocument())
    private val playersLd: MutableLiveData<Pair<UserDocument, UserDocument>> = MutableLiveData()
    private var player = -1

    private val gameRef by lazy {
        db.collection(GAME_COLLECTION).document(gameId)
    }

    override fun makeMove(x: Int, y: Int): Boolean {
        if (currentPlayer == null) return false
        if (currentPlayer != player) return false
        if (isFinished) return false
        if (board[x * BOARD_WIDTH + y] > 0) return false

        board[x * BOARD_WIDTH + y] = currentPlayer!!

        gameRef.update(Game.BOARD_NAME, board.toList())


        val winner = isGameCompleted(board)
        if (winner != null) {
            isFinished = true
            gameRef.update(Game.WINNER_NAME, winner)
            return true
        }

        nextPlayer()
        gameRef.update(Game.CURRENT_PLAYER_NAME, currentPlayer)

        return true
    }

    override fun start() {
        //Get info about game
        gameRef.get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val game = it.result?.toObject(Game::class.java) ?: return@addOnCompleteListener

                    //Get info about players
                    db.collection(USER_COLLECTION).document(game.player1!!).get()
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                val p = it.result?.toObject(
                                    UserDocument::class.java
                                ) ?: return@addOnCompleteListener

                                players = Pair(p, players.second)
                                playersLd.postValue(players)
                            }

                        }
                    db.collection(USER_COLLECTION).document(game.player2!!).get()
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                val p = it.result?.toObject(
                                    UserDocument::class.java
                                ) ?: return@addOnCompleteListener

                                players = Pair(players.first, p)
                                playersLd.postValue(players)
                            }

                        }

                    //Keep listening about any change made in game (e.g. player updates the board)
                    //and update view accordingly
                    gameRef.addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            Log.e("Game", e.message)
                            return@addSnapshotListener
                        }

                        val game = snapshot?.toObject(Game::class.java) ?: return@addSnapshotListener
                        game.board?.toTypedArray()?.apply {
                            board = this
                            gameListener.boardUpdated(board)
                        }

                        currentPlayer = game.currentPlayer ?: return@addSnapshotListener
                        gameListener.onCurrentPlayerChanged(currentPlayer!!)

                        //If game is finished, winner property will be set
                        //Update statistic for current user
                        if (game.winner != null) {
                            isFinished = true
                            gameListener.onGameFinished(game.winner)

                            val p = if (player == PLAYER_1) players.first else players.second

                            when {
                                game.winner == player -> db.collection(USER_COLLECTION).document(currentUser.uid)
                                    .update(UserDocument.ONLINE_GAMES_WINS_NAME, (p.onlineGamesWins ?: 0) + 1)
                                game.winner == 0 -> db.collection(USER_COLLECTION).document(currentUser.uid)
                                    .update(UserDocument.ONLINE_GAMES_DRAWS_NAME, (p.onlineGamesDraws ?: 0) + 1)
                                else -> db.collection(USER_COLLECTION).document(currentUser.uid)
                                    .update(UserDocument.ONLINE_GAMES_LOST_NAME, (p.onlineGamesLost ?: 0) + 1)
                            }

                            db.collection(USER_COLLECTION).document(currentUser.uid)
                                .update(UserDocument.ONLINE_GAMES_TOTAL_NAME, (p.onlineGamesTotal ?: 0) + 1)

                        }
                    }

                    currentPlayer = game.currentPlayer ?: return@addOnCompleteListener
                    player = if (game.player1 == currentUser.uid) {
                        PLAYER_1
                    } else {
                        PLAYER_2
                    }

                    gameListener.onCurrentPlayerChanged(currentPlayer!!)
                }
            }

    }

    override fun getPlayers(): LiveData<Pair<UserDocument, UserDocument>> {
        return playersLd
    }
}