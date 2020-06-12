package com.dragontelnet.mychatapp.ui.fragments.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.model.entity.User
import com.facebook.drawee.view.SimpleDraweeView

class SettingsFragment : Fragment() {
    @BindView(R.id.profile_pic)
    lateinit var profilePic: SimpleDraweeView

    @BindView(R.id.full_name_et)
    lateinit var fullNameEt: EditText

    @BindView(R.id.user_name_et)
    lateinit var userNameEt: EditText

    @BindView(R.id.bio_et)
    lateinit var bioEt: EditText

    @BindView(R.id.radio_male)
    lateinit var radioMale: RadioButton

    @BindView(R.id.radio_female)
    lateinit var radioFemale: RadioButton

    @BindView(R.id.radioGroup)
    lateinit var radioGroup: RadioGroup

    @BindView(R.id.check_avail_btn)
    lateinit var checkAvailBtn: Button

    private var mViewModel: SettingsViewModel? = null
    private var user: User? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_registration_details, container, false)
        ButterKnife.bind(this, view)
        setHasOptionsMenu(true)
        userNameEt.visibility = View.GONE
        checkAvailBtn.visibility = View.GONE
        radioGroup.visibility = View.GONE
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mViewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)
        user = arguments?.getSerializable("user") as User
        fullNameEt.setText(user?.name)
        bioEt.setText(user?.bio)
        if (user?.profilePic != "") {
            profilePic.setImageURI(user?.profilePic)
        } else {
            if (user?.gender == "male") {
                profilePic.setImageResource(R.drawable.user_male_placeholder)
            } else {
                profilePic.setImageResource(R.drawable.user_female_placeholder)
            }
        }
    }

    @OnClick(R.id.submit_btn)
    fun onSubmitBtnClicked() {
    }

}