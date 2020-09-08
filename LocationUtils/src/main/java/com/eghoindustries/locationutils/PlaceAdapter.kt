package com.eghoindustries.locationutils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest

/**
 *Created by Yerimah on 1/22/2020.
 */
class PlaceAdapter(private val activity: LocationUtilsActivity,
                   private val autoCompletePredictions: List<AutocompletePrediction>,
                   private val isSender: Boolean):
    RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.place_item, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        holder.nameTextView.text = autoCompletePredictions[position].getPrimaryText(null).toString()
        holder.formattedAddressTextView.text = autoCompletePredictions[position].getSecondaryText(null).toString()
        holder.itemView.setOnClickListener {
            processEventLocation(autoCompletePredictions[position])
        }
    }

    private fun processEventLocation(prediction: AutocompletePrediction) {

        val placeFilters = listOf(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.ADDRESS)

        val fetchRequest = FetchPlaceRequest.builder(prediction.placeId, placeFilters).build()
        activity.placesClient.fetchPlace(fetchRequest)
            .addOnSuccessListener {
                val coordinates = it.place.latLng
                val locationObject = LocationObject(coordinates?.latitude, coordinates?.longitude, it.place.name, it.place.address)
                if (isSender) {
                    activity.viewModel.originLocation = locationObject
                }
                else {
                    activity.viewModel.destinationLocation = locationObject
                }
                activity.viewModel.loadFragment(activity.viewModel.getFragmentByKey(Constants.ROUTES_FRAGMENT).fragment, activity)

            }
            .addOnFailureListener {
                Toast.makeText(activity, it.printStackTrace().toString(), Toast.LENGTH_SHORT).show()
                it.printStackTrace()
            }

    }

    override fun getItemCount(): Int = autoCompletePredictions.size

    inner class PlaceViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val formattedAddressTextView: TextView = itemView.findViewById(R.id.formattedAddressTextView)
    }
}