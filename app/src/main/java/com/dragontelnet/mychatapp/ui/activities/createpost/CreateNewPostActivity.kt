package com.dragontelnet.mychatapp.ui.activities.createpost

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.model.entity.Post
import com.dragontelnet.mychatapp.utils.auth.CurrentUser
import com.dragontelnet.mychatapp.utils.firestore.MyFirebaseStorageRefs
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs
import com.facebook.drawee.view.SimpleDraweeView
import com.theartofdev.edmodo.cropper.CropImage

class CreateNewPostActivity : AppCompatActivity() {
    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    @BindView(R.id.caption_et)
    lateinit var captionEt: EditText

    @BindView(R.id.selected_post_pic)
    lateinit var selectedPostPic: SimpleDraweeView

    private var directImageUrl: String? = null
    private var croppedImageUri: Uri? = null
    private var mViewModel: CreatePostActivityViewModel? = null
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new_post)
        ButterKnife.bind(this)
        initUi()
        progressDialog = ProgressDialog(this)
        progressDialog?.setCanceledOnTouchOutside(false)
        progressDialog?.setCancelable(false)
        mViewModel = ViewModelProvider(this).get(CreatePostActivityViewModel::class.java)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initUi() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    @OnClick(R.id.upload_camera_ib)
    fun onUploadCameraIbClicked() {
        Toast.makeText(this, "Opening Gallery.....", Toast.LENGTH_SHORT).show()
        CropImage.activity().setAspectRatio(1, 1)
                .start(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK && result != null) {
                croppedImageUri = result.uri
                selectedPostPic.setImageURI(croppedImageUri)
                selectedPostPic.visibility = View.VISIBLE
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Toast.makeText(this, result.error.toString(), Toast.LENGTH_SHORT).show()
                //Exception error = result.getError();
            }
        }
    }

    @OnClick(R.id.upload_post_btn)
    fun onUploadPostBtnClicked() {
        progressDialog?.setMessage("Uploading post....")
        progressDialog?.show()
        val caption = captionEt.text.toString().trim()
        val postId = MyFirestoreDbRefs.getOrganizedPostsCollRefOfUid(CurrentUser.getCurrentUser()?.uid!!).document().id
        checkPost(postId, caption)
    }

    private fun checkPost(postId: String, caption: String) {
        if (croppedImageUri != null) {
            mViewModel?.getPostObjWithImageUrl(croppedImageUri, this, postId, caption, MyFirebaseStorageRefs.postsStorageRef)
                    ?.observe(this, Observer { post -> //post received is with post photo url
                        writePostToDb(post, caption, postId)
                    })
        } else {
            if (caption.isNotBlank()) {
                writePostToDb(null, caption, postId)
            } else {
                progressDialog?.dismiss()
                Toast.makeText(this, "insufficient information..", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun writePostToDb(post: Post?, caption: String, postId: String) {
        mViewModel?.isWrittenSuccess(post, caption, postId)?.observe(this, Observer { success: Boolean ->
            if (success) {
                progressDialog?.dismiss()
                directImageUrl = null
                croppedImageUri = null
                setResult(Activity.RESULT_OK)
                finish()
            }
        })
    }

    companion object {
        private const val TAG = "CreateNewPostActivity"
    }
}