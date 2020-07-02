package com.dragontelnet.mychatapp.ui.fragments.notifications.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.model.entity.Post
import com.dragontelnet.mychatapp.model.entity.PostNotification
import com.dragontelnet.mychatapp.ui.activities.postdetailsview.PostDetailsViewActivity
import com.dragontelnet.mychatapp.ui.activities.profile.view.ProfileActivity
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
        holder.bindNotifDetails(holder, notification)

        holder.bindNotifUserDetails(holder, notification)


        holder.notifCaption.setOnClickListener {
            notification.notifOwnerUser?.let { user ->
                val i = Intent(notificationsFragment.context, ProfileActivity::class.java).apply { putExtra("user", user) }
                notificationsFragment.startActivity(i)
            }
        }

        holder.itemView.setOnClickListener {
            val i = Intent(notificationsFragment.context, PostDetailsViewActivity::class.java)
            val post: Post? = notification.post

            //post is null when post is deleted by post owner
            post?.let { postNonNull ->

                //sending data of post owner to post owner activity
                postNonNull.postOwnerName = notification.postOwnerName
                postNonNull.postOwnerProfilePic = notification.postOwnerProfilePic
                i.putExtra("post", postNonNull)
                notificationsFragment.startActivity(i)
            } ?: run {
                delNotif(notification, holder)
            }
        }

    }

    private fun delNotif(notification: PostNotification, holder: PostNotificationVH) {

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
