package com.example.contacts42.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.contacts42.R
import com.example.contacts42.data.Contact
import com.example.contacts42.data.SortOrder
import com.example.contacts42.databinding.FragmentContactsBinding
import com.example.contacts42.util.exhaustive
import com.example.contacts42.util.onQueryTextChanged
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ContactsFragment : Fragment(R.layout.fragment_contacts), ContactsAdapter.OnItemClickListener {

    private val viewModel: ContactsViewModel by viewModels()

    private lateinit var searchView: SearchView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentContactsBinding.bind(view)

        val contactAdapter = ContactsAdapter(this)

        binding.apply {
            recyclerViewContacts.apply {
                adapter = contactAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }

            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.START or ItemTouchHelper.END
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val contact = contactAdapter.currentList[viewHolder.adapterPosition]
                    viewModel.onContactSwiped(contact)
                }
            }).attachToRecyclerView(recyclerViewContacts)

            fabAddContact.setOnClickListener {
                viewModel.onAddNewContactClick()
            }
        }

        setFragmentResultListener("add_edit_request") { _, bundle ->
            val result = bundle.getInt("add_edit_result")
            viewModel.onAddEditResult(result)
        }

        viewModel.contacts.observe(viewLifecycleOwner) {
            contactAdapter.submitList(it)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.contactsEvents.collect { event ->
                when (event) {
                    is ContactsViewModel.ContactsEvent.NavigateToAddContactScreen -> {
                        val action =
                            ContactsFragmentDirections.actionNavigationContactsToAddEditContactFragment(
                                null,
                                getString(R.string.contacts_add)
                            )
                        findNavController().navigate(action)
                    }
                    is ContactsViewModel.ContactsEvent.NavigateToViewContactScreen -> {
                        val action =
                            ContactsFragmentDirections.actionNavigationContactsToViewContactFragment(
                                event.contact
                            )
                        findNavController().navigate(action)
                    }
                    is ContactsViewModel.ContactsEvent.NavigateToContactsSettingsScreen -> {
                        val action =
                            ContactsFragmentDirections.actionNavigationContactsToContactsSettingsFragment()
                        findNavController().navigate(action)
                    }
                    is ContactsViewModel.ContactsEvent.ShowContactSavedConfirmationMessage -> {
                        Snackbar.make(requireView(), getText(event.stringId), Snackbar.LENGTH_SHORT)
                            .show()
                    }
                    is ContactsViewModel.ContactsEvent.ShowUndoDeleteContactMessage -> {
                        Snackbar.make(requireView(), getText(event.stringId), Snackbar.LENGTH_LONG)
                            .setAction(R.string.contacts_undo) {
                                viewModel.onUndoDeleteClick(event.contact)
                            }.show()
                    }
                }.exhaustive
            }
        }

        setHasOptionsMenu(true)
    }

    override fun onItemClick(contact: Contact) {
        viewModel.onContactSelected(contact)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_contacts, menu)

        val searchItem = menu.findItem(R.id.action_contacts_search)
        searchView = searchItem.actionView as SearchView

        val pendingQuery = viewModel.searchQuery.value
        if (pendingQuery != null && pendingQuery.isNotEmpty()) {
            searchItem.expandActionView()
            searchView.setQuery(pendingQuery, false)
        }

        searchView.onQueryTextChanged {
            viewModel.searchQuery.value = it
        }

        viewLifecycleOwner.lifecycleScope.launch {
            menu.findItem(R.id.action_contacts_show_only_fav).isChecked =
                viewModel.preferencesFlow.first().showOnlyFavorites
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_contacts_sort_by_name -> {
                viewModel.onSortOrderSelected(SortOrder.BY_NAME)
                true
            }
            R.id.action_contacts_sort_by_name_reverse -> {
                viewModel.onSortOrderSelected(SortOrder.BY_NAME_REVERSE)
                true
            }
            R.id.action_contacts_show_only_fav -> {
                item.isChecked = !item.isChecked
                viewModel.onShowOnlyFavoritesClick(item.isChecked)
                true
            }
            R.id.action_contacts_show_settings -> {
                viewModel.onSettingsClick()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchView.setOnQueryTextListener(null)
        viewModel.cleanupUnusedPictures(requireContext())
    }
}