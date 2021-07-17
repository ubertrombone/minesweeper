package com.minesweeperMobile.leaderboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.minesweeperMobile.model.MinesweeperViewModel

class LeaderBoardPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val sharedViewModel = MinesweeperViewModel()

    override fun getItemCount(): Int = sharedViewModel.listOfRecords.size

    override fun createFragment(position: Int): Fragment {
        val fragment = LeaderBoardObjectFragment()
        fragment.arguments = Bundle().apply {
            // Our object is just an integer :-P
            putInt(LeaderBoardObjectFragment.ARG_OBJECT, position + 1)
        }
        return fragment
    }
}