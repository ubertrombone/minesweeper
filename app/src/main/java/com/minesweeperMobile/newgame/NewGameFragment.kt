package com.minesweeperMobile.newgame

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.minesweeperMobile.model.MinesweeperViewModel
import com.minesweeperMobile.Difficulties.*
import com.minesweeperMobile.R
import com.minesweeperMobile.databinding.FragmentNewGameBinding
import com.minesweeperMobile.helperclasses.InputFilterMinMax
import com.minesweeperMobile.helperclasses.MaterialSpinnerAdapter
import java.lang.NumberFormatException

class NewGameFragment: DialogFragment() {

    private val sharedViewModel: MinesweeperViewModel by activityViewModels()

    private var binding: FragmentNewGameBinding? = null

    companion object {
        const val TAG = "NewGameFragment"
        private const val KEY_TITLE = "KEY_TITLE"

        fun newInstance(title: String): NewGameFragment {
            val args = Bundle()
            args.putString(KEY_TITLE, title)
            val fragment = NewGameFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val fragmentBinding = FragmentNewGameBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = sharedViewModel
            newGameFragment = this@NewGameFragment
        }

        binding?.height?.filters = arrayOf(InputFilterMinMax(1, 50))
        binding?.width?.filters = arrayOf(InputFilterMinMax(1, 50))

        setupView()
        createSpinner(sharedViewModel.listOfDifficulties)
        submitNewGame()
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
        binding?.newGameTitle?.text = arguments?.getString(KEY_TITLE)
    }

    private fun createSpinner(dropList: MutableList<String>) {
        val spinnerDropdown = binding?.pickDifficultyDropdown
        val spinnerDropdownAdapter = MaterialSpinnerAdapter(requireContext(), R.layout.item_list, dropList)
        spinnerDropdown?.setAdapter(spinnerDropdownAdapter)
        spinnerDropdown?.setText(spinnerDropdownAdapter.getItem(0).toString(), false)
        spinnerDropdown?.setOnItemClickListener { _, _, position, _ ->
            if (spinnerDropdownAdapter.getItem(position).toString() == CUSTOM.difficulty) {
                binding?.heightField?.visibility = View.VISIBLE
                binding?.widthField?.visibility = View.VISIBLE
                binding?.numberOfMinesField?.visibility = View.VISIBLE
            } else {
                binding?.heightField?.visibility = View.GONE
                binding?.widthField?.visibility = View.GONE
                binding?.numberOfMinesField?.visibility = View.GONE
            }
        }
    }

    private fun submitNewGame() = binding?.submit?.setOnClickListener {
        val dropdownValue = binding?.pickDifficultyDropdown?.text.toString()
        val heightField = binding?.heightField
        val height = binding?.height
        val widthField = binding?.widthField
        val width = binding?.width
        val mineField = binding?.numberOfMinesField
        val mine = binding?.numberOfMines
        val maxNumberOfMines = try {
            height?.text.toString().toInt() * width?.text.toString().toInt() - 1
        } catch (e: NumberFormatException) { 0 }

        when (dropdownValue) {
            CUSTOM.difficulty -> {
                when {
                    height?.text.toString().isEmpty() || width?.text.toString().isEmpty() || mine?.text.toString().isEmpty() -> {
                        orElse(height!!, heightField!!, width!!, widthField!!, mine!!, mineField!!, maxNumberOfMines,
                            errorOne = false, errorTwo = true)
                    }
                    mine?.text.toString().toInt() > maxNumberOfMines -> {
                        binding?.width?.filters = arrayOf(InputFilterMinMax(1, maxNumberOfMines))
                        orElse(height!!, heightField!!, width!!, widthField!!, mine!!, mineField!!, maxNumberOfMines,
                            errorOne = true, errorTwo = false)
                    }
                    else -> {
                        sharedViewModel.setHeight(height?.text.toString().toInt())
                        sharedViewModel.setWidth(width?.text.toString().toInt())
                        sharedViewModel.setMines(mine?.text.toString().toInt())
                        sharedViewModel.setDifficulty(dropdownValue)
                        orElse(height!!, heightField!!, width!!, widthField!!, mine!!, mineField!!, maxNumberOfMines,
                            errorOne = false, errorTwo = false)
                        dismiss()
                    }
                }
            }
            else -> {
                sharedViewModel.setDifficulty(dropdownValue)
                orElse(height!!, heightField!!, width!!, widthField!!, mine!!, mineField!!, maxNumberOfMines,
                    errorOne = false, errorTwo = false)
                dismiss()
            }
        }
    }

    private fun orElse(height: TextInputEditText, heightField: TextInputLayout, width: TextInputEditText,
                       widthField: TextInputLayout, mines: TextInputEditText, mineField: TextInputLayout, max: Int,
                       errorOne: Boolean, errorTwo: Boolean) {
        if (!errorTwo) {
            setErrorEmpty(height, heightField, width, widthField, mines, mineField, errorTwo)
            numberOfMinesTooGreat(mines, mineField, max, errorOne)
        } else {
            numberOfMinesTooGreat(mines, mineField, max, errorOne)
            setErrorEmpty(height, heightField, width, widthField, mines, mineField, errorTwo)
        }
    }

    private fun numberOfMinesTooGreat(mines: TextInputEditText, mineField: TextInputLayout, max: Int, error: Boolean) {
        if (error) ifMaxError(mineField, getString(R.string.max_number_of_mines, max.toString()))
        else ifNotError(mines, mineField)
    }

    private fun setErrorEmpty(height: TextInputEditText, heightField: TextInputLayout, width: TextInputEditText,
                              widthField: TextInputLayout, mines: TextInputEditText, mineField: TextInputLayout,
                              error: Boolean) {
        if (error) {
            ifError(height, heightField, getString(R.string.enter_a_value))
            ifError(width, widthField, getString(R.string.enter_a_value))
            ifError(mines, mineField, getString(R.string.enter_a_value))
        } else {
            ifNotError(height, heightField)
            ifNotError(width, widthField)
            ifNotError(mines, mineField)
        }
    }

    private fun ifMaxError(field: TextInputLayout, string: String) {
        field.isErrorEnabled = true
        field.error = string
    }

    private fun ifError(input: TextInputEditText, field: TextInputLayout, string: String) {
        if (input.text.isNullOrEmpty()) {
            field.isErrorEnabled = true
            field.error = string
        } else field.isErrorEnabled = false
    }

    private fun ifNotError(input: TextInputEditText, field: TextInputLayout) {
        if (!input.text.isNullOrEmpty()) field.isErrorEnabled = false
    }
}