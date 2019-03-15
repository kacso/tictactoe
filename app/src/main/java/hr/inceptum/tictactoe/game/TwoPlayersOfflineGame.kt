package hr.inceptum.tictactoe.game

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import hr.inceptum.tictactoe.models.UserDocument
import hr.inceptum.tictactoe.game.IGameUtil.Companion.BOARD_HEIGHT
import hr.inceptum.tictactoe.game.IGameUtil.Companion.BOARD_WIDTH
import hr.inceptum.tictactoe.game.IGameUtil.Companion.PLAYER_1

class TwoPlayersOfflineGame(
    private val gameListener: GameListener,
    private val currentUser: FirebaseUser
) : IGameUtil {
    override var currentPlayer: Int? = PLAYER_1
    private val board = Array(BOARD_WIDTH * BOARD_HEIGHT) { 0 }
    private var isFinished = false

    override fun makeMove(x: Int, y: Int): Boolean {
        if (isFinished) return false
        if (board[x * BOARD_WIDTH + y] > 0) return false

        board[x * BOARD_WIDTH + y] = currentPlayer!!

        gameListener.boardUpdated(board)

        val winner = isGameCompleted(board)
        if (winner != null) {
            isFinished = true
            gameListener.onGameFinished(winner)
            return true
        }

        nextPlayer()
        gameListener.onCurrentPlayerChanged(currentPlayer!!)

        return true
    }

    override fun start() {
        //Let players decide who will be first
        gameListener.onCurrentPlayerChanged(currentPlayer!!)
    }

    override fun getPlayers(): LiveData<Pair<UserDocument, UserDocument>> {
        val ld = MutableLiveData<Pair<UserDocument, UserDocument>>()
        ld.postValue(
            Pair(
                UserDocument("Player 1"),
                UserDocument("Player 2")
            )
        )
        return ld
    }
}