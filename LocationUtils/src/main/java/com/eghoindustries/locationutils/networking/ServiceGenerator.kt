package com.eghoindustries.locationutils.networking

import com.android.volley.BuildConfig
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 *Created by Yerimah on 9/8/2020.
 */
fun <S> generateService(serviceClass: Class<S>, BASE_URL: String) : S {

    val builder = GsonBuilder()
    builder.serializeNulls()
    builder.setDateFormat("yyyy-MM-dd")
    val retrofitBuilder = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create(builder.create()))
        .baseUrl(BASE_URL)

    val httpClientBuilder = OkHttpClient.Builder()
        .readTimeout(60, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)

    if (BuildConfig.DEBUG) {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        val loggingInterceptor =
            httpLoggingInterceptor.apply {
                httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            }
        httpClientBuilder.addInterceptor(loggingInterceptor)
    }

    retrofitBuilder.client(httpClientBuilder.build())
    return retrofitBuilder.build().create(serviceClass)
}
