package com.dragontelnet.mychatapp.ui.activities.likers.view

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.model.entity.Post
import com.dragontelnet.mychatapp.ui.activities.likers.adapter.LikersListAdapter
import com.dragontelnet.mychatapp.ui.activities.likers.viewmodel.LikersActivityViewModel
import kotlinx.android.synthetic.main.activity_likers.*

class LikersActivity : AppCompatActivity() {

    var mViewModel: LikersActivityViewModel? = null
    var adapter: LikersListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_likers)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mViewModel = ViewModelProvider(this)[LikersActivityViewModel::class.java]

        adapter = LikersListAdapter(this, emptyList())
        likers_rv.adapter = adapter

        updateLikersList()
    }

    private fun updateLikersList() {
        mViewModel?.getLikersUsersList(getIntentPost())?.observe(this, Observer { usersList ->
            likers_progress.visibility = View.GONE
            adapter?.updateList(usersList)
        })
    }

    private fun getIntentPost(): Post {
        return intent.getSerializableExtra("post") as Post
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}