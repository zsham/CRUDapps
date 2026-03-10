package com.example.app1

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.NoConnectionError
import com.android.volley.Request
import com.android.volley.TimeoutError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class AddNoteActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_note)

        etTitle = findViewById(R.id.title)
        etDescription = findViewById(R.id.description)
        btnSave = findViewById(R.id.saveBtn)

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val description = etDescription.text.toString().trim()

            if (title.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                addNote(title, description)
            }
        }
    }

    private fun addNote(title: String, description: String) {
        val pref = getSharedPreferences("user", MODE_PRIVATE)
        val userId = pref.getInt("user_id", 0)

        if (userId == 0) {
            Toast.makeText(this, "Error: User not logged in properly", Toast.LENGTH_SHORT).show()
            return
        }

        // Updated URL: removed :8000 and added /app_testing/ to match Login/Register
        val url = "http://10.0.2.2:8000/add_note.php"
        Log.d("AddNote", "Saving note to: $url")

        val request = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                Log.d("AddNoteResponse", response)
                if (response.trim().contains("success")) {
                    Toast.makeText(this, "Note added successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Server: $response", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                val statusCode = error.networkResponse?.statusCode ?: 0
                val message = when (error) {
                    is TimeoutError -> "Connection Timeout"
                    is NoConnectionError -> "Cannot connect to server (Check XAMPP)"
                    else -> "Error $statusCode: ${error.message ?: "Unknown Error"}"
                }
                Log.e("AddNoteError", "Status: $statusCode, Message: ${error.message}")
                Toast.makeText(this, "Error: $message", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["user_id"] = userId.toString()
                params["title"] = title
                params["description"] = description
                return params
            }
        }

        Volley.newRequestQueue(this).add(request)
    }
}
