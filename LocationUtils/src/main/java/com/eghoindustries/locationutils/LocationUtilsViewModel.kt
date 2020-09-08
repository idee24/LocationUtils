package com.eghoindustries.locationutils

import android.app.Application
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import com.eghoindustries.locationutils.fragments.MapPickerFragment
import com.eghoindustries.locationutils.fragments.PlaceAdapterFragment
import com.eghoindustries.locationutils.fragments.RoutesFragment

/**
 *Created by Yerimah on 9/8/2020.
 */
class LocationUtilsViewModel(application: Application): AndroidViewModel(application)  {

    private var backStack = LinkedHashMap<String, LocationFragment>()
    var fragmentMap = LinkedHashMap<Fragment, LocationFragment>()
    lateinit var currentFragment: Fragment
    var isOrigin = false
    var originLocation = LocationObject()
    var destinationLocation = LocationObject()

    fun initFragmentNavStructure(activity: LocationUtilsActivity) {

        val routesFragment = LocationFragment(activity, Constants.ROUTES_FRAGMENT,
            RoutesFragment(), null)
        deployFragment(routesFragment, Constants.ROUTES_FRAGMENT)

        val placeAdapterFragment = LocationFragment(activity, Constants.PLACE_ADAPTER_FRAGMENT,
            PlaceAdapterFragment(), routesFragment)
        deployFragment(placeAdapterFragment, Constants.PLACE_ADAPTER_FRAGMENT)

        val mapPickerFragment = LocationFragment(activity, Constants.MAP_PICKER_FRAGMENT, MapPickerFragment(),
            placeAdapterFragment)
        deployFragment(mapPickerFragment, Constants.MAP_PICKER_FRAGMENT)
    }


    private fun deployFragment(gigFragment: LocationFragment, fragmentName: String) {
        backStack[fragmentName] = gigFragment
        fragmentMap[gigFragment.fragment] = gigFragment
    }

    fun loadFragment(fragment: Fragment, context: LocationUtilsActivity) {

        val transaction = context.supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commitAllowingStateLoss()
        currentFragment = fragment
    }

    fun getFragmentByKey(key: String): LocationFragment {
        return backStack[key] ?: backStack[Constants.ROUTES_FRAGMENT]!!
    }
}