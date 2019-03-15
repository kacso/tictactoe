package hr.inceptum.tictactoe.game

import androidx.lifecycle.LiveData
import hr.inceptum.tictactoe.models.UserDocument

interface IGameUtil {
    companion object {
        const val PLAYER_1 = 1
        const val PLAYER_2 = 2
        const val BOARD_WIDTH = 3
        const val BOARD_HEIGHT = 3
    }

    var currentPlayer: Int?

    /**
     * Make move on position X, Y if possible.
     * Return true if move was made, or false otherwise
     */
    fun makeMove(x: Int, y: Int): Boolean

    /**
     * Start game.
     * This is used in order to initialize board and in case of online game to
     * start communication between two players
     */
    fun start()

    /**
     * Retrieve info about players which are playing current game
     */
    fun getPlayers(): LiveData<Pair<UserDocument, UserDocument>>

    /**
     * Make turn for next player
     */
    fun nextPlayer() {
        currentPlayer = if (currentPlayer == PLAYER_1) PLAYER_2
        else PLAYER_1
    }

    /**
     * Check if game is completed
     *
     * @return null in case that game is not over,
     *          0 in case game finished draw,
     *          1 in case player 1 won
     *          2 in case player 2 won
     */
    fun isGameCompleted(board: Array<Int>): Int? {
        for (i in 0 until BOARD_WIDTH) {
            val p = board[i * BOARD_WIDTH]

            if (p <= 0) continue

            if (p == board[i * BOARD_WIDTH + 1] && p == board[i * BOARD_WIDTH + 2]) {
                return p
            }
        }

        for (i in 0 until BOARD_HEIGHT) {
            val p = board[i]

            if (p <= 0) continue

            if (p == board[1 * BOARD_WIDTH + i] && p == board[2 * BOARD_WIDTH + i]) {
                return p
            }
        }


        var p = board[0]
        if (p > 0 && p == board[1 * BOARD_WIDTH + 1] && p == board[2 * BOARD_WIDTH + 2]) {
            return p
        }

        p = board[2]
        if (p > 0 && p == board[1 * BOARD_WIDTH + 1] && p == board[2 * BOARD_WIDTH]) {
            return p
        }

        for (i in 0 until BOARD_WIDTH) {
            for (j in 0 until BOARD_HEIGHT) {
                if (board[i * BOARD_WIDTH + j] == 0) {
                    return null
                }
            }
        }

        return 0
    }
}