package com.minesweeperMobile.leaderboard

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.minesweeperMobile.Difficulties.*
import com.minesweeperMobile.R
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

        setClickListeners(view, R.id.text_view_easy)
        setClickListeners(view, R.id.text_view_medium)
        setClickListeners(view, R.id.text_view_hard)
        setClickListeners(view, R.id.text_view_expert)
    }

    override fun onStart() {
        super.onStart()
        val displayMetrics = DisplayMetrics()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) activity?.display?.getRealMetrics(displayMetrics)
        else @Suppress("DEPRECATION") activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)

        dialog?.window?.setLayout(
            displayMetrics.widthPixels - (displayMetrics.widthPixels * .05).toInt(),
            displayMetrics.heightPixels - (displayMetrics.heightPixels * .1).toInt()
        )
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
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
        sharedViewModel.leaderBoardComplexitySelection.changeValue(textView.text.toString())
    }

    private fun setSelected(textView: TextView, difficulty: String, id: Int) {
        val constraint = requireView().findViewById<ConstraintLayout>(id)
        if (textView.text.toString() == difficulty) {
            constraint.background = getDrawable(requireContext(), R.drawable.dialog_straight_selected)
        }
        else constraint.background = getDrawable(requireContext(), R.drawable.dialog_straight)
    }
}