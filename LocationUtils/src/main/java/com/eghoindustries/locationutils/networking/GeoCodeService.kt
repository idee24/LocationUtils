package com.eghoindustries.locationutils.networking

import com.eghoindustries.locationutils.AddressModel
import com.eghoindustries.locationutils.GeoDirectionsResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

/**
 *Created by Yerimah on 9/8/2020.
 */
interface GeoCodeService {

    @GET
    fun getFormattedAddress(@Url url: String): Call<AddressModel>

    @GET
    fun getGeoDirections(@Url url: String): Call<GeoDirectionsResponse>
}