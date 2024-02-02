package com.muham.petv01.Fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.muham.petv01.R
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment() {
    private lateinit var timerTextView: TextView
    private lateinit var giftImageView: ImageView
    private lateinit var countDownTimer: CountDownTimer
    private var timeLeftInMillis: Long = 0
    private lateinit var sharedPreferences: SharedPreferences

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

        // SharedPreferences'u al
        sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)

        // Kalan süreyi kontrol et
        timeLeftInMillis = sharedPreferences.getLong("millisLeft", totalTimeInMillis)
        updateCountDownText()

        if (timeLeftInMillis != totalTimeInMillis) {
            startTimer()
        }

        giftImageView.setOnClickListener {
            timeLeftInMillis = totalTimeInMillis
            startTimer()
        }
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountDownText()
            }

            override fun onFinish() {
                timeLeftInMillis = totalTimeInMillis
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

    override fun onStop() {
        super.onStop()
        countDownTimer.cancel()
        val editor = sharedPreferences.edit()
        editor.putLong("millisLeft", timeLeftInMillis)
        editor.apply()
    }

    companion object {
        private const val totalTimeInMillis: Long = 24 * 60 * 60 * 1000
    }
}
