package com.example.kttelematic.models

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.io.Serializable
import java.util.Date

open class User(
    @PrimaryKey var id: String? = null
) : RealmObject(), Serializable {
    var firstName: String? = null
    var lastName: String? = null
    var phoneNumber: String? = null
    var phoneNUmberCC: Int? = null
    var email: String? = null
    var password: String? = null
    var isLogOut: Boolean? = null
    var isDeleted: Boolean? = null
    var isSelected: Boolean? = false
    var createdAt: Date = Date()
    var updatedAt: Date = Date()

}