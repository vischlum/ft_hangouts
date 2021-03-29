package com.example.contacts42.ui.view

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.contacts42.data.Contact
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewContactViewModel @Inject constructor(
    private val state: SavedStateHandle
) : ViewModel() {
    val contact = state.get<Contact>("contact")

    var contactPicture = state.get<String>("contactPicture") ?: contact?.picture_filename ?: ""
        set(value) {
            field = value
            state.set("contactPicture", value)
        }

    var contactName = state.get<String>("contactName") ?: contact?.name
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
            state.set("contactPhoneWork", value)
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

    private val viewContactChannel = Channel<ViewContactEvent>()
    val viewContactEvents = viewContactChannel.receiveAsFlow()

    fun onSendTextClick(phoneNumber: String) = viewModelScope.launch {
        viewContactChannel.send(ViewContactEvent.ClickSendTextButton(phoneNumber))
    }

    fun onStartCallClick(phoneNumber: String) = viewModelScope.launch {
        viewContactChannel.send(ViewContactEvent.ClickStartCallButton(phoneNumber))
    }

    fun onSendEmailClick(emailAddress: String) = viewModelScope.launch {
        viewContactChannel.send(ViewContactEvent.ClickSendEmailButton(emailAddress))
    }

    fun onEditContactClick(contact: Contact?) = viewModelScope.launch {
        viewContactChannel.send(ViewContactEvent.NavigateToEditContactScreen(contact))
    }

    sealed class ViewContactEvent {
        data class ClickSendTextButton(val phoneNumber: String) : ViewContactEvent()
        data class ClickStartCallButton(val phoneNumber: String) : ViewContactEvent()
        data class ClickSendEmailButton(val emailAddress: String) : ViewContactEvent()
        data class NavigateToEditContactScreen(val contact: Contact?) : ViewContactEvent()
    }
}