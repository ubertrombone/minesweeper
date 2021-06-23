package com.minesweepermobile

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.minesweepermobile.databinding.FragmentFinalMessageBinding
import com.minesweepermobile.model.MinesweeperViewModel

class FinalMessageFragment : DialogFragment() {

    private val sharedViewModel: MinesweeperViewModel by activityViewModels()

    private var binding: FragmentFinalMessageBinding? = null

    companion object {
        const val TAG = "FinalMessageFragment"
        private const val KEY_TITLE = "KEY_TITLE"
        private const val TIME = "TIME"

        fun newInstance(title: String, time: String): FinalMessageFragment {
            val args = Bundle()
            args.putString(KEY_TITLE, title)
            args.putString(TIME, time)
            val fragment = FinalMessageFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val fragmentBinding = FragmentFinalMessageBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        isCancelable = false
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = sharedViewModel
            finalMessageFragment = this@FinalMessageFragment
        }

        setupView()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun setupView() {
        binding?.finalMessageTitle?.text = arguments?.getString(KEY_TITLE)
        binding?.time?.text = arguments?.getString(TIME)
        binding?.moves?.text = getString(R.string.moves, sharedViewModel.mineCounter.toString())
        binding?.complexity?.text = getString(R.string.complexities, sharedViewModel.difficultySet.value)
        binding?.gamesPlayed?.text = getString(R.string.games_played, "1654")
        binding?.winPercentage?.text = getString(R.string.win_percentage, "4.6%")
    }

    //TODO: Restart Game function will need to be moved here too.
    fun onYes() = MinesweeperFragment().checkDifficulty()

    fun closeDialog() {
        dismiss()
        exitGame()
    }
    private fun exitGame() = (0 until sharedViewModel.width * sharedViewModel.height)
        .forEach { makeClickable(sharedViewModel.convertNumberToCoords(it + 1), false) }
    private fun makeClickable(coords: List<Int>, trueOrFalse: Boolean) =
        createCardView(sharedViewModel.convertCoordsToNumber(coords), background = false, clickable = trueOrFalse)
    private fun createCardView(card: Int, background: Boolean, clickable: Boolean): CardView {
        val cardView = requireActivity().findViewById<CardView>(card)
        if (background) cardView.setBackgroundColor(getColor(requireContext(), MinesweeperFragment().getCardBackgroundColor(card, R.color.gray_400, R.color.gray_dark)))
        cardView.isClickable = clickable
        cardView.isLongClickable = clickable
        return cardView
    }
}