package com.minesweeperMobile.model

class BooleanData(initValue: Boolean) {
    private var _backedValue = initValue
    val value: Boolean
        get() = _backedValue

    fun changeValue(boolean: Boolean) { _backedValue = boolean }
}