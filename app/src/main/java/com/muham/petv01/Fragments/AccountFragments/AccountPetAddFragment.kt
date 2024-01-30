package com.muham.petv01.Fragments.AccountFragments

import android.content.ContentValues.TAG
import android.media.Image
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
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.muham.petv01.Components.RoundImageView
import com.muham.petv01.R
import java.util.UUID

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AccountPetAddFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AccountPetAddFragment : Fragment() {
    private lateinit var petNameEditText: EditText
    private lateinit var petAgeEditText: EditText
    private lateinit var petTypeEditText: Spinner

    private lateinit var petAddTextView: TextView

    private lateinit var petTypeImageView: RoundImageView

    private lateinit var petAddBackButton: ImageView
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
        val view = inflater.inflate(R.layout.fragment_account_pet_add, container, false)

        petNameEditText = view.findViewById(R.id.petNameEditText)
        petTypeEditText = view.findViewById(R.id.petTypeEditText)
        petAgeEditText = view.findViewById(R.id.petAgeEditText)

        petTypeImageView = view.findViewById(R.id.petTypeImageView)

        petAddBackButton = view.findViewById(R.id.petadd_backButton)
        petAddBackButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        petAddTextView = view.findViewById(R.id.addPetTextView)
        petAddTextView.setOnClickListener {
            addPet()
            parentFragmentManager.popBackStack()
        }

        val suggestions = arrayOf("Bird", "Dog", "Cat")

        // ArrayAdapter'ı oluşturun ve AutoCompleteTextView'a bağlayın
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_selected_item, suggestions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        petTypeEditText.setAdapter(adapter)

        petTypeEditText.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
    private fun updateImageViewBasedOnPetType(selectedPetType: String) {
        when (selectedPetType) {
            "Dog" -> petTypeImageView.setImageResource(R.drawable.dog)
            "Cat" -> petTypeImageView.setImageResource(R.drawable.cat)
            "Bird" -> petTypeImageView.setImageResource(R.drawable.bird)
            // Diğer seçeneklere göre ekleyebilirsiniz
            else -> petTypeImageView.setImageResource(R.drawable.bird)
        }
    }
    private fun addPet() {
        // Kullanıcının UID'sini al
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Eğer kullanıcı oturum açmışsa ve UID mevcutsa devam et
        userId?.let {
            // Firebase Firestore referansını al
            val db = FirebaseFirestore.getInstance()

            // EditText'lerden girişleri al
            val petName = petNameEditText.text.toString()
            val petAge = petAgeEditText.text.toString()
            val petTypeSpinner = view?.findViewById<Spinner>(R.id.petTypeEditText)
            val selectedPetType = petTypeSpinner?.selectedItem.toString()
            val petId = generatePetId()

            // Eğer bir pet adı girilmişse devam et
            if (petName.isNotEmpty()) {
                // Kişinin "pets" dizisine yeni bir evcil hayvan ekleyin
                val pet = hashMapOf(
                    "name" to petName,
                    "age" to petAge,
                    "type" to selectedPetType,
                    "id" to petId
                )

                // Firestore'daki belirli bir belgeyi güncelleyin veya oluşturun
                db.collection("Persons").document(userId).update("pets", FieldValue.arrayUnion(pet))
                    .addOnSuccessListener {
                        Log.w(TAG,"Pets added")
                    }
                    .addOnFailureListener {
                        Log.w(TAG,"Pets not added")
                    }
            }
        }
    }
    private fun generatePetId(): String {
        // UUID.randomUUID() kullanarak benzersiz bir ID oluşturun
        return UUID.randomUUID().toString()
    }
    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        if (enter) {
            return AnimationUtils.loadAnimation(activity, R.anim.slide_in_from_top)
        } else {
            return AnimationUtils.loadAnimation(activity, R.anim.slide_out_to_top)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AccountPetAddFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AccountPetAddFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}