package hr.inceptum.tictactoe.models

import com.google.firebase.firestore.PropertyName
import java.io.Serializable

data class PendingPlayer(
    @PropertyName(GAME_ID_NAME)
    var gameId: String? = null,
    @PropertyName(WAITING_FROM_NAME)
    var waitingFrom: Long? = null
) : Serializable {
    companion object {
        const val GAME_ID_NAME = "gameId"
        const val WAITING_FROM_NAME = "waitingFrom"
    }
}