package com.dragontelnet.mychatapp.ui.fragments.home.adapter.viewholder

import android.content.Intent
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.datasource.local.MySharedPrefs.getCurrentOfflineUserFromBook
import com.dragontelnet.mychatapp.model.entity.Post
import com.dragontelnet.mychatapp.model.entity.User
import com.dragontelnet.mychatapp.ui.activities.profile.view.ProfileActivity
import com.dragontelnet.mychatapp.ui.fragments.home.view.FeedsFragment
import com.dragontelnet.mychatapp.ui.fragments.home.viewmodel.FeedsFragmentViewModel
import com.dragontelnet.mychatapp.utils.UserProfileDetailsSetter
import com.dragontelnet.mychatapp.utils.auth.CurrentUser.getCurrentUser
import com.facebook.drawee.view.SimpleDraweeView

class PostVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var mLastCommentOwnerUid: String? = null
    var userUid: String = ""
    var mUserProfilePic: SimpleDraweeView = itemView.findViewById(R.id.user_profile_pic)
    var mUserFullName: TextView = itemView.findViewById(R.id.user_full_name)
    var mEditPostBtn: ImageButton = itemView.findViewById(R.id.edit_post_btn)
    var mPostPhoto: SimpleDraweeView = itemView.findViewById(R.id.post_photo)
    var mLikeBtn: ImageButton = itemView.findViewById(R.id.like_btn)
    var mCommentBtn: ImageButton = itemView.findViewById(R.id.comment_btn)
    var mLikesCountTv: TextView = itemView.findViewById(R.id.likes_count_tv)
    var mCommentsCountTv: TextView = itemView.findViewById(R.id.comments_count_tv)
    var mLastCommentOwnerName: TextView = itemView.findViewById(R.id.last_comment_owner_name)
    var mLastComment: TextView = itemView.findViewById(R.id.last_comment)
    var mCaptionTv: TextView = itemView.findViewById(R.id.caption_tv)
    var mPostDateTimeTv: TextView = itemView.findViewById(R.id.post_date_time_tv)

    var mPostContentInclude: View = itemView.findViewById(R.id.post_content_include)

    fun bindUserDetails(holder: PostVH, updatedPost: Post, mViewModel: FeedsFragmentViewModel, feedsFragment: FeedsFragment) {
        if (updatedPost.byUid == getCurrentUser()?.uid && getCurrentOfflineUserFromBook != null) {
            val offlineUser = getCurrentOfflineUserFromBook
            UserProfileDetailsSetter.setAllUserDetails(user = offlineUser, nameTv = holder.mUserFullName, sdv = holder.mUserProfilePic)
            holder.mUserFullName.setOnClickListener {
                holder.startProfileActivity(offlineUser, feedsFragment)
            }
        } else {
            mViewModel.getUser(updatedPost.byUid!!).observe(feedsFragment, Observer { user ->
                if (updatedPost.byUid == user.uid && holder.userUid == user.uid) {
                    holder.mUserFullName.text = user.name
                    if (user.profilePic == "") {
                        if (user.gender == "male") {
                            holder.mUserProfilePic.setImageResource(R.drawable.user_male_placeholder)
                        } else {
                            holder.mUserProfilePic.setImageResource(R.drawable.user_female_placeholder)
                        }
                    } else {
                        holder.mUserProfilePic.setImageURI(user.profilePic)
                    }
                    holder.mUserFullName.setOnClickListener {
                        mViewModel.getUser(updatedPost.byUid!!).observe(feedsFragment, Observer { user ->
                            holder.startProfileActivity(user, feedsFragment)
                        })
                    }
                }
            })

        }
    }

    private fun startProfileActivity(user: User?, feedsFragment: FeedsFragment) {
        user?.let {
            val i = Intent(feedsFragment.context, ProfileActivity::class.java)
            i.putExtra("user", it)
            feedsFragment.startActivity(i)
        }
    }

    fun bindPostDetails(holder: PostVH, updatedPost: Post, mViewModel: FeedsFragmentViewModel, feedsFragment: FeedsFragment) {
        holder.mPostContentInclude.visibility = View.GONE //this contains set of post photo and caption
        holder.mPostDateTimeTv.text = updatedPost.dateTime?.date + " " + updatedPost.dateTime?.time
        holder.mLikesCountTv.text = updatedPost.likersUids?.size.toString() + " Likes"
        holder.mCommentsCountTv.text = updatedPost.commentsCount.toString()

        //only photo
        if (updatedPost.postPhotoUrl != "" && updatedPost.caption == "") {
            bindOnlyPostPhoto(holder, updatedPost)
        }
        // only caption/status
        else if (updatedPost.postPhotoUrl == "" && updatedPost.caption != "") {
            bindOnlyPostCaption(holder, updatedPost)
        }
        //both caption and status
        else {
            bindPostAndCaption(holder, updatedPost)
        }

        if (updatedPost.likersUids!!.contains(getCurrentUser()!!.uid)) {
            //my like exists
            setLikedButtonView(holder)
        } else {
            //my like not exists
            setRemovedLikeButtonView(holder)
        }
        if (updatedPost.lastComment != null) {
            holder.mLastComment.text = updatedPost.lastComment?.content
            mViewModel.getUser(updatedPost.lastComment?.commentByUid!!).observe(feedsFragment, Observer { user ->
                if (holder.mLastCommentOwnerUid != null && holder.mLastCommentOwnerUid == user.uid) {
                    holder.mLastCommentOwnerName.text = user.name
                }
            })

        } else {
            holder.mLastComment.text = "No Comments Available!!!"
            holder.mLastCommentOwnerName.text = ""
        }
    }

    private fun bindPostAndCaption(holder: PostVH, updatedPost: Post) {
        holder.mPostContentInclude.visibility = View.VISIBLE
        holder.mPostPhoto.visibility = View.VISIBLE //showing photo
        holder.mCaptionTv.visibility = View.VISIBLE //showing caption
        holder.mPostPhoto.setImageURI(updatedPost.postPhotoUrl) //binding photo
        holder.mCaptionTv.text = updatedPost.caption //binding caption
    }

    private fun bindOnlyPostCaption(holder: PostVH, updatedPost: Post) {
        holder.mPostContentInclude.visibility = View.VISIBLE
        holder.mPostPhoto.visibility = View.GONE //hiding photo
        holder.mCaptionTv.visibility = View.VISIBLE //showing caption
        holder.mCaptionTv.text = updatedPost.caption //binding caption
    }

    private fun bindOnlyPostPhoto(holder: PostVH, updatedPost: Post) {
        holder.mPostContentInclude.visibility = View.VISIBLE
        holder.mPostPhoto.visibility = View.VISIBLE //showing photo
        holder.mPostPhoto.setImageURI(updatedPost.postPhotoUrl) //binding photo
        holder.mCaptionTv.visibility = View.GONE //hiding caption
    }

    private fun setLikedButtonView(holder: PostVH) {
        holder.mLikeBtn.setImageResource(R.drawable.ic_like_liked)
        holder.mLikeBtn.tag = FeedsFragment.LIKED_TAG
    }

    private fun setRemovedLikeButtonView(holder: PostVH) {
        holder.mLikeBtn.setImageResource(R.drawable.ic_like_not_liked)
        holder.mLikeBtn.tag = FeedsFragment.NOT_LIKED_TAG
    }

}