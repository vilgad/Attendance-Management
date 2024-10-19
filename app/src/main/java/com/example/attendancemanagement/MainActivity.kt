package com.example.attendancemanagement

import android.Manifest
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
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
            if (attendanceViewModel.isClockedIn.value == true) {
                ivUserPhoto.setImageURI(null)
            ivUserPhoto.setImageURI(imageViewModel.imageUri.value)}
            else {
            ivUserPhoto2.setImageURI(null)
            ivUserPhoto2.setImageURI(imageViewModel.imageUri.value)
            }

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
                    if (attendanceViewModel.isClockedIn.value == true)
                        binding.ivUserPhoto.setImageURI(uri)
                    else {
                        binding.ivUserPhoto2.setImageURI(uri)
                    }
                }
            }
        }

        attendanceViewModel.apply {
            isClockedIn.observe(this@MainActivity) {
                if (it) {
                    checkLocationPermission()
                    contract.launch(imageViewModel.imageUri.value!!)
                    binding.tvTime.text = "ClockIn Time: " + getCurrentDateTime()
                    binding.userClockIn.visibility = View.VISIBLE
                    binding.btClock.text = "CLock Out"
                } else {
                    checkLocationPermission()
                    contract.launch(imageViewModel.imageUri.value!!)
                    binding.tvTime2.text = "ClockOut Time: " + getCurrentDateTime()
                    binding.userClockOut.visibility = View.VISIBLE
                    binding.btClock.text = "CLock In"
                }
            }

            location.observe(this@MainActivity) {
                if (it != null) {
                    if (attendanceViewModel.isClockedIn.value == true)
                        binding.textView2.text = it
                    else {
                        binding.location2.text = it
                    }
                } else {
                    binding.textView2.text = "Location: Loading..."
                }
            }
        }

        binding.btClock.setOnClickListener {
            if (attendanceViewModel.isClockedIn.value == false) {
                attendanceViewModel.setIsClockedIn(true)
            } else {
                attendanceViewModel.setIsClockedIn(false)
            }
        }
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            CoroutineScope(Dispatchers.Main).launch {
                checkGPS()
            }
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

        val result = LocationServices.getSettingsClient(this.applicationContext)
            .checkLocationSettings(builder.build())

        result.addOnCompleteListener { task ->
            try {
                val response = task.getResult(
                    ApiException::class.java
                )

                getUserLocation()
            } catch (e: ApiException) {
                e.printStackTrace()

                when (e.statusCode) {
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
                    attendanceViewModel.setLocation(address_line)
                } catch (e: IOException) {

                }
            }
        }
    }

    fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}