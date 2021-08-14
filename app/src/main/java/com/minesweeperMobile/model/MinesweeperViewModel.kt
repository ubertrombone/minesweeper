package com.minesweeperMobile.model

import androidx.lifecycle.*
import com.minesweeperMobile.Markers.*
import com.minesweeperMobile.Numbers.*
import com.minesweeperMobile.Difficulties.*
import com.minesweeperMobile.database.Statistics
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class MinesweeperViewModel: ViewModel() {

    val listOfDifficulties = mutableListOf(EASY.difficulty, MEDIUM.difficulty, HARD.difficulty, EXPERT.difficulty, CUSTOM.difficulty)
    private val listOfNonNumbers = listOf(MINE.mark, EMPTY.mark, FLAG.mark, CLEARED.mark)
    val listOfNumbers = listOf(ONE.alphaNumber, TWO.alphaNumber, THREE.alphaNumber, FOUR.alphaNumber, FIVE.alphaNumber, SIX.alphaNumber, SEVEN.alphaNumber, EIGHT.alphaNumber)
    val listOfRecords = listOf("Most Wins", "Fastest Game", "Fewest Moves", "Longest Streak", "Longest Current Streak", "Win Percentage")
    val mapOfRecords = mapOf(listOfRecords[0] to "gamesWon", listOfRecords[1] to "fastestGame", listOfRecords[2] to "fewestMoves",
        listOfRecords[3] to "longestStreak", listOfRecords[4] to "currentStreak", listOfRecords[5] to "winPercentage")
    val listOfDescendingRecords = listOf(mapOfRecords[listOfRecords[1]], mapOfRecords[listOfRecords[2]])
    private val profanityFilter = listOf("fuck", "f4ck", "f4k", "fck", "fuk", "shit", "sh1t", "shiz", "sh1z", "bitch", "b1tch",
        "biatch", "b1atch", "bi4tch", "b14tch", "cunt", "kunt", "ass", "4ss", "balls", "ballz", "b4lls", "b4llz", "dick",
        "d1ck", "dik", "d1k", "dic", "d1c", "fuc", "f4k", "f4c", "vagina", "v4gina", "v4g1na", "v4g1n4", "v4gin4", "vagin4",
        "vag1na", "vag1n4", "pussy", "pusy", "pussee", "pusee", "tits", "t1ts", "titz", "t1tz", "titties", "t1tties")

    val user = BooleanLiveData(false)
    val username = BooleanLiveData(true)

    val usernameFromDB = StringData("")
    val difficultyHolder = StringData(MEDIUM.difficulty)
    val leaderBoardFragmentPage = StringData(mapOfRecords[listOfRecords[0]]!!)

    val usernameSwitch = BooleanData(false)
    val startSwitch = BooleanData(false)
    val longestStreak = BooleanData(false)
    val fewestMoves = BooleanData(false)
    val fastestGame = BooleanData(false)
    val fabButtonRTL = BooleanData(true)
    val mineAssistFAB = BooleanData(false)
    val mineAssistChanged = BooleanData(false)

    val height = IntegerData(15)
    val width = IntegerData(13)
    val selectedCellBackgroundId = IntegerData(0)
    val selectedCardId = IntegerData(0)
    private val firstMove = IntegerData(width.value * height.value + 1)
    val howManyMines = IntegerData(40)
    val mineCounter = IntegerData(howManyMines.value)
    private val flagCounter = IntegerData(0)
    val moveCounter = IntegerData(0)
    val firstMoveSwitch = IntegerData(0)
    val difficultySet = StringLiveData(MEDIUM.difficulty)
    val mineCounterUI = IntegerLiveData(howManyMines.value - flagCounter.value)

    val leaderBoardData = ListLiveData()
    val leaderBoardComplexitySelection = StringLiveData("Easy")

    private var _usernames = mutableListOf<String>()
    val usernames: List<String>
        get() = _usernames

    fun checkUsernameUniqueness(username: String) = _usernames.contains(username)
    fun addToUsernames(username: String) = _usernames.add(username)

    private var _easy = mutableListOf<Statistics>()
    val easy: List<Statistics>
        get() = _easy

    private var _medium = mutableListOf<Statistics>()
    val medium: List<Statistics>
        get() = _medium

    private var _hard = mutableListOf<Statistics>()
    val hard: List<Statistics>
        get() = _hard

    private var _expert = mutableListOf<Statistics>()
    val expert: List<Statistics>
        get() = _expert

    fun changeEasy(stats: Statistics) = _easy.add(stats)
    fun changeMedium(stats: Statistics) = _medium.add(stats)
    fun changeHard(stats: Statistics) = _hard.add(stats)
    fun changeExpert(stats: Statistics) = _expert.add(stats)
    fun changeAll(stats: Statistics) {
        changeEasy(stats)
        changeMedium(stats)
        changeHard(stats)
        changeExpert(stats)
    }

    fun updateComplexities(difficulty: String, time: Long, message: Boolean) {
        val complexity = when(difficulty) {
            EASY.difficulty -> _easy
            MEDIUM.difficulty -> _medium
            HARD.difficulty -> _hard
            else -> _expert
        }
        incrementGamesPlayed(complexity)
        if (message) {
            incrementWinsAndStreaks(complexity)
            addMovesAndTime(complexity, time)
            calculateAverageMovesAndTime(complexity)
            calculateFewestMovesAndFastestTime(complexity, time)
        } else resetWinStreak(complexity)
        calculateWinPercentage(complexity)
    }
    private fun incrementGamesPlayed(complexity: List<Statistics>) = complexity[0].gamesPlayed ++
    private fun calculateWinPercentage(complexity: List<Statistics>) {
        complexity[0].winPercentage = complexity[0].gamesWon.toDouble() / complexity[0].gamesPlayed
    }
    private fun resetWinStreak(complexity: List<Statistics>) {
        complexity[0].currentStreak = 0
    }
    private fun incrementWinsAndStreaks(complexity: List<Statistics>) {
        complexity[0].gamesWon ++
        complexity[0].currentStreak ++
        if (complexity[0].currentStreak > complexity[0].longestStreak) {
            complexity[0].longestStreak = complexity[0].currentStreak
            longestStreak.changeValue(true)
        }
    }
    private fun addMovesAndTime(complexity: List<Statistics>, time: Long) {
        complexity[0].totalMoves += moveCounter.value
        complexity[0].totalTime += time
    }
    private fun calculateAverageMovesAndTime(complexity: List<Statistics>) {
        complexity[0].averageMoves = complexity[0].totalMoves.toDouble() / complexity[0].gamesWon
        complexity[0].averageTime = complexity[0].totalTime / complexity[0].gamesWon
    }
    private fun calculateFewestMovesAndFastestTime(complexity: List<Statistics>, time: Long) {
        if (moveCounter.value < complexity[0].fewestMoves || complexity[0].fewestMoves == 0) {
            complexity[0].fewestMoves = moveCounter.value
            fewestMoves.changeValue(true)
        }
        if (time < complexity[0].fastestGame || complexity[0].fastestGame == 0L) {
            complexity[0].fastestGame = time
            fastestGame.changeValue(true)
        }
    }

    fun checkProfanityFilter(string: String):Boolean {
        profanityFilter.forEach { if (string.contains(it)) return true }
        return false
    }

    private var _currentCoords = listOf<Int>()
    val currentCoords: List<Int>
        get() = _currentCoords

    val mineLocations = mutableListOf<Int>()

    private var _listOfSelections = mutableListOf<List<Int>>()
    val listOfSelections: List<List<Int>>
        get() = _listOfSelections

    private var _listOfFlags = mutableListOf<List<Int>>()
    val listOfFlags: MutableList<List<Int>>
        get() = _listOfFlags

    private var _emptySelections = mutableListOf<List<Int>>()
    val emptySelections: List<List<Int>>
        get() = _emptySelections

    private var _minefieldWithNumbers = arrayOf<Array<String>>()
    val minefieldWithNumbers: Array<Array<String>>
        get() = _minefieldWithNumbers

    fun getCurrentCoords(id: Int) { _currentCoords = convertNumberToCoords(id) }

    fun getFlagId() = selectedCardId.value + 5000

    fun getComplexity(): MutableList<Statistics> {
        return when(difficultySet.dataValue.value) {
            EASY.difficulty -> _easy
            MEDIUM.difficulty -> _medium
            HARD.difficulty -> _hard
            else -> _expert
        }
    }

    fun move(cardId: Int) {
        if (firstMoveSwitch.value == 0) {
            firstMove.changeValue(cardId + 1)
            _minefieldWithNumbers = readMinefield(howManyMines.value)
            firstMoveSwitch.increment()
        }
    }

    fun resetFirstMove() = firstMoveSwitch.changeValue(0)

    fun convertCoordsToNumber(coords: List<Int>) =
        if (coords[1] == 0) coords[0] else (coords[1]) * width.value + coords[0]

    fun convertNumberToCoords(number: Int): List<Int> {
        val offsetNumber = number - 1
        val listOfXStarts = getXMinValues()
        val listOfXEnds = getXMaxValues()

        return when {
            listOfXStarts.contains(number) -> listOf(0, listOfXStarts.indexOf(number)) // If number should be in column 0
            listOfXEnds.contains(number) -> listOf(width.value - 1, listOfXEnds.indexOf(number)) // if number should be in column _height - 1
            else -> listOf(offsetNumber % width.value, offsetNumber / width.value) // For all other numbers.
        }
    }
    private fun getXMinValues() = (0 until height.value).map { it * width.value + 1 }
    private fun getXMaxValues() = (0 until height.value).map { it * width.value + width.value }

    fun getItemAtMinefieldPosition() = _minefieldWithNumbers[_currentCoords[1]][_currentCoords[0]]

    fun addToSelections(selection: List<Int>) = _listOfSelections.add(selection)

    private var _hours = 0L
    val hours: Long get() = _hours
    private var _minutes = 0L
    val minutes: Long get() = _minutes
    private var _seconds = 0L
    val seconds: Long get() = _seconds

    fun getTimes(time: Long) {
        _hours = getHours(time)
        _minutes = getMinutes(time - (_hours * 3_600_000))
        _seconds = getSeconds(time - (_hours * 3_600_000) - (_minutes * 60_000))
    }

    private fun getSeconds(time: Long) = TimeUnit.MILLISECONDS.toSeconds(time)
    private fun getMinutes(time: Long) = TimeUnit.MILLISECONDS.toMinutes(time)
    private fun getHours(time: Long) = TimeUnit.MILLISECONDS.toHours(time)

    fun resetGame(newHeight: Int, newWidth: Int, newNumberOfMines: Int) {
        height.changeValue(newHeight)
        width.changeValue(newWidth)
        selectedCellBackgroundId.changeValue(0)
        selectedCardId.changeValue(0)
        firstMove.changeValue(width.value * height.value + 1)
        howManyMines.changeValue(newNumberOfMines)
        mineCounter.changeValue(howManyMines.value)
        flagCounter.changeValue(0)
        mineCounterUI.changeValue(howManyMines.value - flagCounter.value)
        moveCounter.changeValue(0)
        firstMoveSwitch.changeValue(0)
        _minefieldWithNumbers = arrayOf()
        _listOfSelections.clear()
        _listOfFlags.clear()
        mineLocations.clear()
        _emptySelections.clear()
    }

    fun addFlag(listOfFlags: MutableList<List<Int>>, coords: List<Int>, coordValue: String) {
        listOfFlags.add(coords)
        flagCounter.increment()
        if (coordValue == MINE.mark) mineCounter.decrement()
        mineCounterUI.changeValue(mineCounterUI.dataValue.value!!.minus(1))
        addToSelections(coords)
    }

    fun removeFlag(listOfFlags: MutableList<List<Int>>, coords: List<Int>, coordValue: String) {
        listOfFlags.remove(coords)
        removeAllItems(_listOfSelections, coords)
        flagCounter.decrement()
        if (coordValue == MINE.mark) mineCounter.increment()
        mineCounterUI.changeValue(mineCounterUI.dataValue.value!!.plus(1))
    }

    fun publicRemoveAll(item: List<Int>) = removeAllItems(_listOfFlags, item)
    private fun removeAllItems(list: MutableList<List<Int>>, item: List<Int>) {
        if (item in list) {
            list.remove(item)
            removeAllItems(list, item)
        }
        return
    }

    fun countProximityMines(coords: List<Int>): Int {
        var counter = 0
        (-1..1).forEach { i -> (-1..1).forEach j@{ j ->
            val xCurrent = coords[0] + j
            val yCurrent = coords[1] + i
            try {
                minefieldWithNumbers[yCurrent][xCurrent]
            } catch (e: ArrayIndexOutOfBoundsException) { return@j }

            val currentCellCoords = listOf(xCurrent, yCurrent)
            if (_listOfFlags.contains(currentCellCoords)) counter ++
        } }
        return counter
    }

    fun emptyCells(row: Int, column: Int, minefield: Array<Array<String>>, number: Boolean) {
        if (!number) minefield[row][column] = CLEARED.mark
        _emptySelections.add(listOf(column, row))

        for (i in -1..1) {
            for (j in -1..1) {

                val xCurrent = column + j
                val yCurrent = row + i
                val currentCoords = listOf(xCurrent, yCurrent)

                try { minefield[yCurrent][xCurrent] } catch (e: ArrayIndexOutOfBoundsException) { continue }
                if (_listOfSelections.contains(currentCoords)) continue

                val currentCell = minefield[yCurrent][xCurrent]

                when {
                    currentCell == EMPTY.mark -> {
                        _listOfSelections.add(currentCoords)
                        emptyCells(yCurrent, xCurrent, minefield, false)
                        if (_listOfFlags.contains(currentCoords)) _listOfFlags.remove(currentCoords)
                    }
                    !listOfNonNumbers.contains(currentCell) -> {
                        _listOfSelections.add(currentCoords)
                        _emptySelections.add(currentCoords)
                        if (_listOfFlags.contains(currentCoords)) _listOfFlags.remove(currentCoords)
                    }
                    currentCell == MINE.mark && !_listOfFlags.contains(currentCoords) -> _emptySelections.add(currentCoords)
                }
            }
        }
        return
    }

    fun clearEmptySelections() = _emptySelections.clear()

    private fun createNumberField(row: Int, column: Int, minefield: Array<Array<String>>): Int {
        var counter = 0
        for (i in -1..1) {
            for (j in -1..1) {
                try {
                    if (minefield[row+i][column+j].contentEquals(MINE.mark)) counter ++
                } catch (e: ArrayIndexOutOfBoundsException) {
                    continue
                }
            }
        }
        return counter
    }

    private fun readMinefield(size: Int): Array<Array<String>> {
        val minefield = createMinefield(size)
        for (i in 0 until height.value) {
            for (j in 0 until width.value) {
                if (!minefield[i][j].contentEquals(MINE.mark)) {
                    val number = createNumberField(i, j, minefield)
                    minefield[i][j] = if (number > 0) number.toString() else EMPTY.mark
                }
            }
        }
        return minefield
    }

    private fun createMinefield(size: Int): Array<Array<String>> {
        placeAMine(size)
        var mineLocationCounter = 1
        val list = mutableListOf<String>()
        return Array(height.value) {
            list.clear()
            (1..width.value).forEach { _ ->
                if (mineLocations.contains(mineLocationCounter)) list.add(MINE.mark)
                else list.add(EMPTY.mark)
                mineLocationCounter++
            }
            return@Array list.toTypedArray()
        }
    }

    private val areaAroundFirstMoveList = mutableListOf<Int>()
    private fun placeAMine(size: Int) {
        areaAroundFirstMove(firstMove.value)
        do {
            val randomDigit = Random.nextInt(1, height.value * width.value + 1)
            if (areaAroundFirstMoveList.contains(randomDigit)) continue
            if (!mineLocations.contains(randomDigit)) mineLocations.add(randomDigit)
        } while (mineLocations.size < size)
    }

    private fun areaAroundFirstMove(firstMove: Int) {
        areaAroundFirstMoveList.clear()
        val rangeSize = when {
            difficultySet.dataValue.value == CUSTOM.difficulty && height.value * width.value > 200
                    && (height.value * width.value) / howManyMines.value > 5 -> 2
            difficultySet.dataValue.value == CUSTOM.difficulty && height.value * width.value > 200
                    && (height.value * width.value) / howManyMines.value > 2 -> 1
            difficultySet.dataValue.value == CUSTOM.difficulty && height.value * width.value > 200 -> {
                areaAroundFirstMoveList.add(firstMove)
                return
            }
            difficultySet.dataValue.value == CUSTOM.difficulty && howManyMines.value / (height.value * width.value) > 5 -> 1
            difficultySet.dataValue.value == CUSTOM.difficulty -> {
                areaAroundFirstMoveList.add(firstMove)
                return
            }
            height.value * width.value > 200 -> 2
            else -> 1
        }
        (rangeSize * -1..rangeSize).forEach { i -> (rangeSize * -1..rangeSize).forEach { j ->
            val coordinates = convertNumberToCoords(firstMove)
            areaAroundFirstMoveList.add(convertCoordsToNumber(listOf(coordinates[0] + j, coordinates[1] + i)) + 1)
        }}
    }

    fun getCardBackgroundColor(card: Int, colorOne: Int, colorTwo: Int) =
        if (width.value % 2 == 0) {
            if (((card / width.value) % 2) % 2 == 0) {
                if (card % 2 == 0) colorOne else colorTwo
            } else { if (card % 2 == 0) colorTwo else colorOne }
        } else { if (card % 2 == 0) colorOne else colorTwo }

    fun refreshedBackgroundColor(cardId: Int, colorOne: Int, colorTwo: Int, colorThree: Int, colorFour: Int): Int {
        val currentCoords = convertNumberToCoords(cardId + 1)
        return if (listOfSelections.contains(currentCoords) && !listOfFlags.contains(currentCoords)) {
            getCardBackgroundColor(cardId, colorThree, colorFour)
        } else getCardBackgroundColor(cardId, colorOne, colorTwo)
    }
}