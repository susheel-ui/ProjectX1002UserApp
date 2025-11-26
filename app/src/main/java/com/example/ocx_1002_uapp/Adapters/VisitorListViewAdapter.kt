package com.example.ocx_1002_uapp.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.ocx_1002_uapp.RequestActivity
import com.example.ocx_1002_uapp.databinding.VisitorListLayoutBinding
import com.example.project_b_security_gardapp.api.Entities.RequestsResultEntity
import java.util.Calendar
import java.util.concurrent.TimeUnit

class VisitorListViewAdapter(
    val context: Context,
    private var visitorList: List<RequestsResultEntity>
) : Adapter<VisitorListViewAdapter.viewHolder>() {
    class viewHolder(val binding: VisitorListLayoutBinding) : ViewHolder(binding.root) {
        fun bind(guest: RequestsResultEntity) {
            binding.tvGuestName.text = guest.guestName
//            val timeAgoTxt = getTimeAgo(guest.createdAt.toString())
//            binding.tvTimestamp.text = timeAgoTxt
            binding.tvGuestType.text = guest.description

        }

        fun getTimeAgo(createdAt: String): String {
            val parts = createdAt.removeSurrounding("[", "]").split(",").map { it.trim().toInt() }
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, parts[0])
                set(Calendar.MONTH, parts[1] - 1)
                set(Calendar.DAY_OF_MONTH, parts[2])
                set(Calendar.HOUR_OF_DAY, parts[3])
                set(Calendar.MINUTE, parts[4])
                set(Calendar.SECOND, parts[5])
                set(Calendar.MILLISECOND, parts[6] / 1_000_000)
            }

            val diffMillis = System.currentTimeMillis() - cal.timeInMillis
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis)
            val hours = TimeUnit.MILLISECONDS.toHours(diffMillis)
            val days = TimeUnit.MILLISECONDS.toDays(diffMillis)

            return when {
                minutes < 1 -> "Just now"
                minutes < 60 -> "$minutes min ago"
                hours < 24 -> "$hours hour${if (hours > 1) "s" else ""} ago"
                else -> "$days day${if (days > 1) "s" else ""} ago"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = VisitorListLayoutBinding.inflate(inflater, parent, false)
        return viewHolder(binding)
    }

    override fun getItemCount(): Int {
        return visitorList.size // here we will return the size of the arraylist
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        val currentGuest = visitorList[position]
        holder.itemView.setOnClickListener {
            val intent = Intent(context, RequestActivity::class.java)
            intent.putExtra("Id", currentGuest.id.toString())
            context.startActivity(intent)
        }
        holder.bind(currentGuest)
    }

    fun updateData(newList: List<RequestsResultEntity>) {
        visitorList = newList
        notifyDataSetChanged()
    }


}