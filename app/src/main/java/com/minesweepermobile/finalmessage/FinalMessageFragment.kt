package com.minesweepermobile.finalmessage

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
import com.minesweepermobile.Difficulties
import com.minesweepermobile.minesweeper.MinesweeperFragment
import com.minesweepermobile.R
import com.minesweepermobile.databinding.FragmentFinalMessageBinding
import com.minesweepermobile.login.LoginFragment
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
        val complexity = when(sharedViewModel.difficultySet.value) {
            Difficulties.EASY.difficulty -> sharedViewModel.easy
            Difficulties.MEDIUM.difficulty -> sharedViewModel.medium
            Difficulties.HARD.difficulty -> sharedViewModel.hard
            else -> sharedViewModel.expert
        }
        val timeCounter = requireActivity().findViewById<Chronometer>(R.id.time_counter)
        val time = SystemClock.elapsedRealtime() - timeCounter?.base!!
        sharedViewModel.updateComplexities(sharedViewModel.difficultySet.value!!, time, arguments?.getString(KEY_TITLE) == getString(
            R.string.win
        ))

        binding?.finalMessageTitle?.text = arguments?.getString(KEY_TITLE)
        binding?.time?.text = arguments?.getString(TIME)
        binding?.moves?.text = getString(R.string.moves, sharedViewModel.moveCounter)
        binding?.complexity?.text = getString(R.string.complexities, sharedViewModel.difficultySet.value)
        binding?.gamesPlayed?.text = getString(R.string.games_played, complexity[0].gamesPlayed.toString())
        binding?.winPercentage?.text = getString(R.string.win_percentage, "%.2f".format(complexity[0].winPercentage * 100) + "%")

        val database = FirebaseDatabase.getInstance("https://minesweeper-2bf76-default-rtdb.europe-west1.firebasedatabase.app/").reference
        database.child(LoginFragment.userId).child(sharedViewModel.difficultySet.value.toString()).setValue(complexity[0])
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
    }
}