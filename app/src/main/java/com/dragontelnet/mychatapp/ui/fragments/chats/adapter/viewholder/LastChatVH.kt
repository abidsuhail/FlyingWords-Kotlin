package com.dragontelnet.mychatapp.ui.fragments.chats.adapter.viewholder

import android.graphics.Typeface
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.model.entity.Chat
import com.dragontelnet.mychatapp.model.entity.User
import com.dragontelnet.mychatapp.utils.MyConstants.FirestoreKeys
import com.dragontelnet.mychatapp.utils.auth.CurrentUser.getCurrentUser
import com.facebook.drawee.view.SimpleDraweeView

class LastChatVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val fname: TextView = itemView.findViewById(R.id.friend_fname)
    val lastMsg: TextView = itemView.findViewById(R.id.friend_gender)
    val bio: TextView = itemView.findViewById(R.id.friend_bio)
    val profilePic: SimpleDraweeView = itemView.findViewById(R.id.friend_pic)
    private val TAG = "LastChatVH"
    fun bindLastChatViewsDetails(holder: LastChatVH, lastChat: Chat, greyColor: Int, primaryColor: Int) {
        holder.lastMsg.text = lastChat.content
        holder.bio.text = lastChat.deviceTime
        if (lastChat.isTyping == "typing") {
            //if receiver is typing...
            //show typing status
            holder.lastMsg.setTextColor(primaryColor)
            holder.lastMsg.typeface = Typeface.DEFAULT_BOLD
            holder.lastMsg.text = FirestoreKeys.TYPING_FIELD
        } else {
            checkLastMsgOwner(holder, lastChat, greyColor, primaryColor)

        }
    }

    private fun checkLastMsgOwner(holder: LastChatVH, chat: Chat, greyColor: Int, primaryColor: Int) {
        if (chat.byUid == getCurrentUser()!!.uid) {
            //my sent last message
            holder.lastMsg.setTextColor(greyColor)
            holder.lastMsg.typeface = Typeface.DEFAULT
        } else {
            //receiver's sent last message
            if (chat.status == FirestoreKeys.SEEN) {
                //receiver's last message is seen
                //set last msg color to grey
                holder.lastMsg.setTextColor(greyColor)
                holder.lastMsg.typeface = Typeface.DEFAULT
            } else {
                //receiver's last message is delivered
                //set last msg color to primary
                holder.lastMsg.setTextColor(primaryColor)
                holder.lastMsg.typeface = Typeface.DEFAULT_BOLD
            }
        }
        if (chat.content?.length!! >= 20) {
            //content length > 20 ,replacing break line with spaces and placing ...
            val modifiedContent = chat.content?.substring(0, 20)
                    ?.replace("\\r\\n|\\r|\\n".toRegex(), " ")?.trim { it <= ' ' } + "....."
            holder.lastMsg.text = modifiedContent
        } else {
            val modifiedContent = chat.content?.replace("\\r\\n|\\r|\\n".toRegex(), " ")
            holder.lastMsg.text = modifiedContent
        }
    }

    fun bindChatUserDetails(holder: LastChatVH, mUser: User?) {
        mUser?.let { user ->
            holder.fname.text = user.name
            if (user.profilePic == "") {
                if (user.gender == "male") {
                    holder.profilePic.setImageResource(R.drawable.user_male_placeholder)
                } else {
                    holder.profilePic.setImageResource(R.drawable.user_female_placeholder)
                }
            } else {
                holder.profilePic.setImageURI(user.profilePic)
            }
        }
    }

}