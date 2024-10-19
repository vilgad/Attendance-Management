package com.example.attendancemanagement.viewModel

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File

class ImageViewModel: ViewModel() {
    private val _imageUri = MutableLiveData<Uri>()
    val imageUri: LiveData<Uri> get() = _imageUri

    fun createImageUri(context: Context): Uri? {
        val image = File(context.filesDir, "camera_photo.png")
        _imageUri.value = FileProvider.getUriForFile(
            context,
            "com.example.attendancemanagement.fileProvider",
            image
        )
        return _imageUri.value
    }
}