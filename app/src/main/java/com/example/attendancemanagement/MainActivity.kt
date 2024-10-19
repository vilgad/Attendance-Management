package com.example.attendancemanagement

import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.attendancemanagement.databinding.ActivityMainBinding
import com.example.attendancemanagement.viewModel.AttendanceViewModel
import com.example.attendancemanagement.viewModel.ImageViewModel

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var attendanceViewModel: AttendanceViewModel
    private lateinit var imageViewModel: ImageViewModel

    private val contract = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        binding.apply {
            userClockIn.ivUserPhoto.setImageURI(null)
            userClockIn.ivUserPhoto.setImageURI(imageViewModel.imageUri.value)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        attendanceViewModel = ViewModelProvider(this)[AttendanceViewModel::class.java]
        imageViewModel = ViewModelProvider(this)[ImageViewModel::class.java]

        imageViewModel.apply {
            createImageUri(applicationContext)

            imageUri.observe(this@MainActivity) { uri ->
                if (uri != null) {
                    binding.userClockIn.ivUserPhoto.setImageURI(uri)
                }
            }
        }

        binding.btClock.setOnClickListener {
            contract.launch(imageViewModel.imageUri.value!!)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}