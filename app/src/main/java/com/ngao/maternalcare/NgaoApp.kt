package com.ngao.maternalcare

import android.app.Application
import com.ngao.maternalcare.data.remote.SessionManager
import com.ngao.maternalcare.data.repository.NgaoRepository

class NgaoApp : Application() {

    lateinit var sessionManager: SessionManager
        private set

    lateinit var repository: NgaoRepository
        private set

    override fun onCreate() {
        super.onCreate()
        sessionManager = SessionManager(this)
        repository = NgaoRepository(sessionManager)
    }
}
