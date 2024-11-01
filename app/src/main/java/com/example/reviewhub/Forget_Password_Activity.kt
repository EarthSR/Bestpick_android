package com.bestpick.reviewhub

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
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

class Forget_Password_Activity : AppCompatActivity() {

    private lateinit var progressBar: LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forget_password)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val emailEditText = findViewById<EditText>(R.id.Email)
        val SendOTPbutton = findViewById<Button>(R.id.btnsent)
        progressBar = findViewById(R.id.lottie_loading)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        SendOTPbutton.setOnClickListener {
            val email = emailEditText.text.toString()
            if (email.isEmpty()) {
                emailEditText.error = "Email is required"
            }else{
                progressBar.visibility = View.VISIBLE
                performRegister(email)
            }
        }
    }
    private fun performRegister(email: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val url = getString(R.string.root_url) + getString(R.string.forgotpassword)
            val okHttpClient = OkHttpClient()
            val formBody: RequestBody = FormBody.Builder()
                .add("email", email)
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
                    handleCreateResponse(response, responseBody, email)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun handleCreateResponse(response: okhttp3.Response, responseBody: String, email: String) {
        try {
            if (response.isSuccessful) {
                val obj = JSONObject(responseBody)
                val message = obj.optString("message", "")

                when {
                    message.contains("OTP sent to email") -> {
                        Log.d("CreateResponse", "OTP sent to $email")
                        val intent = Intent(this, Sent_Otp_forgetpassword_Activity::class.java)
                        intent.putExtra("email", email)
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