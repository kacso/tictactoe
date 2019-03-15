package hr.inceptum.tictactoe.models

import com.google.firebase.firestore.PropertyName
import hr.inceptum.tictactoe.game.IGameUtil
import java.io.Serializable

data class Game(
    @PropertyName(PLAYER_1_NAME)
    var player1: String? = null,
    @PropertyName(PLAYER_2_NAME)
    var player2: String? = null,
    @PropertyName(BOARD_NAME)
    val board: List<Int>? = Array(IGameUtil.BOARD_WIDTH * IGameUtil.BOARD_HEIGHT) { 0 }.toList(),
    @PropertyName(WINNER_NAME)
    val winner: Int? = null,
    @PropertyName(CURRENT_PLAYER_NAME)
    val currentPlayer: Int? = null
) : Serializable {
    companion object {
        const val PLAYER_1_NAME = "player1"
        const val PLAYER_2_NAME = "player2"
        const val BOARD_NAME = "board"
        const val WINNER_NAME = "winner"
        const val CURRENT_PLAYER_NAME = "currentPlayer"
    }
}