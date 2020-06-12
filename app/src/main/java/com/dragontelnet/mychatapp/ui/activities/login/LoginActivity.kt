package com.dragontelnet.mychatapp.ui.activities.login

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.ui.activities.verification.VerificationActivity

class LoginActivity : AppCompatActivity() {

    @BindView(R.id.phone_num_et)
    lateinit var phoneNumEt: EditText

    @BindView(R.id.send_otp_btn)
    lateinit var sendOtpBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        ButterKnife.bind(this)
    }

    @OnClick(R.id.send_otp_btn)
    fun onSendOtpBtnClicked() {
        val phoneNumEtStr = phoneNumEt.text.toString()
        if (!TextUtils.isEmpty(phoneNumEtStr)) {
            if (phoneNumEtStr.length != 10) {
                Toast.makeText(this, "Number is not equal to 10",
                        Toast.LENGTH_SHORT).show()
            } else {

                //now start verifying activity
                startVerfActivity(phoneNumEtStr)
            }
        }
    }

    private fun startVerfActivity(phoneNumEtStr: String) {
        val intent = Intent(this, VerificationActivity::class.java)
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("phone", phoneNumEtStr)
        startActivity(intent)
    }
}