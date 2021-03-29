package com.example.contacts42.ui.addedit

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.contacts42.R
import com.example.contacts42.data.Contact
import com.example.contacts42.data.ContactDao
import com.example.contacts42.ui.ADD_CONTACT_RESULT_OK
import com.example.contacts42.ui.EDIT_CONTACT_RESULT_OK
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.FileDescriptor
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class AddEditContactViewModel @Inject constructor(
    private val contactDao: ContactDao,
    private val state: SavedStateHandle
) : ViewModel() {
    val contact = state.get<Contact>("contact")

    var contactPicture = state.get<String>("contactPicture") ?: contact?.picture_filename ?: ""
        set(value) {
            field = value
            state.set("contactPicture", value)
        }

    var contactName = state.get<String>("contactName") ?: contact?.name ?: ""
        set(value) {
            field = value
            state.set("contactName", value)
        }

    var contactFavorite = state.get<Boolean>("contactFavorite") ?: contact?.favorite ?: false
        set(value) {
            field = value
            state.set("contactFavorite", value)
        }

    var contactPhoneHome = state.get<String>("contactPhoneHome") ?: contact?.phone_home ?: ""
        set(value) {
            field = value
            state.set("contactPhoneHome", value)
        }

    var contactPhoneWork = state.get<String>("contactPhoneWork") ?: contact?.phone_work ?: ""
        set(value) {
            field = value
            state.set("contactPhoneWork", value)
        }

    var contactEmailHome = state.get<String>("contactEmailHome") ?: contact?.email_home ?: ""
        set(value) {
            field = value
            state.set("contactEmailHome", value)
        }

    var contactEmailWork = state.get<String>("contactEmailWork") ?: contact?.email_work ?: ""
        set(value) {
            field = value
            state.set("contactEmailWork", value)
        }

    private val addEditContactEventChannel = Channel<AddEditContactEvent>()
    val addEditContactEvent = addEditContactEventChannel.receiveAsFlow()

    fun onSaveClick() {
        if (contactName.isBlank()) {
            showInvalidInputMessage(R.string.contacts_empty_name)
            return
        }

        if (contact != null) {
            val updatedContact = contact.copy(
                name = contactName,
                favorite = contactFavorite,
                phone_home = contactPhoneHome,
                phone_work = contactPhoneWork,
                email_home = contactEmailHome,
                email_work = contactEmailWork,
                picture_filename = contactPicture
            )
            updateContact(updatedContact)
        } else {
            val newContact =
                Contact(
                    contactName,
                    contactFavorite,
                    contactPhoneHome,
                    contactPhoneWork,
                    contactEmailHome,
                    contactEmailWork,
                    contactPicture
                )
            createContact(newContact)
        }
    }

    private fun createContact(contact: Contact) = viewModelScope.launch {
        contactDao.insert(contact)
        addEditContactEventChannel.send(
            AddEditContactEvent.NavigateBackWithResult(
                ADD_CONTACT_RESULT_OK
            )
        )
    }

    private fun updateContact(contact: Contact) = viewModelScope.launch {
        contactDao.update(contact)
        addEditContactEventChannel.send(
            AddEditContactEvent.NavigateBackWithResult(
                EDIT_CONTACT_RESULT_OK
            )
        )
    }

    private fun showInvalidInputMessage(stringId: Int) = viewModelScope.launch {
        addEditContactEventChannel.send(AddEditContactEvent.ShowInvalidInputMessage(stringId))
    }

    sealed class AddEditContactEvent {
        data class ShowInvalidInputMessage(val stringId: Int) : AddEditContactEvent()
        data class NavigateBackWithResult(val result: Int) : AddEditContactEvent()
    }

    // https://developer.android.com/training/data-storage/shared/documents-files#bitmap
    @Throws(IOException::class)
    private fun getBitmapFromUri(uri: Uri, contentResolver: ContentResolver): Bitmap {
        val parcelFileDescriptor: ParcelFileDescriptor? =
            contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor: FileDescriptor? = parcelFileDescriptor?.fileDescriptor
        if (fileDescriptor == null) {
            Log.d(TAG, "getBitmapFromUri: FD is null")
        }
        val image: Bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor?.close()
        return image
    }

    private fun resizePhoto(pictureFile: Bitmap): Bitmap {
        val aspectRatio: Float = (pictureFile.width).toFloat() / (pictureFile.height).toFloat()
        val resizedWidth = 1024
        val resizedHeight = (resizedWidth / aspectRatio).toInt()
        return Bitmap.createScaledBitmap(pictureFile, resizedWidth, resizedHeight, false)
    }

    fun saveImageFile(uri: Uri, context: Context, fileName: String) =
        CoroutineScope(Dispatchers.IO).launch {
            var pictureFile: Bitmap = getBitmapFromUri(uri, context.contentResolver)
            pictureFile = resizePhoto(pictureFile)
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
                pictureFile.compress(Bitmap.CompressFormat.JPEG, 90, it)
            }
        }
}