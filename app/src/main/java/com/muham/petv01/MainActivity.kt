package com.muham.petv01

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
        // Mevcut kullanıcıyı al
        val currentUser = auth.currentUser

        // Kullanıcı mevcutsa ve oturum açıksa true döndür
        val isLoggedIn = currentUser != null

        // Sonuçları kontrol et
        if (isLoggedIn) {
            setContentView(R.layout.activity_main)


            bottomNavigationView = findViewById(R.id.mainBottomNav)
            mainViewPager2 = findViewById(R.id.pagerMain)
            fragmentPageAdapter = FragmentPageAdapter(supportFragmentManager, lifecycle)

            bottomNavigationView.setOnNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.itHome -> mainViewPager2.currentItem = 0
                    R.id.itForum -> mainViewPager2.currentItem = 1
                    R.id.itEnsyclopedia -> mainViewPager2.currentItem = 2
                    R.id.itAccount -> mainViewPager2.currentItem = 3
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
                        2 -> bottomNavigationView.selectedItemId = R.id.itEnsyclopedia
                        3 -> bottomNavigationView.selectedItemId = R.id.itAccount
                    }
                }
            })
        } else {
            setContentView(R.layout.signin_screen)

            signinViewPager2 = findViewById(R.id.signinViewPager)
            signinadapter = FragmentAccountAdapter(supportFragmentManager, lifecycle)

            signinViewPager2.adapter = signinadapter

        }
    }

}