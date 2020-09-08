package com.eghoindustries.locationutils.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.eghoindustries.locationutils.Constants
import com.eghoindustries.locationutils.LocationUtilsActivity
import com.eghoindustries.locationutils.PlaceAdapter
import com.eghoindustries.locationutils.R
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import kotlinx.android.synthetic.main.fragment_place_adapter.*
import kotlinx.android.synthetic.main.header_card.*

class PlaceAdapterFragment : Fragment() {

    private lateinit var context: LocationUtilsActivity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = activity as LocationUtilsActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_place_adapter, container, false)
    }

    override fun onResume() {
        super.onResume()
        searchField.text.clear()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        backIcon.setOnClickListener { context.onBackPressed() }
        titleTextView.text = getString(R.string.search_address)
        dismissButton.setOnClickListener { instructionLayout.visibility = View.GONE }
        mapPickerLayout.setOnClickListener {
            context.viewModel.loadFragment(context.viewModel.getFragmentByKey(Constants.MAP_PICKER_FRAGMENT).fragment, context)
        }

        val sessionToken = AutocompleteSessionToken.newInstance()
        searchField.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (searchField.text.toString().trim().isNotEmpty()) {
                    initSearchConfig(context, searchField.text.toString().trim(), sessionToken)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }


    private fun initSearchConfig(activity: LocationUtilsActivity,
                                 query: String,
                                 sessionToken: AutocompleteSessionToken) {


        val bounds = RectangularBounds.newInstance(
            LatLng(4.273007390000032, 2.6685335630000395),  //Nigeria lat/lng
            LatLng(14.678816234000067, 13.894419133000042))
        //todo: Implement Generic Bounds Config

        val placeRequest = FindAutocompletePredictionsRequest.builder()
            .setCountry("ng")
            .setLocationBias(bounds)
            .setSessionToken(sessionToken)
            .setQuery(query)
            .build()

        activity.placesClient.findAutocompletePredictions(placeRequest)
            .addOnSuccessListener {
                val response = it
                if (response.autocompletePredictions.isNotEmpty() && placesRecyclerView != null) {
                    placesRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context,
                        RecyclerView.VERTICAL, false)
                    placesRecyclerView.adapter = PlaceAdapter(activity, response.autocompletePredictions, activity.viewModel.isOrigin)
                }

            }
            .addOnFailureListener {
                //todo: Show empty state dialog
                it.printStackTrace()
            }
    }
}
