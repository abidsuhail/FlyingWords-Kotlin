package com.dragontelnet.mychatapp.ui.activities.friends

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.model.entity.User
import com.dragontelnet.mychatapp.ui.activities.friends.adapter.FriendsListAdapter
import com.dragontelnet.mychatapp.ui.activities.friends.viewmodel.FriendsActivityViewModel
import kotlinx.android.synthetic.main.activity_friends.*

class FriendsActivity : AppCompatActivity() {

    @BindView(R.id.friends_rv)
    lateinit var friendsRv: RecyclerView

    @BindView(R.id.friends_progress)
    lateinit var friendsProgress: ProgressBar

    @BindView(R.id.friends_error_msg_tv)
    lateinit var friendsErrorMsgTv: TextView

    private var mViewModel: FriendsActivityViewModel? = null
    private var adapter: FriendsListAdapter? = null
    private var mFriendList = emptyList<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends)
        ButterKnife.bind(this)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mViewModel = ViewModelProvider(this).get(FriendsActivityViewModel::class.java)

    }

    override fun onStart() {
        super.onStart()
        populateFriendsList()
    }

    private fun populateFriendsList() {
        adapter = FriendsListAdapter(friendsProgress, this, emptyList())
        friendsRv.adapter = adapter
        mViewModel?.getFriendListLive()?.observe(this, Observer { friendList ->
            if (friendList.isNotEmpty()) {
                friendsErrorMsgTv.visibility = View.GONE
                // and friend progress visibility will be removed by onBindViewHolder
                mFriendList = friendList
                adapter?.updateFriendList(mFriendList.sortedBy { it.name })
            } else {
                // list is empty
                friendsErrorMsgTv.visibility = View.VISIBLE
                friendsProgress.visibility = View.GONE
                mFriendList = emptyList()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)
        menu?.let {
            setUpSearchView(it)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setUpSearchView(menu: Menu) {
        val searchMenuItem = menu.findItem(R.id.search_bar)
        val searchView = searchMenuItem.actionView as SearchView
        //Query listener
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                // adapter?.updateOptions(getSearchingOptions(query))
                if (mFriendList.isNotEmpty()) {
                    val queriedList = mFriendList.filter { it.name?.startsWith(query, true)!! }.sortedBy { it.name }
                    adapter?.updateFriendList(queriedList)
                }
                return true
            }
        })
        //search view focus listener
        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                //adapter?.updateOptions(defaultPagingOptions())
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // adapter?.stopListening()
        mViewModel?.removeDbListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewModel?.removeDbListeners()
    }
}