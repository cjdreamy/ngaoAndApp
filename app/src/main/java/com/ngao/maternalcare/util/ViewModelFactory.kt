package com.ngao.maternalcare.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.ngao.maternalcare.data.remote.SessionManager
import com.ngao.maternalcare.data.repository.NgaoRepository

class ViewModelFactory(
    private val repository: NgaoRepository,
    private val sessionManager: SessionManager,
    private val creator: (NgaoRepository, SessionManager) -> ViewModel
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return creator(repository, sessionManager) as T
    }
}
