package com.dragontelnet.mychatapp.ui.activities.main

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ViewModelProvider
import butterknife.BindView
import butterknife.ButterKnife
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.datasource.local.MySharedPrefs
import com.dragontelnet.mychatapp.model.entity.User
import com.dragontelnet.mychatapp.ui.activities.friends.view.FriendsActivity
import com.dragontelnet.mychatapp.ui.activities.login.LoginActivity
import com.dragontelnet.mychatapp.ui.activities.profile.view.ProfileActivity
import com.dragontelnet.mychatapp.ui.activities.registration.RegistrationDetailsActivity
import com.dragontelnet.mychatapp.ui.activities.splash.SplashActivity
import com.dragontelnet.mychatapp.ui.fragments.chats.view.ChatsFragment
import com.dragontelnet.mychatapp.ui.fragments.home.view.HomeBaseFragment
import com.dragontelnet.mychatapp.ui.fragments.notifications.view.NotificationsFragment
import com.dragontelnet.mychatapp.ui.fragments.requests.view.RequestsFragment
import com.dragontelnet.mychatapp.ui.fragments.search.view.SearchFragment
import com.dragontelnet.mychatapp.utils.AppClosingService
import com.dragontelnet.mychatapp.utils.AppObserver
import com.dragontelnet.mychatapp.utils.MyConstants.FirestoreKeys
import com.facebook.drawee.view.SimpleDraweeView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener, NavigationView.OnNavigationItemSelectedListener {


    @BindView(R.id.bottom_nav_view)
    lateinit var bottomNavView: BottomNavigationView

    @BindView(R.id.my_toolbar)
    lateinit var myToolbar: Toolbar

    @BindView(R.id.drawer_layout)
    lateinit var drawerLayout: DrawerLayout

    @BindView(R.id.left_nav_view)
    lateinit var leftSideNavDrawerView: NavigationView

    private var mViewModel: MainActivityViewModel? = null
    private var headerFullName: TextView? = null
    private var headerUserName: TextView? = null
    private var headerViewProfile: TextView? = null
    private var headerProfilePic: SimpleDraweeView? = null
    private val fm = supportFragmentManager

    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)
        setSupportActionBar(myToolbar)

        //this is for observing app level life cycle i.e background/foreground
        //its used in ProcessObserver class
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppObserver())
        startService(Intent(this, AppClosingService::class.java))



        mViewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        //setting device token to db
        mViewModel?.getAndSetDeviceTokenToDb(this)

        //setting up ui
        setUpUi()
        initUserDetails()
        if (fm.findFragmentByTag("home") == null) {
            addFragment(HomeBaseFragment(), "home")
            title = resources.getString(R.string.title_home)
        }

    }


    private fun initUserDetails() {
        mViewModel?.currentStableUser()?.observe(this, Observer { user: User ->
            headerFullName?.text = user.name
            headerUserName?.text = "@" + user.username
            if (user.profilePic == "") {
                if (user.gender == "male") {
                    headerProfilePic?.setImageResource(R.drawable.user_male_placeholder)
                } else {
                    headerProfilePic?.setImageResource(R.drawable.user_female_placeholder)
                }
            } else {
                headerProfilePic?.setImageURI(user.profilePic)
            }
        })

    }

    override fun onStart() {
        super.onStart()
        listenForUnreadChats()
        listenForUnreadRequests()
        listenForNotifications()
    }

    override fun onStop() {
        super.onStop()
        mViewModel?.removeAllListeners()
    }

    private fun listenForUnreadRequests() {
        mViewModel?.startListenForUnreadRequests()?.observe(this, Observer { isReqDelivered: Boolean ->
            val badge = bottomNavView.getOrCreateBadge(R.id.navigation_requests)
            badge.isVisible = isReqDelivered
        })
    }

    private fun listenForUnreadChats() {
        mViewModel?.listenLiveForUnreadChats()?.observe(this, Observer { isMsgDelivered: Boolean ->
            val badge = bottomNavView.getOrCreateBadge(R.id.navigation_chats)
            badge.isVisible = isMsgDelivered
        })
    }

    private fun listenForNotifications() {
        mViewModel?.listenForUnreadNotifications()?.observe(this, Observer { isNotifDelivered: Boolean ->
            val badge = bottomNavView.getOrCreateBadge(R.id.navigation_notifications)
            badge.isVisible = isNotifDelivered
        })
    }

    private fun hideFragment(fragment: Fragment?) {
        fm.beginTransaction().hide(fragment!!).commit()
    }

    private fun showFragment(fragment: Fragment) {
        fm.beginTransaction().show(fragment).commit()
    }

    private fun addFragment(fragment: Fragment, tag: String) {
        fm.beginTransaction()
                .add(R.id.nav_host_fragment, fragment, tag)
                .commit()
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.navigation_drawer_profile -> {
                startProfileActivity()
                drawerLayout.closeDrawer(GravityCompat.START)
                return true
            }
            R.id.navigation_drawer_about -> {
                startSplashActivity()
                drawerLayout.closeDrawer(GravityCompat.START)
                return true
            }
            R.id.navigation_drawer_logout -> {
                startLogoutProcess()
                return true
            }
            R.id.navigation_drawer_settings -> {
                startRegDetailsActivity()
                drawerLayout.closeDrawer(GravityCompat.START)
                return true
            }
            R.id.navigation_notifications -> {
                title = resources.getString(R.string.title_notification)
                val notification = fm.findFragmentByTag("notification")
                if (notification != null) {
                    if (notification.isHidden) {
                        showFragment(notification)
                    }
                } else {
                    addFragment(NotificationsFragment(), "notification")
                }
                if (fm.findFragmentByTag("chats") != null) {
                    hideFragment(fm.findFragmentByTag("chats"))
                }
                if (fm.findFragmentByTag("home") != null) {
                    hideFragment(fm.findFragmentByTag("home"))
                }
                if (fm.findFragmentByTag("requests") != null) {
                    hideFragment(fm.findFragmentByTag("requests"))
                }
                if (fm.findFragmentByTag("search") != null) {
                    hideFragment(fm.findFragmentByTag("search"))
                }
                return true
            }
            R.id.navigation_drawer_friends -> {
                val intent = Intent(this, FriendsActivity::class.java)
                startActivity(intent)
                drawerLayout.closeDrawer(GravityCompat.START)
                return true
            }
            R.id.navigation_home -> {
                title = resources.getString(R.string.title_home)
                val home = fm.findFragmentByTag("home")
                if (home != null) {
                    if (home.isHidden) {
                        showFragment(home)
                    }
                } else {
                    addFragment(HomeBaseFragment(), "home")
                }
                if (fm.findFragmentByTag("chats") != null) {
                    hideFragment(fm.findFragmentByTag("chats"))
                }
                if (fm.findFragmentByTag("notification") != null) {
                    hideFragment(fm.findFragmentByTag("notification"))
                }
                if (fm.findFragmentByTag("requests") != null) {
                    hideFragment(fm.findFragmentByTag("requests"))
                }
                if (fm.findFragmentByTag("search") != null) {
                    hideFragment(fm.findFragmentByTag("search"))
                }
                return true
            }
            R.id.navigation_chats -> {
                val chats = fm.findFragmentByTag("chats")
                if (chats != null) {
                    if (chats.isHidden) {
                        showFragment(chats)
                    }
                } else {
                    addFragment(ChatsFragment(), "chats")
                }
                title = resources.getString(R.string.title_chats)
                if (fm.findFragmentByTag("home") != null) {
                    hideFragment(fm.findFragmentByTag("home"))
                }
                if (fm.findFragmentByTag("notification") != null) {
                    hideFragment(fm.findFragmentByTag("notification"))
                }
                if (fm.findFragmentByTag("requests") != null) {
                    hideFragment(fm.findFragmentByTag("requests"))
                }
                if (fm.findFragmentByTag("search") != null) {
                    hideFragment(fm.findFragmentByTag("search"))
                }
                return true
            }
            R.id.navigation_requests -> {
                val requests = fm.findFragmentByTag("requests")
                if (requests != null) {
                    if (requests.isHidden) {
                        showFragment(requests)
                    }
                } else {
                    addFragment(RequestsFragment(), "requests")
                }
                title = resources.getString(R.string.title_requests)
                if (fm.findFragmentByTag("home") != null) {
                    hideFragment(fm.findFragmentByTag("home"))
                }
                if (fm.findFragmentByTag("chats") != null) {
                    hideFragment(fm.findFragmentByTag("chats"))
                }
                if (fm.findFragmentByTag("notification") != null) {
                    hideFragment(fm.findFragmentByTag("notification"))
                }
                if (fm.findFragmentByTag("search") != null) {
                    hideFragment(fm.findFragmentByTag("search"))
                }
                return true
            }
            R.id.navigation_search -> {
                val search = fm.findFragmentByTag("search")
                if (search != null) {
                    if (search.isHidden) {
                        showFragment(search)
                    }
                } else {
                    addFragment(SearchFragment(), "search")
                }
                title = resources.getString(R.string.title_search)
                if (fm.findFragmentByTag("home") != null) {
                    hideFragment(fm.findFragmentByTag("home"))
                }
                if (fm.findFragmentByTag("chats") != null) {
                    hideFragment(fm.findFragmentByTag("chats"))
                }
                if (fm.findFragmentByTag("notification") != null) {
                    hideFragment(fm.findFragmentByTag("notification"))
                }
                if (fm.findFragmentByTag("requests") != null) {
                    hideFragment(fm.findFragmentByTag("requests"))
                }
                return true
            }
        }
        return false
    }

    private fun startSplashActivity() {
        val i = Intent(this, SplashActivity::class.java)
        i.putExtra(SplashActivity.IS_ABOUT_NAV_CLICKED_INTENT, true)
        startActivity(i)
    }

    private fun startLogoutProcess() {

        AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Confirm Logout?")
                .setPositiveButton("Logout") { _, _ ->
                    //confirm logout

                    val progressDialog = ProgressDialog(this)
                    progressDialog.setMessage("Logging out....")
                    progressDialog.setCancelable(false)
                    progressDialog.setCanceledOnTouchOutside(false)
                    progressDialog.show()

                    //clear all user data who is logging out
                    clearLocalData()

                    //set offline status to db
                    mViewModel?.setUserState(FirestoreKeys.OFFLINE)?.observe(this, Observer {
                        progressDialog.dismiss()
                        mViewModel?.removeAllListeners()
                        FirebaseAuth.getInstance().signOut()
                        startLoginActivity()
                    })
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    //cancel
                    dialog.cancel()
                    drawerLayout.closeDrawer(GravityCompat.START)
                }.create().show()
    }

    private fun startRegDetailsActivity() {
        val intent = Intent(this, RegistrationDetailsActivity::class.java)
        intent.putExtra("isEditing", true)
        startActivity(intent)

    }

    private fun startLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun startProfileActivity() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }

    private fun clearLocalData() {
        MySharedPrefs.getUserRegistrationSharedPref(this).edit().clear().apply()
        MySharedPrefs.getUsersBook().destroy()
        MySharedPrefs.getPostsBook().destroy()
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewModel?.setUserState(FirestoreKeys.OFFLINE)
    }

    private fun refreshToolbarPic() {
        mViewModel?.currentStableUser()?.observe(this@MainActivity, Observer { user ->
            if (user.profilePic == "") {
                if (user.gender == "male") {
                    //male
                    profile_pic_toolbar.setActualImageResource(R.drawable.user_male_placeholder)
                } else {
                    //female
                    profile_pic_toolbar.setActualImageResource(R.drawable.user_female_placeholder)
                }
            } else {
                profile_pic_toolbar.setImageURI(user.profilePic)
            }
        })
    }

    private fun setUpUi() {
        val toggle = object : ActionBarDrawerToggle(this, drawerLayout, myToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            override fun onDrawerOpened(drawerView: View) {
                initUserDetails()
            }

            override fun onDrawerClosed(drawerView: View) {
                refreshToolbarPic()
            }
        }
        toggle.syncState()
        toggle.isDrawerIndicatorEnabled = false
        drawerLayout.addDrawerListener(toggle)
        leftSideNavDrawerView.setNavigationItemSelectedListener(this)
        bottomNavView.setOnNavigationItemSelectedListener(this)

        initHeaderViewDetails()
        refreshToolbarPic()
        setClickListeners()
    }

    private fun setClickListeners() {
        profile_pic_toolbar.setOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }
        headerViewProfile?.setOnClickListener { startProfileActivity() }
    }

    private fun initHeaderViewDetails() {
        val header = leftSideNavDrawerView.getHeaderView(0)
        headerFullName = header.findViewById(R.id.nav_header_full_name)
        headerUserName = header.findViewById(R.id.nav_header_username)
        headerProfilePic = header.findViewById(R.id.nav_header_image)
        headerViewProfile = header.findViewById(R.id.nav_header_view_profile)

    }

    override fun onBackPressed() {
        //exit on two times back pressed
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }
        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()
        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}