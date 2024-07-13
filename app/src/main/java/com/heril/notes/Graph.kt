package com.heril.notes

import android.content.Context
import androidx.room.Room
import com.heril.notes.data.NoteDatabase
import com.heril.notes.data.NoteRepository

object Graph {
    lateinit var database: NoteDatabase

    //by lazy makes sure this variable is initialized only once when needed
    val noteRepository by lazy {
        NoteRepository(noteDao = database.noteDao())
    }

    //It will build/initialize the database
    fun provide(context: Context) {
        database = Room.databaseBuilder(context, NoteDatabase::class.java, "Notes.db").build()
    }

}