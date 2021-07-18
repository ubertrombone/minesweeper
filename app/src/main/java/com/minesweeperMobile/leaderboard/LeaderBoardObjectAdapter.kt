package com.minesweeperMobile.leaderboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.minesweeperMobile.R
import com.minesweeperMobile.database.Statistics
import com.minesweeperMobile.model.MinesweeperViewModel

class LeaderBoardObjectAdapter:
    ListAdapter<Map<String, Statistics>, LeaderBoardObjectAdapter.LeaderBoardObjectViewHolder>(LeaderBoardObjectDiffCallback()) {

    private val sharedViewModel = MinesweeperViewModel()

    class LeaderBoardObjectViewHolder(val view: View): RecyclerView.ViewHolder(view) {
        //TODO: Add stuff
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderBoardObjectViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.leaderboard_cards, parent, false)
        return LeaderBoardObjectViewHolder(layout)
    }

    override fun onBindViewHolder(holder: LeaderBoardObjectViewHolder, position: Int) {
        TODO("Not yet implemented")
    }
}

class LeaderBoardObjectDiffCallback: DiffUtil.ItemCallback<Map<String, Statistics>>() {
    override fun areItemsTheSame(oldItem: Map<String, Statistics>, newItem: Map<String, Statistics>) = oldItem == newItem
    override fun areContentsTheSame(oldItem: Map<String, Statistics>, newItem: Map<String, Statistics>) = oldItem == newItem
}