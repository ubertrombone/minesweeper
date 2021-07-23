package com.minesweeperMobile.leaderboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.minesweeperMobile.R
import com.minesweeperMobile.database.LeaderPairs
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
    }

    override fun onResume() {
        super.onResume()
        sharedViewModel.getLeaderBoardFragmentPage(binding?.sectionTitle?.text.toString())
        setupAdapter()
        observeViewModel()
    }

    private fun setupAdapter() {
        adapter = LeaderBoardObjectAdapter(requireContext(), sharedViewModel.leaderBoardFragmentPage)
        recyclerView.adapter = adapter
    }

    private fun observeViewModel() {
        sharedViewModel.leaderBoardData.observe(viewLifecycleOwner) { data ->
            val distinctlySorted = if (sharedViewModel.listOfDescendingRecords.contains(sharedViewModel.leaderBoardFragmentPage)) {
                val distinctlyCleaned = data.distinct().sortedBy { (_, value) -> value }
                val cleaned = mutableListOf<LeaderPairs>()
                distinctlyCleaned.forEach { if (it.record.toDouble() != 0.0) cleaned.add(it) }
                cleaned
            } else data.distinct().sortedByDescending { (_, value) -> value }

            adapter.submitList(distinctlySorted)
            binding?.loadingPanel?.visibility = View.GONE
        }
        sharedViewModel.leaderBoardComplexitySelection.observe(viewLifecycleOwner) { readDatabase() }
    }

    private fun readDatabase() {
        sharedViewModel.clearLeaderBoardData()
        val database = FirebaseDatabase.getInstance("https://minesweeper-2bf76-default-rtdb.europe-west1.firebasedatabase.app/").reference
        val statisticsListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.children.forEach { child ->
                    val username = child.child("username").value
                    val values = child.child(sharedViewModel.leaderBoardComplexitySelection.value.toString())
                        .child(sharedViewModel.leaderBoardFragmentPage).value
                    try {
                        sharedViewModel.newLeaderBoardData(username.toString(), values.toString().toFloat())
                    } catch (e: IndexOutOfBoundsException) { println("EMPTY") } catch (e: NumberFormatException) { println("EMPTY") }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) = println("FAIL")
        }
        database.addListenerForSingleValueEvent(statisticsListener)
    }
}