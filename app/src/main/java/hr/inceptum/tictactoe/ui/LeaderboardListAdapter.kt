package hr.inceptum.tictactoe.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import hr.inceptum.tictactoe.R
import hr.inceptum.tictactoe.models.UserDocument
import kotlinx.android.synthetic.main.item_user.view.*

class LeaderboardListAdapter(
    var users: List<UserDocument>
) : RecyclerView.Adapter<LeaderboardListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)

        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        users[position]?.let { holder.bind(it) }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        /**
         * Method called to initialize UI with user data
         */
        fun bind(user: UserDocument) {
            itemView.user.text = user.name
            itemView.results.text =
                    "${user.onlineGamesWins ?: 0}W ${user.onlineGamesDraws ?: 0}D ${user.onlineGamesLost ?: 0}L"
        }
    }
}