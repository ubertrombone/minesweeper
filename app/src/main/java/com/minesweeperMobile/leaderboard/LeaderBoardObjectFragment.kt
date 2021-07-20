package com.minesweeperMobile.leaderboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.*
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.minesweeperMobile.Difficulties.*
import com.minesweeperMobile.R
import com.minesweeperMobile.databinding.FragmentLeaderBoardObjectBinding
import com.minesweeperMobile.model.MinesweeperViewModel
import java.lang.IndexOutOfBoundsException
import java.lang.NumberFormatException

class LeaderBoardObjectFragment : Fragment() {

    private val sharedViewModel: MinesweeperViewModel by activityViewModels()
    private var binding: FragmentLeaderBoardObjectBinding? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LeaderBoardObjectAdapter

    companion object {
        const val ARG_OBJECT = "object"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val fragmentBinding = FragmentLeaderBoardObjectBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = sharedViewModel
            leaderBoardObjectFragment = this@LeaderBoardObjectFragment
        }

        arguments?.takeIf { it.containsKey(ARG_OBJECT) }?.apply {
            binding?.sectionTitle?.text = sharedViewModel.listOfRecords[getInt(ARG_OBJECT)]
        }

        recyclerView = binding?.recyclerView!!
        setupAdapter()
    }

    override fun onResume() {
        super.onResume()
        observeViewModel()
    }

    private fun setupAdapter() {
        adapter = LeaderBoardObjectAdapter()
        recyclerView.adapter = adapter
    }

    private fun observeViewModel() {
        sharedViewModel.leaderBoardData.observe(viewLifecycleOwner) { data ->
            println("LEADERBOARDDATA OBSERVER: ${sharedViewModel.leaderBoardData.value}")
            adapter.submitList(data.sortedByDescending { (_, value) -> value })
            binding?.loadingPanel?.visibility = View.GONE
        }
        sharedViewModel.leaderBoardComplexitySelection.observe(viewLifecycleOwner) {
            println("LEADERBOARDCOMPLEXITYSELECTION OBSERVER BEFORE: ${sharedViewModel.leaderBoardData.value}")
            sharedViewModel.clearLeaderBoardData()
            println("LEADERBOARDCOMPLEXITYSELECTION OBSERVER AFTER: ${sharedViewModel.leaderBoardData.value}")
            readDatabase()
        }
    }

    private fun readDatabase() {

        val mapValue = when (binding?.sectionTitle?.text.toString()) {
            sharedViewModel.listOfRecords[0] -> "gamesWon"
            sharedViewModel.listOfRecords[1] -> "fastestGame"
            sharedViewModel.listOfRecords[2] -> "fewestMoves"
            sharedViewModel.listOfRecords[3] -> "longestStreak"
            sharedViewModel.listOfRecords[4] -> "currentStreak"
            sharedViewModel.listOfRecords[5] -> "winPercentage"
            else -> ""
        }

        val database = FirebaseDatabase.getInstance("https://minesweeper-2bf76-default-rtdb.europe-west1.firebasedatabase.app/").reference
        val statisticsListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.children.forEach { child ->
                    val username = child.child("username").value
                    val values = child.child(sharedViewModel.leaderBoardComplexitySelection.value.toString()).child(mapValue).value
                    try {
                        sharedViewModel.newLeaderBoardData(username.toString(), values.toString().toFloat())
                    } catch (e: IndexOutOfBoundsException) { println("EMPTY") } catch (e: NumberFormatException) { println("EMPTY") }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) = println("FAIL")
        }
        database.addValueEventListener(statisticsListener)
    }
}

//TODO: Game remembers the complexity toggled, need to make UI remember it from page to page
//TODO: Float numbers should lose decimal