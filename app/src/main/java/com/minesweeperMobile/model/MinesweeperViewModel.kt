package com.minesweeperMobile.model

import androidx.lifecycle.*
import com.minesweeperMobile.Markers.*
import com.minesweeperMobile.Numbers.*
import com.minesweeperMobile.Difficulties.*
import com.minesweeperMobile.database.Statistics
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class MinesweeperViewModel: ViewModel() {

    private var _user = MutableLiveData(false)
    val user: LiveData<Boolean>
        get() = _user

    fun getUser(user: Boolean) { _user.value = user }

    private var _startSwitch = false
    val startSwitch: Boolean
        get() = _startSwitch

    fun changeStartSwitch(switch: Boolean) {
        _startSwitch = switch
    }

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

    private var _longestStreak = false
    val longestStreak: Boolean
        get() = _longestStreak
    private var _fewestMoves = false
    val fewestMoves: Boolean
        get() = _fewestMoves
    private var _fastestGame = false
    val fastestGame: Boolean
        get() = _fastestGame
    fun setLongestStreak(set: Boolean) {
        _longestStreak = set
    }fun setFastestGame(set: Boolean) {
        _fastestGame = set
    }
    fun setFewestMoves(set: Boolean) {
        _fewestMoves = set
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
            _longestStreak = true
        }
    }
    private fun addMovesAndTime(complexity: List<Statistics>, time: Long) {
        complexity[0].totalMoves += _moveCounter
        complexity[0].totalTime += time
    }
    private fun calculateAverageMovesAndTime(complexity: List<Statistics>) {
        complexity[0].averageMoves = complexity[0].totalMoves.toDouble() / complexity[0].gamesWon
        complexity[0].averageTime = complexity[0].totalTime / complexity[0].gamesWon
    }
    private fun calculateFewestMovesAndFastestTime(complexity: List<Statistics>, time: Long) {
        if (_moveCounter < complexity[0].fewestMoves || complexity[0].fewestMoves == 0) {
            complexity[0].fewestMoves = _moveCounter
            _fewestMoves = true
        }
        if (time < complexity[0].fastestGame || complexity[0].fastestGame == 0L) {
            complexity[0].fastestGame = time
            _fastestGame = true
        }
    }

    private var _height: Int
    val height: Int
        get() = _height

    private var _width: Int
    val width: Int
        get() = _width

    val listOfDifficulties = mutableListOf(EASY.difficulty, MEDIUM.difficulty, HARD.difficulty, EXPERT.difficulty, CUSTOM.difficulty)
    private val listOfNonNumbers = listOf(MINE.mark, EMPTY.mark, FLAG.mark, CLEARED.mark)
    val listOfNumbers = listOf(ONE.alphaNumber, TWO.alphaNumber, THREE.alphaNumber, FOUR.alphaNumber, FIVE.alphaNumber, SIX.alphaNumber, SEVEN.alphaNumber, EIGHT.alphaNumber)

    private var _selectedCellBackgroundId: Int
    val selectedCellBackgroundId: Int
        get() = _selectedCellBackgroundId

    private var _selectedCardId: Int
    val selectedCardId: Int
        get() = _selectedCardId

    private var _firstMove: Int

    private var _howManyMines: Int
    val howManyMines: Int
        get() = _howManyMines

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

    private var _fabButtonsRTL = true
    val fabButtonRTL: Boolean
        get() = _fabButtonsRTL

    private var _mineCounter: Int
    val mineCounter: Int
        get() = _mineCounter

    private var _mineCounterUI = MutableLiveData<Int>()
    val mineCounterUI: LiveData<Int>
        get() = _mineCounterUI

    private var _difficultySet = MutableLiveData<String>()
    val difficultySet: LiveData<String>
        get() = _difficultySet

    private var _difficultyHolder = MEDIUM.difficulty
    val difficultyHolder: String
        get() = _difficultyHolder

    fun setDifficultyHolder(difficulty: String) {
        _difficultyHolder = difficulty
    }

    private var _flagCounter: Int

    private var _moveCounter: Int
    val moveCounter: Int
        get() = _moveCounter

    private var _firstMoveSwitch: Int
    val firstMoveSwitch: Int
        get() = _firstMoveSwitch

    fun setHeight(height: Int) {
        _height = height
    }

    fun setWidth(width: Int) {
        _width = width
    }

    fun setMines(mines: Int) {
        _howManyMines = mines
    }

    fun getSelectedCellBackgroundId(id: Int) {
        _selectedCellBackgroundId = id
    }

    fun getSelectedCardId(id: Int) {
        _selectedCardId = id
    }

    fun getCurrentCoords(id: Int) {
        _currentCoords = convertNumberToCoords(id)
    }

    fun getFlagId() = _selectedCardId + 5000
    fun getDifficultySet() = _difficultySet.value.toString()

    fun incrementMoveCounter() = _moveCounter ++
    fun setDifficulty(diff: String) {
        _difficultySet.value = diff
    }

    fun fabButtonSettings(switch: Boolean) {
        _fabButtonsRTL = switch
    }

    init {
        _height = 15
        _width = 13
        _selectedCellBackgroundId = 0
        _selectedCardId = 0
        _firstMove = _width * _height + 1
        _howManyMines = 40
        _mineCounter = _howManyMines
        _flagCounter = 0
        _mineCounterUI.value = _howManyMines - _flagCounter
        _moveCounter = 0
        _firstMoveSwitch = 0
        _difficultySet.value = MEDIUM.difficulty
    }

    fun move(cardId: Int) {
        if (_firstMoveSwitch == 0) {
            _firstMove = cardId + 1
            _minefieldWithNumbers = readMinefield(_howManyMines)
            _firstMoveSwitch ++
        }
    }

    fun convertCoordsToNumber(coords: List<Int>) =
        if (coords[1] == 0) coords[0] else (coords[1]) * _width + coords[0]

    fun convertNumberToCoords(number: Int): List<Int> {
        val offsetNumber = number - 1
        val listOfXStarts = getXMinValues()
        val listOfXEnds = getXMaxValues()

        return when {
            listOfXStarts.contains(number) -> listOf(0, listOfXStarts.indexOf(number)) // If number should be in column 0
            listOfXEnds.contains(number) -> listOf(_width - 1, listOfXEnds.indexOf(number)) // if number should be in column _height - 1
            else -> listOf(offsetNumber % _width, offsetNumber / _width) // For all other numbers.
        }
    }

    private fun getXMinValues() = (0 until _height).map { it * _width + 1 }
    private fun getXMaxValues() = (0 until _height).map { it * _width + _width }

    fun getItemAtMinefieldPosition() = _minefieldWithNumbers[_currentCoords[1]][_currentCoords[0]]

    fun addToSelections(selection: List<Int>) = _listOfSelections.add(selection)

    private var _hours = 0L
    val hours: Long
        get() = _hours
    private var _minutes = 0L
    val minutes: Long
        get() = _minutes
    private var _seconds = 0L
    val seconds: Long
        get() = _seconds

    fun getTimes(time: Long) {
        _hours = getHours(time)
        _minutes = getMinutes(time - (_hours * 3_600_000))
        _seconds = getSeconds(time - (_hours * 3_600_000) - (_minutes * 60_000))
    }

    private fun getSeconds(time: Long) = TimeUnit.MILLISECONDS.toSeconds(time)
    private fun getMinutes(time: Long) = TimeUnit.MILLISECONDS.toMinutes(time)
    private fun getHours(time: Long) = TimeUnit.MILLISECONDS.toHours(time)

    fun resetGame(height: Int, width: Int, numberOfMines: Int) {
        _height = height
        _width = width
        _selectedCellBackgroundId = 0
        _selectedCardId = 0
        _firstMove = _width * _height + 1
        _howManyMines = numberOfMines
        _mineCounter = _howManyMines
        _minefieldWithNumbers = arrayOf()
        _flagCounter = 0
        _mineCounterUI.value = _howManyMines - _flagCounter
        _moveCounter = 0
        _firstMoveSwitch = 0
        _listOfSelections.clear()
        _listOfFlags.clear()
        mineLocations.clear()
        _emptySelections.clear()
    }

    fun addFlag(listOfFlags: MutableList<List<Int>>, coords: List<Int>, coordValue: String) {
        listOfFlags.add(coords)
        _flagCounter++
        if (coordValue == MINE.mark) _mineCounter --
        _mineCounterUI.value = _mineCounterUI.value?.minus(1)
        addToSelections(coords)
    }

    fun removeFlag(listOfFlags: MutableList<List<Int>>, coords: List<Int>, coordValue: String) {
        listOfFlags.remove(coords)
        removeAllItems(_listOfSelections, coords)
        _flagCounter --
        if (coordValue == MINE.mark) _mineCounter ++
        _mineCounterUI.value = _mineCounterUI.value?.plus(1)
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
                val currentCoordsInEmptyFunc = listOf(xCurrent, yCurrent)

                try {
                    minefield[yCurrent][xCurrent]
                } catch (e: ArrayIndexOutOfBoundsException) {
                    continue
                }

                val currentCell = minefield[yCurrent][xCurrent]
                if (currentCell == EMPTY.mark) {
                    _listOfSelections.add(currentCoordsInEmptyFunc)
                    emptyCells(yCurrent, xCurrent, minefield, false)
                    if (_listOfFlags.contains(currentCoordsInEmptyFunc)) {
                        _listOfFlags.remove(currentCoordsInEmptyFunc)
                    }
                } else if (currentCell !in listOfNonNumbers) {
                    _listOfSelections.add(currentCoordsInEmptyFunc)
                    _emptySelections.add(currentCoordsInEmptyFunc)
                    if (_listOfFlags.contains(currentCoordsInEmptyFunc)) {
                        _listOfFlags.remove(currentCoordsInEmptyFunc)
                    }
                } else if (currentCell == MINE.mark && currentCoordsInEmptyFunc !in _listOfFlags) {
                    _emptySelections.add(currentCoordsInEmptyFunc)
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
        for (i in 0 until _height) {
            for (j in 0 until _width) {
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
        return Array(_height) {
            list.clear()
            (1.._width).forEach { _ ->
                if (mineLocations.contains(mineLocationCounter)) list.add(MINE.mark)
                else list.add(EMPTY.mark)
                mineLocationCounter++
            }
            return@Array list.toTypedArray()
        }
    }

    private val areaAroundFirstMoveList = mutableListOf<Int>()
    private fun placeAMine(size: Int) {
        areaAroundFirstMove(_firstMove)
        do {
            val randomDigit = Random.nextInt(1, _height * _width + 1)
            if (areaAroundFirstMoveList.contains(randomDigit)) continue
            if (!mineLocations.contains(randomDigit)) mineLocations.add(randomDigit)
        } while (mineLocations.size < size)
    }

    private fun areaAroundFirstMove(firstMove: Int) {
        areaAroundFirstMoveList.clear()
        val rangeSize = when {
            _difficultySet.value == CUSTOM.difficulty && _height * _width > 200 && (_height * _width) / _howManyMines > 5 -> 2
            _difficultySet.value == CUSTOM.difficulty && _height * _width > 200 && (_height * _width) / _howManyMines > 2 -> 1
            _difficultySet.value == CUSTOM.difficulty && _height * _width > 200 -> {
                areaAroundFirstMoveList.add(firstMove)
                return
            }
            _difficultySet.value == CUSTOM.difficulty && _howManyMines / (_height * _width) > 5 -> 1
            _difficultySet.value == CUSTOM.difficulty -> {
                areaAroundFirstMoveList.add(firstMove)
                return
            }
            _height * _width > 200 -> 2
            else -> 1
        }
        (rangeSize * -1..rangeSize).forEach { i -> (rangeSize * -1..rangeSize).forEach { j ->
            val coordinates = convertNumberToCoords(firstMove)
            areaAroundFirstMoveList.add(convertCoordsToNumber(listOf(coordinates[0] + j, coordinates[1] + i)) + 1)
        }}
    }

    fun getCardBackgroundColor(card: Int, colorOne: Int, colorTwo: Int) =
        if (_width % 2 == 0) {
            if (((card / _width) % 2) % 2 == 0) {
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