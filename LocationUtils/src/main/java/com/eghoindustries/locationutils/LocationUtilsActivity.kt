package com.eghoindustries.locationutils

import android.content.Context
import android.location.Geocoder
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import java.util.*

class LocationUtilsActivity(googleApiKey: String) : AppCompatActivity() {

    lateinit var viewModel: LocationUtilsViewModel
    lateinit var placesClient: PlacesClient
    lateinit var geocoder: Geocoder
    lateinit var locationManager: LocationManager
    var apiKey = googleApiKey

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_utils)
        initViewModel()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        geocoder = Geocoder(this, Locale.getDefault())
        Places.initialize(applicationContext, apiKey)
        placesClient = Places.createClient(this)
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this).get(LocationUtilsViewModel::class.java)
    }

    fun getLocationProvider(): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(this)
    }
}