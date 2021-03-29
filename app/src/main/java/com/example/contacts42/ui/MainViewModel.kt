package com.example.contacts42.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.contacts42.data.DatastoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.DateFormat
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val datastoreManager: DatastoreManager
) : ViewModel() {

    fun onAppPaused(timeWhenPaused: Long) = viewModelScope.launch {
        datastoreManager.updateTimeWhenPaused(timeWhenPaused)
    }

    suspend fun getTime(): String {
        val time = datastoreManager.getTimeWhenPaused().first()
        return DateFormat.getDateTimeInstance().format(time).toString()
    }
}