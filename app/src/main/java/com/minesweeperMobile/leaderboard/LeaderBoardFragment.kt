package com.minesweeperMobile.leaderboard

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.minesweeperMobile.databinding.FragmentLeaderBoardBinding
import com.minesweeperMobile.model.MinesweeperViewModel

class LeaderBoardFragment : DialogFragment() {

    private val sharedViewModel: MinesweeperViewModel by activityViewModels()
    private var binding: FragmentLeaderBoardBinding? = null
    private lateinit var leaderBoardPagerAdapter: LeaderBoardPagerAdapter
    private lateinit var viewPager: ViewPager2

    companion object {
        const val TAG = "LeaderBoardFragment"
        fun newInstance() = LeaderBoardFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val fragmentBinding = FragmentLeaderBoardBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = sharedViewModel
            leaderBoardFragment = this@LeaderBoardFragment
        }

        leaderBoardPagerAdapter = LeaderBoardPagerAdapter(this)
        viewPager = binding?.pager!!
        viewPager.adapter = leaderBoardPagerAdapter
    }

    override fun onStart() {
        super.onStart()
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getRealMetrics(displayMetrics)
        dialog?.window?.setLayout(
            displayMetrics.widthPixels - (displayMetrics.widthPixels * .05).toInt(),
            displayMetrics.heightPixels - (displayMetrics.heightPixels * .1).toInt()
        )
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
}