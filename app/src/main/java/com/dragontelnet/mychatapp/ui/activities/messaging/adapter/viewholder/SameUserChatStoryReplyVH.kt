package com.dragontelnet.mychatapp.ui.activities.messaging.adapter.viewholder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.model.entity.Chat
import com.facebook.drawee.view.SimpleDraweeView

class SameUserChatStoryReplyVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bindSameUserStoryReplyViews(holder: SameUserChatStoryReplyVH, chat: Chat, position: Int, itemCount: Int, mRecyclerView: RecyclerView?) {
        holder.sameUserMsgDt.text = chat.deviceDate + " " + chat.deviceTime
        holder.sameUserStatusTv.text = chat.status
        holder.sameUserStoryReplyContentTv.text = chat.content
        holder.sameUserStoryPhotoSdv.setImageURI(chat.storyPhotoLink)

        //showing msg info on onClick msg
        holder.itemView.setOnClickListener {
            if (holder.sameUserMsgDt.visibility == View.VISIBLE) {
                holder.sameUserMsgDt.visibility = View.GONE
            } else {
                holder.sameUserMsgDt.visibility = View.VISIBLE
            }
        }
        if (position == itemCount - 1 && mRecyclerView != null) {
            holder.sameUserStatusTv.visibility = View.VISIBLE

            // chatVH.senderStatus.setText(chat.getStatus());

            //hiding second last message status,because it does'nt hide automatically
            val secondLastVH = mRecyclerView.findViewHolderForAdapterPosition(itemCount - 2)
            if (secondLastVH is SameUserChatStoryReplyVH) {
                secondLastVH.sameUserStatusTv.visibility = View.GONE
            }
            if (secondLastVH is SameUserChatVH) {
                secondLastVH.senderStatus.visibility = View.GONE
            }
        } else {
            holder.sameUserStatusTv.visibility = View.GONE
        }
    }

    private val sameUserStoryReplyContentTv: TextView = itemView.findViewById(R.id.same_user_story_reply_content_tv)
    private val sameUserStoryPhotoSdv: SimpleDraweeView = itemView.findViewById(R.id.same_user_story_reply_photo_sdv)
    private val sameUserMsgDt: TextView = itemView.findViewById(R.id.same_user_date_time)
    val sameUserStatusTv: TextView = itemView.findViewById(R.id.same_user_status)

}