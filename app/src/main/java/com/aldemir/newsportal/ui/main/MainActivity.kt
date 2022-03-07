package com.aldemir.newsportal.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.aldemir.newsportal.R
import com.aldemir.newsportal.databinding.ActivityMainBinding
import com.aldemir.newsportal.ui.signin.SignInActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var userNameHeader: TextView
    private lateinit var userEmailHeader: TextView
    private var mUserEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        binding.logout.setOnClickListener {
            dialogLogout()
        }

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_search
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        val headerView: View = navView.getHeaderView(0)

        userNameHeader = headerView.findViewById(R.id.user_name_drawer)
        userEmailHeader = headerView.findViewById(R.id.user_email_drawer)

        observers()
        mainViewModel.getUserNameSharedPreference()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_logout -> {
                dialogLogout()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun observers() {
        mainViewModel.userEmail.observe(this@MainActivity, Observer { userEmail ->
            if (userEmail != null) {
                mUserEmail = userEmail
                userEmailHeader.text = userEmail
                mainViewModel.getUserLogged(userEmail)
            }
        })

        mainViewModel.userLogged.observe(this@MainActivity, Observer { userLogged ->
            if (userLogged != null) {
                userNameHeader.text = userLogged.name
            }
        })
    }

    private fun logOut() {
        if(mUserEmail != null) {
            mainViewModel.logout(mUserEmail!!)
        }
        finish()
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
    }

    private fun dialogLogout() {
        MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.alert_title))
            .setMessage(resources.getString(R.string.alert_message))
            .setNegativeButton(resources.getString(R.string.alert_button_cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(resources.getString(R.string.alert_button_confirm)) { dialog, _ ->
                dialog.dismiss()
                logOut()
            }
            .setCancelable(false)
            .show()
    }
}