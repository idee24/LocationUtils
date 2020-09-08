package com.eghoindustries.locationutils.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.location.Criteria
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.eghoindustries.locationutils.*
import com.eghoindustries.locationutils.R
import com.eghoindustries.locationutils.networking.GeoCodeService
import com.eghoindustries.locationutils.networking.Routes
import com.eghoindustries.locationutils.networking.generateService
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.fragment_map_picker.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 *Created by Yerimah on 1/8/2020.
 */
class MapPickerFragment : Fragment(), OnMapReadyCallback {

    private lateinit var context: LocationUtilsActivity
    private lateinit var mapView: SupportMapFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = activity as LocationUtilsActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_map_picker, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initDisplay()
        backIcon.setOnClickListener { context.onBackPressed() }
    }

    private fun initDisplay() {
        mapView = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapView.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        MapsInitializer.initialize(context.applicationContext)

        val locationManager = context.locationManager
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,  Manifest.permission.ACCESS_COARSE_LOCATION), Constants.LOCATION_REQUEST_CODE)
            return
        }
        googleMap.isMyLocationEnabled = true

        context.getLocationProvider().lastLocation.addOnSuccessListener {
            val location = it
            if (location != null) {
                val currentLocation = LatLng(location.latitude, location.longitude)
                val cameraPosition = CameraPosition.Builder()
                    .bearing(0.toFloat())
                    .target(currentLocation)
                    .zoom(15.toFloat())
                    .build()
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            }
        }
        googleMap.setOnMapClickListener {
            val clickedLocation = it
            val clickedPosition = CameraPosition.Builder()
                .bearing(0.toFloat())
                .target(clickedLocation)
                .zoom(15.toFloat())
                .build()
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(clickedPosition))
        }

        googleMap.setOnCameraIdleListener {
            val updatedPosition = googleMap.cameraPosition.target
            getAddress(context, updatedPosition.latitude, updatedPosition.longitude)
        }
    }


    private fun getAddress(activity: LocationUtilsActivity,
                           latitude: Double,
                           longitude: Double) {

        val retryAction = View.OnClickListener {
            getAddress(activity, latitude, longitude)
        }
        addressTextView.text = context.resources.getString(R.string.searching_text)
        confirmButton.setOnClickListener {  }
        val call = generateService(GeoCodeService::class.java, Routes.GEOCODER_URL)
            .getFormattedAddress(Routes.GEOCODER_URL + Routes.GEOCODE_ENDPOINT + "?key=" + activity.apiKey +
                    "&latlng=" + latitude + ", " + longitude + "&sensor=true")
        call.enqueue(object : Callback<AddressModel> {
            override fun onResponse(call: Call<AddressModel>, response: Response<AddressModel>) {
                if (response.body() != null) {
                    if (!response.body()!!.results.isNullOrEmpty()) {
                        val formattedAddress = response.body()!!.results?.get(0)?.formattedAddress
                        val name = response.body()!!.results?.get(0)?.mAddressComponent?.get(0)?.shortName
                        if (addressTextView != null) {
                            addressTextView.text = formattedAddress
                            confirmButton.setOnClickListener {

                                val locationObject = LocationObject(latitude, longitude, name, formattedAddress)
                                if (context.viewModel.isOrigin) {
                                    activity.viewModel.originLocation = locationObject
                                }
                                else {
                                    activity.viewModel.destinationLocation = locationObject
                                }
                                context.viewModel.loadFragment(context.viewModel.getFragmentByKey(Constants.ROUTES_FRAGMENT).fragment, context)
                            }
                        }

                    }

                }
            }
            override fun onFailure(call: Call<AddressModel>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.LOCATION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initDisplay()
        }
    }
}
