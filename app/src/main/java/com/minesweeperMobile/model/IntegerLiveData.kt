package com.minesweeperMobile.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class IntegerLiveData(initValue: Int) {
    private var _backedValue = MutableLiveData(initValue)
    val dataValue: LiveData<Int>
        get() = _backedValue

    fun changeValue(int: Int) { _backedValue.value = int }
}