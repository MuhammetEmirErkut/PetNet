package com.muham.petv01.Accounts

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.muham.petv01.MainActivity
import com.muham.petv01.R

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SigninFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var nameEditText: EditText
    private lateinit var surnameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    private lateinit var auth: FirebaseAuth

    private val TAG = "SigninFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_signin, container, false)

        nameEditText = view.findViewById(R.id.signinNameEditText)
        surnameEditText = view.findViewById(R.id.signinSurnameEditText)
        emailEditText = view.findViewById(R.id.signinEmailEditText)
        passwordEditText = view.findViewById(R.id.signinPasswordEditText)

        val nextButton = view.findViewById<Button>(R.id.signinButton)
        nextButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val surname = surnameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Firebase Authentication
            auth = FirebaseAuth.getInstance()

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val uid = user?.uid

                        // Firestore veri ekleme
                        val db = Firebase.firestore
                        val person = hashMapOf(
                            "userId" to uid,
                            "firstName" to name,
                            "lastName" to surname,
                            "email" to email,
                            "password" to password
                        )

                        db.collection("Persons").document(uid!!)
                            .set(person)
                            .addOnSuccessListener {
                                Log.d(TAG, "DocumentSnapshot added with ID: $uid")

                                val intent = Intent(requireContext(), MainActivity::class.java)
                                startActivity(intent)
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Error adding document", e)
                            }
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    }
                }

            val viewPager = activity?.findViewById<ViewPager2>(R.id.signinViewPager)
            viewPager?.currentItem = 1
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SigninFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

