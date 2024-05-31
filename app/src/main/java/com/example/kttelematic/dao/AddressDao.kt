package com.example.kttelematic.dao

import com.example.kttelematic.managers.PrefManager
import com.example.kttelematic.models.Addresses
import io.realm.Realm
import java.io.IOException
import java.util.UUID

object AddressDao {

    fun saveOrUpdateAddress(address: Addresses) {
        val realm = Realm.getDefaultInstance()
        try {
            realm.beginTransaction()
            realm.insertOrUpdate(address)
            realm.commitTransaction()
        } catch (e: IOException) {
            realm.cancelTransaction()
        }
        realm.close()
    }

    fun fetchAddress(): ArrayList<Addresses>{
        var addressList: ArrayList<Addresses> = ArrayList<Addresses>()
        val realm = Realm.getDefaultInstance()
        realm.use {
            val result = realm.where(Addresses::class.java).findAll()
            if (result != null) {
                for (userResult in realm.copyFromRealm(result)){
                    addressList.add(userResult)
                }
            }
        }
        realm.close()
        return addressList
    }

    fun fetchAddressListBasedId(userId:String): ArrayList<Addresses>{
        var addressList: ArrayList<Addresses> = ArrayList<Addresses>()
        val realm = Realm.getDefaultInstance()
        realm.use {
            val result = realm.where(Addresses::class.java).equalTo("userId", userId).findAll()
            if (result != null) {
                for (userResult in realm.copyFromRealm(result)){
                    addressList.add(userResult)
                }
            }
        }
        realm.close()
        return addressList
    }

    fun deleteAddressFromId(id: String) {
        val realm = Realm.getDefaultInstance()
        realm.use {
            val result = realm.where(Addresses::class.java).equalTo("id", id).findFirst()
            if (result != null) {
                realm.beginTransaction()
                result.deleteFromRealm()
                realm.commitTransaction()
            }
        }
        realm.close()
    }

    fun addAddresses(address: Addresses, userId: String) {
        val addresses = Addresses()
        addresses.id = UUID.randomUUID().toString()
        addresses.userId = userId
        addresses.addressTitle = address.addressTitle
        addresses.addressLine1 = address.addressLine1
        addresses.addressLine2 = address.addressLine2
        addresses.address = address.address
        addresses.subCity = address.subCity
        addresses.latitude = address.latitude
        addresses.longitude = address.longitude
        addresses.city = address.city
        addresses.state = address.state
        addresses.country = address.country
        addresses.zipCode = address.zipCode
        saveOrUpdateAddress(addresses)
    }

}