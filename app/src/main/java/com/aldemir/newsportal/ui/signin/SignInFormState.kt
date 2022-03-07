package com.aldemir.newsportal.ui.signin

/**
 * Data validation state of the signIn form.
 */
data class SignInFormState(
    val usernameError: Int? = null,
    val passwordError: Int? = null,
    val isDataValid: Boolean = false,
    val isLoginDataValid: Boolean = false,
    val isUserNameValid: Boolean = false
)