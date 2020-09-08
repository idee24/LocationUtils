package com.eghoindustries.locationutils

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.gson.annotations.SerializedName

/**
 *Created by Yerimah on 9/8/2020.
 */

data class LocationFragment(
    var activity: AppCompatActivity,
    var fragmentName: String,
    var fragment: Fragment,
    var previousFragment: LocationFragment?
)


data class AddressModel(
    var status: String?,
    var results: List<AddressResult>?
)

data class AddressResult(
    @SerializedName("address_components") var mAddressComponent: List<AddressComp>?,
    @SerializedName("formatted_address") var formattedAddress: String?
)

data class AddressComp(
    @SerializedName("long_name") var longName: String?,
    @SerializedName("short_name") var shortName: String?,
    var types: List<String>?
)

data class GeoDirectionsResponse(
    @SerializedName("geocoded_waypoints") var geoCodedWayPoints: List<GeoCodedWayPoint>?,
    var routes: List<GeoCodeRoute>?,
    var status: String?
)

data class GeoCodedWayPoint(
    @SerializedName("geocoder_status") var geocoderStatus: String?,
    @SerializedName("place_id") var placeId: String?,
    var types: List<String>?
)

data class GeoCodeRoute(
    var bounds: GeoBounds?,
    var copyrights: String?,
    var legs: List<GeoDirections>?,
    var summary: String?
)

data class GeoDirections(
    var distance: GeoDirectionMetric?,
    var duration: GeoDirectionMetric?,
    @SerializedName("end_address") var endAddress: String?,
    @SerializedName("end_location") var endLocation: GeoLocation?,
    @SerializedName("start_address") var startAddress: String?,
    @SerializedName("start_location") var startLocation: GeoLocation?,
    var steps: List<DirectionStep>?
)

data class DirectionStep(
    var distance: GeoDirectionMetric?,
    var duration: GeoDirectionMetric?,
    @SerializedName("end_location") var endLocation: GeoLocation?,
    @SerializedName("start_location") var startLocation: GeoLocation?,
    @SerializedName("html_instructions") var instructions: String?,
    var travelMode: String?,
    var polyline: GeoPolyline?
)

data class GeoDirectionMetric(
    var text: String?,
    var value: Long?
)

data class GeoPolyline(
    var points: String?
)

data class GeoBounds(
    var northeast: GeoLocation?,
    var southwest: GeoLocation?
)

data class GeoLocation(
    var lat: Double?,
    var lng: Double?
)

data class LocationObject(
    @SerializedName("Latitude") var latitude: Double? = 0.0,
    @SerializedName("Longitude") var longitude: Double? = 0.0,
    @SerializedName("Name") var name: String? = "",
    @SerializedName("FormattedAddress") var address: String? = ""
)