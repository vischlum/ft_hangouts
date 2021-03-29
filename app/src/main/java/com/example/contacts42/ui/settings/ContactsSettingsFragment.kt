package com.example.contacts42.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.example.contacts42.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ContactsSettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.contacts_preferences, rootKey)
    }
}