package com.dragontelnet.mychatapp.ui.activities.showallmystories

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.model.entity.Story
import com.dragontelnet.mychatapp.model.entity.StoryItem
import com.dragontelnet.mychatapp.ui.activities.showallmystories.adapter.MyStoryListAdapter

class ShowMyAllStoryActivity : AppCompatActivity() {

    @BindView(R.id.my_story_list_rv)
    lateinit var myStoryListRv: RecyclerView

    @BindView(R.id.my_toolbar)
    lateinit var myToolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_my_all_story)
        ButterKnife.bind(this)
        setSupportActionBar(myToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val story = intent.getSerializableExtra("story") as Story

        val storyItemList: MutableList<StoryItem>? = story.storyItemList

        val adapter = story.byUid?.let { storyItemList?.let { stItem -> MyStoryListAdapter(it, stItem) } }
        myStoryListRv.layoutManager = LinearLayoutManager(this)
        myStoryListRv.adapter = adapter
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}