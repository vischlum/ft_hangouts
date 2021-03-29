package com.example.contacts42.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {

    fun getContacts(
        query: String,
        sortOrder: SortOrder,
        showOnlyFavorites: Boolean
    ): Flow<List<Contact>> =
        when (sortOrder) {
            SortOrder.BY_NAME -> getContactsSortedByName(query, showOnlyFavorites)
            SortOrder.BY_NAME_REVERSE -> getContactsSortedByNameReverse(query, showOnlyFavorites)
        }

    @Query("SELECT * FROM contacts_table WHERE (favorite = :showOnlyFavorites OR favorite = 1) AND name LIKE '%' || :searchQuery || '%' ORDER BY favorite DESC, name")
    fun getContactsSortedByName(
        searchQuery: String,
        showOnlyFavorites: Boolean
    ): Flow<List<Contact>>

    @Query("SELECT * FROM contacts_table WHERE (favorite = :showOnlyFavorites OR favorite = 1) AND name LIKE '%' || :searchQuery || '%' ORDER BY favorite DESC, name DESC")
    fun getContactsSortedByNameReverse(
        searchQuery: String,
        showOnlyFavorites: Boolean
    ): Flow<List<Contact>>

    @Query("SELECT picture_filename FROM contacts_table WHERE picture_filename IS NOT ''")
    fun getAllPictures(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: Contact)

    @Update
    suspend fun update(contact: Contact)

    @Delete
    suspend fun delete(contact: Contact)
}