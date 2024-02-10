package com.muham.petv01.Fragments.HomeFragments

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.muham.petv01.Adapters.AlarmRecyclerViewAdapter
import com.muham.petv01.AlarmReceiver
import com.muham.petv01.Fragments.HomeFragment
import com.muham.petv01.Inheritance.Alarm
import com.muham.petv01.R
import com.muham.petv01.R.id.remindersRecyclerView
import java.util.*

class AddReminderFragment : Fragment() {
    private lateinit var addreminder_back_button: ImageView
    private lateinit var petReminderSpinner: Spinner
    private lateinit var reminderNameEditText: EditText
    private lateinit var timePicker: TimePicker

    private val firebaseAuth = FirebaseAuth.getInstance()
    private lateinit var database: DatabaseReference

    private lateinit var reminderRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_reminder, container, false)

        addreminder_back_button = view.findViewById(R.id.addreminder_back_button)
        petReminderSpinner = view.findViewById(R.id.petReminderSpinner)
        reminderNameEditText = view.findViewById(R.id.reminderNameEditText)
        timePicker = view.findViewById(R.id.timePicker)

        addreminder_back_button.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Get Firebase database reference
        database = FirebaseDatabase.getInstance().reference

        fetchPetForSpinner()

        val setAlarmButton = view.findViewById<Button>(R.id.setAlarmButton)
        setAlarmButton.setOnClickListener {
            onSetAlarmButtonClicked(it)
        }

        return view
    }

    private fun fetchPetForSpinner(){
        // Firestore connection
        val db = FirebaseFirestore.getInstance()

        val userId = firebaseAuth.currentUser?.uid

        // Fetch pets from Firestore
        if (userId != null) {
            db.collection("Persons")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val petList = ArrayList<String>()

                        val pets = document.get("pets") as ArrayList<HashMap<String, Any>>

                        for (pet in pets) {
                            val name = pet["name"] as String
                            petList.add(name)
                        }

                        // Add pet list to the Spinner
                        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
                            requireContext(),
                            R.layout.spinner_selected_item,
                            petList
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        petReminderSpinner.adapter = adapter
                    } else {
                        Log.d(TAG, "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                }
        }
    }

    private fun saveReminderToFirebase(selectedPetName: String, reminderName: String, hour: Int, minute: Int) {
        // Save the reminder to Firebase database
        val userId = firebaseAuth.currentUser?.uid
        val remindersRef = FirebaseDatabase.getInstance().reference.child("reminders").child(userId ?: "")

        // Generate a new unique key (ID)
        val reminderKey = remindersRef.push().key

        val reminder = Alarm(reminderName, selectedPetName, hour, minute, reminderKey ?: "")

        // Save the reminder under the generated key
        reminderKey?.let {
            remindersRef.child(it).setValue(reminder)
                .addOnSuccessListener {
                    // Data saved successfully
                    Log.d(TAG, "Reminder saved successfully")

                    // Add the reminder to the RecyclerView
                    // updateRecyclerViewWithNewReminder(reminder)

                    // Set the alarm using the key and other information
                    setAlarm(hour, minute)
                }
                .addOnFailureListener { e ->
                    // Handle the failure
                    Log.e(TAG, "Error saving reminder", e)
                }
        }
    }

    /* private fun updateRecyclerViewWithNewReminder(reminder: Alarm) {
         val adapter = (recyclerView.adapter as? AlarmRecyclerViewAdapter)
         adapter?.addItem(reminder)
     }
     */


    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        return if (enter) {
            AnimationUtils.loadAnimation(activity, R.anim.slide_in_from_top)
        } else {
            AnimationUtils.loadAnimation(activity, R.anim.slide_out_to_top)
        }
    }

    private fun setAlarm(hour: Int, minute: Int) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(requireContext(), AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(requireContext(), 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )

        Log.d(TAG, "Daily alarm set for $hour:$minute")
    }

    // Operations to be performed when the button is clicked
    fun onSetAlarmButtonClicked(view: View) {
        val selectedPetName = petReminderSpinner.selectedItem.toString()
        val feedingTime = "Feeding time for $selectedPetName"

        // Get the selected hour and minute
        val hour = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            timePicker.hour
        } else {
            timePicker.currentHour
        }
        val minute = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            timePicker.minute
        } else {
            timePicker.currentMinute
        }

        // Set the alarm
        setAlarm(hour, minute)

        // Update the EditText
        reminderNameEditText.setText(feedingTime)

        saveReminderToFirebase(selectedPetName, feedingTime, hour, minute)

        parentFragmentManager.popBackStack()
    }

}
