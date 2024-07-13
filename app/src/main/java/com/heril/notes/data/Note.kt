package com.heril.notes.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_note")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long=0L,
    @ColumnInfo(name = "note_title")
    val title: String="",
    @ColumnInfo(name = "note_content")
    val content: String=""
)
