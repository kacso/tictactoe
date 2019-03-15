package hr.inceptum.tictactoe.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import hr.inceptum.tictactoe.R
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * This is first activity that will be shown to user
 *
 * It will check if user already set nickname and automatically redirect it to [StartGameActivity]
 * in case that he has nickname set. Otherwise, it will prompt user to set nickname.
 */
class SplashActivity : AppCompatActivity() {
    private val auth by lazy {
        FirebaseAuth.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        GlobalScope.launch {
            Thread.sleep(500)

            if (auth.currentUser == null) {
                NicknameActivity.start(this@SplashActivity)
            } else {
                StartGameActivity.start(this@SplashActivity)
            }
            finish()
        }
    }
}
