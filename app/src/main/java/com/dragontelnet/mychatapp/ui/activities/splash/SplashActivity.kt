package com.dragontelnet.mychatapp.ui.activities.splash

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.dragontelnet.mychatapp.BuildConfig
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.ui.activities.login.LoginActivity
import com.dragontelnet.mychatapp.ui.activities.main.MainActivity
import com.dragontelnet.mychatapp.ui.activities.registration.RegistrationDetailsActivity
import com.dragontelnet.mychatapp.utils.MyConstants.ActivityLaunch
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        version_tv.text = "v${BuildConfig.VERSION_NAME}"
        val isAboutNavClicked = intent.getBooleanExtra("isAboutNavClicked", false)

        if (!isAboutNavClicked) {
            val viewModel = ViewModelProvider(this).get(SplashActivityViewModel::class.java)
            viewModel.timeOver().observe(this,
                    Observer { activityLaunchCode: Int? ->
                        when (activityLaunchCode) {
                            ActivityLaunch.START_LOGIN_ACTIVITY -> startSuitableActivity(LoginActivity::class.java)
                            ActivityLaunch.START_MAIN_ACTIVITY -> startSuitableActivity(MainActivity::class.java)
                            ActivityLaunch.START_REG_DETAILS_ACTIVITY -> startSuitableActivity(RegistrationDetailsActivity::class.java)
                            else -> Toast.makeText(this@SplashActivity, "Unknown Activity!!!",
                                    Toast.LENGTH_SHORT)
                                    .show()
                        }
                    })
        }
    }

    private fun startSuitableActivity(cls: Class<*>) {
        val intent = Intent(this, cls)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    companion object {
        private const val TAG = "SplashActivity"
        const val IS_ABOUT_NAV_CLICKED_INTENT = "isAboutNavClicked"
    }
}