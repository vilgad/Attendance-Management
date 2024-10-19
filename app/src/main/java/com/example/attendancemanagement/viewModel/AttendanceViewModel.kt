package com.example.attendancemanagement.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.attendancemanagement.model.Attendance

class AttendanceViewModel : ViewModel() {
    private val _attendance = MutableLiveData<Attendance>()
    val attendance: LiveData<Attendance> get() = _attendance

    private val _isClockedIn = MutableLiveData<Boolean>()
    val isClockedIn: LiveData<Boolean> get() = _isClockedIn

    fun clockIn(image: String, location: String) {
        val clockInData = Attendance(
            clockInTime = System.currentTimeMillis().toString(),
            clockInLocation = location,
            clockInImage = image
        )
        _attendance.value = clockInData
    }

    fun clockOut(image: String, location: String) {
        _attendance.value?.let {
            val clockOutData = it.copy(
                clockOutTime = System.currentTimeMillis().toString(),
                clockOutImage = image,
                clockOutLocation = location
            )
            _attendance.value = clockOutData
        }
    }
}