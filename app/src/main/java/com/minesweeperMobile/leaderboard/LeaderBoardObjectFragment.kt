package com.minesweeperMobile.leaderboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.minesweeperMobile.databinding.FragmentLeaderBoardObjectBinding
import com.minesweeperMobile.model.MinesweeperViewModel

class LeaderBoardObjectFragment : Fragment() {

    private val sharedViewModel: MinesweeperViewModel by activityViewModels()
    private var binding: FragmentLeaderBoardObjectBinding? = null

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
            val textView = binding?.textView
            textView?.text = getInt(ARG_OBJECT).toString()

            binding?.sectionTitle?.text = sharedViewModel.listOfRecords[getInt(ARG_OBJECT) - 1]
        }
    }

    fun onPress() {
        Toast.makeText(requireContext(), "THIS", Toast.LENGTH_SHORT).show()
    }
}