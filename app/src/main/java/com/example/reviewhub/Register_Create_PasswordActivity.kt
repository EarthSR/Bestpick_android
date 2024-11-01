package com.bestpick.reviewhub

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.airbnb.lottie.LottieAnimationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject

class Register_Create_PasswordActivity : AppCompatActivity() {

    private lateinit var progressBar: LottieAnimationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_create_password)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        progressBar = findViewById(R.id.lottie_loading)
        val passwordEditText = findViewById<EditText>(R.id.registerpassword)
        val confirmPasswordEditText = findViewById<EditText>(R.id.registerpasswordconfirm)
        val registerButton = findViewById<Button>(R.id.registerButton) // Assuming you have a button to submit
        val togglePassword1 = findViewById<ImageView>(R.id.togglePasswordConfirm1)
        val togglePassword2 = findViewById<ImageView>(R.id.togglePasswordConfirm2)

        togglePassword1.setOnClickListener {
            if (passwordEditText.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                // Hide password
                passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                togglePassword1.setImageResource(R.drawable.eye_hide)
            } else {
                // Show password
                passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                togglePassword1.setImageResource(R.drawable.eye_open)
            }
            // Move the cursor to the end of the text
            passwordEditText.setSelection(passwordEditText.text.length)
        }

        togglePassword2.setOnClickListener {
            if (confirmPasswordEditText.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                // Hide confirm password
                confirmPasswordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                togglePassword2.setImageResource(R.drawable.eye_hide)
            } else {
                // Show confirm password
                confirmPasswordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                togglePassword2.setImageResource(R.drawable.eye_open)
            }
            // Move the cursor to the end of the text
            progressBar.visibility = View.VISIBLE
            confirmPasswordEditText.setSelection(confirmPasswordEditText.text.length)
        }

        val email = intent.getStringExtra("email") ?: run {
            progressBar.visibility = View.GONE
            Toast.makeText(this, "No email found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        registerButton.setOnClickListener {
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (password != confirmPassword) {
                confirmPasswordEditText.error = "Passwords do not match"
            } else if (password.isEmpty()) {
                passwordEditText.error = "Password cannot be empty"
                confirmPasswordEditText.error = "Confirm password cannot be empty"
            } else {
                performRegister(email, password)
            }
        }
    }

    private fun performRegister(email: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val url = getString(R.string.root_url) + getString(R.string.setpasswordregister)
            val okHttpClient = OkHttpClient()
            val formBody: RequestBody = FormBody.Builder()
                .add("email", email)
                .add("password", password)
                .build()
            val request: Request = Request.Builder()
                .url(url)
                .post(formBody)
                .build()

            try {
                val response = okHttpClient.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                Log.d("ResponseBody", responseBody) // Debugging line

                withContext(Dispatchers.Main) {
                    handleLoginResponse(response, responseBody)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(applicationContext, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun handleLoginResponse(response: okhttp3.Response, responseBody: String) {
        try {
            if (response.isSuccessful) {
                val obj = JSONObject(responseBody)
                val message = obj.optString("message", "")

                when {
                    message.contains("User registered successfully") -> {
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    else -> {
                        progressBar.visibility = View.GONE
                    }
                }
            } else {
                // Parse the responseBody for error message
                val errorMessage = try {
                    val errorObj = JSONObject(responseBody)
                    errorObj.optString("error", "Unknown error")
                } catch (e: JSONException) {
                    "Unknown error"
                }
                progressBar.visibility = View.GONE
            }
        } catch (e: JSONException) {
            progressBar.visibility = View.GONE
        }
    }
}
