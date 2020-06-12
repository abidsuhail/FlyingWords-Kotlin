package com.dragontelnet.mychatapp.ui.fragments.requests.view

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
import com.dragontelnet.mychatapp.ui.fragments.requests.adapter.FriendRequestListAdapter
import com.dragontelnet.mychatapp.ui.fragments.requests.viewmodel.RequestsViewModel

class RequestsFragment : Fragment() {
    @BindView(R.id.requests_rv)
    lateinit var requestsRv: RecyclerView

    @BindView(R.id.requests_progress)
    lateinit var requestsProgress: ProgressBar

    @BindView(R.id.request_error_tv)
    lateinit var requestErrorTv: TextView

    private var mViewModel: RequestsViewModel? = null
    private var adapter: FriendRequestListAdapter? = null

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_requests, container, false)
        ButterKnife.bind(this, root)
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val toolbarTxt = activity?.findViewById<TextView>(R.id.toolbar_title)
        toolbarTxt?.text = resources.getString(R.string.title_requests)
        mViewModel = ViewModelProvider(this).get(RequestsViewModel::class.java)
        mViewModel?.startAllReqSeenListener() //start req seen listener
        adapter = FriendRequestListAdapter(mViewModel, emptyList(), this, requestsProgress)
        requestsRv.adapter = adapter

        populateRequestsList()

    }

    private fun populateRequestsList() {
        requestsProgress.visibility = View.VISIBLE

        mViewModel?.getRequestsListLive()?.observe(this, Observer { reqList ->

            if (reqList.isEmpty()) {
                //empty
                requestErrorTv.visibility = View.VISIBLE
                requestsProgress.visibility = View.GONE
            } else {
                requestErrorTv.visibility = View.GONE
            }
            adapter?.updateList(reqList)
        })
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            //fragment hidden
            mViewModel?.removeAllListeners()
        } else {
            val toolbarTxt = activity?.findViewById<TextView>(R.id.toolbar_title)
            toolbarTxt?.text = resources.getString(R.string.title_requests)

            populateRequestsList()
            //fragment shown
            mViewModel?.startAllReqSeenListener() //starting req seen listener
        }
    }

    companion object {
        private const val TAG = "RequestsFragment"
    }
}