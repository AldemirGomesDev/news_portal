package com.aldemir.newsportal.ui.signUp

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aldemir.newsportal.R
import com.aldemir.newsportal.api.SessionManager
import com.aldemir.newsportal.api.models.RequestRegister
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
class SignUpViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    companion object {
        const val TAG = "SignUpViewModel"
    }

    private val _token = MutableLiveData<Resource<ResponseLogin>>()
    var mToken: LiveData<Resource<ResponseLogin>> = _token

    private val _loginForm = MutableLiveData<SignUpFormState>()
    val loginForm: LiveData<SignUpFormState> = _loginForm

    fun signUp(name: String, username: String, passwords: String) {
        val requestRegister = RequestRegister(name, username, passwords)

        viewModelScope.launch {
            try {
                val result = userRepository.sinUp(requestRegister)
                _token.value = (Resource.success(result))
                val user = User(0, name, username, true)
                insertUser(user)

            }catch (error: HttpException){
                if (error.code() == 422) {
                    _token.value = (Resource.error(Constants.EMAIL_ALREADY_EXISTS, null))
                }else {
                    _token.value = (Resource.error(Constants.ERROR_500, null))
                }
                Log.e(TAG, "ERROR => : ${error.code()}")
            }
        }

    }

    private fun insertUser(user: User) {
        viewModelScope.launch {
            val userId = userRepository.insertUser(user)
            sessionManager.saveUserId(userId)
            sessionManager.saveUserName(user.email)
            sessionManager.saveName(user.name)
        }
    }

    fun saveToken(token: String){
        sessionManager.saveAuthToken(token)
    }

    fun userNameChanged(name: String) {
        if (!isNameValid(name)) {
            SignUpActivity.isNameValid = false
            _loginForm.value = SignUpFormState(usernameError = R.string.invalid_name)
        } else {
            SignUpActivity.isNameValid = true
            _loginForm.value = SignUpFormState(isLoginDataValid = true)
        }
    }

    fun userEmailChanged(email: String) {
        if (!isEmailValid(email)) {
            SignUpActivity.isEmailValid = false
            _loginForm.value = SignUpFormState(usernameError = R.string.invalid_username)
        } else {
            SignUpActivity.isEmailValid = true
            _loginForm.value = SignUpFormState(isLoginDataValid = true)
        }
    }

    fun passwordDataChanged(password: String) {
        if (!isPasswordValid(password)) {
            SignUpActivity.isPasswordValid = false
            _loginForm.value = SignUpFormState(passwordError = R.string.invalid_password)
        } else {
            SignUpActivity.isPasswordValid = true
            _loginForm.value = SignUpFormState(isLoginDataValid = true)

        }
    }

    fun passwordConfirmDataChanged(password: String, passwordConfirm: String) {
        if (!isPasswordConfirm(password, passwordConfirm)) {
            SignUpActivity.isPasswordConfirm = false
            _loginForm.value = SignUpFormState(passwordConfirmError = R.string.confirm_password)
        } else {
            SignUpActivity.isPasswordConfirm = true
            _loginForm.value = SignUpFormState(isLoginDataValid = true)

        }
    }

    private fun isNameValid(name: String): Boolean {
        return name.length > 5
    }

    private fun isEmailValid(username: String): Boolean {
        return if (username.contains('@')) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }

    private fun isPasswordConfirm(password: String, passwordConfirm: String): Boolean {
        return password == passwordConfirm
    }
}