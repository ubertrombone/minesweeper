package com.minesweeperMobile.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.minesweeperMobile.Difficulties
import com.minesweeperMobile.R
import com.minesweeperMobile.database.Statistics
import com.minesweeperMobile.databinding.FragmentLoginBinding
import com.minesweeperMobile.model.MinesweeperViewModel

class LoginFragment : Fragment(), View.OnClickListener {

    private var binding: FragmentLoginBinding? = null
    private val sharedViewModel: MinesweeperViewModel by activityViewModels()
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val RC_SIGN_IN = 9001
        var userId = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.hide()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val fragmentBinding = FragmentLoginBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = sharedViewModel
            loginFragment = this@LoginFragment
        }

        auth = Firebase.auth
        userId = auth.uid.toString()
        binding?.googleSigninButton?.setOnClickListener(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        observeUserState()
    }

    private fun observeUserState() = sharedViewModel.user.dataValue.observe(viewLifecycleOwner) {
        if (it) {
            binding?.googleSigninButton?.visibility = View.GONE
            binding?.loadingPanel?.visibility = View.VISIBLE
            findNavController().navigate(R.id.action_loginFragment_to_minesweeperFragment)
            val toast = Toast.makeText(requireContext(), "Long press to \n\n- lay a flag \n\n- clear numbers", Toast.LENGTH_LONG)
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()
        }
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) { updateUI(null) }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task -> if (task.isSuccessful) updateUI(auth.currentUser) }
    }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) { updateUI(null) }
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) resultLauncher.launch(signInIntent)
        else @Suppress("DEPRECATION") startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun signOut() {
        auth.signOut()
        try {
            googleSignInClient.signOut().addOnCompleteListener(requireActivity()) { updateUI(null) }
        } catch (e: Exception) { updateUI(null) }
    }

    override fun onClick(v: View) {
        if (v.id == R.id.google_signin_button) signIn()
        else signOut()
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            userId = user.uid
            sharedViewModel.user.changeValue(true)

            val database = FirebaseDatabase.getInstance("https://minesweeper-2bf76-default-rtdb.europe-west1.firebasedatabase.app/").getReference("${userId}/")
            database.child("userLog").setValue(sharedViewModel.user.dataValue.value)
            database.addListenerForSingleValueEvent(readDatabase(database))
        }
    }

    private fun readDatabase(database: DatabaseReference): ValueEventListener {
        return object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) = pickUpUserFromDatabase(dataSnapshot, database)
            override fun onCancelled(error: DatabaseError) = println("FAIL")
        }
    }

    private fun pickUpUserFromDatabase(dataSnapshot: DataSnapshot, database: DatabaseReference) {
        var counter = 0
        dataSnapshot.children.forEach { _ -> counter ++ }
        if (counter < 7) setupDatabase(database)
    }

    private fun setupDatabase(database: DatabaseReference) {
        database.child("RTL").setValue(sharedViewModel.fabButtonRTL.value)
        database.child("DefaultDifficulty").setValue(sharedViewModel.difficultyHolder.value)
        database.child("userLog").setValue(sharedViewModel.user.dataValue.value)
        database.child("MineAssist").setValue(sharedViewModel.mineAssistFAB.value)
        val allComplexities = Statistics(0, 0, 0, 0.0, 0L, 0L, 0, 0L, 0, 0, 0.0)
        sharedViewModel.changeAll(allComplexities)
        database.child(Difficulties.EASY.difficulty).setValue(sharedViewModel.easy[0])
        database.child(Difficulties.MEDIUM.difficulty).setValue(sharedViewModel.medium[0])
        database.child(Difficulties.HARD.difficulty).setValue(sharedViewModel.hard[0])
        database.child(Difficulties.EXPERT.difficulty).setValue(sharedViewModel.expert[0])
    }
}