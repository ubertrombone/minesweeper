package com.minesweeperMobile.minesweeper

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.minesweeperMobile.databinding.FragmentForfeitWarningBinding
import com.minesweeperMobile.model.MinesweeperViewModel
import com.minesweeperMobile.newgame.NewGameFragment
import com.minesweeperMobile.settings.SettingsFragment

class ForfeitWarningFragment : DialogFragment() {
    private val sharedViewModel: MinesweeperViewModel by activityViewModels()
    private var binding: FragmentForfeitWarningBinding? = null

    companion object {
        const val TAG = "ForfeitWarningFragment"
        private const val KEY_TITLE = "KEY_TITLE"
        private const val FRAGMENT = "FRAGMENT"

        fun newInstance(title: String, dialogFragment: String): ForfeitWarningFragment {
            val args = Bundle()
            args.putString(KEY_TITLE, title)
            args.putString(FRAGMENT, dialogFragment)
            val fragment = ForfeitWarningFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val fragmentBinding = FragmentForfeitWarningBinding.inflate(inflater,container, false)
        binding = fragmentBinding
        isCancelable = false
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = sharedViewModel
            forfeitWarningFragment = this@ForfeitWarningFragment
        }

        setupView()
    }

    override fun onStart() {
        super.onStart()
        val displayMetrics = DisplayMetrics()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) activity?.display?.getRealMetrics(displayMetrics)
        else @Suppress("DEPRECATION") activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)

        dialog?.window?.setLayout(
            displayMetrics.widthPixels - (displayMetrics.widthPixels * .2).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun setupView() { binding?.forfeitWarningTitle?.text = arguments?.getString(KEY_TITLE) }

    fun onConfirm() {
        dismiss()
        when (arguments?.getString(FRAGMENT)) {
            SettingsFragment.TAG -> {
                (parentFragment as SettingsFragment).dismiss()
                (parentFragment as SettingsFragment).countTheLossInSettings()
                if (sharedViewModel.mineAssistChanged) (parentFragment as SettingsFragment).checkDifficultyInSettings()
            }
            NewGameFragment.TAG -> {
                (parentFragment as NewGameFragment).dismiss()
                (parentFragment as NewGameFragment).countTheLossInNewGame()
                sharedViewModel.setDifficulty(NewGameFragment.dropdownValueForForfeit)
            }
        }
    }
}