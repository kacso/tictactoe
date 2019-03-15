package hr.inceptum.tictactoe.models

import com.google.firebase.firestore.PropertyName
import java.io.Serializable

data class UserDocument(
    @PropertyName(NAME_NAME)
    var name: String? = null,
    @PropertyName(ONLINE_GAMES_TOTAL_NAME)
    var onlineGamesTotal: Int? = null,
    @PropertyName(ONLINE_GAMES_WINS_NAME)
    var onlineGamesWins: Int? = null,
    @PropertyName(ONLINE_GAMES_DRAWS_NAME)
    var onlineGamesDraws: Int? = null,
    @PropertyName(ONLINE_GAMES_LOST_NAME)
    var onlineGamesLost: Int? = null
) : Serializable {
    companion object {
        const val NAME_NAME = "name"
        const val ONLINE_GAMES_TOTAL_NAME = "onlineGamesTotal"
        const val ONLINE_GAMES_WINS_NAME = "onlineGamesWins"
        const val ONLINE_GAMES_DRAWS_NAME = "onlineGamesDraws"
        const val ONLINE_GAMES_LOST_NAME = "onlineGamesLost"
    }
}