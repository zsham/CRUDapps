//package com.example.app1
//
//import android.content.Intent
//import android.os.Bundle
//import android.widget.Button
//import androidx.appcompat.app.AppCompatActivity
//
//class HomeActivity : AppCompatActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_home)
//
//        val btnLogout = findViewById<Button>(R.id.btnLogout)
//
//        btnLogout.setOnClickListener {
//            startActivity(Intent(this, LoginActivity::class.java))
//            finish()
//        }
//    }
//}

package com.example.app1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
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

        loadNotes()

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

    private fun loadNotes() {

        val pref = getSharedPreferences("user", MODE_PRIVATE)
        val userId = pref.getInt("user_id", 0)

        val url = "http://10.0.2.2:8000/get_notes.php?user_id=$userId"

        val request = StringRequest(
            Request.Method.GET, url,
            { response ->

                val array = JSONArray(response)

                notesList.clear()

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

                recyclerView.adapter = NotesAdapter(this, notesList)

            },
            { }
        )

        Volley.newRequestQueue(this).add(request)
    }
}