package com.minesweeperMobile.model

class MutableStringListData {
    private var _backedValue = mutableListOf<String>()
    val value: List<String>
        get() = _backedValue

    fun isValueUnique(string: String) = _backedValue.contains(string)
    fun addValue(string: String) = _backedValue.add(string)
}