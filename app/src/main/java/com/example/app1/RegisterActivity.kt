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

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnBack = findViewById<TextView>(R.id.btnBack)

        btnBack.setOnClickListener {
            finish()
        }

        btnRegister.setOnClickListener {
            val name = findViewById<EditText>(R.id.etName).text.toString().trim()
            val email = findViewById<EditText>(R.id.etEmail).text.toString().trim()
            val password = findViewById<EditText>(R.id.etPassword).text.toString().trim()

            if(name.isEmpty() || email.isEmpty() || password.isEmpty()){
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Using the correct URL (Port 80 for XAMPP)
            val url = "http://10.0.2.2:8000/register.php"
            Log.d("RegisterURL", "Attempting registration at: $url")

            val stringRequest = object : StringRequest(Request.Method.POST, url,
                { response ->
                    Log.d("RegisterResponse", response)
                    // Added .trim() to handle whitespace from PHP
                    if(response.trim().contains("success")){
                        Toast.makeText(this, "Registered successfully", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Server Error: $response", Toast.LENGTH_SHORT).show()
                    }
                },
                { error ->
                    val statusCode = error.networkResponse?.statusCode ?: 0
                    val message = when (error) {
                        is TimeoutError -> "Connection Timeout"
                        is NoConnectionError -> "Cannot connect to server (Check XAMPP/WAMP)"
                        else -> "Error $statusCode: ${error.message ?: "Unknown Error"}"
                    }
                    Log.e("RegisterError", "Status: $statusCode, Message: ${error.message}")
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }){
                override fun getParams(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    params["name"] = name
                    params["email"] = email
                    params["password"] = password
                    return params
                }
            }

            Volley.newRequestQueue(this).add(stringRequest)
        }
    }
}
