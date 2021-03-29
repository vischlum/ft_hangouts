package com.example.contacts42.ui.texting

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.contacts42.data.Sms
import com.example.contacts42.databinding.ItemReceivedSmsBinding
import com.example.contacts42.databinding.ItemSentSmsBinding
import java.text.DateFormat

class ContactsTextingAdapter(
    val context: Context,
    private val smsList: ArrayList<Sms>
) : BaseAdapter() {
    override fun getCount(): Int {
        return smsList.size
    }

    override fun getItem(position: Int): Any {
        return smsList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val date = smsList[position].date.toLong()
        val formattedDate =
            DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(date)
                .toString()

        return if (smsList[position].type == 1) {
            val binding =
                ItemReceivedSmsBinding.inflate(LayoutInflater.from(context), parent, false)
            binding.itemReceivedSmsMessage.text = smsList[position].body
            binding.itemReceivedSmsDate.text = formattedDate
            binding.root
        } else {
            val binding = ItemSentSmsBinding.inflate(LayoutInflater.from(context), parent, false)
            binding.itemSentSmsMessage.text = smsList[position].body
            binding.itemSentSmsDate.text = formattedDate
            binding.root
        }
    }
}