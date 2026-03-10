package com.example.app1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.NoConnectionError
import com.android.volley.Request
import com.android.volley.TimeoutError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        btnLogin.setOnClickListener {
            val email = findViewById<EditText>(R.id.etEmail).text.toString().trim()
            val password = findViewById<EditText>(R.id.etPassword).text.toString().trim()

            if(email.isEmpty() || password.isEmpty()){
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // The logs showed a 404 error for this URL. 
            // Ensure this matches your XAMPP/WAMP folder structure exactly.
            val url = "http://10.0.2.2:8000/login.php"
            Log.d("LoginURL", "Attempting login at: $url")

            val stringRequest = object : StringRequest(Request.Method.POST, url,
                { response ->
                    Log.d("LoginResponse", response)
                    try {
                        val jsonObject = JSONObject(response)
                        if(jsonObject.getString("status") == "success"){
                            val userId = jsonObject.getInt("user_id")
                            
                            val pref = getSharedPreferences("user", MODE_PRIVATE)
                            pref.edit().putInt("user_id", userId).apply()
                            
                            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, HomeActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        if(response.trim() == "success"){
                            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, HomeActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Server error: 404 or Invalid JSON", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                { error ->
                    val statusCode = error.networkResponse?.statusCode ?: 0
                    val message = when (error) {
                        is TimeoutError -> "Connection Timeout"
                        is NoConnectionError -> "Cannot connect to server (Check XAMPP)"
                        else -> "Error $statusCode: ${error.message ?: "File Not Found (404)"}"
                    }
                    Log.e("LoginError", "Status: $statusCode, Message: ${error.message}")
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }){
                override fun getParams(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    params["email"] = email
                    params["password"] = password
                    return params
                }
            }

            Volley.newRequestQueue(this).add(stringRequest)
        }
    }
}
