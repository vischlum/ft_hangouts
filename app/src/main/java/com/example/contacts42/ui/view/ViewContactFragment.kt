package com.example.contacts42.ui.view

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.contacts42.R
import com.example.contacts42.databinding.FragmentViewContactBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

private const val TAG = "ViewContactFragment"

@AndroidEntryPoint
class ViewContactFragment : Fragment(R.layout.fragment_view_contact) {

    private val viewModel: ViewContactViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding: FragmentViewContactBinding = FragmentViewContactBinding.bind(view)

        binding.apply {
            if (viewModel.contactPicture.isNotBlank()) {
                requireContext().openFileInput(viewModel.contactPicture).use {
                    contactViewPicture.setImageBitmap(BitmapFactory.decodeStream(it))
                }
            }
            contactViewName.text = viewModel.contactName
            contactViewFav.isChecked = viewModel.contactFavorite
            contactViewPhoneHome.text = viewModel.contactPhoneHome
            contactViewPhoneWork.text = viewModel.contactPhoneWork
            contactViewEmailHome.text = viewModel.contactEmailHome
            contactViewEmailWork.text = viewModel.contactEmailWork

            contactViewPhoneHomeSms.setOnClickListener {
                viewModel.onSendTextClick(viewModel.contactPhoneHome)
            }
            contactViewPhoneWorkSms.setOnClickListener {
                viewModel.onSendTextClick(viewModel.contactPhoneWork)
            }

            contactViewPhoneHomeCall.setOnClickListener {
                viewModel.onStartCallClick(viewModel.contactPhoneHome)
            }
            contactViewPhoneWorkCall.setOnClickListener {
                viewModel.onStartCallClick(viewModel.contactPhoneWork)
            }

            contactViewEmailHomeSend.setOnClickListener {
                viewModel.onSendEmailClick(viewModel.contactEmailHome)
            }
            contactViewEmailWorkSend.setOnClickListener {
                viewModel.onSendEmailClick(viewModel.contactEmailWork)
            }

            fabEditContact.setOnClickListener {
                viewModel.onEditContactClick(viewModel.contact)
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.viewContactEvents.collect { event ->
                when (event) {
                    is ViewContactViewModel.ViewContactEvent.ClickSendTextButton -> {
                        val action =
                            ViewContactFragmentDirections.actionViewContactFragmentToContactsTextingFragment(
                                event.phoneNumber
                            )
                        findNavController().navigate(action)
                    }
                    is ViewContactViewModel.ViewContactEvent.ClickStartCallButton -> {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:" + event.phoneNumber)
                        }
                        try {
                            startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            Log.e(TAG, "ActivityNotFound Exception when trying to start phonecall")
                        }
                    }
                    is ViewContactViewModel.ViewContactEvent.ClickSendEmailButton -> {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:")
                            putExtra(Intent.EXTRA_EMAIL, arrayOf(event.emailAddress))
                        }
                        try {
                            startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            Log.e(TAG, "ActivityNotFound Exception when trying to send email")
                        }
                    }

                    is ViewContactViewModel.ViewContactEvent.NavigateToEditContactScreen -> {
                        val action =
                            ViewContactFragmentDirections.actionViewContactFragmentToAddEditContactFragment(
                                event.contact,
                                getString(R.string.contacts_edit)
                            )
                        findNavController().navigate(action)
                    }
                }
            }
        }

        // Hide TextView if they're empty
        binding.apply {
            when {
                contactViewPhoneHome.text.isBlank() && contactViewPhoneWork.text.isBlank() ->
                    view.findViewById<LinearLayout>(R.id.contact_view_phone_category).isVisible =
                        false
                contactViewPhoneHome.text.isBlank() ->
                    view.findViewById<LinearLayout>(R.id.contact_view_phone_home_category).isVisible =
                        false
                contactViewPhoneWork.text.isBlank() ->
                    view.findViewById<LinearLayout>(R.id.contact_view_phone_work_category).isVisible =
                        false
            }
            when {
                contactViewEmailHome.text.isBlank() && contactViewEmailWork.text.isBlank() ->
                    view.findViewById<LinearLayout>(R.id.contact_view_email_category).isVisible =
                        false
                contactViewEmailHome.text.isBlank() ->
                    view.findViewById<LinearLayout>(R.id.contact_view_email_home_category).isVisible =
                        false
                contactViewEmailWork.text.isBlank() ->
                    view.findViewById<LinearLayout>(R.id.contact_view_email_work_category).isVisible =
                        false
            }
        }
    }
}