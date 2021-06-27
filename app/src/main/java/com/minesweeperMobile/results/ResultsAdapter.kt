package com.minesweeperMobile.results

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.minesweeperMobile.R
import com.minesweeperMobile.database.Statistics
import com.minesweeperMobile.model.MinesweeperViewModel

class ResultsAdapter(
    private val context: Context,
    private val dataset: List<List<Statistics>>
): RecyclerView.Adapter<ResultsAdapter.ResultsViewHolder>() {

    private val sharedViewModel = MinesweeperViewModel()

    class ResultsViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        var complexityTitle: TextView = view.findViewById(R.id.complexity_title)
        var gamesPlayed: TextView = view.findViewById(R.id.games_played)
        var gamesWon: TextView = view.findViewById(R.id.games_won)
        var currentStreak: TextView = view.findViewById(R.id.current_streak)
        var averageTime: TextView = view.findViewById(R.id.average_time)
        var averagesMoves: TextView = view.findViewById(R.id.average_moves)
        var winPct: TextView = view.findViewById(R.id.win_pct)
        var fastestGame: TextView = view.findViewById(R.id.fastest_game)
        var fewestMoves: TextView = view.findViewById(R.id.fewest_moves)
        var longestStreak: TextView = view.findViewById(R.id.longest_streak)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultsViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.results_cards, parent, false)
        return ResultsViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ResultsViewHolder, position: Int) {
        val item = dataset[position]
        holder.complexityTitle.text = sharedViewModel.listOfDifficulties[position]
        holder.gamesPlayed.text = context.getString(R.string.games_played, item[0].gamesPlayed.toString())
        holder.gamesWon.text = context.getString(R.string.statistics_games_won, item[0].gamesWon)
        holder.currentStreak.text = context.getString(R.string.statistics_current_streak, item[0].currentStreak)
        sharedViewModel.getTimes(item[0].averageTime)
        holder.averageTime.text = context.getString(
            R.string.statistics_average_time,
            "${sharedViewModel.hours}hr",
            "${sharedViewModel.minutes}min",
            "${sharedViewModel.seconds}s"
        )
        holder.averagesMoves.text = context.getString(R.string.statistics_average_moves, "%.2f".format(item[0].averageMoves))
        holder.winPct.text = context.getString(R.string.records_win_percentage, "%.2f".format(item[0].winPercentage * 100) + "%")
        sharedViewModel.getTimes(item[0].fastestGame)
        holder.fastestGame.text = context.getString(
            R.string.records_fastest_game,
            "${sharedViewModel.hours}hr",
            "${sharedViewModel.minutes}min",
            "${sharedViewModel.seconds}s"
        )
        holder.fewestMoves.text = context.getString(R.string.records_fewest_moves, item[0].fewestMoves)
        holder.longestStreak.text = context.getString(R.string.records_longest_streak, item[0].longestStreak)
    }

    override fun getItemCount() = dataset.size
}