package com.dragontelnet.mychatapp.ui.fragments.requests.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.model.entity.FriendRequest
import com.dragontelnet.mychatapp.model.entity.User
import com.dragontelnet.mychatapp.ui.activities.profile.view.ProfileActivity
import com.dragontelnet.mychatapp.ui.fragments.requests.adapter.viewholder.FriendReqVH
import com.dragontelnet.mychatapp.ui.fragments.requests.view.RequestsFragment
import com.dragontelnet.mychatapp.ui.fragments.requests.viewmodel.RequestsViewModel

class FriendRequestListAdapter(private val mViewModel: RequestsViewModel?,
                               private var friendReqList: List<FriendRequest>,
                               private val requestsFragment: RequestsFragment,
                               private val requestsProgress: ProgressBar) : RecyclerView.Adapter<FriendReqVH>() {

    fun updateList(newReqList: List<FriendRequest>) {
        friendReqList = newReqList
        notifyDataSetChanged()
    }

    override fun getItemCount() = friendReqList.size
    override fun onBindViewHolder(holder: FriendReqVH, position: Int) {

        requestsProgress.visibility = View.GONE
        val request = friendReqList[position]
        holder.bindUserDetails(request.user!!, holder)
        holder.bindRequestButtons(request, holder)
        setUpOnClickListeners(request.user, holder)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendReqVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.friend_req_layout, parent, false)
        return FriendReqVH(view)
    }

    private fun setUpOnClickListeners(user: User?, holder: FriendReqVH) {

        //start profile fragment
        holder.itemView.setOnClickListener { startProfileActivity(user) }
        setConfirmBtnListener(user, holder)
        setDeclineBtnListener(user, holder)
        setCancelBtnListener(user, holder)
    }

    private fun setCancelBtnListener(user: User?, holder: FriendReqVH) {
        //cancel sent request
        holder.cancelSentRequest.setOnClickListener { v: View? ->
            holder.cancelSentRequest.isEnabled = false
            //decline request and cancel sent request functionality is same
            user?.uid?.let {
                mViewModel?.declineRequest(it)
                        ?.observe(requestsFragment.viewLifecycleOwner, Observer { isSuccess ->
                            holder.cancelSentRequest.isEnabled = true
                        })
            }
        }
    }

    private fun setDeclineBtnListener(user: User?, holder: FriendReqVH) {
        //decline request
        holder.declineBtn.setOnClickListener { v: View? ->
            holder.confirmBtn.isEnabled = false
            holder.declineBtn.isEnabled = false
            //decline request and cancel sent request functionality is same
            user?.uid?.let {
                mViewModel?.declineRequest(it)
                        ?.observe(requestsFragment.viewLifecycleOwner, Observer {
                            holder.confirmBtn.isEnabled = true
                            holder.declineBtn.isEnabled = true
                        })
            }
        }
    }

    private fun setConfirmBtnListener(user: User?, holder: FriendReqVH) {
        //accept request
        holder.confirmBtn.setOnClickListener {
            holder.confirmBtn.isEnabled = false
            holder.declineBtn.isEnabled = false
            user?.uid?.let { uid ->
                requestsFragment.activity?.let { activity ->
                    mViewModel?.acceptRequest(uid, activity)
                            ?.observe(requestsFragment.viewLifecycleOwner, Observer {
                                holder.confirmBtn.isEnabled = true
                                holder.declineBtn.isEnabled = true

                            })
                }
            }
        }
    }

    private fun startProfileActivity(user: User?) {
        val i = Intent(requestsFragment.context, ProfileActivity::class.java)
        i.putExtra("user", user)
        requestsFragment.startActivity(i)
    }


}