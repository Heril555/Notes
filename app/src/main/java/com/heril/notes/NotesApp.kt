package com.heril.notes

import android.app.Application

class NotesApp: Application() {
    override fun onCreate() {
        super.onCreate()
        Graph.provide(this)
    }
}