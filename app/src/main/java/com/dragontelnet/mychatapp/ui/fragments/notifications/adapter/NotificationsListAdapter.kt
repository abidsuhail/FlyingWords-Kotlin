package com.dragontelnet.mychatapp.ui.fragments.notifications.adapter

import android.content.Intent
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.model.entity.Post
import com.dragontelnet.mychatapp.model.entity.PostNotification
import com.dragontelnet.mychatapp.ui.activities.postdetailsview.PostDetailsViewActivity
import com.dragontelnet.mychatapp.ui.fragments.notifications.view.NotificationsFragment
import com.dragontelnet.mychatapp.utils.auth.CurrentUser
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs

class NotificationsListAdapter(private var notifsList: MutableList<PostNotification>,
                               private val notificationsFragment: NotificationsFragment) :
        RecyclerView.Adapter<PostNotificationVH>() {

    fun updateList(newNotifsList: MutableList<PostNotification>) {
        notifsList = newNotifsList
        notifyDataSetChanged()
    }

    override fun getItemCount() = notifsList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostNotificationVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.notification_layout, parent, false)
        return PostNotificationVH(view)
    }

    override fun onBindViewHolder(holder: PostNotificationVH, position: Int) {
        val notification = notifsList[position]
        if (notification.imgUrl?.isNotBlank()!!) {
            holder.notifPic.visibility = View.VISIBLE
            holder.notifPic.setImageURI(notification.imgUrl)
        } else {
            holder.notifPic.visibility = View.GONE
        }
        holder.notifOwnerPic.setImageURI(notification.notifOwnerProfilePic)
        holder.notifDateTime.text = "${notification.dateTime?.date} ${notification.dateTime?.time}"
        if (PostNotification.COMMENT_TYPE == notification.type) {
            if (notification.imgUrl == "") {
                val txt = Html.fromHtml("<b> ${notification.notifOwnerName} </b> commented on your status : ${notification.commentContent}")
                holder.notifCaption.text = txt
            } else {
                val txt = Html.fromHtml("<b> ${notification.notifOwnerName} </b> commented on your photo : ${notification.commentContent}")
                holder.notifCaption.text = txt
            }
        } else {
            if (notification.imgUrl == "") {
                val txt = Html.fromHtml("<b> ${notification.notifOwnerName} </b> liked your status : ${notification.commentContent}")
                holder.notifCaption.text = txt
            } else {
                val txt = Html.fromHtml("<b> ${notification.notifOwnerName} </b> liked your photo : ${notification.commentContent}")
                holder.notifCaption.text = txt
            }
        }

        holder.itemView.setOnClickListener {
            val i = Intent(notificationsFragment.context, PostDetailsViewActivity::class.java)
            val post: Post? = notification.post

            //post is null when post is deleted by post owner
            post?.let { postNonNull ->
                postNonNull.postOwnerName = notification.postOwnerName
                postNonNull.postOwnerProfilePic = notification.postOwnerProfilePic
                i.putExtra("post", postNonNull)
                notificationsFragment.startActivity(i)
            } ?: run {
                Toast.makeText(notificationsFragment.context, "Post was deleted by post owner!!", Toast.LENGTH_SHORT).show()
                MyFirestoreDbRefs.getNotificationCollectionRef(CurrentUser.getCurrentUser()?.uid)
                        .document(notification.notifId!!).delete().addOnSuccessListener {

                            if (holder.adapterPosition != RecyclerView.NO_POSITION) {
                                notifsList.removeAt(holder.adapterPosition)
                                notifyItemRemoved(holder.adapterPosition)
                            }
                        }
            }
        }
        holder.notifOwnerPic.setImageURI(notification.postOwnerProfilePic)
        if (PostNotification.COMMENT_TYPE == notification.type) {
            if (notification.imgUrl == "") {
                val txt = Html.fromHtml("<b> ${notification.postOwnerName} </b> commented on your status : ${notification.commentContent}")
                holder.notifCaption.text = txt
            } else {
                val txt = Html.fromHtml("<b> ${notification.postOwnerName} </b> commented on your photo : ${notification.commentContent}")
                holder.notifCaption.text = txt
            }
        } else {
            if (notification.imgUrl == "") {
                val txt = Html.fromHtml("<b> ${notification.postOwnerName} </b> liked your status : ${notification.commentContent}")
                holder.notifCaption.text = txt
            } else {
                val txt = Html.fromHtml("<b> ${notification.postOwnerName} </b> liked your photo : ${notification.commentContent}")
                holder.notifCaption.text = txt
            }

            holder.itemView.setOnClickListener {
                val i = Intent(notificationsFragment.context, PostDetailsViewActivity::class.java)
                val post = notification.post
                post?.postOwnerName = notification.postOwnerName
                post?.postOwnerProfilePic = notification.postOwnerProfilePic
                i.putExtra("post", post)
                notificationsFragment.startActivity(i)
            }

        }

    }
}
