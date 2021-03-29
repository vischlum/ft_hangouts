package com.example.contacts42.ui.texting

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.ListView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.contacts42.R
import com.example.contacts42.data.Sms
import com.example.contacts42.databinding.FragmentTextingBinding
import com.example.contacts42.ui.MainActivity
import com.example.contacts42.util.exhaustive
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

const val TAG = "Texting"

@AndroidEntryPoint
class ContactsTextingFragment : Fragment(R.layout.fragment_texting) {
    private val viewModel: ContactsTextingViewModel by viewModels()

    private lateinit var requestSmsPermissions: ActivityResultLauncher<Array<String>>

    private val smsList = ArrayList<Sms>()
    private lateinit var textingAdapter: ContactsTextingAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding: FragmentTextingBinding = FragmentTextingBinding.bind(view)

        binding.apply {
            contactTextingSend.setOnClickListener {
                if (contactTextingWrite.text.isNotEmpty()) {
                    viewModel.onSendTextClick(
                        viewModel.phoneNumber,
                        contactTextingWrite.text.toString()
                    )
                }
                contactTextingWrite.text.clear()
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.contactsTextingEvents.collect { event ->
                when (event) {
                    is ContactsTextingViewModel.ContactsTextingEvent.ClickSendTextButton -> {
                        viewModel.sendSms(event.phoneNumber, event.message, smsList)
                        textingAdapter.notifyDataSetChanged()
                    }
                }.exhaustive
            }
        }

        requestSmsPermissions =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsMap ->
                if (permissionsMap.containsValue(false)) {
                    Log.d(TAG, "requestSmsPermissions: Permissions are not OK")
                    val writingLayout: LinearLayout =
                        view.findViewById(R.id.contact_texting_writing_zone)
                    writingLayout.visibility = View.GONE
                    Snackbar.make(
                        requireView(),
                        getText(R.string.contacts_sms_permission_denied_permanent),
                        Snackbar.LENGTH_LONG
                    )
                        .setAction(getText(R.string.contacts_permission_fix)) { (activity as MainActivity).showAppInfo() }
                        .show()
                } else {
                    Log.d(TAG, "requestSmsPermissions: Permissions are OK")
                    loadSmsList(view)
                    viewModel.smsReceiver(smsList, textingAdapter, requireContext())
                }
            }

        requestSmsPermissions.launch(
            arrayOf(
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.SEND_SMS
            )
        )
    }

    private fun loadSmsList(view: View) {
        viewModel.refreshSmsList(requireContext().contentResolver, smsList)
        val messages = view.findViewById<ListView>(R.id.texting_list)
        textingAdapter = ContactsTextingAdapter(requireContext(), smsList)
        messages.adapter = textingAdapter
    }
}