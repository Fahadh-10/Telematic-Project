package com.example.kttelematic.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kttelematic.R
import com.example.kttelematic.adapter.StoreLocationsAdapter
import com.example.kttelematic.bottomSheetDialog.LoggedUserBSD
import com.example.kttelematic.dao.AddressDao
import com.example.kttelematic.databinding.ActivityDashboardBinding
import com.example.kttelematic.helpers.ConstantValues.ADDRESS_ADDED_CODE
import com.example.kttelematic.helpers.ConstantValues.LOCATION_PERMISSION_REQUEST_CODE
import com.example.kttelematic.helpers.ConstantValues.LOCATION_REQUEST_CODE
import com.example.kttelematic.helpers.ConstantValues.LOCATION_SETTING_PERMISSION
import com.example.kttelematic.helpers.ConstantValues.PROFILE_RESULT_CODE
import com.example.kttelematic.helpers.NavKey
import com.example.kttelematic.helpers.Utils
import com.example.kttelematic.managers.PrefManager
import com.example.kttelematic.models.Addresses
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.tasks.Task

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var loggedUserBsd: LoggedUserBSD
    private lateinit var storeLocatorAdapter: StoreLocationsAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mProgressDialog: ProgressDialog
    var addressList: ArrayList<Addresses>? = null
    private var address: Addresses? = null
    private val handler = Handler(Looper.getMainLooper())
    private val intervalMillis: Long = 15 * 60 * 1000

    private val activityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == PROFILE_RESULT_CODE) {
                setupData()
            }

            if (result.resultCode == ADDRESS_ADDED_CODE) {
                setupData()
                setAdapter()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this@DashboardActivity)
        mProgressDialog = ProgressDialog(this@DashboardActivity)
        loggedUserBsd = LoggedUserBSD()
        setContentView(binding.root)
        if (checkPermission()) {
            fetchCurrentLocation()
        }
        setupListeners()
        setupData()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    /**
     * Sets up the data for the dashboard.
     * This function retrieves the user's first name and last name from the shared preferences,
     * concatenates them, and sets the resulting full name to the userNameTV TextView.
     * It also sets up the adapter for any data that needs to be displayed in the dashboard.
     */
    private fun setupData() {
        binding.userNameTV.text =
            PrefManager.getUserData(this@DashboardActivity)?.firstName.plus(" ")
                .plus(PrefManager.getUserData(this@DashboardActivity)?.lastName)
        setAdapter()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOCATION_SETTING_PERMISSION) {
            if (Utils.getLocationPermission(this@DashboardActivity) && Utils.isGpsLocationEnabled(
                    this@DashboardActivity
                )
            ) {
                fetchCurrentLocation()
            } else if (!Utils.isGpsLocationEnabled(this@DashboardActivity) && !Utils.getLocationPermission(
                    this@DashboardActivity
                )
            ) {
                showPermissionRationaleDialog()
            } else {
                showPermissionRationaleDialog()
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchCurrentLocation()
        } else {
            showPermissionRationaleDialog()
        }
    }

    /**
     * Displays a dialog to explain the rationale for requesting location permission.
     * This function creates and shows an AlertDialog with a title and message explaining
     * why the app requires location permission.
     * It provides a button to navigate to the app's settings where the user can grant the permission.
     * The dialog is set to be non-cancelable to ensure the user interacts with it.
     */
    private fun showPermissionRationaleDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.location_permission))
            .setMessage(getString(R.string.this_app_required_location_permission))
            .setPositiveButton(getString(R.string.go_to_setting)) { dialog, _ ->
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts("package", this.packageName, null)
                intent.data = uri
                startActivityForResult(intent, LOCATION_SETTING_PERMISSION)
                dialog.dismiss()
            }
            .setCancelable(false).show()
    }

    /**
     * Checks if the required location permissions are granted and if the GPS location is enabled.
     * This function first checks if the location permissions are granted using Utils.getLocationPermission().
     * If the permissions are not granted, it requests them from the user.
     * If the permissions are granted, it then checks if GPS location is enabled using Utils.isGpsLocationEnabled().
     * If GPS location is not enabled, it prompts the user to enable it by calling checkLocationSettings().
     * @return True if the required permissions are granted and GPS location is enabled, false otherwise.
     */
    private fun checkPermission(): Boolean {
        if (!Utils.getLocationPermission(this@DashboardActivity)) {
            if (ActivityCompat.checkSelfPermission(
                    this@DashboardActivity, Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this@DashboardActivity, Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@DashboardActivity as Activity, arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ), LOCATION_PERMISSION_REQUEST_CODE
                )
            }
            return false
        } else if (!Utils.isGpsLocationEnabled(this@DashboardActivity)) {
            checkLocationSettings()
            return false
        }
        return true
    }

    /**
     * Checks the location settings to ensure that the required location mode is enabled.
     * This function creates a LocationRequest with high accuracy priority and builds a
     * LocationSettingsRequest using LocationSettingsRequest.Builder to check if the required
     * location settings are satisfied.
     * It then uses the SettingsClient to check the location settings asynchronously.
     * If the required settings are not satisfied, it prompts the user to change the settings
     * by displaying a dialog with resolution options.
     * @see LocationRequest
     * @see LocationSettingsRequest
     * @see LocationServices
     * @param activity The activity instance from which this function is called.
     * It's used to start an activity to resolve the location settings issue.
     */
    private fun checkLocationSettings() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
            .setAlwaysShow(true)
        val client = LocationServices.getSettingsClient(this@DashboardActivity as Activity)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnCompleteListener { task ->
            try {
                val response = task.getResult(ApiException::class.java)
            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        val resolvable = exception as ResolvableApiException
                        resolvable.startResolutionForResult(
                            this, LOCATION_REQUEST_CODE
                        )
                    }

                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                    }
                }
            }
        }
    }

    /**
     * Sets up listeners for UI elements in the dashboard activity.
     * This function assigns click listeners to various UI elements to handle user interactions.
     * - Clicking on the user profile image view (userProfileIV) opens the logged-in user's bottom sheet dialog (loggedUserBsd).
     *   It also sets a callback listener to update data and adapter when a user is selected in the bottom sheet dialog.
     * - Clicking on the add locations action button (addLocationsACB) navigates the user to the MapsActivity
     *   to add new locations.
     * - Clicking on the profile container layout (profileCL) navigates the user to the ProfileActivity
     *   to view and edit their profile.
     */
    private fun setupListeners() {
        binding.userProfileIV.setOnClickListener {
            loggedUserBsd.show(this@DashboardActivity.supportFragmentManager, loggedUserBsd.tag)
            loggedUserBsd.setOnCallbacksListeners(object : LoggedUserBSD.Callback {
                override fun onUserSelected() {
                    setupData()
                    setAdapter()
                }

            })
        }

        binding.addLocationsACB.setOnClickListener {
            val intent = Intent(this@DashboardActivity, MapsActivity::class.java)
            intent.putExtra(NavKey.ADDRESS , address)
            this@DashboardActivity.activityResult.launch(intent)
        }

        binding.profileCL.setOnClickListener {
            val intent = Intent(this@DashboardActivity, ProfileActivity::class.java)
            this@DashboardActivity.activityResult.launch(intent)
        }
    }

    /**
     * Fetches the current location and updates the address information.
     * This function retrieves the current location using FusedLocationProviderClient.
     * If location permissions are not granted, it returns null.
     * If the location is available, it updates the address information, adds it to the database,
     * schedules the next location update, fetches the updated address list, and updates the adapter.
     * If the location is not available, it shows a progress dialog and retries fetching the location after a delay.
     * @return The current address information, or null if location permissions are not granted.
     */
    private fun fetchCurrentLocation(): Addresses? {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this@DashboardActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener(this@DashboardActivity as Activity) { location ->
                if (location != null) {
                    address = Utils.getAddressFromCoordinates(
                        location.latitude, location.longitude, this@DashboardActivity
                    )
                    AddressDao.addAddresses(
                        address ?: Addresses(),
                        PrefManager.getUserData(this@DashboardActivity)?.id ?: ""
                    )
                    handler.postDelayed(addressRunnable, intervalMillis)
                    val ss = AddressDao.fetchAddressListBasedId(
                        PrefManager.getUserData(this@DashboardActivity)?.id ?: ""
                    )
                    Log.e("SSSS", ss.toString())
                    setAdapter()
                } else {
                    if (!mProgressDialog.isShowing) {
                        mProgressDialog.setTitle("Fetching the Location")
                        mProgressDialog.setMessage("Please wait...")
                        mProgressDialog.setCancelable(false)
                        mProgressDialog.show()
                    }
                    Handler().postDelayed({
                        fetchCurrentLocation()
                    }, 5000)
                }
            }
        return address
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(addressRunnable)
    }

    private val addressRunnable = object : Runnable {
        override fun run() {
            fetchCurrentLocation()
            handler.postDelayed(this, intervalMillis)
        }
    }

    /**
     * Sets up the RecyclerView adapter for displaying store locations.
     * This function fetches the list of addresses based on the user ID from the database,
     * sets up the RecyclerView with LinearLayoutManager, and assigns a custom adapter (StoreLocationsAdapter)
     * to display the fetched addresses.
     * It also sets up item click listeners on the adapter to handle user interactions.
     * @see AddressDao
     * @see StoreLocationsAdapter
     */
    private fun setAdapter() {
        addressList = AddressDao.fetchAddressListBasedId(
            PrefManager.getUserData(this@DashboardActivity)?.id ?: ""
        )
        binding.locationsRV.layoutManager =
            LinearLayoutManager(this@DashboardActivity, LinearLayoutManager.VERTICAL, false)
        storeLocatorAdapter = StoreLocationsAdapter(addressList ?: ArrayList())
        binding.locationsRV.adapter = storeLocatorAdapter
        storeLocatorAdapter.setOnItemClickListeners(object :
            StoreLocationsAdapter.OnItemClickListener {
            override fun onItemClick(address: Addresses, position: Int) {
                if (addressList?.isNotEmpty() == true) {
                    if (addressList!![position].isSelected == true) {
                        addressList!![position].isSelected = false
                        storeLocatorAdapter.addressList = addressList ?: ArrayList<Addresses>()
                        storeLocatorAdapter.notifyDataSetChanged()
                    } else {
                        storeLocatorAdapter.addressList.forEach { it.isSelected = false }
                        addressList!![position].isSelected = true
                        storeLocatorAdapter.addressList = addressList ?: ArrayList<Addresses>()
                        storeLocatorAdapter.notifyDataSetChanged()
                        val intent = Intent(this@DashboardActivity, MapsActivity::class.java)
                        intent.putExtra(NavKey.ADDRESS, address)
                        startActivity(intent)
                    }
                }
            }

        })
    }
}