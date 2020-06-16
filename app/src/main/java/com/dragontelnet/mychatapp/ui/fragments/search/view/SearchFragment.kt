package com.dragontelnet.mychatapp.ui.fragments.search.view

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import butterknife.ButterKnife
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.model.entity.User
import com.dragontelnet.mychatapp.ui.fragments.search.adapter.AllUsersListAdapter
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs.allUsersCollection
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.firebase.firestore.Query

class SearchFragment : Fragment() {

    @BindView(R.id.find_friends_rv)
    lateinit var findFriendsRv: RecyclerView

    @BindView(R.id.swipe_refresh_layout)
    lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private var adapter: AllUsersListAdapter? = null
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_search, container, false)
        ButterKnife.bind(this, root)
        setHasOptionsMenu(true)
        val toolbarTxt = activity?.findViewById<TextView>(R.id.toolbar_title)
        toolbarTxt?.text = resources.getString(R.string.title_search)
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        populateUsersList()
        swipeRefreshLayout.setOnRefreshListener {
            adapter?.updateOptions(defaultPagingOptions())
        }
    }

    private fun populateUsersList() {
        adapter = AllUsersListAdapter(defaultPagingOptions(), this, swipeRefreshLayout)
        findFriendsRv.adapter = adapter
        adapter?.startListening()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.search_menu, menu)
        setUpSearchView(menu)
    }

    private fun setUpSearchView(menu: Menu) {
        val searchMenuItem = menu.findItem(R.id.search_bar)
        val searchView = searchMenuItem.actionView as SearchView
        //searchView.queryHint = "Search name or phone with country code..."
        //Query listener
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                val capitalizeQuery = query.capitalize()
                getSearchingOptions(capitalizeQuery)?.let {
                    adapter?.updateOptions(it)
                    return true
                } ?: run {
                    adapter?.updateOptions(defaultPagingOptions())
                    return false
                }
            }
        })

        //search view focus listener
        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                adapter?.updateOptions(defaultPagingOptions())
                searchView.isIconified = true
            }
        }
    }

    private fun buildPagingOptions(field: String, searchQuery: String): FirestorePagingOptions<User> {
        return FirestorePagingOptions.Builder<User>()
                .setLifecycleOwner(this)
                .setQuery(getSearchQuery(field, searchQuery), config, User::class.java)
                .build()
    }

    private fun getSearchingOptions(query: String): FirestorePagingOptions<User>? {
        if (query.isNotBlank()) {
            if (query[0].isLetterOrDigit()) {
                //checking first letter

                return if (query[0].isDigit()) {
                    //is digit like 1,2.....etc
                    val newQuery = query.padStart(query.length + 1, '+') //adding plus '+' at starting of phone
                    buildPagingOptions("phone", newQuery)
                } else {
                    //is eng. letter a,b,c...etc
                    buildPagingOptions("name", query)
                }
            } else {
                return if (query[0] != '@') {
                    //special char i.e +
                    buildPagingOptions("phone", query)
                } else {
                    //special char i.e @
                    val newQuery = query.removePrefix("@")
                    buildPagingOptions("username", newQuery)
                }
            }
        } else
            return null
    }

    private val config: PagedList.Config
        get() = PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(10)
                .setPageSize(20)
                .build()

    private fun defaultPagingOptions(): FirestorePagingOptions<User> {
        return FirestorePagingOptions.Builder<User>()
                .setLifecycleOwner(this)
                .setQuery(allUsersCollection, config, User::class.java)
                .build()
    }

    private fun getSearchQuery(field: String, searchText: String): Query {
        return allUsersCollection
                .orderBy(field)
                .startAt(searchText.trim { it <= ' ' })
                .endAt(searchText.trim { it <= ' ' } + "\uf8ff")
                .limit(15)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            adapter?.stopListening()
        } else {
            adapter?.startListening()

            val toolbarTxt = activity?.findViewById<TextView>(R.id.toolbar_title)
            toolbarTxt?.text = resources.getString(R.string.title_search)
        }
    }

    companion object {
        private const val TAG = "SearchFragment"
    }
}