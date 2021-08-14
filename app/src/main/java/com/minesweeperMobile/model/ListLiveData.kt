package com.minesweeperMobile.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.minesweeperMobile.database.LeaderPairs

class ListLiveData {
    private var _backedValue = MutableLiveData<List<LeaderPairs>>()
    val dataValue: LiveData<List<LeaderPairs>>
        get() = _backedValue

    fun newValue(username: String, value: Float) {
        if (_backedValue.value.isNullOrEmpty()) _backedValue.value = listOf(LeaderPairs(username, value))
        else _backedValue.value = _backedValue.value!!.plus(LeaderPairs(username, value))
    }

    fun clearValues() {
        if (_backedValue.value.isNullOrEmpty()) return
        _backedValue.value!!.forEach { _backedValue.value = _backedValue.value!!.minus(it) }
    }
}