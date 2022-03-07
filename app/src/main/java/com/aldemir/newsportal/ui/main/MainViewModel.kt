package com.aldemir.newsportal.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aldemir.newsportal.api.SessionManager
import com.aldemir.newsportal.data.repository.UserRepository
import com.aldemir.newsportal.models.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
): ViewModel() {

    private val _userLogged = MutableLiveData<User>()
    var userLogged: LiveData<User> = _userLogged

    private val _userEmail = MutableLiveData<String>()
    var userEmail: LiveData<String> = _userEmail

    fun getUserLogged(email: String) {
        viewModelScope.launch {
            val user = userRepository.getUserLogged(email, true)
            _userLogged.value = user
        }
    }

    fun logout(email: String) {
        viewModelScope.launch {
            val user = userRepository.getUserByEmail(email)
            if (user != null) {
                user.isLogged = false
                userRepository.updateUser(user)
            }
        }
    }
    fun getUserNameSharedPreference() {
        _userEmail.value = sessionManager.getUserName()
    }

}