package com.muham.petv01

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.muham.petv01.Accounts.FragmentAccountAdapter
import com.muham.petv01.Adapters.FragmentPageAdapter

class MainActivity : AppCompatActivity() {

    //Fragments

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var mainViewPager2: ViewPager2
    private lateinit var fragmentPageAdapter: FragmentPageAdapter

    private lateinit var signinViewPager2 : ViewPager2
    private lateinit var signinadapter : FragmentAccountAdapter

    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkUserLoggedIn()
    }

    private fun checkUserLoggedIn() {
        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser

        val isLoggedIn = currentUser != null

        if (isLoggedIn) {
            // User is logged in
            setContentView(R.layout.activity_main)

            bottomNavigationView = findViewById(R.id.mainBottomNav)
            mainViewPager2 = findViewById(R.id.pagerMain)
            fragmentPageAdapter = FragmentPageAdapter(supportFragmentManager, lifecycle)

            bottomNavigationView.setOnNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.itHome -> mainViewPager2.currentItem = 0
                    R.id.itForum -> mainViewPager2.currentItem = 1
                    R.id.itAccount -> mainViewPager2.currentItem = 2
                }
                true
            }

            mainViewPager2.adapter = fragmentPageAdapter

            mainViewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    when (position) {
                        0 -> bottomNavigationView.selectedItemId = R.id.itHome
                        1 -> bottomNavigationView.selectedItemId = R.id.itForum
                        2 -> bottomNavigationView.selectedItemId = R.id.itAccount
                    }
                }
            })
        } else {
            // User is not logged in
            setContentView(R.layout.signin_screen)

            if (!isNotificationSettingsOpenedBefore()) {
                openAppNotificationSettings()
                setNotificationSettingsOpened()
            }

            signinViewPager2 = findViewById(R.id.signinViewPager)
            signinadapter = FragmentAccountAdapter(supportFragmentManager, lifecycle)

            signinViewPager2.adapter = signinadapter
        }
    }

    private fun isNotificationSettingsOpenedBefore(): Boolean {
        val sharedPreferences = getPreferences(MODE_PRIVATE)
        return sharedPreferences.getBoolean("notification_settings_opened", false)
    }

    private fun setNotificationSettingsOpened() {
        val sharedPreferences = getPreferences(MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("notification_settings_opened", true)
        editor.apply()
    }

    private fun openAppNotificationSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        intent.data = Uri.fromParts("package", packageName, null)
        startActivity(intent)
    }
}