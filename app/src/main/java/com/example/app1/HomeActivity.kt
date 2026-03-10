package com.example.app1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray

class HomeActivity : AppCompatActivity() {

    lateinit var recyclerView: RecyclerView
    lateinit var btnLogout: Button
    lateinit var btnAddNote: Button

    var notesList = ArrayList<NoteModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        recyclerView = findViewById(R.id.recyclerView)
        btnLogout = findViewById(R.id.btnLogout)
        btnAddNote = findViewById(R.id.btnAddNote)

        recyclerView.layoutManager = LinearLayoutManager(this)

        btnAddNote.setOnClickListener {
            startActivity(Intent(this, AddNoteActivity::class.java))
        }

        btnLogout.setOnClickListener {
            val pref = getSharedPreferences("user", MODE_PRIVATE)
            pref.edit().clear().apply()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        loadNotes()
    }

    private fun loadNotes() {
        val pref = getSharedPreferences("user", MODE_PRIVATE)
        val userId = pref.getInt("user_id", 0)

        if (userId == 0) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val url = "http://10.0.2.2:8000/get_notes.php?user_id=$userId"
        Log.d("HomeActivity", "Fetching from: $url")

        val request = StringRequest(
            Request.Method.GET, url,
            { response ->
                Log.d("HomeActivity", "Server Response: $response")
                try {
                    val array = JSONArray(response)
                    notesList.clear()
                    
                    if (array.length() == 0) {
                        Toast.makeText(this, "No notes found", Toast.LENGTH_SHORT).show()
                    }

                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        notesList.add(
                            NoteModel(
                                obj.getString("id"),
                                obj.getString("title"),
                                obj.getString("description")
                            )
                        )
                    }
                    
                    // Always set the adapter or notify it
                    recyclerView.adapter = NotesAdapter(this, notesList,
                        onUpdate = { note ->
                            val intent = Intent(this, AddNoteActivity::class.java)
                            intent.putExtra("id", note.id)
                            intent.putExtra("title", note.title)
                            intent.putExtra("description", note.description)
                            startActivity(intent)
                        },
                        onDelete = { note ->
                            deleteNote(note.id)
                        }
                    )
                } catch (e: Exception) {
                    Log.e("HomeActivity", "JSON Parsing Error: ${e.message}")
                    Toast.makeText(this, "Server error: ${response.take(30)}", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                Log.e("HomeActivity", "Volley Error: ${error.message}")
                Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show()
            }
        )

        Volley.newRequestQueue(this).add(request)
    }

    private fun deleteNote(id: String) {
        val url = "http://10.0.2.2:8000/delete_note.php"
        val request = object : StringRequest(Request.Method.POST, url,
            { response ->
                if (response.trim().contains("success")) {
                    Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show()
                    loadNotes()
                } else {
                    Toast.makeText(this, "Failed: $response", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["id"] = id
                return params
            }
        }
        Volley.newRequestQueue(this).add(request)
    }
}
