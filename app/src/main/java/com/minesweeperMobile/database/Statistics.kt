package com.minesweeperMobile.database

data class Statistics(
    var gamesPlayed: Int,
    var gamesWon: Int,
    var totalMoves: Int,
    var averageMoves: Double,
    var totalTime: Long,
    var averageTime: Long,
    var fewestMoves: Int,
    var fastestGame: Long,
    var currentStreak: Int,
    var longestStreak: Int,
    var winPercentage: Double,
)