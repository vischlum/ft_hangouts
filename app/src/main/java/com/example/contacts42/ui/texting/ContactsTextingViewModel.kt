package com.example.contacts42.ui.texting

import android.content.*
import android.net.Uri
import android.provider.Telephony
import android.telephony.SmsManager
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.contacts42.data.Sms
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactsTextingViewModel @Inject constructor(
    state: SavedStateHandle
) : ViewModel() {
    val phoneNumber: String = state.get<String>("phoneNumber") ?: ""

    fun refreshSmsList(contentResolver: ContentResolver, smsList: ArrayList<Sms>) {
        val cleanNumber: String = phoneNumber.filter { !it.isWhitespace() }
        val cursor = contentResolver.query(
            Uri.parse("content://sms/"),
            null,
            "address=?",
            arrayOf(cleanNumber),
            "date ASC"
        )

        if (cursor == null || !cursor.moveToFirst())
            return

        val indexAddress: Int = cursor.getColumnIndex("address")
        val indexBody: Int = cursor.getColumnIndex("body")
        val indexDate: Int = cursor.getColumnIndex("date")
        val indexType: Int = cursor.getColumnIndex("type")

        do {
            smsList.add(
                Sms(
                    cursor.getString(indexAddress),
                    cursor.getString(indexBody),
                    cursor.getString(indexDate),
                    cursor.getInt(indexType)
                )
            )
        } while (cursor.moveToNext())
        cursor.close()
    }

    fun smsReceiver(
        smsList: ArrayList<Sms>,
        textingAdapter: ContactsTextingAdapter,
        context: Context
    ) {
        val br = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                for (sms in Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                    smsList.add(
                        Sms(
                            sms.displayOriginatingAddress,
                            sms.displayMessageBody,
                            sms.timestampMillis.toString(),
                            1
                        )
                    )
                }
                textingAdapter.notifyDataSetChanged()
            }
        }
        context.registerReceiver(
            br,
            IntentFilter("android.provider.Telephony.SMS_RECEIVED")
        )
    }

    fun sendSms(phoneNumber: String, message: String, smsList: ArrayList<Sms>) {
        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(
            phoneNumber,
            "ME",
            message,
            null,
            null
        )
        smsList.add(
            Sms(
                phoneNumber,
                message,
                System.currentTimeMillis().toString(),
                2
            )
        )
    }

    private val contactsTextingChannel = Channel<ContactsTextingEvent>()
    val contactsTextingEvents = contactsTextingChannel.receiveAsFlow()

    fun onSendTextClick(phoneNumber: String, message: String) = viewModelScope.launch {
        contactsTextingChannel.send(ContactsTextingEvent.ClickSendTextButton(phoneNumber, message))
    }

    sealed class ContactsTextingEvent {
        data class ClickSendTextButton(val phoneNumber: String, val message: String) :
            ContactsTextingEvent()
    }
}