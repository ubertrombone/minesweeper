package com.minesweeperMobile.splash

import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.minesweeperMobile.Difficulties
import com.minesweeperMobile.R
import com.minesweeperMobile.database.Statistics
import com.minesweeperMobile.databinding.FragmentSplashBinding
import com.minesweeperMobile.login.LoginFragment
import com.minesweeperMobile.model.MinesweeperViewModel

class SplashFragment : Fragment() {
    private var binding: FragmentSplashBinding? = null
    private val sharedViewModel: MinesweeperViewModel by activityViewModels()
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.hide()
        auth = Firebase.auth
        if (auth.uid == null) findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
        LoginFragment.userId = auth.uid.toString()
        database = FirebaseDatabase.getInstance("https://minesweeper-2bf76-default-rtdb.europe-west1.firebasedatabase.app/").getReference("${auth.uid}/")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val fragmentBinding = FragmentSplashBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = sharedViewModel
            splashFragment = this@SplashFragment
        }

        queryUserFromDatabase()
    }

    private fun queryUserFromDatabase() {
        database.addListenerForSingleValueEvent(readDatabase("complexities"))
        database.addListenerForSingleValueEvent(readDatabase("user"))
    }

    private fun readDatabase(reference: String): ValueEventListener {
        return object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (reference == "user") pickUpUserFromDatabase(dataSnapshot)
                else if (reference == "complexities") pickUpComplexitiesFromDatabase(dataSnapshot)
            }
            override fun onCancelled(error: DatabaseError) = println("FAIL")
        }
    }

    private fun pickUpUserFromDatabase(dataSnapshot: DataSnapshot) {
        val children = mutableMapOf<String, String>()
        dataSnapshot.children.forEach { child -> children[child.key.toString()] = child.value.toString() }

        sharedViewModel.user.changeValue(children["userLog"].toString().toBoolean())
        sharedViewModel.fabButtonRTL.changeValue(children["RTL"].toString().toBoolean())
        sharedViewModel.difficultySet.changeValue(
            if (children["DefaultDifficulty"].toString() == "null") Difficulties.MEDIUM.difficulty
            else children["DefaultDifficulty"].toString()
        )
        sharedViewModel.difficultyHolder.changeValue(
            if (children["DefaultDifficulty"].toString() == "null") Difficulties.MEDIUM.difficulty
            else children["DefaultDifficulty"].toString()
        )
        sharedViewModel.mineAssistFAB.changeValue(children["MineAssist"].toString().toBoolean())
        sharedViewModel.username.changeValue(children.keys.contains("username"))
        if (children.keys.contains("username")) sharedViewModel.usernameFromDB.changeValue(children["username"].toString())
        navigate()
    }

    private fun pickUpComplexitiesFromDatabase(dataSnapshot: DataSnapshot) {
        dataSnapshot.children.forEach { child ->
            val complexityChildren = mutableListOf<String>()
            child.children.forEach { item -> complexityChildren.add(item.value.toString()) }
            when (child.key) {
                Difficulties.EASY.difficulty -> sharedViewModel.changeEasy(getComplexityValue(complexityChildren))
                Difficulties.MEDIUM.difficulty -> sharedViewModel.changeMedium(getComplexityValue(complexityChildren))
                Difficulties.HARD.difficulty -> sharedViewModel.changeHard(getComplexityValue(complexityChildren))
                Difficulties.EXPERT.difficulty -> sharedViewModel.changeExpert(getComplexityValue(complexityChildren))
            }
        }
    }

    private fun getComplexityValue(value: MutableList<String>): Statistics {
        return Statistics(value[5].toInt(), value[6].toInt(), value[8].toInt(), value[0].toDouble(), value[9].toLong(),
            value[1].toLong(), value[4].toInt(), value[3].toLong(), value[2].toInt(), value[7].toInt(), value[10].toDouble())
    }

    private fun navigate() {
        if (sharedViewModel.user.value) {
            findNavController().navigate(R.id.action_splashFragment_to_minesweeperFragment)
            val toast = Toast.makeText(requireContext(), "Long press to \n\n- lay a flag \n\n- clear numbers", Toast.LENGTH_LONG)
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()
        }
        else findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
    }
}