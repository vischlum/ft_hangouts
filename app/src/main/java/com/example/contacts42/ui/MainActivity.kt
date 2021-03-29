package com.example.contacts42.ui

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.preference.PreferenceManager
import com.example.contacts42.R
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController

    private val viewModel: MainViewModel by viewModels()

    private val updateThemeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == "current_theme") {
            finish()
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        when (sharedPreferences.getString("current_theme", "purple_teal")) {
            "orange_green" -> setTheme(R.style.Theme_Contacts42_OrangeGreen)
            "solarized_light" -> setTheme(R.style.Theme_Contacts42_SolarizedLight)
            "solarized_dark" -> setTheme(R.style.Theme_Contacts42_SolarizedDark)
            "oled" -> setTheme(R.style.Theme_Contacts42_OLED)
            else -> setTheme(R.style.Theme_Contacts42)
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(updateThemeListener)

        setContentView(R.layout.activity_main)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.navigation_contacts))
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onPause() {
        super.onPause()

        viewModel.onAppPaused(System.currentTimeMillis())
    }

    override fun onResume() {
        super.onResume()

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        lifecycleScope.launchWhenStarted {
            if (sharedPreferences.getBoolean("enable_snackbar_when_paused", false)) {
                Snackbar.make(
                    findViewById(R.id.nav_host_fragment),
                    viewModel.getTime(),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    fun showAppInfo() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", applicationContext.packageName, null)
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}

const val ADD_CONTACT_RESULT_OK = Activity.RESULT_FIRST_USER
const val EDIT_CONTACT_RESULT_OK = Activity.RESULT_FIRST_USER + 1