package com.example.attendancemanagement.model

data class Attendance(
    private val clockInTime: String? = null,
    private val clockOutTime: String? = null,
    private val clockInLocation: String? = null,
    private val clockOutLocation: String? = null,
    private val clockInImage: String? = null,
    private val clockOutImage: String? = null,
) {

}