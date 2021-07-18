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
import com.minesweeperMobile.database.Statistics
import com.minesweeperMobile.databinding.FragmentLeaderBoardObjectBinding
import com.minesweeperMobile.model.MinesweeperViewModel
import java.lang.IndexOutOfBoundsException

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
            binding?.sectionTitle?.text = sharedViewModel.listOfRecords[getInt(ARG_OBJECT) - 1]
        }

        setClickListeners(view, R.id.text_view_easy)
        setClickListeners(view, R.id.text_view_medium)
        setClickListeners(view, R.id.text_view_hard)
        setClickListeners(view, R.id.text_view_expert)

        recyclerView = binding?.recyclerView!!
        setupAdapter()
        observeViewModel()
    }

    private fun setClickListeners(view: View, id: Int) {
        val textView = view.findViewById<TextView>(id)
        textView.setOnClickListener { getID(textView) }
    }

    private fun getID(textView: TextView) {
        setSelected(textView, EASY.difficulty, R.id.easy_constraint)
        setSelected(textView, MEDIUM.difficulty, R.id.medium_constraint)
        setSelected(textView, HARD.difficulty, R.id.hard_constraint)
        setSelected(textView, EXPERT.difficulty, R.id.expert_constraint)
        sharedViewModel.setLeaderBoardComplexitySelection(textView.text.toString())
    }

    private fun setSelected(textView: TextView, difficulty: String, id: Int) {
        val constraint = requireView().findViewById<ConstraintLayout>(id)
        if (textView.text.toString() == difficulty) constraint.background = getDrawable(requireContext(), R.drawable.dialog_straight_selected)
        else constraint.background = getDrawable(requireContext(), R.drawable.dialog_straight)
    }

    private fun setupAdapter() {
        adapter = LeaderBoardObjectAdapter()
        recyclerView.adapter = adapter
    }

    private fun observeViewModel() {
        sharedViewModel.leaderBoardDate.observe(viewLifecycleOwner) { data ->
//            adapter.submitList(listOf(data))
            sharedViewModel.leaderBoardDate.value!!.values.forEach { println(it.winPercentage) }
        }
        sharedViewModel.leaderBoardComplexitySelection.observe(viewLifecycleOwner) {
            readDatabase()
            // TODO: hide progress bar
        }
    }

    private fun readDatabase() {
        val database = FirebaseDatabase.getInstance("https://minesweeper-2bf76-default-rtdb.europe-west1.firebasedatabase.app/").reference
        val statisticsListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.children.forEach { child ->
                    val username = dataSnapshot.child(child.key.toString()).child("username").value
                    val listOfStatistics = mutableListOf<String>()
                    child.child(sharedViewModel.leaderBoardComplexitySelection.value.toString()).children.forEach { child2 ->
                        listOfStatistics.add(child2.value.toString())
                    }
                    try {
                        sharedViewModel.newLeaderBoardData(username.toString(), toStatistics(listOfStatistics))
                    } catch (e: IndexOutOfBoundsException) { println("EMPTY") }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) = println("FAIL")
        }
        database.addValueEventListener(statisticsListener)
    }

    private fun toStatistics(value: List<String>): Statistics {
        return Statistics(value[5].toInt(), value[6].toInt(), value[8].toInt(), value[0].toDouble(), value[9].toLong(),
            value[1].toLong(), value[4].toInt(), value[3].toLong(), value[2].toInt(), value[7].toInt(), value[10].toDouble())
    }
}