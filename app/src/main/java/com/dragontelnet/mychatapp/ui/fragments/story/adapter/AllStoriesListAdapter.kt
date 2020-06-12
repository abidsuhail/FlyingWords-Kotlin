package com.dragontelnet.mychatapp.ui.fragments.story.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.model.entity.Story
import com.dragontelnet.mychatapp.ui.activities.storyviewer.StoryViewerActivity
import com.dragontelnet.mychatapp.ui.fragments.story.adapter.viewholder.StoryVH
import com.dragontelnet.mychatapp.ui.fragments.story.view.StoryFragment
import com.dragontelnet.mychatapp.ui.fragments.story.viewmodel.StoryViewModel

class AllStoriesListAdapter constructor(private var mStoryList: List<Story>,
                                        private val storyFragment: StoryFragment,
                                        private val mViewModel: StoryViewModel?) : RecyclerView.Adapter<StoryVH>() {

    override fun getItemCount() = mStoryList.size

    fun updateStoryList(story: List<Story>) {
        mStoryList = story
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: StoryVH, position: Int) {

        val story = mStoryList[position]
        story.storyItemList?.lastOrNull()?.let { lastStory ->
            holder.showAllViews()
            destroyStory(story)
            holder.bindStoryToViews(holder, lastStory)
            holder.fullNameTv.text = story.ownerName
        }

        holder.view.setOnClickListener {
            val intent = Intent(storyFragment.context, StoryViewerActivity::class.java)
            intent.putExtra("story", story)
            storyFragment.startActivity(intent)
        }
    }

    private fun destroyStory(story: Story) {
        mViewModel?.destroyStory(story)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryVH {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.story_layout, parent, false)
        return StoryVH(view)
    }

}