package com.minesweeperMobile.results

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.minesweeperMobile.databinding.FragmentResultsBinding
import com.minesweeperMobile.model.MinesweeperViewModel

class ResultsFragment : DialogFragment() {

    private val sharedViewModel: MinesweeperViewModel by activityViewModels()
    private var binding: FragmentResultsBinding? = null

    companion object {
        const val TAG = "ResultsFragment"
        private const val KEY_TITLE = "KEY_TITLE"

        fun newInstance(title: String): ResultsFragment {
            val args = Bundle()
            args.putString(KEY_TITLE, title)
            val fragment = ResultsFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val fragmentBinding = FragmentResultsBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = sharedViewModel
            resultsFragment = this@ResultsFragment
        }

        setupView()
        setupAdapter()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun setupView() {
        binding?.resultsTitle?.text = arguments?.getString(KEY_TITLE)
    }

    private fun setupAdapter() {
        val listOfComplexities = listOf(sharedViewModel.easy, sharedViewModel.medium, sharedViewModel.hard, sharedViewModel.expert)
        binding?.recyclerView?.adapter = ResultsAdapter(requireContext(), listOfComplexities)
    }
}