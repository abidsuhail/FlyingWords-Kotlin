package com.dragontelnet.mychatapp.ui.activities.registration

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.model.entity.User
import com.dragontelnet.mychatapp.ui.activities.main.MainActivity
import com.dragontelnet.mychatapp.utils.CurrentDateAndTime
import com.dragontelnet.mychatapp.utils.MyConstants.FirestoreKeys
import com.dragontelnet.mychatapp.utils.auth.CurrentUser
import com.dragontelnet.mychatapp.utils.firestore.MyFirebaseStorageRefs
import com.facebook.drawee.view.SimpleDraweeView
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_registration_details.*

class RegistrationDetailsActivity : AppCompatActivity() {

    @BindView(R.id.full_name_et)
    lateinit var fullNameEt: EditText

    @BindView(R.id.radioGroup)
    lateinit var radioGroup: RadioGroup

    @BindView(R.id.user_name_et)
    lateinit var userNameEt: EditText

    @BindView(R.id.submit_btn)
    lateinit var submitBtn: Button

    @BindView(R.id.bio_et)
    lateinit var bioEt: EditText

    @BindView(R.id.profile_pic)
    lateinit var profilePicView: SimpleDraweeView

    @BindView(R.id.check_avail_btn)
    lateinit var checkAvailBtn: Button

