package com.muham.petv01.Fragments

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.DatabaseReference
import com.muham.petv01.R
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment() {
    private lateinit var timerTextView: TextView
    private lateinit var giftImageView: ImageView
    private lateinit var countDownTimer: CountDownTimer
    private var timeLeftInMillis: Long = 0

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var userId: String

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

        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: ""
        database = FirebaseDatabase.getInstance().reference.child("users").child(userId)

        // Zamanlayıcıyı kontrol et
        checkTimer()

        giftImageView.setOnClickListener {
            // Kullanıcı hediye butonuna tıkladığında süreyi sıfırlayıp tekrar başlat
            resetAndStartTimer()
        }
    }

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
                    startTimer()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Hata durumunda işlemler
            }
        })
    }

    private fun resetAndStartTimer() {
        timeLeftInMillis = TOTAL_TIME_IN_MILLIS
        startTimer()

        // Firebase'e kalan süreyi kaydet
        database.child("millisLeft").setValue(System.currentTimeMillis() + timeLeftInMillis)
    }

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
