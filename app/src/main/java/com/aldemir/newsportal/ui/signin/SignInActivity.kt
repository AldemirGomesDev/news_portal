package com.aldemir.newsportal.ui.signin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import com.aldemir.newsportal.ui.main.MainActivity
import com.aldemir.newsportal.databinding.ActivitySignInBinding
import com.aldemir.newsportal.ui.signUp.SignUpActivity
import com.aldemir.newsportal.util.Status
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private val signInViewModel: SignInViewModel by viewModels()

    companion object {
        var isUserValid = false
        var isPasswordValid = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loadingUser.visibility = View.VISIBLE
        binding.signInContent.visibility = View.GONE

        signInViewModel.getUserNameSharedPreference()

        observers()
        setupUi()
    }

    private fun setupUi() {
        binding.buttonScreenRegister.setOnClickListener {
            startSignUpActivity()
        }

        binding.username.addTextChangedListener {
            signInViewModel.userNameDataChanged(
                binding.username.text.toString()
            )
        }
        binding.password.apply {
            addTextChangedListener {
                signInViewModel.passwordDataChanged(
                    binding.password.text.toString()
                )
            }
            binding.login.setOnClickListener {
                binding.loading.visibility = View.VISIBLE
                binding.login.isEnabled = false
                signInViewModel.signIn(binding.username.text.toString(), binding.password.text.toString())
            }
        }
    }

    private fun startMainActivity() {
        finish()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)

    }

    private fun startSignUpActivity() {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
    }

    private fun observers() {
        signInViewModel.user.observe(this@SignInActivity, Observer {user ->
            if (user != null) {
                startMainActivity()
            } else {
                binding.signInContent.visibility = View.VISIBLE
                binding.loadingUser.visibility = View.GONE
            }
        })

        signInViewModel.mToken.observe(this@SignInActivity, Observer {token->
            when (token.status) {
                Status.SUCCESS -> {
                    binding.loading.visibility = View.GONE
                    signInViewModel.saveTokenSharedPreference(token.data!!.token)
                    startMainActivity()
                }
                Status.LOADING -> {
                    binding.loading.visibility = View.VISIBLE
                }
                Status.ERROR -> {
                    binding.loading.isEnabled = true
                    Toast.makeText(this@SignInActivity, "${token.message}", Toast.LENGTH_SHORT).show()
                    binding.loading.visibility = View.GONE

                }
            }

        })

        signInViewModel.loginForm.observe(this@SignInActivity, Observer {
            val loginState = it ?: return@Observer

            binding.login.isEnabled = isUserValid && isPasswordValid

            if (loginState.usernameError != null) {
                binding.username.error = getString(loginState.usernameError)
            }else {
                binding.username.error = null
            }
            if (loginState.passwordError != null) {
                binding.password.error = getString(loginState.passwordError)
            }else {
                binding.password.error = null
            }
        })
    }

}