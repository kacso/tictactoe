package hr.inceptum.tictactoe.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.Observer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import hr.inceptum.tictactoe.R
import hr.inceptum.tictactoe.game.*
import hr.inceptum.tictactoe.game.IGameUtil.Companion.BOARD_HEIGHT
import hr.inceptum.tictactoe.game.IGameUtil.Companion.BOARD_WIDTH
import hr.inceptum.tictactoe.game.IGameUtil.Companion.PLAYER_1
import hr.inceptum.tictactoe.game.IGameUtil.Companion.PLAYER_2
import hr.inceptum.tictactoe.models.UserDocument
import kotlinx.android.synthetic.main.activity_game.*

/**
 * This activity will handle tic tac toe game by allowing user to
 * make moves, displaying info about users and showing final result
 */
class GameActivity : AppCompatActivity() {
    companion object {
        const val SINGLE_PLAYER_MODE = 1
        const val TWO_PLAYERS_OFFLINE_MODE = 2
        const val TWO_PLAYERS_ONLINE_MODE = 3

        const val MODE_EXTRAS = "mode"
        const val GAME_ID_EXTRAS = "gameId"
        const val X_ICON_RES = R.drawable.x_icon
        const val O_ICON_RES = R.drawable.circle_icon

        fun start(context: Context?, mode: Int, gameId: String? = null) {
            val intent = Intent(context, GameActivity::class.java)
            intent.putExtra(MODE_EXTRAS, mode)
            intent.putExtra(GAME_ID_EXTRAS, gameId)
            context?.startActivity(intent)
        }
    }

    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val currentUser: FirebaseUser by lazy {
        auth.currentUser!!
    }
    private val db by lazy {
        FirebaseFirestore.getInstance()
    }

    private val mode by lazy {
        intent.getIntExtra(MODE_EXTRAS, SINGLE_PLAYER_MODE)
    }

    private val gameId by lazy {
        intent.getStringExtra(GAME_ID_EXTRAS)
    }

    private lateinit var player1: UserDocument

    private lateinit var player2: UserDocument

    private val game: IGameUtil by lazy {
        when (mode) {
            SINGLE_PLAYER_MODE -> SinglePlayerGame(gameListener, currentUser)
            TWO_PLAYERS_OFFLINE_MODE -> TwoPlayersOfflineGame(gameListener, currentUser)
            TWO_PLAYERS_ONLINE_MODE -> TwoPlayersOnlineGame(gameListener, currentUser, db, gameId)
            else -> throw UnsupportedOperationException()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        initUI()
        initListeners()
        game.start()
    }

    private val gameListener = object : GameListener {
        override fun onGameFinished(winner: Int) {
            when (winner) {
                PLAYER_1 -> Toast.makeText(
                    this@GameActivity,
                    getString(R.string.game_winner, player1.name),
                    Toast.LENGTH_SHORT
                )
                    .show()
                PLAYER_2 -> Toast.makeText(
                    this@GameActivity,
                    getString(R.string.game_winner, player2.name),
                    Toast.LENGTH_SHORT
                )
                    .show()
                else -> Toast.makeText(
                    this@GameActivity,
                    getString(R.string.game_finished_draw),
                    Toast.LENGTH_SHORT
                ).show()
            }

        }

        override fun onCurrentPlayerChanged(player: Int) {
            if (player == IGameUtil.PLAYER_1) {
                currentPlayerIcon.setImageResource(R.drawable.arrow_left_bold)
            } else {
                currentPlayerIcon.setImageResource(R.drawable.arrow_right_bold)
            }
        }

        override fun boardUpdated(board: Array<Int>) {
            if (board.isNotEmpty()) {
                for (x in 0 until BOARD_WIDTH) {
                    for (y in 0 until BOARD_HEIGHT) {
                        val player = board[x * BOARD_WIDTH + y]
                        when {
                            x == 0 && y == 0 -> setPlayerIcon(icon_1_1, player)
                            x == 0 && y == 1 -> setPlayerIcon(icon_1_2, player)
                            x == 0 && y == 2 -> setPlayerIcon(icon_1_3, player)
                            x == 1 && y == 0 -> setPlayerIcon(icon_2_1, player)
                            x == 1 && y == 1 -> setPlayerIcon(icon_2_2, player)
                            x == 1 && y == 2 -> setPlayerIcon(icon_2_3, player)
                            x == 2 && y == 0 -> setPlayerIcon(icon_3_1, player)
                            x == 2 && y == 1 -> setPlayerIcon(icon_3_2, player)
                            x == 2 && y == 2 -> setPlayerIcon(icon_3_3, player)
                        }
                    }
                }
            }
        }
    }

    private fun initUI() {
        game.getPlayers().observe(this, Observer {
            player1 = it.first
            player2 = it.second

            player1User.text = player1.name
            player2User.text = player2.name

            if (player1.onlineGamesTotal != null) {
                player1Results.text = formatResults(player1)
                player1Results.visibility = VISIBLE
            } else {
                player1Results.visibility = GONE
            }
            if (player2.onlineGamesTotal != null) {
                player2Results.text = formatResults(player2)
                player2Results.visibility = VISIBLE
            } else {
                player2Results.visibility = GONE
            }
        })

    }

    private fun initListeners() {
        //Here we handle taps on each cell in tic tac toe table
        btn_1_1.setOnClickListener {
            game.makeMove(0, 0)
        }
        btn_1_2.setOnClickListener {
            game.makeMove(0, 1)
        }
        btn_1_3.setOnClickListener {
            game.makeMove(0, 2)
        }
        btn_2_1.setOnClickListener {
            game.makeMove(1, 0)
        }
        btn_2_2.setOnClickListener {
            game.makeMove(1, 1)
        }
        btn_2_3.setOnClickListener {
            game.makeMove(1, 2)
        }
        btn_3_1.setOnClickListener {
            game.makeMove(2, 0)
        }
        btn_3_2.setOnClickListener {
            game.makeMove(2, 1)
        }
        btn_3_3.setOnClickListener {
            game.makeMove(2, 2)
        }
    }

    private fun setPlayerIcon(view: AppCompatImageView, player: Int) {
        when (player) {
            IGameUtil.PLAYER_1 -> view.setImageResource(X_ICON_RES)
            IGameUtil.PLAYER_2 -> view.setImageResource(O_ICON_RES)
            else -> view.setImageDrawable(null)
        }
    }

    private fun formatResults(user: UserDocument): String {
        return "${user.onlineGamesWins ?: 0}W ${user.onlineGamesDraws ?: 0}D ${user.onlineGamesLost ?: 0}L"
    }

}
