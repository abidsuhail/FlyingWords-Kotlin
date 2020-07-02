package com.dragontelnet.mychatapp.ui.fragments.story.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.Observer
import butterknife.BindView
import butterknife.ButterKnife
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.model.entity.StoryItem
import com.dragontelnet.mychatapp.ui.activities.messaging.viewmodel.MessagingViewModel
import com.dragontelnet.mychatapp.ui.activities.storyviewer.StoryViewerActivity
import com.facebook.drawee.view.SimpleDraweeView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class StoryReplyBottomFragment(private val messagingViewModel: MessagingViewModel?, private val storyByUid: String, private val activity: StoryViewerActivity) : BottomSheetDialogFragment() {
    @BindView(R.id.story_reply_btn)
    lateinit var storyReplyBtn: SimpleDraweeView

    @BindView(R.id.story_reply_image_sdv)
    lateinit var storyReplySdv: SimpleDraweeView

    @BindView(R.id.story_reply_et)
    lateinit var storyReplyEt: EditText

    private var mListener: StoryReplyBottomSheetListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            mListener = context as StoryReplyBottomSheetListener
        } catch (e: ClassCastException) {
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.story_reply_bottom_sheet, container, false)
        ButterKnife.bind(this, view)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val storyItem = arguments?.getSerializable(StoryViewerActivity.STORY_ITEM_INTENT_KEY) as? StoryItem

        storyReplySdv.setImageURI(storyItem?.imageUrl)

        storyReplyBtn.setOnClickListener {
            val reply = storyReplyEt.text.trim().toString()
            if (reply.isNotBlank()) {
                Toast.makeText(activity, "Sending reply...", Toast.LENGTH_SHORT).show()
                activity.supportFragmentManager.beginTransaction().remove(this).commit()

                messagingViewModel?.sendMessage(reply, storyByUid, activity, storyItem?.imageUrl)?.observe(activity, Observer {
                    if (it) {

                        //sending http notification
                        messagingViewModel.getUser(storyByUid).observe(activity, Observer { storyOwnerUser ->
                            messagingViewModel.sendNotification(storyOwnerUser, storyReplyEt.text.trim().toString(), activity)
                        })
                    }
                })
            }
        }
    }

    interface StoryReplyBottomSheetListener {
        fun onStoryReplySheetDismiss()
        fun onStoryReplySheetShown()
    }

    override fun onStart() {
        super.onStart()
        mListener?.onStoryReplySheetShown()
    }

    override fun onDestroy() {
        super.onDestroy()
        mListener?.onStoryReplySheetDismiss()
        mListener = null
    }
}