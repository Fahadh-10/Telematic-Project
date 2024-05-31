package com.example.kttelematic.helpers

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.util.Log
import android.webkit.WebView
import androidx.core.app.ActivityCompat
import com.example.kttelematic.helpers.Extensions.showToast
import com.example.kttelematic.models.Addresses
import java.io.IOException
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.regex.Pattern

object Utils {
    var token : String ?= null
    /**
     * Checks if the provided email address is valid.
     *
     * This function uses a regular expression pattern to validate the email address format.
     * It returns true if the email address matches the pattern, indicating that it is valid.
     * Otherwise, it returns false.
     *
     * @param email The email address to be validated.
     * @return True if the email address is valid, false otherwise.
     */
    fun isEmailValid(email: String): Boolean {
        return Pattern.compile(
            "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]|[\\w-]{2,}))@"
                    + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                    + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                    + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                    + "[0-9]{1,2}|25[0-5]|2[0-4][0-9]))|" + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$"
        ).matcher(email).matches()
    }

    /**
     * Checks if the provided password meets the required criteria for a strong password.
     *
     * This function uses a regular expression pattern to validate the password format.
     * It checks if the password contains at least one digit, one lowercase letter,
     * one uppercase letter, one special character, and has a minimum length of 8 characters.
     * It returns true if the password meets all the criteria, indicating that it is valid.
     * Otherwise, it returns false.
     *
     * @param password The password to be validated.
     * @return True if the password is valid, false otherwise.
     */
    fun isPasswordValid(password: String): Boolean {
        return Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#\$%^&+=]).{8,}\$")
            .matcher(password).matches()
    }

    /**
     * Show an alert dialog with a specified title and message.
     *
     * This function displays an alert dialog with the provided title and message. It includes a single "Ok" button
     * that dismisses the dialog when clicked.
     *
     * @param context The [Activity] context in which to display the alert dialog.
     * @param title The title of the alert dialog.
     * @param message The message or content of the alert dialog.
     */
    fun showAlert(context: Activity?, title: String, message: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("Ok", null)
        builder.show()
    }

    /**
     * Checks if the app has been granted location permissions.
     * This function checks if the ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION permissions are granted.
     * @param context The context in which the permissions are checked.
     * @return True if both ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION permissions are granted, false otherwise.
     */
    fun getLocationPermission(context: Context): Boolean {
        return (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    }

    /**
     * Checks if GPS location services are enabled on the device.
     * This function determines if either GPS_PROVIDER or NETWORK_PROVIDER is enabled in the device's location settings.
     * @param context The context used to access the LocationManager service.
     * @return True if GPS location services are enabled, false otherwise.
     */
    fun isGpsLocationEnabled(context: Context): Boolean {
        val locationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    /**
     * Retrieves address information from latitude and longitude coordinates.
     * This function uses Geocoder to fetch address details based on the provided latitude and longitude.
     * It handles the case when no address details are found or when there are connectivity issues.
     * @param latitude The latitude coordinate of the location.
     * @param longitude The longitude coordinate of the location.
     * @param context The context used to access Geocoder and display error messages.
     * @return An Addresses object containing the address details if available, or null if address retrieval fails.
     * @see Geocoder
     * @see Addresses
     */
    fun getAddressFromCoordinates(
        latitude: Double,
        longitude: Double,
        context: Context
    ): Addresses? {
        val geocoder = Geocoder(context, Locale.getDefault())

        try {
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

            if (!addresses.isNullOrEmpty()) {
                val address: Address = addresses[0]
                Log.e("mm", address.getAddressLine(0) ?: "")
                if (address.thoroughfare.isNullOrEmpty() && address.featureName.isNullOrEmpty()
                    && address.subThoroughfare.isNullOrEmpty()
                ) {
                    context.showToast("Unable to connect")
                } else {
                    Log.e("featureName", address.featureName ?: "")
                    Log.e("adminArea", address.adminArea ?: "")
                    Log.e("countryCode", address.countryCode ?: "")
                    Log.e("countryName", address.countryName ?: "")
                    Log.e("subAdminArea", address.subAdminArea ?: "")
                    Log.e("postalCode", address.postalCode ?: "")
                    Log.e("subLocality", address.subLocality ?: "")
                    Log.e("subThoroughfare", address.subThoroughfare ?: "")
                    Log.e("thoroughfare", address.thoroughfare ?: "")
                    Log.e("locality", address.locality ?: "")
                    val places = Addresses()
                    places.latitude = latitude
                    places.longitude = longitude
                    places.address = address.getAddressLine(0)
                    places.houseNo = address.subThoroughfare
                    places.addressTitle = address.featureName
                    places.street = address.thoroughfare
                    places.city = address.locality
                    places.subCity = address.subLocality
                    places.state = address.adminArea
                    places.zipCode = address.postalCode
                    places.country = address.countryName
                    places.countryCode = address.countryCode
                    return places
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    /**
     * Extracts address information and coordinates from a Google Maps URL.
     * This function parses the given URL to extract the address and coordinates (latitude and longitude).
     * It handles URL decoding and extraction of address details and coordinates.
     * @param url The Google Maps URL containing the address and coordinates.
     * @return A Triple containing the extracted address, latitude, and longitude.
     * If extraction fails or the URL format is incorrect, null values are returned.
     */
    fun extractAddressFromUrl(url: String): Triple<String?, Double?, Double?> {
        try {
            val startIndex = url.indexOf("/place/") + "/place/".length
            val endIndex = url.indexOf("/@", startIndex)
            val encodedAddress = url.substring(startIndex, endIndex)
            val decodedAddress = URLDecoder.decode(encodedAddress, "UTF-8")
            val address = decodedAddress.replace("+", " ")

            val latLongStartIndex = url.indexOf("@") + 1
            val latLongEndIndex = url.indexOf(",", latLongStartIndex)
            val lat = url.substring(latLongStartIndex, latLongEndIndex).toDouble()

            val longStartIndex = latLongEndIndex + 1
            val longEndIndex = url.indexOf(",", longStartIndex)
            val long = url.substring(longStartIndex, longEndIndex).toDouble()

            return Triple(address, lat, long)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Triple(null, null, null)
    }

    /**
     * Formats the input date string to a different date string format.
     * This function parses the input date string using the provided input format,
     * then formats it into the desired output format.
     * @param inputDateString The input date string to be formatted.
     * @return The formatted date string in the desired output format.
     */
    fun formatDateString(inputDateString: String): String {
        val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss 'GMT'z yyyy", Locale.ENGLISH)
        val outputFormat = SimpleDateFormat("MMM dd hh:mm a", Locale.ENGLISH)

        val date = inputFormat.parse(inputDateString)
        return outputFormat.format(date)
    }

}