    private var viewModel: RegistrationActivityViewModel? = null
    private var progressDialog: ProgressDialog? = null
    private var mFullNameStr: String? = null
    private var mUserNameStr: String? = null
    private var mGenderStr: String? = null
    private var mDeviceToken: String? = null
    private var mBioStr: String? = null
    private var mProfilePhotoUrl: String? = null
    private var mCityStr: String? = null
    private var mCroppedImageUri: Uri? = null
    private var isEditing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration_details)
        ButterKnife.bind(this)
        initProgressDialog()

        val toolbar = toolbar
        viewModel = ViewModelProvider(this).get(RegistrationActivityViewModel::class.java)
        isEditing = intent.getBooleanExtra(IS_EDITING_INTENT_FLAG, false)
        if (isEditing) {
            //editing profile
            toolbar.visibility = View.VISIBLE
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            title = "Edit Profile"
            userNameEt.isEnabled = false
            checkAvailBtn.isEnabled = false
            fetchProfileDetails()
        } else {
            //new user,registration process
            toolbar.visibility = View.GONE
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            userNameEt.isEnabled = true
            checkAvailBtn.isEnabled = true
            initRegistUiDetails()
        }

    }

    private fun initProgressDialog() {
        progressDialog = ProgressDialog(this)
        progressDialog?.setCanceledOnTouchOutside(false)
        progressDialog?.setCancelable(false)
    }

    private fun fetchProfileDetails() {

        //get stable user for class
        viewModel?.getStableCurrentUser()?.observe(this, Observer { user ->
            with(user)
            {
                city_et.setText(city)
                fullNameEt.setText(name)
                userNameEt.setText(username)
                bioEt.setText(bio)
                if (profilePic != "") {
                    mProfilePhotoUrl = profilePic
                    profilePicView.setImageURI(profilePic)
                    radioGroup.setOnCheckedChangeListener(null)
                } else {
                    if (gender == "male") {
                        profilePicView.setActualImageResource(R.drawable.user_male_placeholder)
                    } else {
                        profilePicView.setActualImageResource(R.drawable.user_female_placeholder)
                    }
                }

                if (gender == "male") {
                    radioGroup.check(R.id.radio_male)
                } else {
                    radioGroup.check(R.id.radio_female)
                }
            }
        })

    }

    private fun initRegistUiDetails() {
        profilePicView.setActualImageResource(R.drawable.user_male_placeholder)
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.radio_male) {
                profilePicView.setActualImageResource(R.drawable.user_male_placeholder)
            } else {
                profilePicView.setActualImageResource(R.drawable.user_female_placeholder)
            }
        }

    }

    @OnClick(R.id.submit_btn)
    fun onSubmitBtnClicked() {
        val id = radioGroup.checkedRadioButtonId
        val fullName = fullNameEt.text.toString().trim { it <= ' ' }
        val userName = userNameEt.text.toString().trim { it <= ' ' }
        val bio = bioEt.text.toString().trim { it <= ' ' }
        val city = city_et.text.toString().trim { it <= ' ' }
        mGenderStr = if (id == R.id.radio_male) {
            "male"
        } else {
            "female"
        }
        CurrentUser.getCurrentUser()?.let {
            if (fullName.isNotBlank() && fullName.isNotBlank() && fullName.isNotBlank() && city.isNotBlank()) {
                if (fullName.length > 2) {
                    //setting full name and username to instance variable
                    mFullNameStr = fullName
                    mUserNameStr = userName
                    mBioStr = bio
                    mCityStr = city

                    if (isEditing) {
                        //update user to db
                        progressDialog!!.setMessage("updating please wait...")
                        progressDialog!!.show()

                        configUserDetails(userWithCurrentDetails)
                    } else {
                        progressDialog!!.setMessage("signing up please wait...")
                        progressDialog!!.show()
                        //registration process
                        setDeviceTokenToDb()
                    }
                } else {
                    Toast.makeText(this, "Name should be more than 2 words!!!", Toast.LENGTH_SHORT).show()

                }
            } else {
                Toast.makeText(this, "please enter all details..", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setDeviceTokenToDb() {
        //getting device token
        viewModel?.getDeviceToken(applicationContext)
                ?.observe(this, Observer { deviceToken ->
                    mDeviceToken = deviceToken

                    //false because i clicked on submit button
                    checkUsernameExistence(false)
                })
    }

    private fun configUserDetails(user: User) {
        if (mCroppedImageUri != null) {
            //get photo link then add to db
            uploadProfilePhoto()
        } else {
            //add data to db without profile photo
            writeUserDetailsToDb(user)
        }
    }

    private fun writeUserDetailsToDb(user: User) {
        viewModel?.isWritten(user, applicationContext)?.observe(this, Observer { isWritten ->
            if (isWritten) {
                //data written successfully
                progressDialog!!.dismiss()
                if (isEditing) {
                    finish()
                } else {
                    startMainActivity()
                }
            } else {
                //write error to database
                progressDialog!!.dismiss()
                Toast.makeText(this@RegistrationDetailsActivity, "Unknown error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun startMainActivity() {
        val intent = Intent(this@RegistrationDetailsActivity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    @OnClick(R.id.check_avail_btn)
    fun onCheckAvailabilityBtnClicked() {
        mUserNameStr = userNameEt.text.toString().trim { it <= ' ' }
        if (!TextUtils.isEmpty(mUserNameStr)) {
            progressDialog!!.setMessage("checking for username")
            progressDialog!!.show()
            //here isOnlyChecking is true,we are only checking for user existence
            checkUsernameExistence(true)
        } else {
            Toast.makeText(this, "username cant be empty", Toast.LENGTH_SHORT).show()
        }
    }

    @OnClick(R.id.remove_profile_pic_btn)
    fun removeProfilePicBtnClicked() {
        AlertDialog.Builder(this)
                .setTitle("Profile Photo")
                .setMessage("Reset profile picture to default?")
                .setPositiveButton("RESET PROFILE PIC") { dialog, which ->
                    // confirm
                    if (radioGroup.checkedRadioButtonId == R.id.radio_male) {
                        profilePicView.setImageResource(R.drawable.user_male_placeholder)
                    } else {
                        profilePicView.setImageResource(R.drawable.user_female_placeholder)
                    }
                    mProfilePhotoUrl = null //if null then user.profilePic=""
                    mCroppedImageUri = null //remove selected image uri

                    //setting radio btn listener
                    radioGroup.setOnCheckedChangeListener { group, checkedId ->
                        if (checkedId == R.id.radio_male) {
                            profilePicView.setActualImageResource(R.drawable.user_male_placeholder)
                        } else {
                            profilePicView.setActualImageResource(R.drawable.user_female_placeholder)
                        }
                    }
                }
                .setNegativeButton("CANCEL") { dialog, _ ->
                    //cancel
                    dialog.cancel()
                }.create().show()

    }

    private fun checkUsernameExistence(isOnlyChecking: Boolean) {
        viewModel?.isUsernameExists(mUserNameStr, applicationContext)
                ?.observe(this, Observer { isUsernameExists ->
                    if (isUsernameExists) {
                        progressDialog!!.dismiss()
                        submitBtn.isEnabled = false
                        Toast.makeText(this@RegistrationDetailsActivity, "Username already exists!!", Toast.LENGTH_SHORT)
                                .show()
                    } else {

                        //here username not exists i.e its available,now continue next part
                        submitBtn.isEnabled = true

                        //when isOnlyChecking==false ,we are finally submitting details
                        if (!isOnlyChecking) {
                            configUserDetails(userWithCurrentDetails) //writing user to db
                        } else {
                            //when isOnlyChecking==true ,we are just only checking username existence
                            progressDialog?.dismiss()
                            Toast.makeText(this@RegistrationDetailsActivity,
                                    "Congrats,username available!!", Toast.LENGTH_SHORT)
                                    .show()
                        }
                    }
                })
    }

    private val userWithCurrentDetails: User
        get() {
            val user = User()
            with(user)
            {
                name = mFullNameStr
                username = mUserNameStr
                gender = mGenderStr
                deviceToken = mDeviceToken
                uid = CurrentUser.getCurrentUser()?.uid
                phone = CurrentUser.getCurrentUser()?.phoneNumber
                mProfilePhotoUrl?.let { profilePic = it } ?: run { profilePic = "" }
                status = FirestoreKeys.ONLINE
                date = CurrentDateAndTime.currentDate
                time = CurrentDateAndTime.currentTime
                bio = mBioStr
                city = mCityStr
            }
            return user
        }

    @OnClick(R.id.linearLayoutProfile)
    fun onProfilePicLayout() {
        cropImage()
    }

    private fun cropImage() {
        CropImage.activity()
                .setAspectRatio(1, 1)
                .start(this)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK && result != null) {
                mCroppedImageUri = result.uri
                mProfilePhotoUrl = null
                profilePicView.setImageURI(mCroppedImageUri)
                // uploadProfilePhoto();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                mCroppedImageUri = null
                val error = result!!.error
                Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun uploadProfilePhoto() {
        mCroppedImageUri?.let {
            viewModel?.getUploadedPhotoLink(it, this, MyFirebaseStorageRefs.profilePhotosStorageRef)
                    ?.observe(this, Observer { croppedImgLink ->
                        mProfilePhotoUrl = croppedImgLink
                        mCroppedImageUri = null
                        writeUserDetailsToDb(userWithCurrentDetails)
                    })
        } ?: Toast.makeText(this, "Please select image", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "RegistrationDetailsAc"
        public const val IS_EDITING_INTENT_FLAG = "isEditing"
    }
}