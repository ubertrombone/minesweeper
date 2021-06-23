package com.minesweepermobile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.minesweepermobile.databinding.FragmentLoginBinding
import com.minesweepermobile.model.MinesweeperViewModel

class LoginFragment : Fragment(), View.OnClickListener {

    private var binding: FragmentLoginBinding? = null
    private val sharedViewModel: MinesweeperViewModel by activityViewModels()
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val G_TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 9001
        var userId = ""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val fragmentBinding = FragmentLoginBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth
        binding?.googleSigninButton?.setOnClickListener(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        observeUserState()
    }

    private fun observeUserState() = sharedViewModel.user.observe(viewLifecycleOwner) { if (it) findNavController().navigate(R.id.action_loginFragment_to_minesweeperFragment) }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(G_TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(G_TAG, "Google sign in failed", e)
                updateUI(null)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Log.d(G_TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    Log.w(G_TAG, "signInWithCredential:failure", task.exception)
                }
            }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun signOut() {
        auth.signOut()
        try {
            googleSignInClient.signOut().addOnCompleteListener(requireActivity()) {
                updateUI(null)
            }
        } catch (e: Exception) {
            updateUI(null)
        }
    }

    fun revokeAccess() {
        // Firebase sign out
        auth.signOut()
        // Google revoke access
        googleSignInClient.revokeAccess().addOnCompleteListener(requireActivity()) {
            updateUI(null)
        }
    }

    override fun onClick(v: View) {
        if (v.id == R.id.google_signin_button) {
            signIn()
        }
        else signOut()
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            userId = user.uid
            sharedViewModel.getUser(true)

            val database = Firebase.database("https://minesweeper-2bf76-default-rtdb.europe-west1.firebasedatabase.app/").reference
            database.child(userId).child("userLog").setValue(sharedViewModel.user.value)

        } else findNavController().navigate(R.id.action_minesweeperFragment_to_loginFragment)
    }
}