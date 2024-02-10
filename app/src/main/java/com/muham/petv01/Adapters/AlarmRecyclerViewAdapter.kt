package com.muham.petv01.Adapters

import android.content.ContentValues.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.muham.petv01.Inheritance.Alarm
import com.muham.petv01.R

class AlarmRecyclerViewAdapter(private var alarmList: List<Alarm>) :
    RecyclerView.Adapter<AlarmRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.reminder_alarm_cell, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val alarm = alarmList[position]
        holder.bind(alarm)
    }

    override fun getItemCount(): Int {
        return alarmList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val alarmNameTextView: TextView = itemView.findViewById(R.id.alarmNameTextView)
        private val alarmTimeTextView: TextView = itemView.findViewById(R.id.alarmTimeTextView)
        private val deleteAlarmTextView: TextView = itemView.findViewById(R.id.deleteAlarmTextView)

        fun bind(alarm: Alarm) {
            alarmNameTextView.text = alarm.reminderName

            // Using SimpleDateFormat to convert time to a readable format
            val formattedTime = String.format("%02d:%02d", alarm.hour, alarm.minute)
            alarmTimeTextView.text = formattedTime

            // Adding onClickListener for deletion operation
            deleteAlarmTextView.setOnClickListener {
                // Access Firebase Realtime Database for deletion
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                userId?.let { uid ->
                    val alarmRef = FirebaseDatabase.getInstance().reference.child("reminders").child(uid)
                    println(alarm.key)
                    alarmRef.child(alarm.key).removeValue()
                        .addOnSuccessListener {
                            // Deletion successful
                            Log.d(TAG, "Alarm successfully deleted")
                            // Update RecyclerView
                            updateRecyclerViewAfterDeletion(alarm)
                        }
                        .addOnFailureListener { e ->
                            // Deletion failed
                            Log.e(TAG, "Error deleting alarm", e)
                        }
                }
            }
        }

        // Helper method to update RecyclerView after deletion
        private fun updateRecyclerViewAfterDeletion(deletedAlarm: Alarm) {
            val newList = alarmList.toMutableList()
            newList.remove(deletedAlarm)
            alarmList = newList.toList().toMutableList()
            notifyDataSetChanged()
        }

    }
}
