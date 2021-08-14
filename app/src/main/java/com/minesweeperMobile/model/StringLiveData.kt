package com.minesweeperMobile.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class StringLiveData(initValue: String) {
    private var _backedValue = MutableLiveData(initValue)
    val dataValue: LiveData<String>
        get() = _backedValue

    fun changeValue(string: String) { _backedValue.value = string }
}