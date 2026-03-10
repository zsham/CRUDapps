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
    private var noteId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_note)

        etTitle = findViewById(R.id.title)
        etDescription = findViewById(R.id.description)
        btnSave = findViewById(R.id.saveBtn)

        // Check if we are updating an existing note
        noteId = intent.getStringExtra("id")
        if (noteId != null) {
            etTitle.setText(intent.getStringExtra("title"))
            etDescription.setText(intent.getStringExtra("description"))
            btnSave.text = "Update Note"
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val description = etDescription.text.toString().trim()

            if (title.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                if (noteId == null) {
                    addNote(title, description)
                } else {
                    updateNote(noteId!!, title, description)
                }
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

        val url = "http://10.0.2.2:8000/add_note.php"
        
        val request = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                if (response.trim().contains("success")) {
                    Toast.makeText(this, "Note added successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Server: $response", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
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

    private fun updateNote(id: String, title: String, description: String) {
        val url = "http://10.0.2.2:8000/update_note.php"

        val request = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                if (response.trim().contains("success")) {
                    Toast.makeText(this, "Note updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Update failed: $response", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["id"] = id
                params["title"] = title
                params["description"] = description
                return params
            }
        }
        Volley.newRequestQueue(this).add(request)
    }
}
