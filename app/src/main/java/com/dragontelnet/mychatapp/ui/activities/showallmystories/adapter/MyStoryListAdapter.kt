package com.dragontelnet.mychatapp.ui.activities.showallmystories.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.model.entity.Story
import com.dragontelnet.mychatapp.model.entity.StoryItem
import com.dragontelnet.mychatapp.ui.activities.showallmystories.adapter.viewholder.MyStoryVH
import com.dragontelnet.mychatapp.utils.MyConstants.FirestoreCollection
import com.dragontelnet.mychatapp.utils.auth.CurrentUser.getCurrentUser
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs
import com.google.firebase.firestore.FieldValue

class MyStoryListAdapter(private val storyOwnerUid: String, private val storyItemList: MutableList<StoryItem>) : RecyclerView.Adapter<MyStoryVH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyStoryVH {
        val view = LayoutInflater
                .from(parent.context).inflate(R.layout.my_story_item_layout, parent, false)
        return MyStoryVH(view)
    }

    override fun onBindViewHolder(holder: MyStoryVH, position: Int) {
        val storyItem = storyItemList[position]
        holder.myStoryPhoto.setImageURI(storyItem.imageUrl)
        setStoryItemCount(storyItem, holder)
        holder.dateAndTimeTv.text = storyItem.date + "," + storyItem.time
        holder.deleteStoryIv.setOnClickListener {
            MyFirestoreDbRefs.getAllStoriesDocumentRefOfUid(getCurrentUser()?.uid)
                    .get()
                    .addOnSuccessListener { documentSnapshot ->
                        val story = documentSnapshot.toObject(Story::class.java)
                        if (story != null && story.storyItemList?.size!! <= 1) {
                            MyFirestoreDbRefs
                                    .getAllStoriesDocumentRefOfUid(getCurrentUser()?.uid)
                                    .delete().addOnSuccessListener { //removing from list
                                        if (holder.adapterPosition > RecyclerView.NO_POSITION) {
                                            storyItemList.removeAt(holder.adapterPosition)
                                            notifyItemRemoved(holder.adapterPosition)
                                        }
                                    }
                        } else {
                            MyFirestoreDbRefs.getAllStoriesDocumentRefOfUid(getCurrentUser()?.uid)
                                    .update(FirestoreCollection.STORIES_ITEM_LIST_ARRAY, FieldValue.arrayRemove(storyItem))
                                    .addOnSuccessListener { //removing from list
                                        if (holder.adapterPosition > RecyclerView.NO_POSITION) {
                                            //storyItemList.remove(storyItemList[holder.adapterPosition])
                                            storyItemList.removeAt(holder.adapterPosition)
                                            notifyItemRemoved(holder.adapterPosition)
                                        }
                                    }
                        }
                    }
        }
    }

    private fun setStoryItemCount(storyItem: StoryItem, holder: MyStoryVH) {
        MyFirestoreDbRefs.rootRef.collection(FirestoreCollection.SEEN_STORY_UIDS)
                .document(storyOwnerUid)
                .collection(storyItem.timeStamp.toString()).addSnapshotListener { snapshot, e ->
                    if (snapshot != null) {
                        holder.viewsCountTv.text = "${snapshot.size()} views"
                    } else {
                        holder.viewsCountTv.text = "0 views"
                    }
                }
    }

    override fun getItemCount(): Int {
        return storyItemList.size
    }

    init {
        notifyDataSetChanged()
    }
}