package com.muham.petv01.BottomSheets

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.muham.petv01.Fragments.AccountFragments.AccountDetailsFragment
import com.muham.petv01.Fragments.AccountFragments.ApplicationRulesFragment
import com.muham.petv01.MainActivity
import com.muham.petv01.R

class AccountSettingBottomSheetFragment: BottomSheetDialogFragment() {

    private lateinit var accountLogOutTextView: TextView
    private lateinit var rulesTextView: TextView
    private lateinit var accountDetailtsTextView: TextView
    companion object {
        const val TAG = "AccountSettingBottomSheetFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.accountsettings_bottomsheet_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        accountLogOutTextView = view.findViewById(R.id.accountLogOutTextView)
        accountDetailtsTextView = view.findViewById(R.id.accountDetailtsTextView)
        rulesTextView = view.findViewById(R.id.rulesTextView)

        accountLogOutTextView.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            dismiss()

            //Starting Main Activity
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        rulesTextView.setOnClickListener {
            dismiss()

            val applicationRulesFragment = ApplicationRulesFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.account_fragment_container, applicationRulesFragment)
                .addToBackStack("AccountFragment")
                .commit()

        }

        accountDetailtsTextView.setOnClickListener {
            dismiss()

            val accountDetailsFragment = AccountDetailsFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.account_fragment_container, accountDetailsFragment)
                .addToBackStack("AccountFragment")
                .commit()
        }
    }
}