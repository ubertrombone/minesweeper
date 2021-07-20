package com.minesweeperMobile.leaderboard

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.minesweeperMobile.R
import com.minesweeperMobile.database.LeaderPairs

class LeaderBoardObjectAdapter:
    ListAdapter<LeaderPairs, LeaderBoardObjectAdapter.LeaderBoardObjectViewHolder>(LeaderBoardObjectDiffCallback()) {

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
        holder.record.text = item.record.toString()
    }
}

class LeaderBoardObjectDiffCallback: DiffUtil.ItemCallback<LeaderPairs>() {
    override fun areItemsTheSame(oldItem: LeaderPairs, newItem: LeaderPairs) = oldItem.username == newItem.username
    override fun areContentsTheSame(oldItem: LeaderPairs, newItem: LeaderPairs) = oldItem.username == newItem.username
}