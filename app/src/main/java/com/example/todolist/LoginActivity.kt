package com.example.todolist

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
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

    // Attempt to create a new user and launch main activity if successful
    private fun createPasswordSignUp() {
        val email = findViewById<EditText>(R.id.emailEditText).text.toString()
        val password = findViewById<EditText>(R.id.passwordEditText).text.toString()
        if (email.isEmpty() || password.isEmpty()) {
            makeToast("Fields cannot be empty")
            return
        }
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) { launchMainActivity() }
                else { makeToast("Authentication failed: ${task.exception?.message}") }
            }
    }

    // Attempt to login an existing user and launch main activity if successful
    private fun createPasswordSignIn() {
        val email = findViewById<EditText>(R.id.emailEditText).text.toString()
        val password = findViewById<EditText>(R.id.passwordEditText).text.toString()
        if (email.isEmpty() || password.isEmpty()) {
            makeToast("Fields cannot be empty")
            return
        }
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) { launchMainActivity() }
                else { makeToast("Authentication failed: ${task.exception?.message}") }
            }
    }

    private val mainActivityLauncher = registerForActivityResult(StartActivityForResult()) { }

    private fun launchMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        mainActivityLauncher.launch(intent)
    }

    // Helper method for making toasts on failure
    private fun makeToast(str: String) = Toast.makeText(this, str, Toast.LENGTH_LONG).show()
}