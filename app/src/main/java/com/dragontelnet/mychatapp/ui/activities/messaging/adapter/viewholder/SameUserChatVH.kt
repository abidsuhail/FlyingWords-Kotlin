package com.dragontelnet.mychatapp.ui.activities.messaging.adapter.viewholder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.model.entity.Chat

class SameUserChatVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val senderMsg: TextView = itemView.findViewById(R.id.public_sender_msg)
    private val senderStatus: TextView = itemView.findViewById(R.id.private_sender_status)
    private val senderMsgDt: TextView = itemView.findViewById(R.id.sender_date_time)
    fun bindSenderChatViews(senderHolder: SameUserChatVH, chat: Chat, position: Int, itemCount: Int, mRecyclerView: RecyclerView?) {
        senderHolder.senderMsg.text = chat.content
        senderHolder.senderMsgDt.text = chat.deviceDate + " " + chat.deviceTime
        senderHolder.senderStatus.text = chat.status
        //showing msg info on onClick msg
        senderHolder.senderMsg.setOnClickListener {
            if (senderHolder.senderMsgDt.visibility == View.VISIBLE) {
                senderHolder.senderMsgDt.visibility = View.GONE
            } else {
                senderHolder.senderMsgDt.visibility = View.VISIBLE
            }
        }
        if (position == itemCount - 1 && mRecyclerView != null) {
            senderHolder.senderStatus.visibility = View.VISIBLE
            // chatVH.senderStatus.setText(chat.getStatus());

            //hiding second last message status,because it does'nt hide automatically
            val secondLastVH = mRecyclerView.findViewHolderForAdapterPosition(itemCount - 2)
            if (secondLastVH is SameUserChatVH) {
                secondLastVH.senderStatus.visibility = View.GONE
            }
        } else {
            senderHolder.senderStatus.visibility = View.GONE
        }
    }

}