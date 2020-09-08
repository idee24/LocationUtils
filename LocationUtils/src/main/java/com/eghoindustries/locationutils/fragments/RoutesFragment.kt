package com.eghoindustries.locationutils.fragments

import android.Manifest
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.eghoindustries.locationutils.*
import com.eghoindustries.locationutils.R
import com.eghoindustries.locationutils.networking.GeoCodeService
import com.eghoindustries.locationutils.networking.Routes
import com.eghoindustries.locationutils.networking.generateService
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.fragment_routes.*
import kotlinx.android.synthetic.main.header_card.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class RoutesFragment : Fragment(), OnMapReadyCallback {

    private lateinit var context: LocationUtilsActivity
    private lateinit var mapView: SupportMapFragment
    private lateinit var gMap: GoogleMap
    private lateinit var geoDirections: GeoDirectionsResponse

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = activity as LocationUtilsActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_routes, container, false)
    }

    override fun onResume() {
        super.onResume()
        pickupAddressTextField.setText(context.viewModel.originLocation.address)
        dropOffAddressField.setText(context.viewModel.destinationLocation.address)
        if (!context.viewModel.originLocation.address.isNullOrEmpty() && !context.viewModel.destinationLocation.address.isNullOrEmpty()) {
            getGeoDirections()
        }
        else {
            if (this::gMap.isInitialized) { gMap.clear() }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        backIcon.setOnClickListener { context.onBackPressed() }
        titleTextView.text = getString(R.string.pick_location_text)
        proceedButton.setOnClickListener {

        }
        mapView = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment

        pickupAddressTextField.setOnClickListener {
            context.viewModel.isOrigin = true
            context.viewModel.loadFragment(context.viewModel.getFragmentByKey(Constants.PLACE_ADAPTER_FRAGMENT).fragment, context)
        }
        dropOffAddressField.setOnClickListener {
            context.viewModel.isOrigin = false
            context.viewModel.loadFragment(context.viewModel.getFragmentByKey(Constants.PLACE_ADAPTER_FRAGMENT).fragment, context)
        }
        mapView.getMapAsync(this)
        displayLocationSettingsRequest()
    }

    private fun initExitSequence() {
        //todo: Exit Sequence
//        context.viewModel.loadFragment(context.viewModel.getFragmentByKey(Constants.ADDRESS_FRAGMENT).fragment, context)
    }

    private fun displayLocationSettingsRequest() {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 500
        locationRequest.fastestInterval = 100
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(context)
        val task = client.checkLocationSettings(builder.build())
        task.addOnFailureListener { locationException: java.lang.Exception? ->
            if (locationException is ResolvableApiException) {
                try {
                    locationException.startResolutionForResult(context, Constants.LOCATION_REQUEST_CODE)
                } catch (senderException: SendIntentException) {
                    senderException.printStackTrace()
                    displayLocationSettingsRequest()
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {

        if (googleMap != null) {
            gMap = googleMap
        }
        MapsInitializer.initialize(context.applicationContext)

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,  Manifest.permission.ACCESS_COARSE_LOCATION), Constants.LOCATION_REQUEST_CODE)
            return
        }
        val gMapView = mapView.view
        if (gMapView?.findViewById<View>("1".toInt()) != null) {
            val locationButton = (gMapView.findViewById<View>("1".toInt()).parent as View).findViewById<View>("2".toInt())
            val layoutParams = locationButton.layoutParams as RelativeLayout.LayoutParams
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
            layoutParams.setMargins(0, 0, 30, 200)
        }

        googleMap?.isMyLocationEnabled = true

        if (context.viewModel.originLocation.address.isNullOrEmpty() && context.viewModel.destinationLocation.address.isNullOrEmpty()) {
            context.getLocationProvider().lastLocation.addOnSuccessListener {
                val location = it
                if (location != null) {
                    val currentLocation = LatLng(location.latitude, location.longitude)
                    val cameraPosition = CameraPosition.Builder()
                        .bearing(0.toFloat())
                        .target(currentLocation)
                        .zoom(15.toFloat())
                        .build()
                    googleMap?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                    getAddress(context, location.latitude, location.longitude, pickupAddressTextField)
                }
                else { displayLocationSettingsRequest() }
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
        }

    }

    private fun getAddress(activity: LocationUtilsActivity,
                           latitude: Double,
                           longitude: Double,
                           senderAddressField: EditText) {

        val retryAction = View.OnClickListener { getAddress(activity, latitude, longitude, senderAddressField) }
        senderAddressField.setText( context.resources.getString(R.string.searching_text))

        val call = generateService(GeoCodeService::class.java, Routes.GEOCODER_URL)
            .getFormattedAddress(Routes.GEOCODER_URL + Routes.GEOCODE_ENDPOINT +
                    "?key=" + activity.apiKey + "&latlng=" + latitude +
                    ", " + longitude + "&sensor=true")
        call.enqueue(object : Callback<AddressModel> {
            override fun onResponse(call: Call<AddressModel>, response: Response<AddressModel>) {
                if (response.body() != null) {
                    if (!response.body()!!.results.isNullOrEmpty()) {
                        val formattedAddress = response.body()!!.results?.get(0)?.formattedAddress
                        val name = response.body()!!.results?.get(0)?.mAddressComponent?.get(0)?.shortName
                        senderAddressField.setText(formattedAddress)
                        activity.viewModel.originLocation = LocationObject(latitude, longitude, name, formattedAddress)
//
//                        try {
//                            val addressComponent = context.geocoder.getFromLocation(latitude, longitude, 3)[0]
//                            activity.viewModel.currentShipmentLocality = context.geocoder.getFromLocation(latitude, longitude, 3)[1].adminArea
//                            context.viewModel.currentShipment.country = addressComponent.countryName.toUpperCase()
//                            context.viewModel.currentShipmentSubAdmin = addressComponent.subAdminArea
//                        }
//                        catch (e: Exception) {e.printStackTrace()}
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
            mapView.getMapAsync(this)
        }
    }

    private fun plotPolyLines() {

        val steps: ArrayList<LatLng> = ArrayList()
        if (geoDirections.routes.isNullOrEmpty()) {
            return
        }
        val geoBounds = geoDirections.routes?.get(0)?.bounds
        val geoSteps = geoDirections.routes?.get(0)?.legs?.get(0)?.steps
        geoSteps?.forEach { geoStep ->
            steps.addAll(decodePolyline(geoStep.polyline?.points!!))
        }
        val endLocation = geoDirections.routes?.get(0)?.legs?.get(0)?.endLocation
        steps.add(LatLng(endLocation?.lat ?: 0.0, endLocation?.lng ?: 0.0))

        val builder = LatLngBounds.Builder()
        builder.include(LatLng(geoBounds?.northeast?.lat ?: 0.0, geoBounds?.northeast?.lng ?: 0.0))
        builder.include(LatLng(geoBounds?.southwest?.lat ?: 0.0, geoBounds?.southwest?.lng ?: 0.0))

        val bounds = builder.build()
        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        val padding = (width * 0.3).toInt()

        val boundsUpdate = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding)
        gMap.animateCamera(boundsUpdate)
        val rectLine = PolylineOptions().width(10f).color(ContextCompat.getColor(context, R.color.colorAccent))
        for (step in steps) { rectLine.add(step) }
        gMap.clear()
        gMap.addPolyline(rectLine)
        gMap.addMarker(
            MarkerOptions()
                .position(LatLng(endLocation?.lat ?: 0.0, endLocation?.lng ?: 0.0))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.drop_off_marker))).showInfoWindow()

        val startLocation = geoDirections.routes?.get(0)?.legs?.get(0)?.startLocation
        gMap.addMarker(MarkerOptions()
            .position(LatLng(startLocation?.lat ?: 0.0, startLocation?.lng ?: 0.0))
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.pick_up_marker))).showInfoWindow()

        if (proceedButton != null) {
            proceedButton.visibility = View.VISIBLE
        }
    }

    private fun getGeoDirections() {

        val originLocation = context.viewModel.originLocation
        val destinationLocation = context.viewModel.destinationLocation

        val retryAction = View.OnClickListener { getGeoDirections() }
        val directionsEndpoint = "json?origin=" + originLocation?.latitude + "," + originLocation?.longitude +
                "&destination=" + destinationLocation?.latitude + "," + destinationLocation?.longitude +
                "&sensor=false&units=metric&mode=driving"+ "&key=" + context.apiKey

        val call = generateService(GeoCodeService::class.java, Routes.GEO_DIRECTIONS_URL).getGeoDirections(directionsEndpoint)
        call.enqueue(object: Callback<GeoDirectionsResponse> {

            override fun onResponse(call: Call<GeoDirectionsResponse>, response: Response<GeoDirectionsResponse>) {
                if (response.isSuccessful) {
                    response.body().let {
                        val directionsPayload = it
                        if (directionsPayload != null) {
                            geoDirections = directionsPayload
                            plotPolyLines()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<GeoDirectionsResponse>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }


    private fun decodePolyline(encoded: String): ArrayList<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val position = LatLng(lat.toDouble() / 1E5, lng.toDouble() / 1E5)
            poly.add(position)
        }
        return poly
    }
}
