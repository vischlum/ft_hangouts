package com.example.contacts42.ui.addedit

import android.Manifest
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.contacts42.BuildConfig
import com.example.contacts42.R
import com.example.contacts42.databinding.FragmentAddEditContactBinding
import com.example.contacts42.ui.MainActivity
import com.example.contacts42.util.exhaustive
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import java.io.File

const val TAG = "AddEditContact Fragment"

@AndroidEntryPoint
class AddEditContactFragment : Fragment(R.layout.fragment_add_edit_contact) {

    private val viewModel: AddEditContactViewModel by viewModels()

    private lateinit var requestCameraPermission: ActivityResultLauncher<String>
    private lateinit var requestStoragePermission: ActivityResultLauncher<String>
    private lateinit var getImageFromCamera: ActivityResultLauncher<Uri>
    private lateinit var getImageFromStorage: ActivityResultLauncher<String>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding: FragmentAddEditContactBinding = FragmentAddEditContactBinding.bind(view)

        binding.apply {
            if (viewModel.contactPicture.isNotBlank()) {
                Log.d(TAG, "contactPicture isNotBlank = ${viewModel.contactPicture}")
                requireContext().openFileInput(viewModel.contactPicture).use {
                    contactEditPicture.setImageBitmap(BitmapFactory.decodeStream(it))
                }
            }
            contactEditName.setText(viewModel.contactName)
            contactEditFav.isChecked = viewModel.contactFavorite
            contactEditFav.jumpDrawablesToCurrentState()
            contactEditPhoneHome.setText(viewModel.contactPhoneHome)
            contactEditPhoneWork.setText(viewModel.contactPhoneWork)
            contactEditEmailHome.setText(viewModel.contactEmailHome)
            contactEditEmailWork.setText(viewModel.contactEmailWork)

            contactEditName.addTextChangedListener {
                viewModel.contactName = it.toString()
            }

            contactEditFav.setOnCheckedChangeListener { _, isChecked ->
                viewModel.contactFavorite = isChecked
            }

            contactEditPhoneHome.addTextChangedListener {
                viewModel.contactPhoneHome = it.toString()
            }

            contactEditPhoneWork.addTextChangedListener {
                viewModel.contactPhoneWork = it.toString()
            }

            contactEditEmailHome.addTextChangedListener {
                viewModel.contactEmailHome = it.toString()
            }

            contactEditEmailWork.addTextChangedListener {
                viewModel.contactEmailWork = it.toString()
            }

            contactEditPicture.setOnClickListener {
                showDialog()
            }

            fabSaveContact.setOnClickListener {
                viewModel.onSaveClick()
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addEditContactEvent.collect { event ->
                when (event) {
                    is AddEditContactViewModel.AddEditContactEvent.ShowInvalidInputMessage -> {
                        Snackbar.make(requireView(), getText(event.stringId), Snackbar.LENGTH_LONG)
                            .show()
                    }
                    is AddEditContactViewModel.AddEditContactEvent.NavigateBackWithResult -> {
                        binding.contactEditName.clearFocus()
                        setFragmentResult(
                            "add_edit_request",
                            bundleOf("add_edit_result" to event.result)
                        )
                        findNavController().navigate(R.id.navigation_contacts)
                    }
                }.exhaustive
            }
        }

        requestCameraPermission =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                Log.d(TAG, "requestCameraPermission: isGranted = $isGranted")
                when {
                    isGranted -> launchCamera()
                    shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) ->
                        Snackbar.make(
                            requireView(),
                            getText(R.string.contacts_camera_permission_denied),
                            Snackbar.LENGTH_LONG
                        ).show()
                    else -> Snackbar.make(
                        requireView(),
                        getText(R.string.contacts_camera_permission_denied_permanent),
                        Snackbar.LENGTH_LONG
                    )
                        .setAction(getText(R.string.contacts_permission_fix)) { (activity as MainActivity).showAppInfo() }
                        .show()
                }
            }

        requestStoragePermission =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                Log.d(TAG, "requestStoragePermission: isGranted = $isGranted")
                when {
                    isGranted -> getImageFromStorage.launch("image/*")
                    shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) ->
                        Snackbar.make(
                            requireView(),
                            getText(R.string.contacts_storage_permission_denied),
                            Snackbar.LENGTH_LONG
                        ).show()
                    else -> Snackbar.make(
                        requireView(),
                        getText(R.string.contacts_storage_permission_denied_permanent),
                        Snackbar.LENGTH_LONG
                    )
                        .setAction(getText(R.string.contacts_permission_fix)) { (activity as MainActivity).showAppInfo() }
                        .show()
                }

            }

        getImageFromCamera =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { pictureSaved: Boolean ->
                Log.d(TAG, "getImageFromCamera: pictureSaved = $pictureSaved")
                if (pictureSaved) {
                    val uri =
                        Uri.fromFile(File(requireContext().filesDir.toString() + "/" + viewModel.contactPicture))
                    Log.d(TAG, "getImageFromCamera: Uri = $uri")

                    //Resize picture from camera
                    viewModel.saveImageFile(uri, requireContext(), viewModel.contactPicture)

                    binding.contactEditPicture.setImageURI(uri)
                } else {
                    viewModel.contactPicture = ""
                }
            }

        getImageFromStorage =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                Log.d(TAG, "getImageFromStorage: uri = $uri")
                if (uri != null) {
                    // This Uri only has a short access lifetime so we need to copy the image
                    val fileName: String = System.currentTimeMillis().toString() + ".jpg"
                    viewModel.saveImageFile(uri, requireContext(), fileName)
                    viewModel.contactPicture = fileName

                    // Using this Uri causes problems if the picture is too big, so we use our resized copy
                    Thread.sleep(1000)
                    requireContext().openFileInput(viewModel.contactPicture).use {
                        binding.contactEditPicture.setImageBitmap(BitmapFactory.decodeStream(it))
                    }
                }
            }
    }

    private fun showDialog() {
        val alertDialogBuilder = android.app.AlertDialog.Builder(context)
        alertDialogBuilder.setTitle(R.string.contact_picture_add)
        alertDialogBuilder.setItems(R.array.contact_picture_source) { _, which ->
            Log.d(TAG, "showDialog: which = $which")
            when (which) {
                0 -> requestCameraPermission.launch(Manifest.permission.CAMERA)
                1 -> requestStoragePermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        alertDialogBuilder.show()
    }

    private fun launchCamera() {
        val fileName: String = System.currentTimeMillis().toString() + ".jpg"
        val file = File(requireContext().filesDir, fileName)
        val uri: Uri = FileProvider.getUriForFile(
            requireContext(),
            BuildConfig.APPLICATION_ID + ".provider",
            file
        )

        viewModel.contactPicture = fileName
        Log.d(TAG, "launchCamera: Uri = $uri")
        getImageFromCamera.launch(uri)
    }
}