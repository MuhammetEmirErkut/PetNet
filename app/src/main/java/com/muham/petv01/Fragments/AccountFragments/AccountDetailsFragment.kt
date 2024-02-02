package com.muham.petv01.Fragments.AccountFragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.muham.petv01.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AccountDetailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AccountDetailsFragment : Fragment() {
    private lateinit var updateProfileButton: AppCompatButton
    private lateinit var editNameEditText: EditText
    private lateinit var editSurnameEditText: EditText
    private lateinit var editEmailEditText: EditText
    private lateinit var editPasswordEditText: EditText

    private lateinit var accountdetails_back_button: ImageView
    private lateinit var userPhotoChangeSpinner: Spinner
    private lateinit var userPhotoImageView: ImageView

    private val db = FirebaseFirestore.getInstance()
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

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
        val view = inflater.inflate(R.layout.fragment_account_details, container, false)

        accountdetails_back_button = view.findViewById(R.id.accountdetails_back_button)
        userPhotoChangeSpinner = view.findViewById(R.id.userPhotoChangeSpinner)
        userPhotoImageView = view.findViewById(R.id.userPhotoImageView)
        updateProfileButton = view.findViewById(R.id.updateProfileButton)
        editNameEditText = view.findViewById(R.id.editNameEditText)
        editSurnameEditText = view.findViewById(R.id.editSurnameEditText)
        editEmailEditText = view.findViewById(R.id.editEmailEditText)
        editPasswordEditText = view.findViewById(R.id.editPasswordEditText)




        val uid = FirebaseAuth.getInstance().currentUser?.uid

        // UID varsa ve Firestore'da ilgili belge varsa, mevcut bilgileri alın
        if (uid != null) {
            db.collection("Persons").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        // Firestore'dan alınan bilgileri kullanarak EditText'leri doldurun
                        editNameEditText.setText(document.getString("firstName"))
                        editSurnameEditText.setText(document.getString("lastName"))
                        editEmailEditText.setText(document.getString("email"))
                        editPasswordEditText.setText(document.getString("password"))

                        // İlgili pet tipine göre ImageView'ın kaynağını değiştirin
                        val petType = document.getString("petType")
                        if (petType != null) {
                            updateImageViewBasedOnPetType(petType)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("AccountDetailsFragment", "Error getting documents.", exception)
                }
        }

        updateProfileButton.setOnClickListener {
            updateProfile()
        }


        accountdetails_back_button.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val suggestions = arrayOf("G1", "B1", "B2", "G2")

        // ArrayAdapter'ı oluşturun ve AutoCompleteTextView'a bağlayın
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_selected_item, suggestions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        userPhotoChangeSpinner.adapter = adapter

        userPhotoChangeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View?, position: Int, id: Long) {
                // Seçilen değeri alın
                val selectedPetType = parentView.getItemAtPosition(position).toString()

                // Seçilen değere göre ImageView'ın kaynağını değiştirin
                updateImageViewBasedOnPetType(selectedPetType)
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                // Hiçbir şey seçilmediğinde yapılacak işlemler (isteğe bağlı)
            }
        }

        return view
    }
    private fun updateProfile() {
        // EditText'lerden bilgileri alın
        val newName = editNameEditText.text.toString()
        val newSurname = editSurnameEditText.text.toString()
        val newEmail = editEmailEditText.text.toString()
        val newPassword = editPasswordEditText.text.toString()
        val userPhotoSpinner = view?.findViewById<Spinner>(R.id.userPhotoChangeSpinner)
        val userPhoto = userPhotoSpinner?.selectedItem.toString()

        // FirebaseAuth ile giriş yapmış kullanıcının UID'sini alın
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        // UID varsa ve EditText'lerden alınan bilgiler boş değilse güncelleme işlemini yapın
        if (uid != null && newName.isNotBlank() && newSurname.isNotBlank() && newEmail.isNotBlank() && newPassword.isNotBlank()) {
            // Persons koleksiyonundaki ilgili belgeyi güncelle
            db.collection("Persons").document(uid)
                .update(
                    "firstName", newName,
                    "lastName", newSurname,
                    "email", newEmail,
                    "password", newPassword,
                    "userPhoto", userPhoto
                )
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Profil Güncellendi", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Profil Güncelleme Başarısız", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Eksik veya hatalı bilgi durumunda kullanıcıyı bilgilendirin
            Toast.makeText(requireContext(), "Bilgileri eksiksiz doldurun", Toast.LENGTH_SHORT).show()
        }
    }
    private fun updateImageViewBasedOnPetType(selectedPetType: String) {
        when (selectedPetType) {
            "B1" -> userPhotoImageView.setImageResource(R.drawable.b1char)
            "B2" -> userPhotoImageView.setImageResource(R.drawable.b2char)
            "G1" -> userPhotoImageView.setImageResource(R.drawable.g1char)
            "G2" -> userPhotoImageView.setImageResource(R.drawable.g2char)
            // Diğer seçeneklere göre ekleyebilirsiniz
            else -> userPhotoImageView.setImageResource(R.drawable.dog)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AccountDetailsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AccountDetailsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        if (enter) {
            return AnimationUtils.loadAnimation(activity, R.anim.slide_in_from_top)
        } else {
            return AnimationUtils.loadAnimation(activity, R.anim.slide_out_to_top)
        }
    }
}