package com.minesweeperMobile.settings

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.FirebaseDatabase
import com.minesweeperMobile.R
import com.minesweeperMobile.databinding.FragmentSettingsBinding
import com.minesweeperMobile.helperclasses.MaterialSpinnerAdapter
import com.minesweeperMobile.login.LoginFragment
import com.minesweeperMobile.minesweeper.ForfeitWarningFragment
import com.minesweeperMobile.minesweeper.MinesweeperFragment
import com.minesweeperMobile.model.MinesweeperViewModel

class SettingsFragment : DialogFragment() {

    private val sharedViewModel: MinesweeperViewModel by activityViewModels()
    private var binding: FragmentSettingsBinding? = null
    private val database = FirebaseDatabase.getInstance("https://minesweeper-2bf76-default-rtdb.europe-west1.firebasedatabase.app/").getReference(LoginFragment.userId)
    private var mineAssistTurnedOn = false

    companion object {
        const val TAG = "SettingsFragment"
        private const val KEY_TITLE = "KEY_TITLE"

        fun newInstance(title: String): SettingsFragment {
            val args = Bundle()
            args.putString(KEY_TITLE, title)
            val fragment = SettingsFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val fragmentBinding = FragmentSettingsBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = sharedViewModel
            settingsFragment = this@SettingsFragment
        }

        mineAssistTurnedOn = sharedViewModel.mineAssistFAB
        binding?.rtlSwitch?.isChecked = sharedViewModel.fabButtonRTL
        binding?.mineAssistSwitch?.isChecked = sharedViewModel.mineAssistFAB
        setupView()
        createSpinnerForLaunch(sharedViewModel.listOfDifficulties.dropLast(1))
        binding?.setDifficultyDropdown?.setText(sharedViewModel.difficultyHolder)
        saveSettings()
        binding?.mineAssistSwitch?.setOnClickListener { mineSwitchWarning() }
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

    private fun setupView() { binding?.settingsTitle?.text = arguments?.getString(KEY_TITLE) }

    private fun createSpinnerForLaunch(dropList: List<String>) {
        val spinnerDropdown = binding?.setDifficultyDropdown
        val spinnerDropdownAdapter = MaterialSpinnerAdapter(requireContext(), R.layout.item_list, dropList.toMutableList())
        spinnerDropdown?.setAdapter(spinnerDropdownAdapter)
    }

    private fun saveSettings() = binding?.save?.setOnClickListener {
        onSwitchClicked()
        onMineAssistSwitch()
        if (sharedViewModel.mineAssistFAB != mineAssistTurnedOn) sharedViewModel.changeMineAssist(true)
        else sharedViewModel.changeMineAssist(false)
        onLaunchDifficultySet()
        if (!mineAssistTurnedOn && sharedViewModel.mineAssistFAB && sharedViewModel.firstMoveSwitch != 0)
            ForfeitWarningFragment.newInstance(getString(R.string.forfeit), TAG)
                .show(childFragmentManager, ForfeitWarningFragment.TAG)
        else {
            dismiss()
            if (sharedViewModel.mineAssistChanged) (parentFragment as MinesweeperFragment).checkDifficulty()
        }
    }

    private fun orientFABButtons() {
        sharedViewModel.fabButtonSettings(binding?.rtlSwitch!!.isChecked)
        val fabButtons = requireActivity().findViewById<LinearLayout>(R.id.fab_buttons)
        fabButtons.layoutDirection = if (sharedViewModel.fabButtonRTL) View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
    }

    private fun onSwitchClicked() {
        database.child("RTL").setValue(binding?.rtlSwitch?.isChecked)
        orientFABButtons()
    }

    private fun onLaunchDifficultySet() {
        database.child("DefaultDifficulty").setValue(binding?.setDifficultyDropdown?.text.toString())
        sharedViewModel.setDifficultyHolder(binding?.setDifficultyDropdown?.text.toString())
    }

    private fun onMineAssistSwitch() {
        database.child("MineAssist").setValue(binding?.mineAssistSwitch?.isChecked)
        engageMineAssist()
    }

    private fun mineSwitchWarning() {
        if (binding?.mineAssistSwitch?.isChecked == true) {
            WarningFragment.newInstance(getString(R.string.warning).uppercase())
                .show(childFragmentManager, WarningFragment.TAG)
        }
    }

    private fun engageMineAssist() {
        sharedViewModel.mineAssistSettings(binding?.mineAssistSwitch!!.isChecked)
        val mineAssistButton = requireActivity().findViewById<FloatingActionButton>(R.id.fab_mine)
        mineAssistButton.isEnabled = sharedViewModel.mineAssistFAB
        mineAssistButton.visibility = if (sharedViewModel.mineAssistFAB) View.VISIBLE else View.GONE
    }

    fun countTheLossInSettings() = (parentFragment as MinesweeperFragment).countTheLoss(false)
    fun checkDifficultyInSettings() = (parentFragment as MinesweeperFragment).checkDifficulty()
}