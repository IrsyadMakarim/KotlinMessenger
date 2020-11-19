package com.example.kotlinmessenger.registerLogin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.messages.LatestMessagesActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        login_btn.setOnClickListener {
            performLogin()
        }

        register_text_login.setOnClickListener {
            finish()
        }
    }

    private fun performLogin(){
        val email = email_editText_login.text.toString()
        val password = pass_editText_login.text.toString()

        Log.d("Login", "Attemp to Login with Email : $email and Password : $password")

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) {
                if (it.isSuccessful){
                    Log.d("Login", "Login Succesful")
                    Toast.makeText(this,"Login Successful",Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LatestMessagesActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }else{
                    Log.d("Login", "Login failed")
                    return@addOnCompleteListener
                }
            }
            .addOnFailureListener {
                Log.d("Login", "Failed to login user : ${it.message}")
                Toast.makeText(this,"Login Failed ${it.message}",Toast.LENGTH_SHORT).show()
            }
    }
}