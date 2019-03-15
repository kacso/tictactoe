package hr.inceptum.tictactoe.game

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import hr.inceptum.tictactoe.game.IGameUtil.Companion.BOARD_HEIGHT
import hr.inceptum.tictactoe.game.IGameUtil.Companion.BOARD_WIDTH
import hr.inceptum.tictactoe.game.IGameUtil.Companion.PLAYER_1
import hr.inceptum.tictactoe.game.IGameUtil.Companion.PLAYER_2
import hr.inceptum.tictactoe.models.UserDocument
import java.util.*

class SinglePlayerGame(
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

        if (currentPlayer == PLAYER_1) {
            nextPlayer()
            gameListener.onCurrentPlayerChanged(currentPlayer!!)
            makeComputerMove()
        } else {
            nextPlayer()
            gameListener.onCurrentPlayerChanged(currentPlayer!!)
        }

        return true
    }

    override fun start() {
        val r = Random()

        currentPlayer = r.nextInt(2) + 1
        gameListener.onCurrentPlayerChanged(currentPlayer!!)
        if (currentPlayer == PLAYER_2) {
            makeComputerMove()
        }
    }

    override fun getPlayers(): LiveData<Pair<UserDocument, UserDocument>> {
        val ld = MutableLiveData<Pair<UserDocument, UserDocument>>()
        ld.postValue(
            Pair(
                UserDocument(currentUser.displayName),
                UserDocument("Player 2")
            )
        )
        return ld
    }

    /**
     * It will take random position available
     */
    private fun makeComputerMove() {
        val r = Random()
        while (!makeMove(r.nextInt(BOARD_WIDTH), r.nextInt(BOARD_HEIGHT)));
    }
}