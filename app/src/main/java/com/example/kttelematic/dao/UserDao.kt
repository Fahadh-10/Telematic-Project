package com.example.kttelematic.dao

import com.example.kttelematic.models.User
import io.realm.Realm
import java.io.IOException

object UserDao {
    /**
     * Saves or updates a user's details in the Realm database.
     *
     * This function takes a User object and inserts or updates it in the Realm database.
     * If the user already exists in the database, it will be updated with the new details;
     * otherwise, a new user entry will be created.
     *
     * @param user The User object to be saved or updated.
     */
    fun saveOrUpdateUser(user : User) {
        val realm = Realm.getDefaultInstance()
        try {
            realm.beginTransaction()
            realm.insertOrUpdate(user)
            realm.commitTransaction()
        } catch (e: IOException) {
            realm.cancelTransaction()
        }
        realm.close()
    }

    /**
     * Fetches the list of users' data from the Realm database.
     *
     * This function retrieves all user details stored in the Realm database and returns them as an ArrayList.
     *
     * @return An ArrayList containing the fetched user data.
     */
    fun fetchUser(): ArrayList<User>{
        var userList: ArrayList<User> = ArrayList<User>()
        val realm = Realm.getDefaultInstance()
        realm.use {
            val result = realm.where(User::class.java).findAll()
            if (result != null) {
                for (userResult in realm.copyFromRealm(result)){
                    userList.add(userResult)
                }
            }
        }
        realm.close()
        return userList
    }

    /**
     * Fetches user data from the Realm database based on the provided user ID.
     *
     * This function retrieves the details of a user with the specified user ID from the Realm database
     * and returns it as a User object.
     *
     * @param id The team ID used to query the user data.
     * @return A User object containing the fetched user data, or null if no user with the specified ID is found.
     */
    fun fetchUserBasedEmail(email:String): User?{
        var user: User? =null
        val realm = Realm.getDefaultInstance()
        realm.use {
            val result = realm.where(User::class.java).equalTo("email", email).findFirst()
            if (result != null) {
                user =  realm.copyFromRealm(result)
            }
        }
        realm.close()
        return user
    }

    /**
     * Deletes a user from the Realm database based on the provided user ID.
     *
     * This function searches for a user with the specified ID in the Realm database and deletes it if found.
     * If no user with the specified ID is found, the function does nothing.
     *
     * @param id The user ID used to identify the user to be deleted.
     */
    fun deleteUserFromId(id: String) {
        val realm = Realm.getDefaultInstance()
        realm.use {
            val result = realm.where(User::class.java).equalTo("id", id).findFirst()
            if (result != null) {
                realm.beginTransaction()
                result.deleteFromRealm()
                realm.commitTransaction()
            }
        }
        realm.close()
    }

    fun userLoggedList(): ArrayList<User> {
        val userList = fetchUser()
        return ArrayList(userList.filter { it.isLogOut == false })
    }
}