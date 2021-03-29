package com.example.contacts42.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "contacts_table")
@Parcelize
data class Contact(
    val name: String,
    val favorite: Boolean = false,
    val phone_home: String = "",
    val phone_work: String = "",
    val email_home: String = "",
    val email_work: String = "",
    val picture_filename: String = "",
    @PrimaryKey(autoGenerate = true) val id: Int = 0
) : Parcelable