package com.example.kttelematic.models

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.io.Serializable
import java.util.Date

open class Addresses(
    @PrimaryKey var id: String? = null
) : RealmObject(), Serializable {
    var userId : String ?= null
    var addressLine1: String? = null
    var addressLine2: String? = null
    var latitude: Double? = null
    var longitude: Double? = null
    var address: String? = null
    var houseNo: String? = null
    var addressTitle: String? = null
    var street: String? = null
    var city: String? = null
    var subCity: String? = null
    var state: String? = null
    var zipCode: String? = null
    var country: String? = null
    var countryCode: String? = null
    var isSelected : Boolean ?= null
    var createdAt: Date = Date()
    var updatedAt: Date = Date()
}