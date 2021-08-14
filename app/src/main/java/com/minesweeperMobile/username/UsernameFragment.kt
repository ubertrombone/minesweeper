package com.minesweeperMobile.username

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
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.minesweeperMobile.R
import com.minesweeperMobile.databinding.FragmentUsernameBinding
import com.minesweeperMobile.login.LoginFragment
import com.minesweeperMobile.model.MinesweeperViewModel

class UsernameFragment : DialogFragment() {
    private val sharedViewModel: MinesweeperViewModel by activityViewModels()
    private var binding: FragmentUsernameBinding? = null
    private val database = FirebaseDatabase.getInstance("https://minesweeper-2bf76-default-rtdb.europe-west1.firebasedatabase.app/").getReference(
        LoginFragment.userId)
    private val usernamesDatabase = FirebaseDatabase.getInstance("https://minesweeper-2bf76-default-rtdb.europe-west1.firebasedatabase.app/")
        .getReference("usernames")

    companion object {
        const val TAG = "UsernameFragment"
        private const val KEY_TITLE = "KEY_TITLE"

        fun newInstance(title: String): UsernameFragment {
            val args = Bundle()
            args.putString(KEY_TITLE, title)
            val fragment = UsernameFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val fragmentBinding = FragmentUsernameBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        isCancelable = false
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = sharedViewModel
            usernameFragment = this@UsernameFragment
        }

        if (!sharedViewModel.usernameSwitch.value) dismiss()
        sharedViewModel.usernameSwitch.changeValue(true)
        setupView()
        usernamesDatabase.addListenerForSingleValueEvent(readDatabase())
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

    private fun setupView() { binding?.usernameTitle?.text = arguments?.getString(KEY_TITLE) }

    fun confirmUsername() {
        val usernameInput = binding?.username
        val usernameField = binding?.usernameField

        if (usernameInput?.text.toString().isEmpty()) {
            inputEmpty(usernameInput!!, usernameField!!, true)
            return
        } else inputEmpty(usernameInput!!, usernameField!!, false)

        if (sharedViewModel.checkProfanityFilter(usernameInput.text.toString())) {
            usernameProfane(usernameInput, usernameField, true)
            return
        } else usernameProfane(usernameInput, usernameField, false)

        if (!sharedViewModel.checkUsernameUniqueness(usernameInput.text.toString())) {
            sharedViewModel.usernameFromDB.changeValue(usernameInput.text.toString())
            database.child("username").setValue(sharedViewModel.usernameFromDB)
            usernamesDatabase.child(sharedViewModel.usernameFromDB.value).setValue(sharedViewModel.usernameFromDB)
            usernameExists(usernameInput, usernameField, false)
            dismiss()
        } else {
            usernameExists(usernameInput, usernameField, true)
            return
        }
    }

    private fun inputEmpty(input: TextInputEditText, field: TextInputLayout, error: Boolean) {
        errorCheck(error, input.text.isNullOrEmpty(), field, R.string.choose_username)
    }

    private fun usernameExists(input: TextInputEditText, field: TextInputLayout, error: Boolean) {
        errorCheck(error, sharedViewModel.usernames.contains(input.text.toString()), field, R.string.username_exists)
    }

    private fun usernameProfane(input: TextInputEditText, field: TextInputLayout, error: Boolean) {
        errorCheck(error, sharedViewModel.checkProfanityFilter(input.text.toString()), field, R.string.profanity_filter)
    }

    private fun errorCheck(error: Boolean, condition: Boolean, field: TextInputLayout, string: Int) {
        if (error) {
            if (condition) {
                field.isErrorEnabled = true
                field.error = getString(string)
            } else field.isErrorEnabled = false
        } else {
            if (!condition) field.isErrorEnabled = false
        }
    }

    private fun readDatabase(): ValueEventListener {
        return object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.children.forEach { child -> sharedViewModel.addToUsernames(child.key.toString()) }
            }
            override fun onCancelled(error: DatabaseError) = println("FAIL")
        }
    }
}