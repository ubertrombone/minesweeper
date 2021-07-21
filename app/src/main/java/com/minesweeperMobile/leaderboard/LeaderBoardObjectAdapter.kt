package com.minesweeperMobile.leaderboard

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.minesweeperMobile.R
import com.minesweeperMobile.database.LeaderPairs
import com.minesweeperMobile.model.MinesweeperViewModel
import kotlin.math.roundToInt

class LeaderBoardObjectAdapter(private val context: Context, private val page: String):
    ListAdapter<LeaderPairs, LeaderBoardObjectAdapter.LeaderBoardObjectViewHolder>(LeaderBoardObjectDiffCallback()) {

    private val sharedViewModel = MinesweeperViewModel()

    class LeaderBoardObjectViewHolder(val view: View): RecyclerView.ViewHolder(view) {
        var number: TextView = view.findViewById(R.id.number)
        var username: TextView = view.findViewById(R.id.username_section)
        var record: TextView = view.findViewById(R.id.record)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderBoardObjectViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.leaderboard_cards, parent, false)
        return LeaderBoardObjectViewHolder(layout)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: LeaderBoardObjectViewHolder, position: Int) {
        val item = getItem(position)

        holder.number.text = (position + 1).toString() + "."
        holder.username.text = item.username
        println(sharedViewModel.leaderBoardFragmentPage)
        holder.record.text = when (page) {
            "fastestGame" -> {
                sharedViewModel.getTimes(item.record.toLong())
                context.getString(R.string.record,
                    "${sharedViewModel.minutes}min", "${sharedViewModel.seconds}s")
            }
            "winPercentage" -> "%.2f".format(item.record * 100) + "%"
            else -> item.record.roundToInt().toString()
        }
    }
}

class LeaderBoardObjectDiffCallback: DiffUtil.ItemCallback<LeaderPairs>() {
    override fun areItemsTheSame(oldItem: LeaderPairs, newItem: LeaderPairs) = oldItem.username == newItem.username
    override fun areContentsTheSame(oldItem: LeaderPairs, newItem: LeaderPairs) = oldItem.username == newItem.username
}