package com.dragontelnet.mychatapp.ui.activities.messaging.adapter.viewholder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.model.entity.Chat
import com.dragontelnet.mychatapp.model.entity.User
import com.facebook.drawee.view.SimpleDraweeView

class DifferentUserChatStoryReplyVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bindDifferentUserStoryReplyViews(holder: DifferentUserChatStoryReplyVH, chat: Chat, receiverUser: User?) {
        holder.receiverMsgDt.text = chat.deviceDate + " " + chat.deviceTime
        holder.receiverMsg.text = chat.content
        holder.receiverStoryReplyImageSdv.setImageURI(chat.storyPhotoLink)
        holder.receiverProfileImage.setImageURI(receiverUser?.profilePic)

        //showing msg info on onClick msg
        holder.itemView.setOnClickListener {
            if (holder.receiverMsgDt.visibility == View.VISIBLE) {
                holder.receiverMsgDt.visibility = View.GONE
            } else {
                holder.receiverMsgDt.visibility = View.VISIBLE
            }
        }
    }

    private val receiverMsg: TextView = itemView.findViewById(R.id.receiver_msg)
    private val receiverMsgDt: TextView = itemView.findViewById(R.id.receiver_date_time)
    private val receiverProfileImage: SimpleDraweeView = itemView.findViewById(R.id.public_receiver_msg_image)
    private val receiverStoryReplyImageSdv: SimpleDraweeView = itemView.findViewById(R.id.story_reply_different_user_sdv)

}