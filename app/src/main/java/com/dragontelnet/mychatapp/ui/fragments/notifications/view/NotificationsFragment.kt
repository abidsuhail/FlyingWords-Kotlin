package com.dragontelnet.mychatapp.ui.fragments.notifications.view

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
import com.dragontelnet.mychatapp.ui.fragments.notifications.adapter.NotificationsListAdapter
import com.dragontelnet.mychatapp.ui.fragments.notifications.viewmodel.NotificationsFragmentViewModel

class NotificationsFragment : Fragment() {

    private var mViewModel: NotificationsFragmentViewModel? = null
    private var adapter: NotificationsListAdapter? = null

    @BindView(R.id.notifications_rv)
    lateinit var notifRv: RecyclerView

    @BindView(R.id.notif_progress)
    lateinit var progressBar: ProgressBar

    @BindView(R.id.notifs_error_tv)
    lateinit var noNotifsTv: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_notifications, container, false)
        ButterKnife.bind(this, view)
        mViewModel = ViewModelProvider(this).get(NotificationsFragmentViewModel::class.java)
        mViewModel?.startNotifSeenListener()
        val toolbarTxt = activity?.findViewById<TextView>(R.id.toolbar_title)
        toolbarTxt?.text = resources.getString(R.string.title_notification)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mViewModel = ViewModelProvider(this).get(NotificationsFragmentViewModel::class.java)

        adapter = NotificationsListAdapter(mutableListOf(), this)
        notifRv.adapter = adapter
        populateNotifsList()
    }

    private fun populateNotifsList() {
        mViewModel?.getLiveNotifsList()?.observe(this, Observer { notifsList ->
            progressBar.visibility = View.GONE
            if (notifsList.isEmpty()) {
                //show error tv
                noNotifsTv.visibility = View.VISIBLE
            } else {
                //hide error tv
                noNotifsTv.visibility = View.GONE
            }
            val updatedSortedList = notifsList.sortedByDescending { it.dateTime?.timeStamp }.toMutableList()
            adapter?.updateList(updatedSortedList)
        })
    }

    override fun onStop() {
        super.onStop()
        // mViewModel?.removeAllListeners()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            mViewModel?.removeAllListeners()
        } else {
            //shown
            val toolbarTxt = activity?.findViewById<TextView>(R.id.toolbar_title)
            toolbarTxt?.text = resources.getString(R.string.title_notification)
            populateNotifsList()
            mViewModel?.startNotifSeenListener()
        }
    }
}