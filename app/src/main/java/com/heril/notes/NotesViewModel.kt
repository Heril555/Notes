package com.heril.notes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heril.notes.data.Note
import com.heril.notes.data.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class NotesViewModel(
    private val noteRepository: NoteRepository = Graph.noteRepository
): ViewModel() {
    var noteTitleState by mutableStateOf("")
    var noteContentState by mutableStateOf("")

    fun onNoteTitleChange(newTitle: String) {
        noteTitleState = newTitle
    }

    fun onNoteContentChange(newContent: String) {
        noteContentState = newContent
    }

    lateinit var getAllNotes: Flow<List<Note>>

    init{
        viewModelScope.launch {
            getAllNotes = noteRepository.getAllNotes()
        }
    }

    fun addNote(note: Note){
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.addNote(note)
        }
    }

    fun getNoteById(id: Long): Flow<Note> {
        return noteRepository.getNoteById(id)
    }

    fun updateNote(note: Note){
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.updateNote(note)
        }
    }

    fun deleteNote(note: Note){
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.deleteNote(note)
        }
    }
}