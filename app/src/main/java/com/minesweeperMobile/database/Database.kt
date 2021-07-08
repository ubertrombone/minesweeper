package com.minesweeperMobile.database

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.minesweeperMobile.Difficulties
import com.minesweeperMobile.model.MinesweeperViewModel

class Database(auth: FirebaseAuth) {
    val database = FirebaseDatabase.getInstance("https://minesweeper-2bf76-default-rtdb.europe-west1.firebasedatabase.app/").getReference("${auth.uid}/")
    private val sharedViewModel = MinesweeperViewModel()

    fun readDatabase(database: DatabaseReference): ValueEventListener {
        return object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) = pickUpUserFromDatabase(dataSnapshot, database)
            override fun onCancelled(error: DatabaseError) = println("FAIL")
        }
    }

    private fun pickUpUserFromDatabase(dataSnapshot: DataSnapshot, database: DatabaseReference) {
        dataSnapshot.children.forEach { child ->
            if (child.key.toString() == "userLog") if (!child.value.toString().toBoolean()) setupDatabase(database)
        }
    }

    private fun setupDatabase(database: DatabaseReference) {
        database.child("RTL").setValue(sharedViewModel.fabButtonRTL)
        database.child("DefaultDifficulty").setValue(sharedViewModel.difficultyHolder)
        val allComplexities = Statistics(0, 0, 0, 0.0, 0L, 0L, 0, 0L, 0, 0, 0.0)
        sharedViewModel.changeAll(allComplexities)
        database.child(Difficulties.EASY.difficulty).setValue(sharedViewModel.easy[0])
        database.child(Difficulties.MEDIUM.difficulty).setValue(sharedViewModel.medium[0])
        database.child(Difficulties.HARD.difficulty).setValue(sharedViewModel.hard[0])
        database.child(Difficulties.EXPERT.difficulty).setValue(sharedViewModel.expert[0])
    }
}