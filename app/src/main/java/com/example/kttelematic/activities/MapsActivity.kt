package com.example.kttelematic.activities

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.kttelematic.R
import com.example.kttelematic.dao.AddressDao
import com.example.kttelematic.databinding.ActivityMapsBinding
import com.example.kttelematic.helpers.ConstantValues
import com.example.kttelematic.helpers.ConstantValues.ADDRESS_ADDED_CODE
import com.example.kttelematic.helpers.NavKey
import com.example.kttelematic.helpers.Utils
import com.example.kttelematic.managers.PrefManager
import com.example.kttelematic.models.Addresses
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place

import android.location.Geocoder
import android.widget.Toast
import java.io.IOException
import java.util.Locale

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnCameraIdleListener {

    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mMap: GoogleMap
    private var address: Addresses? = null
    var addressUrl: String? = null
    var latitude: Double? = null
    var longitude: Double? = null
    var currentAddress: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setUpMapView()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setContentView(binding.root)
        readBundles()
        setupListeners()
    }

    private fun readBundles() {
        if (intent.hasExtra(NavKey.ADDRESS)) {
            address = intent.getSerializableExtra(NavKey.ADDRESS) as Addresses
        } else {
            fusedLastLocationClient()
        }
    }

    private fun setUpMapView() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setupListeners() {
        binding.addressBackIV.setOnClickListener {
            finish()
        }

        binding.saveLocationACB.setOnClickListener {
            val addressList = currentAddress?.split(",")
            if (addressList?.isNotEmpty() == true) {
                binding.processLoader.visibility = VISIBLE
                address?.address = binding.locationTV.text.toString()
                address?.latitude = latitude
                address?.longitude = longitude
                address?.address = PrefManager.getUserData(this@MapsActivity)?.id ?: ""
                address?.city = addressList[addressList.size - 3]
                address?.state = addressList[addressList.size - 2]
                address?.zipCode = addressList[addressList.size - 1]
            }
            AddressDao.addAddresses(
                address ?: Addresses(),
                PrefManager.getUserData(this@MapsActivity)?.id ?: ""
            )

            Handler().postDelayed({
                showSuccessAlertDialog()
            }, 5000)
        }
    }

    private fun showSuccessAlertDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.success))
            .setMessage(getString(R.string.address_added_successfully))
            .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                binding.processLoader.visibility = GONE
                dialog.dismiss()
                val intent = Intent()
                setResult(ADDRESS_ADDED_CODE, intent)
                finish()
            }
            .setCancelable(false).show()
    }

    private fun fusedLastLocationClient(): Addresses? {
        if (ActivityCompat.checkSelfPermission(
                this@MapsActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this@MapsActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener(this@MapsActivity) { location ->
                if (location != null) {
                    address = Utils.getAddressFromCoordinates(
                        location.latitude, location.longitude, this@MapsActivity
                    )
                    if (address != null) {
                        val latLng = LatLng(
                            address?.latitude ?: 0.0,
                            address?.longitude ?: 0.0
                        )
                        binding.locationTV.text = address?.city
                        binding.mapAddressTV.text = address?.address
                    }
                }
            }
        return address
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = false
        mMap.uiSettings.isZoomGesturesEnabled = true
        mMap.setOnCameraIdleListener(this) // Set the camera idle listener

        if (Utils.getLocationPermission(this@MapsActivity) && Utils.isGpsLocationEnabled(this@MapsActivity)) {
            fusedLastLocationClient()
        }
        val latLng = LatLng(
            address?.latitude ?: 0.0,
            address?.longitude ?: 0.0
        )
        mMap.addMarker(MarkerOptions().position(latLng))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
    }

    override fun onCameraIdle() {
        val latLng = mMap.cameraPosition.target
        latitude = latLng.latitude
        longitude = latLng.longitude

        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude ?: 0.0, longitude ?: 0.0, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                currentAddress = addresses[0].getAddressLine(0)
                binding.locationTV.text = currentAddress
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Unable to fetch address", Toast.LENGTH_SHORT).show()
        }
    }
}
