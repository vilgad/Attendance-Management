package com.example.attendancemanagement.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AttendanceViewModel : ViewModel() {
    private val _location = MutableLiveData<String>()
    val location: LiveData<String> get() = _location

    private val _isClockedIn = MutableLiveData<Boolean>()
    val isClockedIn: LiveData<Boolean> get() = _isClockedIn

    fun setIsClockedIn(value: Boolean) {
            _isClockedIn.value = value
    }

    fun setLocation(value: String) {
        _location.value = value
    }
}