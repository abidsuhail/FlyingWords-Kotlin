package com.dragontelnet.mychatapp.ui.fragments.chats.view

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.ui.activities.friends.FriendsActivity
import com.dragontelnet.mychatapp.ui.fragments.chats.adapter.ChatsListAdapter
import com.dragontelnet.mychatapp.ui.fragments.chats.viewmodel.ChatsFragmentViewModel
import kotlinx.android.synthetic.main.fragment_chats.*


class ChatsFragment : Fragment() {
    @BindView(R.id.chats_rv)
    lateinit var chatsRv: RecyclerView

    @BindView(R.id.older_chats_progress)
    lateinit var olderChatsProgress: ProgressBar

    @BindView(R.id.older_no_chats_tv)
    lateinit var olderNoChatsTv: TextView

    private var mViewModel: ChatsFragmentViewModel? = null
    private var adapter: ChatsListAdapter? = null

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_chats, container, false)
        ButterKnife.bind(this, root)

        val toolbarTxt = activity?.findViewById<TextView>(R.id.toolbar_title)
        toolbarTxt?.text = resources.getString(R.string.title_chats)

        mViewModel = ViewModelProvider(this).get(ChatsFragmentViewModel::class.java)
        adapter = ChatsListAdapter(olderChatsProgress, this, mutableListOf())
        chatsRv.adapter = adapter

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fab_new_msg.setOnClickListener {
            startActivity(Intent(activity, FriendsActivity::class.java))
        }
        fab_new_msg.setColorFilter(Color.WHITE)
        populateChats()
    }

    private fun populateChats() {

        mViewModel?.getLastChatListLive()?.observe(this, Observer { chatList ->
            adapter?.updateChatList(chatList.toMutableList())
            if (chatList.isNotEmpty()) {
                //not empty
                olderNoChatsTv.visibility = View.GONE
            } else {
                //empty
                olderNoChatsTv.visibility = View.VISIBLE
                olderChatsProgress.visibility = View.GONE
            }
        })
    }


    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            //fragment hidden
            //removing listeners
            mViewModel?.removeAllListeners()
        } else {
            //fragment shown
            populateChats()

            //setting title
            val toolbarTxt = activity?.findViewById<TextView>(R.id.toolbar_title)
            toolbarTxt?.text = resources.getString(R.string.title_chats)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //removing listeners
        adapter = null
        mViewModel?.removeAllListeners()
    }
}