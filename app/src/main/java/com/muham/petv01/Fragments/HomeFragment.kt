package com.muham.petv01.Fragments

import android.content.ContentValues.TAG
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.muham.petv01.Adapters.AlarmRecyclerViewAdapter
import com.muham.petv01.Fragments.HomeFragments.AddReminderFragment
import com.muham.petv01.Inheritance.Alarm
import com.muham.petv01.R
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment() {
    private lateinit var timerTextView: TextView
    private lateinit var giftImageView: ImageView
    private lateinit var petPointTextView: TextView
    private lateinit var addReminderTextView: TextView
    private lateinit var giftLinearLayout: LinearLayout
    private lateinit var countDownTimer: CountDownTimer
    private var timeLeftInMillis: Long = 0

    private lateinit var leaderboardTextView1: TextView
    private lateinit var leaderboardTextView2: TextView
    private lateinit var leaderboardTextView3: TextView
    private lateinit var leaderboardTextView4: TextView
    private lateinit var leaderboardTextView5: TextView

    private lateinit var reminderRecyclerView: RecyclerView

    private lateinit var leaderBoardRefreshTextView: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var userId: String
    private var isRewardClaimed: Boolean = false
    private var isButtonClickable = false

    private val alarmList = ArrayList<Alarm>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        timerTextView = view.findViewById(R.id.timerTextView)
        giftImageView = view.findViewById(R.id.giftImageView)
        giftLinearLayout = view.findViewById(R.id.giftLinearLayout)
        petPointTextView = view.findViewById(R.id.petPointTextView)
        addReminderTextView = view.findViewById(R.id.addReminderTextView)
        reminderRecyclerView = view.findViewById(R.id.remindersRecyclerView)

        leaderboardTextView1 = view.findViewById(R.id.leaderboardTextView1)
        leaderboardTextView2 = view.findViewById(R.id.leaderboardTextView2)
        leaderboardTextView3 = view.findViewById(R.id.leaderboardTextView3)
        leaderboardTextView4 = view.findViewById(R.id.leaderboardTextView4)
        leaderboardTextView5 = view.findViewById(R.id.leaderboardTextView5)

        leaderBoardRefreshTextView = view.findViewById(R.id.leaderBoardRefreshTextView)

        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: ""
        database = FirebaseDatabase.getInstance().reference.child("users").child(userId)

        fetchLeaderboardData()

        Handler().postDelayed({
            // Enable button clickability after two seconds
            isButtonClickable = true
        }, 2000)

        database.child("PetPoints").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentPetPoints = snapshot.getValue(Long::class.java) ?: 0
                updatePetPointsTextView(currentPetPoints)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors
            }
        })

        // Check the timer
        checkTimer()

        giftLinearLayout.setOnClickListener {
            if (!isRewardClaimed && isButtonClickable) {
                // If the reward has not been claimed yet, reset and start the timer
                resetAndStartTimer()

                // Save the remaining time to Firebase
                database.child("millisLeft").setValue(System.currentTimeMillis() + timeLeftInMillis)

                // Mark the reward as claimed
                isRewardClaimed = true

                // Disable and dim the GiftLinearLayout
                giftLinearLayout.isEnabled = false
                giftLinearLayout.alpha = 0.5f

                increasePetPointsBy1000()
            } else {
                // If the reward has been claimed before, show a message to the user
                Toast.makeText(requireContext(), "Reward Claimed.", Toast.LENGTH_SHORT).show()
            }
        }

        addReminderTextView.setOnClickListener {
            val addReminderFragment = AddReminderFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.home_fragment_container, addReminderFragment)
                .addToBackStack("HomeFragment")
                .commit()
        }

        leaderBoardRefreshTextView.setOnClickListener {
            fetchLeaderboardData()
        }

        // Create the RecyclerView
        fetchRemindersFromFirebase()
    }

    // Function to update the RecyclerView with reminders
    private fun updateRecyclerViewWithReminders(alarms: List<Alarm>) {
        reminderRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        val adapter = AlarmRecyclerViewAdapter(alarms)
        reminderRecyclerView.adapter = adapter
    }

    // Function to fetch reminders from Firebase
    private fun fetchRemindersFromFirebase() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val remindersRef = FirebaseDatabase.getInstance().reference.child("reminders").child(userId ?: "")

        remindersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val reminders = mutableListOf<Alarm>()
                for (reminderSnapshot in snapshot.children) {
                    val petName = reminderSnapshot.child("petName").getValue(String::class.java)
                    val reminderName = reminderSnapshot.child("reminderName").getValue(String::class.java)
                    val hour = reminderSnapshot.child("hour").getValue(Int::class.java)
                    val minute = reminderSnapshot.child("minute").getValue(Int::class.java)

                    if (petName != null && reminderName != null && hour != null && minute != null) {
                        val key = reminderSnapshot.key
                        val reminder = key?.let { Alarm(reminderName, petName, hour, minute, it) }
                        if (reminder != null) {
                            reminders.add(reminder)
                        }
                    }
                }
                // Update the RecyclerView
                updateRecyclerViewWithReminders(reminders)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors
            }
        })
    }

    // Function to fetch leaderboard data
    private fun fetchLeaderboardData() {
        val leaderboardQuery = database.parent?.orderByChild("PetPoints")?.limitToLast(5)
        clearLeaderboardTextViews()
        leaderboardQuery?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.reversed().forEachIndexed { index, userSnapshot ->
                    val userId = userSnapshot.key
                    val petPoints = userSnapshot.child("PetPoints").getValue(Long::class.java) ?: 0

                    if (userId != null) {
                        fetchUserData(userId, petPoints, index)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors
            }
        })
    }

    // Function to fetch user data for leaderboard
    private fun fetchUserData(userId: String, petPoints: Long, index: Int) {
        val firestoreDB = FirebaseFirestore.getInstance()
        val userRef = firestoreDB.collection("Persons").document(userId)

        userRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val firstName = document.getString("firstName") ?: ""
                    val lastName = document.getString("lastName") ?: ""
                    val fullName = "$firstName ${abbreviateLastName(lastName)}"

                    // Send the index to the new function
                    setLeaderboardTextView(fullName, petPoints, index)
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }

    // Function to abbreviate last name
    private fun abbreviateLastName(lastName: String): String {
        return if (lastName.isNotEmpty()) {
            "${lastName.first()}***"
        } else {
            ""
        }
    }

    // Function to set leaderboard text view based on index
    private fun setLeaderboardTextView(fullName: String, petPoints: Long, index: Int) {
        when (index) {
            0 -> leaderboardTextView1.text = "$fullName - $petPoints PetPoints"
            1 -> leaderboardTextView2.text = "$fullName - $petPoints PetPoints"
            2 -> leaderboardTextView3.text = "$fullName - $petPoints PetPoints"
            3 -> leaderboardTextView4.text = "$fullName - $petPoints PetPoints"
            4 -> leaderboardTextView5.text = "$fullName - $petPoints PetPoints"
        }
    }

    // Function to clear leaderboard text views
    private fun clearLeaderboardTextViews() {
        leaderboardTextView1.text = ""
        leaderboardTextView2.text = ""
        leaderboardTextView3.text = ""
        leaderboardTextView4.text = ""
        leaderboardTextView5.text = ""
    }

    // Function to increase PetPoints by 1000
    private fun increasePetPointsBy1000() {
        database.child("PetPoints").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentPetPoints = snapshot.getValue(Long::class.java) ?: 0

                if (isRewardClaimed) {
                    val newPetPoints = currentPetPoints + 1000
                    database.child("PetPoints").setValue(newPetPoints)

                    isRewardClaimed = true

                    updatePetPointsTextView(newPetPoints)

                    Log.d("PetPoints", "PetPoints increased by 1000")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors
            }
        })
    }

    // Function to update PetPoints TextView
    private fun updatePetPointsTextView(newPetPoints: Long) {
        petPointTextView.text = "PetPoints: $newPetPoints"
    }

    // Function to check the timer
    private fun checkTimer() {
        database.child("millisLeft").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val storedTime = snapshot.getValue(Long::class.java) ?: 0
                timeLeftInMillis = if (storedTime > System.currentTimeMillis()) {
                    storedTime - System.currentTimeMillis()
                } else {
                    0
                }

                if (timeLeftInMillis > 0) {
                    giftLinearLayout.isEnabled = false
                    giftLinearLayout.alpha = 0.5f

                    startTimer()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors
            }
        })
    }

    // Function to reset and start the timer
    private fun resetAndStartTimer() {
        timeLeftInMillis = TOTAL_TIME_IN_MILLIS
        startTimer()

        isRewardClaimed = false

        database.child("millisLeft").setValue(System.currentTimeMillis() + timeLeftInMillis)
    }

    // Function to start the timer
    private fun startTimer() {
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountDownText()
            }

            override fun onFinish() {
                timeLeftInMillis = 0
                updateCountDownText()
            }
        }.start()
    }

    // Function to update the countdown text
    private fun updateCountDownText() {
        val hours = TimeUnit.MILLISECONDS.toHours(timeLeftInMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeLeftInMillis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeLeftInMillis) % 60

        val timeLeftFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        timerTextView.text = timeLeftFormatted
    }

    companion object {
        private const val TOTAL_TIME_IN_MILLIS: Long = 24 * 60 * 60 * 1000
    }
}
