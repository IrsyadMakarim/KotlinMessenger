package com.example.kotlinmessenger.registerLogin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.Toast
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.messages.LatestMessagesActivity
import com.example.kotlinmessenger.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

private lateinit var auth: FirebaseAuth

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = Firebase.auth

        register_btn.setOnClickListener {
           performRegister()
        }

        already_have_acc_register.setOnClickListener{
            Log.d("RegisterActivity", "Try to show login activity")

            //launch the login menu/activity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        regist_image_btn.setOnClickListener {
            Log.d("Register", "Try to show photo selector")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        btn_show_hide_pass.setOnClickListener {
            if(btn_show_hide_pass.text.toString().equals("Show")){
                password_editText_registration.transformationMethod = HideReturnsTransformationMethod.getInstance()
                btn_show_hide_pass.text = "Hide"
            } else{
                password_editText_registration.transformationMethod = PasswordTransformationMethod.getInstance()
                btn_show_hide_pass.text = "Show"
            }
        }
    }

    var selectedPhotoUri : Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            //Proceed and check what the selected image was...
            Log.d("RegisterActivity", "Photo was selected")

            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            regist_image_imageview.setImageBitmap(bitmap)

            regist_image_btn.alpha = 0f
        }
    }

    private fun performRegister() {
        val email = email_editText_registration.text.toString()
        val password = password_editText_registration.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter text in email or password", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("RegisterActivity", "Email is : $email")
        Log.d("RegisterActivity", "Password : $password")

        //Firebase Authentication to create a user with email and password
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) {
                if(it.isSuccessful) {
                    Log.d("Register","Succesfully created user with uid : ${it.result?.user?.uid}")
                    Toast.makeText(this, "Register Succesful",Toast.LENGTH_SHORT).show()

                    uploadImageToFirebaseStorage()
                }else{
                    Log.d("Register","Error at registration, password must be 6 characters long or more")
                    return@addOnCompleteListener
                }
            }
            .addOnFailureListener{
                Log.d("Register", "Failed to create user : ${it.message}")
                Toast.makeText(this,"Register Failed : ${it.message}",Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadImageToFirebaseStorage() {
        if (selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "Successfully Uploaded Image : ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {
                    Log.d("RegisterActivity", "File Location : $it")

                    saveUserToFirebaseDatabase(it.toString())
                }
            }
            .addOnFailureListener {
                Log.d("RegisterActivity","Error : ${it.message}")
            }
    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?:""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(
            uid,
            username_editText_registration.text.toString(),
            profileImageUrl,
            password_editText_registration.text.toString()
        )

        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "Finally we saved the user to Firebase Database")

                val intent = Intent(this, LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Log.d("RegisterActivity","Error : ${it.message}")
            }
    }
}

//class User(val uid : String, val username : String, val profileImageUrl : String) {
//    constructor() : this("","","")
//}