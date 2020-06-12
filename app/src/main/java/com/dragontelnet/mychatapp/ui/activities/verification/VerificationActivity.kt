package com.dragontelnet.mychatapp.ui.activities.verification

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.datasource.local.MySharedPrefs.putRegistrationDone
import com.dragontelnet.mychatapp.datasource.local.MySharedPrefs.putUserObjToBook
import com.dragontelnet.mychatapp.model.entity.User
import com.dragontelnet.mychatapp.ui.activities.main.MainActivity
import com.dragontelnet.mychatapp.ui.activities.registration.RegistrationDetailsActivity
import com.google.firebase.auth.FirebaseUser

class VerificationActivity : AppCompatActivity() {
    @BindView(R.id.progress_bar)
    lateinit var progressBar: ProgressBar

    @BindView(R.id.otp_et)
    lateinit var otpEt: EditText

    @BindView(R.id.seconds_tv)
    lateinit var secondsTv: TextView

    @BindView(R.id.resend_otp_btn)
    lateinit var resendOtpBtn: Button

    private var viewModel: VerificationActivityViewModel? = null

    //private IsUserExistsViewModel userExistsViewModel;
    private var mOtpStr: String? = null
    private var mVerfId: String? = null
    private var mIsVerfIdReceivedFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification)
        ButterKnife.bind(this)
        viewModel = ViewModelProvider(this).get(VerificationActivityViewModel::class.java)
        //getting phone num from previous activity
        getPhoneNumber()?.let {
            sendOtp(it)
        }
        textChangeListener()
        startVerfTimer()
    }

    //getting phone num from previous activity
    private fun getPhoneNumber(): String? {
        //getting phone num from previous activity
        return if (intent.hasExtra("phone")) {
            intent.getStringExtra("phone")
        } else {
            Toast.makeText(this, "No phone num found", Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun startVerfTimer() {

        //start verification timer
        resendOtpBtn.isEnabled = false
        progressBar.visibility = View.VISIBLE
        viewModel?.startVerfTimer()?.observe(this, Observer { seconds ->
            secondsTv.text = "00:$seconds"
            if (seconds == 0L) {
                //30sec timer over
                resendOtpBtn.isEnabled = true
                //hide progress
                progressBar.visibility = View.INVISIBLE
            }
        })
    }

    private fun textChangeListener() {
        //listening otp edit text
        otpEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (s.length == 6) {
                    Toast.makeText(this@VerificationActivity, "Checking otp..", Toast.LENGTH_SHORT).show()
                    //otp typed
                    otpEt.isEnabled = false
                    mOtpStr = s.toString()
                    if (mIsVerfIdReceivedFlag) {
                        //using previous verfId for getting credential
                        getCredentials()
                    } else {
                        //getting first time verfId and then get credentials
                        getVerfId()
                    }
                }
            }
        })
    }

    //checking if otp length is 6digits
    private fun getVerfId() {
        viewModel?.getVerfId()?.observe(this, Observer { verfId ->
            mVerfId = verfId
            mIsVerfIdReceivedFlag = true //got first verfId ,setting flag to true
            //checking if otp length is 6digits
            if (!TextUtils.isEmpty(mOtpStr) && mOtpStr!!.length == 6) {
                getCredentials()
            }
        })
    }

    //to get credentials we must have mVerfId and mOtpStr
    private fun getCredentials() {
        //to get credentials we must have mVerfId and mOtpStr
        if (!TextUtils.isEmpty(mVerfId) && !TextUtils.isEmpty(mOtpStr)) {
            viewModel!!.getCredential(mVerfId, mOtpStr, this).observe(this, Observer { firebaseUser ->
                firebaseUser?.let {
                    startSuitableActivity(firebaseUser)
                }
            })
        } else {
            Toast.makeText(this, "verf id / otp cannot be null!!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendOtp(phoneNum: String?) {
        Log.d(TAG, "sendOtp: in")
        viewModel!!.isVerified(phoneNum, this)
                .observe(this, Observer { firebaseUser ->
                    firebaseUser?.let {
                        startSuitableActivity(firebaseUser)
                    }
                })
    }

    @OnClick(R.id.enter_code_tv)
    fun onEnterOtpTvClicked() {
        //showing otp edit text
        otpEt.visibility = View.VISIBLE
        //clearing entered data
        otpEt.setText("")
        //enabling otp edit text
        otpEt.isEnabled = true
    }

    private fun startSuitableActivity(firebaseUser: FirebaseUser) {
        viewModel?.isUserExists(firebaseUser.uid)
                ?.observe(this, Observer { user: User? ->
                    user?.let {
                        //registration already done //user exists in firestore db
                        putRegistrationDone(applicationContext)
                        putUserObjToBook(user)
                        startMainActivity()
                    } ?: startRegsActivity()
                })
    }

    private fun startRegsActivity() {
        val intent = Intent(this@VerificationActivity, RegistrationDetailsActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun startMainActivity() {
        val intent = Intent(this@VerificationActivity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    @OnClick(R.id.resend_otp_btn)
    fun onResendOtpBtnClick() {
        startVerfTimer()
        mOtpStr = ""
        otpEt.setText("")
        getPhoneNumber()?.let {
            viewModel!!.resendOtp(it, this)
                    .observe(this, Observer { firebaseUser -> //resendOtpBtn.setEnabled(true);
                        firebaseUser?.let {
                            startSuitableActivity(firebaseUser)
                        }
                    })
        }
    }

    companion object {
        private const val TAG = "VerificationActivity"
    }
}