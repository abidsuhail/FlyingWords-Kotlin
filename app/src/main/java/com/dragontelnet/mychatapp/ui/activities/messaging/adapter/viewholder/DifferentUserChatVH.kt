package com.dragontelnet.mychatapp.ui.activities.messaging.adapter.viewholder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.model.entity.Chat
import com.facebook.drawee.view.SimpleDraweeView

class DifferentUserChatVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val receiverMsg: TextView = itemView.findViewById(R.id.receiver_msg)
    private val receiverMsgDt: TextView = itemView.findViewById(R.id.receiver_date_time)
    private val receiverProfileImage: SimpleDraweeView = itemView.findViewById(R.id.public_receiver_msg_image)
    fun bindReceiverChatViews(receiverHolder: DifferentUserChatVH, chat: Chat) {
        receiverHolder.receiverMsg.text = chat.content
        receiverHolder.receiverMsgDt.text = chat.deviceDate + " " + chat.deviceTime

        //showing msg info on onClick msg
        receiverHolder.receiverMsg.setOnClickListener {
            if (receiverHolder.receiverMsgDt.visibility == View.VISIBLE) {
                receiverHolder.receiverMsgDt.visibility = View.GONE
            } else {
                receiverHolder.receiverMsgDt.visibility = View.VISIBLE
            }
        }
    }

}