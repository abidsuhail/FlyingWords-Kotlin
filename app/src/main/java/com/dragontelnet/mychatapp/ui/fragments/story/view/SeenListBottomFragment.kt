package com.dragontelnet.mychatapp.ui.fragments.story.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.model.entity.SeenIds
import com.dragontelnet.mychatapp.model.entity.StoryItem
import com.dragontelnet.mychatapp.model.entity.User
import com.dragontelnet.mychatapp.ui.activities.storyviewer.StoryViewerActivity
import com.dragontelnet.mychatapp.ui.fragments.story.adapter.viewholder.SeenStoryVH
import com.dragontelnet.mychatapp.utils.MyConstants
import com.dragontelnet.mychatapp.utils.auth.CurrentUser.getCurrentUser
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs.allUsersCollection
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs.rootRef
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.CollectionReference

class SeenListBottomFragment : BottomSheetDialogFragment() {

    @BindView(R.id.seen_peoples_rv)
    lateinit var seenPeoplesRv: RecyclerView

    @BindView(R.id.my_toolbar)
    lateinit var myToolbar: Toolbar

    private var adapter: FirestoreRecyclerAdapter<SeenIds, SeenStoryVH>? = null
    private var mListener: BottomFragmentSheetListener? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            mListener = context as BottomFragmentSheetListener?
        } catch (e: ClassCastException) {
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.seen_bottom_sheet_layout, parent, false)
        ButterKnife.bind(this, view)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        populateSeenList()
    }

    private fun populateSeenList() {
        val storyItem: StoryItem? = arguments?.getSerializable(StoryViewerActivity.STORY_ITEM_INTENT_KEY) as StoryItem
        val query: CollectionReference? = getCurrentUser()?.uid?.let {
            rootRef.collection(MyConstants.FirestoreCollection.SEEN_STORY_UIDS)
                    .document(it)
                    .collection(storyItem?.timeStamp.toString())
        }

        query?.let {
            val options: FirestoreRecyclerOptions<SeenIds> = FirestoreRecyclerOptions.Builder<SeenIds>()
                    .setQuery(it, SeenIds::class.java)
                    .build()

            adapter = object : FirestoreRecyclerAdapter<SeenIds, SeenStoryVH>(options) {
                override fun onBindViewHolder(holder: SeenStoryVH, position: Int, seenIds: SeenIds) {
                    holder.seenByUid = seenIds.seenByUid
                    holder.profilePic.setImageResource(R.drawable.user_male_placeholder)
                    seenIds.seenByUid?.let { seenUid ->
                        allUsersCollection.document(seenUid)
                                .addSnapshotListener { documentSnapshot, e ->
                                    if (documentSnapshot != null && documentSnapshot.exists()) {
                                        val user: User? = documentSnapshot.toObject(User::class.java)
                                        if (holder.seenByUid != null && holder.seenByUid == user?.uid) {
                                            holder.fullNameTv.text = user?.name
                                            if (user?.profilePic != "") {
                                                holder.profilePic.setImageURI(user?.profilePic)
                                            } else {
                                                if (user.gender == "male") {
                                                    holder.profilePic.setActualImageResource(R.drawable.user_male_placeholder)
                                                } else {
                                                    holder.profilePic.setActualImageResource(R.drawable.user_female_placeholder)
                                                }
                                            }
                                        }
                                    }
                                }
                    }
                    holder.dateTimeTv.text = seenIds.seenByDate + "," + seenIds.seenByTime
                }

                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeenStoryVH {
                    val view: View = LayoutInflater.from(parent.context).inflate(R.layout.seen_item_layout, parent, false)
                    return SeenStoryVH(view)
                }
            }

            seenPeoplesRv.adapter = adapter
            adapter?.startListening()

            query.get().addOnSuccessListener { snapshot ->
                myToolbar.title = "Viewed by " + snapshot.documents.size
            }.addOnFailureListener { e -> Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show() }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter?.stopListening()
        mListener?.onBottomFragmentSheetDismiss()
        mListener = null
    }

    override fun onStart() {
        super.onStart()
        mListener?.onBottomFragmentSheetShown()
    }

    interface BottomFragmentSheetListener {
        fun onBottomFragmentSheetDismiss()
        fun onBottomFragmentSheetShown()
    }

    companion object {
        private val TAG: String = "SeenListBottomFragment"
    }
}