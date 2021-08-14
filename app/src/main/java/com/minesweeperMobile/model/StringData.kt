package com.minesweeperMobile.model

class StringData(initValue: String) {
    private var _backedValue = initValue
    val value: String
        get() = _backedValue

    fun changeValue(string: String) { _backedValue = string }
}