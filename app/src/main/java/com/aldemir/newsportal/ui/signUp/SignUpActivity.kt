package com.aldemir.newsportal.ui.signUp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import com.aldemir.newsportal.ui.main.MainActivity
import com.aldemir.newsportal.databinding.ActivitySignUpBinding
import com.aldemir.newsportal.util.Status
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private val signUpViewModel: SignUpViewModel by viewModels()

    companion object {
        var isNameValid = false
        var isEmailValid = false
        var isPasswordValid = false
        var isPasswordConfirm = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)

        setContentView(binding.root)

        observers()
        setupUi()
    }

    private fun setupUi() {
        binding.editTextName.addTextChangedListener {
            signUpViewModel.userNameChanged(
                binding.editTextName.text.toString()
            )
        }

        binding.editTextEmail.addTextChangedListener {
            signUpViewModel.userEmailChanged(
                binding.editTextEmail.text.toString()
            )
        }

        binding.password.apply {
            addTextChangedListener {
                signUpViewModel.passwordDataChanged(
                    binding.password.text.toString()
                )
            }
        }

        binding.passwordConfirm.apply {
            addTextChangedListener {
                signUpViewModel.passwordConfirmDataChanged(
                    binding.password.text.toString(),
                    binding.passwordConfirm.text.toString()
                )
            }
        }

        binding.buttonRegister.setOnClickListener {
            binding.loading.visibility = View.VISIBLE
            binding.buttonRegister.isEnabled = false
            signUpViewModel.signUp(
                binding.editTextName.text.toString(),
                binding.editTextEmail.text.toString(),
                binding.password.text.toString()
            )
        }
    }

    private fun observers() {

        signUpViewModel.loginForm.observe(this@SignUpActivity, Observer {
            val registerState = it ?: return@Observer

            binding.buttonRegister.isEnabled = isEmailValid && isNameValid && isPasswordValid && isPasswordConfirm

            if (registerState.usernameError != null) {
                binding.editTextName.error = getString(registerState.usernameError)
            }else {
                binding.editTextName.error = null
            }
            if (registerState.usernameError != null) {
                binding.editTextEmail.error = getString(registerState.usernameError)
            }else {
                binding.editTextEmail.error = null
            }
            if (registerState.passwordError != null) {
                binding.password.error = getString(registerState.passwordError)
            }else {
                binding.password.error = null
            }
            if (registerState.passwordConfirmError != null) {
                binding.passwordConfirm.error = getString(registerState.passwordConfirmError)
            }else {
                binding.passwordConfirm.error = null
            }
        })

        signUpViewModel.mToken.observe(this@SignUpActivity, Observer {token->
            Log.d("RegisterViewModel: ", "token ==>: ${token.status}")

            when (token.status) {
                Status.SUCCESS -> {
                    binding.loading.visibility = View.GONE
                    signUpViewModel.saveToken(token.data!!.token)
                    startMainActivity()
                }
                Status.LOADING -> {
                    binding.loading.visibility = View.VISIBLE
                }
                Status.ERROR -> {
                    binding.buttonRegister.isEnabled = true
                    Toast.makeText(this@SignUpActivity, "${token.message}",
                        Toast.LENGTH_SHORT).show()
                    binding.loading.visibility = View.GONE

                }
            }

        })
    }

    private fun startMainActivity() {
        finish()
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("key", "value")
        startActivity(intent)

    }
}