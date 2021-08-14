package com.minesweeperMobile.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class BooleanLiveData(initValue: Boolean) {
    private var _backedValue = MutableLiveData(initValue)
    val dataValue: LiveData<Boolean>
        get() = _backedValue

    fun changeValue(user: Boolean) { _backedValue.value = user }
}