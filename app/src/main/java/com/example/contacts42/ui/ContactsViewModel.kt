package com.example.contacts42.ui

import android.content.Context
import androidx.lifecycle.*
import com.example.contacts42.R
import com.example.contacts42.data.Contact
import com.example.contacts42.data.ContactDao
import com.example.contacts42.data.DatastoreManager
import com.example.contacts42.data.SortOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val contactDao: ContactDao,
    private val datastoreManager: DatastoreManager,
    state: SavedStateHandle
) : ViewModel() {

    val searchQuery = state.getLiveData("searchQuery", "")

    val preferencesFlow = datastoreManager.contactsDisplayPreferencesFlow

    private val contactsEventChannel = Channel<ContactsEvent>()
    val contactsEvents = contactsEventChannel.receiveAsFlow()

    private val contactsFlow = combine(
        searchQuery.asFlow(),
        preferencesFlow
    ) { query, filterPreferences ->
        Pair(query, filterPreferences)
    }.flatMapLatest { (query, filterPreferences) ->
        contactDao.getContacts(
            query,
            filterPreferences.sortOrder,
            filterPreferences.showOnlyFavorites
        )
    }

    val contacts = contactsFlow.asLiveData()

    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        datastoreManager.updateSortOrder(sortOrder)
    }

    fun onShowOnlyFavoritesClick(showOnlyFavorites: Boolean) = viewModelScope.launch {
        datastoreManager.updateShowOnlyFavorites(showOnlyFavorites)
    }

    fun onContactSelected(contact: Contact) = viewModelScope.launch {
        contactsEventChannel.send(ContactsEvent.NavigateToViewContactScreen(contact))
    }

    fun onContactSwiped(contact: Contact) = viewModelScope.launch {
        contactDao.delete(contact)
        contactsEventChannel.send(
            ContactsEvent.ShowUndoDeleteContactMessage(
                contact,
                R.string.contacts_delete
            )
        )
    }

    fun onUndoDeleteClick(contact: Contact) = viewModelScope.launch {
        contactDao.insert(contact)
    }

    fun onAddNewContactClick() = viewModelScope.launch {
        contactsEventChannel.send(ContactsEvent.NavigateToAddContactScreen)
    }

    fun onSettingsClick() = viewModelScope.launch {
        contactsEventChannel.send(ContactsEvent.NavigateToContactsSettingsScreen)
    }


    fun onAddEditResult(result: Int) {
        when (result) {
            ADD_CONTACT_RESULT_OK -> showContactSavedConfirmationMessage(
                R.string.contacts_added
            )
            EDIT_CONTACT_RESULT_OK -> showContactSavedConfirmationMessage(
                R.string.contacts_edited
            )
        }
    }

    private fun showContactSavedConfirmationMessage(stringId: Int) = viewModelScope.launch {
        contactsEventChannel.send(ContactsEvent.ShowContactSavedConfirmationMessage(stringId))
    }

    sealed class ContactsEvent {
        object NavigateToAddContactScreen : ContactsEvent()
        data class NavigateToViewContactScreen(val contact: Contact) : ContactsEvent()
        object NavigateToContactsSettingsScreen : ContactsEvent()
        data class ShowContactSavedConfirmationMessage(val stringId: Int) : ContactsEvent()
        data class ShowUndoDeleteContactMessage(val contact: Contact, val stringId: Int) :
            ContactsEvent()
    }

    /*
     * Remove all stored pictures not currently used by a contact
     */
    fun cleanupUnusedPictures(context: Context) = CoroutineScope(Dispatchers.IO).launch {
        val allUsedPictures: List<String> = contactDao.getAllPictures()
        val allStoredPictures = mutableListOf<String>()

        val pathname = context.filesDir.absoluteFile
        pathname.walk()
            .filter { it.isFile }
            .forEach { file ->
                if (file.extension == "jpg") {
                    allStoredPictures.add(file.name)
                }
            }

        val picturesToDelete: List<String> = allStoredPictures.minus(allUsedPictures)
        picturesToDelete.forEach { filename ->
            val file = File("$pathname/$filename")
            if (file.exists()) {
                file.delete()
            }
        }

    }
}