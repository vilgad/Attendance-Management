package com.example.attendancemanagement

import android.Manifest
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.attendancemanagement.databinding.ActivityMainBinding
import com.example.attendancemanagement.viewModel.AttendanceViewModel
import com.example.attendancemanagement.viewModel.ImageViewModel
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.Priority
import java.io.IOException
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var attendanceViewModel: AttendanceViewModel
    private lateinit var imageViewModel: ImageViewModel

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

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

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        imageViewModel.apply {
            createImageUri(applicationContext)

            imageUri.observe(this@MainActivity) { uri ->
                if (uri != null) {
                    binding.userClockIn.ivUserPhoto.setImageURI(uri)
                }
            }
        }

        binding.btClock.setOnClickListener {
//            contract.launch(imageViewModel.imageUri.value!!)
            checkLocationPermission()
        }
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            checkGPS()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
        }
    }

    private fun checkGPS() {
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .build()

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)

        val result = LocationServices.getSettingsClient(this.applicationContext).checkLocationSettings(builder.build())

        result.addOnCompleteListener {
                task -> try {
            val response = task.getResult(
                ApiException::class.java
            )

            getUserLocation()
        } catch (e: ApiException) {
            e.printStackTrace()

            when(e.statusCode) {
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {

                    val resolveApiException = e as ResolvableApiException
                    resolveApiException.startResolutionForResult(this, 200)

                } catch (sendIntentException: IntentSender.SendIntentException) {

                }

                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {

                }
            }
        }
        }
    }

    private fun getUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
            val location = task.getResult()

            if (location != null) {
                try {
                    val geocoder = Geocoder(this, Locale.getDefault())

                    val address = geocoder.getFromLocation(location.latitude, location.longitude, 1)

                    val address_line = address!![0].getAddressLine(0)
                    binding.userClockIn.textView2.text = address_line
                } catch (e: IOException) {

                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}