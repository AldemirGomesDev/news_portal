package com.aldemir.newsportal.ui.signUp

/**
 * Data validation state of the signUp form.
 */
data class SignUpFormState(
    val usernameError: Int? = null,
    val passwordError: Int? = null,
    val passwordConfirmError: Int? = null,
    val isDataValid: Boolean = false,
    val isLoginDataValid: Boolean = false,
    val isUserNameValid: Boolean = false
)