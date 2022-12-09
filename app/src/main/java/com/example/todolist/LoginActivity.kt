package com.example.todolist

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = Firebase.auth

        findViewById<Button>(R.id.signUpButton).setOnClickListener { createPasswordSignUp() }
        findViewById<Button>(R.id.signInButton).setOnClickListener { createPasswordSignIn() }
    }

    private fun createPasswordSignUp() {
        val email = findViewById<EditText>(R.id.emailEditText).text.toString()
        val password = findViewById<EditText>(R.id.passwordEditText).text.toString()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("MainActivity", "sign up successful: ${auth.currentUser?.uid}")
                    launchMainActivity()
                }
                else {
                    Log.d("MainActivity", task.exception.toString())
                }
            }
    }

    private fun createPasswordSignIn() {
        val email = findViewById<EditText>(R.id.emailEditText).text.toString()
        val password = findViewById<EditText>(R.id.passwordEditText).text.toString()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("MainActivity", "sign in successful: ${auth.currentUser?.uid}")
                    launchMainActivity()
                }
                else {
                    Log.d("MainActivity", task.exception.toString())
                }
            }
    }

    private val mainActivityLauncher = registerForActivityResult(StartActivityForResult()) { }

    private fun launchMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        mainActivityLauncher.launch(intent)
    }
}