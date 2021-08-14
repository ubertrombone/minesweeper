package com.minesweeperMobile.model

class IntegerData(initValue: Int) {
    private var _backedValue = initValue
    val value: Int
        get() = _backedValue

    fun changeValue(int: Int) { _backedValue = int }
    fun increment() = _backedValue ++
    fun decrement() = _backedValue --
}