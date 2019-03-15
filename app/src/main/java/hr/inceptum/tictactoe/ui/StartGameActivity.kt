package hr.inceptum.tictactoe.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import hr.inceptum.tictactoe.GAME_COLLECTION
import hr.inceptum.tictactoe.PENDING_PLAYERS_COLLECTION
import hr.inceptum.tictactoe.R
import hr.inceptum.tictactoe.USER_COLLECTION
import hr.inceptum.tictactoe.models.Game
import hr.inceptum.tictactoe.models.PendingPlayer
import hr.inceptum.tictactoe.models.UserDocument
import hr.inceptum.tictactoe.ui.GameActivity.Companion.TWO_PLAYERS_ONLINE_MODE
import kotlinx.android.synthetic.main.activity_start_game.*
import java.util.*

/**
 * This activity will give user option to choose game mode (single player, online/offline multiplayer)
 *
 * Additionally, it will leaderboard of users. Only online games are calculated.
 */
class StartGameActivity : AppCompatActivity() {
    companion object {
        fun start(context: Context?) {
            val intent = Intent(context, StartGameActivity::class.java)
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

    private val listAdapter by lazy {
        LeaderboardListAdapter(listOf())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_game)

        initUI()
        initListeners()
        initDB()
    }

    private lateinit var leaderboardRegistration: ListenerRegistration
    override fun onStart() {
        super.onStart()

        //Listen to any changes in leader board and display it to user
        leaderboardRegistration = db.collection(USER_COLLECTION)
            .orderBy(UserDocument.ONLINE_GAMES_WINS_NAME, Query.Direction.DESCENDING)
            .orderBy(UserDocument.ONLINE_GAMES_LOST_NAME, Query.Direction.ASCENDING)
            .orderBy(UserDocument.ONLINE_GAMES_DRAWS_NAME, Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("Leaderboard", e.message)
                    return@addSnapshotListener
                }

                val users = snapshot?.toObjects(UserDocument::class.java) ?: return@addSnapshotListener
                listAdapter.users = users
                listAdapter.notifyDataSetChanged()
            }
    }

    override fun onStop() {
        super.onStop()
        db.collection(PENDING_PLAYERS_COLLECTION).document(currentUser.uid).delete()
        leaderboardRegistration.remove()
    }

    private fun initUI() {
        val linearLayoutManager = LinearLayoutManager(this)
        listView.adapter = listAdapter
        listView.layoutManager = linearLayoutManager

    }

    private fun initListeners() {
        //TODO uncomment this
        /*
        singlePlayerMode.setOnClickListener {
            //This will be triggered when user taps on single player button
            GameActivity.start(this, GameActivity.SINGLE_PLAYER_MODE)
        }
        twoPlayersOffline.setOnClickListener {
            //This will be triggered when user taps on two players offline button
            GameActivity.start(this, GameActivity.TWO_PLAYERS_OFFLINE_MODE)
        }

        twoPlayersOnline.setOnClickListener {
            //This will be triggered when user taps on two players online button
            startOnlineGame()
        }
        */
    }

    /**
     * This will init info about user in database
     */
    private fun initDB() {
        db.collection(USER_COLLECTION)
            .document(currentUser.uid)
            .get()
            .addOnCompleteListener {
                var user: UserDocument? = null
                if (it.isSuccessful && it.result?.exists() == true) {
                    user = it.result?.toObject(UserDocument::class.java)
                }
                if (user == null) {
                    user = UserDocument()
                }
                user.name = currentUser.displayName ?: currentUser.email ?: "Anonymous"
                db.collection(USER_COLLECTION).document(currentUser.uid).set(user)
            }
    }

    /**
     * This will start online game.
     * First it will check if there are any pending players for game.
     * If they are, it will just join them, otherwise, it will put this user
     * in queue.
     */
    private fun startOnlineGame() {
        //Show progress indicator as it could take a while until we find another player
        val progressDialog = ProgressDialog.newInstance()
        progressDialog.show(supportFragmentManager, "Progress")

        //Get player who waits the longest for game to start
        db.collection(PENDING_PLAYERS_COLLECTION)
            .orderBy(PendingPlayer.WAITING_FROM_NAME, Query.Direction.ASCENDING)
            .limit(1)
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful && it.result?.documents?.isEmpty() == false) {
                    //If such a user exist, start the game
                    val player2Id = it.result!!.documents.first().id

                    //Create game for this players
                    db.collection(GAME_COLLECTION)
                        .add(
                            Game(
                                player1 = currentUser.uid,
                                player2 = player2Id,
                                currentPlayer = Random().nextInt(2) + 1
                            )
                        ).addOnCompleteListener {
                            if (it.isSuccessful) {
                                //Let other player know, on which game to join
                                val gameId = it.result!!.id
                                db.collection(PENDING_PLAYERS_COLLECTION)
                                    .document(player2Id)
                                    .update(PendingPlayer.GAME_ID_NAME, gameId)
                                    .addOnCompleteListener {
                                        progressDialog.dismiss()

                                        if (it.isSuccessful) {
                                            GameActivity.start(this, TWO_PLAYERS_ONLINE_MODE, gameId)
                                        } else {
                                            Toast.makeText(
                                                this,
                                                "Something went wrong, please try again",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }

                            } else {
                                Toast.makeText(this, "Something went wrong, please try again", Toast.LENGTH_SHORT)
                                    .show()
                                progressDialog.dismiss()

                            }
                        }


                } else {
                    //If such user those not exist, put this player in queue
                    val pendingPlayer = db.collection(PENDING_PLAYERS_COLLECTION)
                        .document(currentUser.uid)

                    pendingPlayer.set(PendingPlayer(waitingFrom = System.currentTimeMillis()))
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                //Wait until other player is matched with you and then join the game
                                pendingPlayer
                                    .addSnapshotListener { snapshot, e ->
                                        if (e != null) {
                                            return@addSnapshotListener
                                        }

                                        if (snapshot != null && snapshot.exists()) {
                                            val p = snapshot.toObject(PendingPlayer::class.java)
                                                ?: return@addSnapshotListener

                                            p.gameId?.apply {
                                                pendingPlayer.delete()
                                                progressDialog.dismiss()

                                                GameActivity.start(
                                                    this@StartGameActivity,
                                                    TWO_PLAYERS_ONLINE_MODE,
                                                    this
                                                )
                                            }

                                        }
                                    }
                            }
                        }
                }
            }
    }

}
