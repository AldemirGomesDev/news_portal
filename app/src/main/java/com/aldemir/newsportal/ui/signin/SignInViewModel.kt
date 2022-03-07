package com.aldemir.newsportal.ui.signin

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aldemir.newsportal.R
import com.aldemir.newsportal.api.SessionManager
import com.aldemir.newsportal.api.models.RequestLogin
import com.aldemir.newsportal.api.models.ResponseLogin
import com.aldemir.newsportal.data.repository.UserRepository
import com.aldemir.newsportal.models.User
import com.aldemir.newsportal.util.Constants
import com.aldemir.newsportal.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    companion object {
        const val TAG = "SignViewModel"
    }

    private val _user = MutableLiveData<User>()
    var user: LiveData<User> = _user

    private val _userCreated= MutableLiveData<Boolean>()
    var userCreated: LiveData<Boolean> = _userCreated

    private val _token = MutableLiveData<Resource<ResponseLogin>>()
    var mToken: LiveData<Resource<ResponseLogin>> = _token

    private val _loginForm = MutableLiveData<SignInFormState>()
    val loginForm: LiveData<SignInFormState> = _loginForm

    fun signIn(username: String, passwords: String) {
        val requestLogin = RequestLogin(username, passwords)

        viewModelScope.launch {
            try {
                val result = userRepository.sinIn(requestLogin)
                _token.value = (Resource.success(result))
                updateUser(username)

            }catch (error: HttpException){
                if (error.code() == 401) {
                    _token.value = (Resource.error(Constants.ERROR_401, null))
                }else {
                    _token.value = (Resource.error(Constants.ERROR_500, null))
                }
                Log.e(TAG, "ERROR => : ${error}")
            }
        }

    }

    private fun updateUser(email: String) {
        sessionManager.saveUserName(email)
        viewModelScope.launch {
            val user = userRepository.getUserByEmail(email)
            if (user == null) {
                insertUser(email)
            } else {
                user.isLogged = true
                userRepository.updateUser(user)
            }
        }
    }

    private fun insertUser(email: String) {
        viewModelScope.launch {
            val user = User(0, "", email, true)
            userRepository.insertUser(user)
        }
    }

    private fun isLoggedUser(email: String) {
        viewModelScope.launch {
            val userLogged = userRepository.getUserLogged(email, true)
            _user.value = userLogged
        }
    }


    fun saveTokenSharedPreference(token: String){
        sessionManager.saveAuthToken(token)
    }

    fun getUserNameSharedPreference() {
        val userName: String? = sessionManager.getUserName()
        if (userName != null) {
            isLoggedUser(userName)
        }else {
            isLoggedUser("")
        }
    }

    fun userNameDataChanged(username: String) {
        if (!isUserNameValid(username)) {
            SignInActivity.isUserValid = false
            _loginForm.value = SignInFormState(usernameError = R.string.invalid_username)
        } else {
            SignInActivity.isUserValid = true
            _loginForm.value = SignInFormState(isLoginDataValid = true)
        }
    }

    fun passwordDataChanged(password: String) {
        if (!isPasswordValid(password)) {
            SignInActivity.isPasswordValid = false
            _loginForm.value = SignInFormState(passwordError = R.string.invalid_password)
        } else {
            SignInActivity.isPasswordValid = true
            _loginForm.value = SignInFormState(isLoginDataValid = true)

        }
    }

    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains('@')) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}