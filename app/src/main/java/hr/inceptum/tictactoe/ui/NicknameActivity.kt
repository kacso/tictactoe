package hr.inceptum.tictactoe.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import hr.inceptum.tictactoe.R
import kotlinx.android.synthetic.main.activity_nickname.*

class NicknameActivity : AppCompatActivity() {
    companion object {
        fun start(context: Context?) {
            val intent = Intent(context, NicknameActivity::class.java)
            context?.startActivity(intent)
        }
    }

    private val auth by lazy {
        FirebaseAuth.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nickname)

        initListeners()
    }

    private fun initListeners() {
        nextBtn.setOnClickListener {
            //This will be called when user taps on next button
            handleNextBtn()
        }
    }

    private fun handleNextBtn() {
        val nickname = nicknameInput.text.toString()

        if (nickname.isNullOrEmpty()) {
            Toast.makeText(this, "Nickname is required", Toast.LENGTH_LONG).show()
            return
        }

        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                val user = task.result?.user
                user?.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(nickname).build())
                    ?.addOnCompleteListener {
                        StartGameActivity.start(this@NicknameActivity)
                    }
            }
    }
}
