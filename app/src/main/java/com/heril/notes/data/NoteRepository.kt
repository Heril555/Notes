package com.heril.notes.data

import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {

    suspend fun addNote(note: Note) {
        noteDao.addNote(note)
    }

    fun getAllNotes(): Flow<List<Note>> {
        return noteDao.getAllNotes()
    }

    fun getNoteById(id: Long): Flow<Note> {
        return noteDao.getNote(id)
    }

    suspend fun updateNote(note: Note) {
        noteDao.updateNote(note)
    }

    suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(note)
    }

}