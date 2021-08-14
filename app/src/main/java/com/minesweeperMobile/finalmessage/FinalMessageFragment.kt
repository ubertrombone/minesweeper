package com.minesweeperMobile.finalmessage

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.firebase.database.FirebaseDatabase
import com.minesweeperMobile.Difficulties.*
import com.minesweeperMobile.minesweeper.MinesweeperFragment
import com.minesweeperMobile.R
import com.minesweeperMobile.databinding.FragmentFinalMessageBinding
import com.minesweeperMobile.login.LoginFragment
import com.minesweeperMobile.model.MinesweeperViewModel

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

        if (sharedViewModel.difficultySet.dataValue.value == CUSTOM.difficulty || sharedViewModel.mineAssistFAB.value) {
            binding?.finalMessageTitle?.text = arguments?.getString(KEY_TITLE)
            binding?.time?.text = arguments?.getString(TIME)
            binding?.moves?.text = getString(R.string.moves, sharedViewModel.moveCounter.value)
            binding?.complexity?.text = getString(R.string.complexities, sharedViewModel.difficultySet.dataValue.value)
            binding?.currentStreak?.visibility = View.GONE
            binding?.gamesPlayed?.visibility = View.GONE
            binding?.winPercentage?.visibility = View.GONE
            return
        }

        (parentFragment as MinesweeperFragment).countTheLoss(arguments?.getString(KEY_TITLE) == getString(R.string.win))

        binding?.finalMessageTitle?.text = arguments?.getString(KEY_TITLE)
        binding?.time?.text = arguments?.getString(TIME)
        if (sharedViewModel.fastestGame.value){
            binding?.timeRecord?.visibility = View.VISIBLE
            sharedViewModel.fastestGame.changeValue(false)
        }
        binding?.moves?.text = getString(R.string.moves, sharedViewModel.moveCounter.value)
        if (sharedViewModel.fewestMoves.value) {
            binding?.movesRecord?.visibility = View.VISIBLE
            sharedViewModel.fewestMoves.changeValue(false)
        }
        binding?.currentStreak?.text = getString(R.string.statistics_current_streak, sharedViewModel.getComplexity()[0].currentStreak)
        if (sharedViewModel.longestStreak.value) {
            binding?.streakRecord?.visibility = View.VISIBLE
            sharedViewModel.longestStreak.changeValue(false)
        }
        binding?.complexity?.text = getString(R.string.complexities, sharedViewModel.difficultySet.dataValue.value)
        binding?.gamesPlayed?.text = getString(R.string.games_played, sharedViewModel.getComplexity()[0].gamesPlayed.toString())
        binding?.winPercentage?.text = getString(R.string.win_percentage, "%.2f".format(sharedViewModel.getComplexity()[0].winPercentage * 100) + "%")

        val database = FirebaseDatabase.getInstance("https://minesweeper-2bf76-default-rtdb.europe-west1.firebasedatabase.app/").getReference(LoginFragment.userId)
        database.child(sharedViewModel.difficultySet.dataValue.value.toString()).setValue(sharedViewModel.getComplexity()[0])
    }

    fun onYes() {
        dismiss()
        (parentFragment as MinesweeperFragment).checkDifficulty()
        val timeCounter = requireActivity().findViewById<Chronometer>(R.id.time_counter)
        timeCounter.base = SystemClock.elapsedRealtime()
    }

    fun closeDialog() {
        dismiss()
        (parentFragment as MinesweeperFragment).exitGame()
        sharedViewModel.resetFirstMove()
    }
}