package com.dragontelnet.mychatapp.datasource.local

import android.content.Context
import android.content.SharedPreferences
import com.dragontelnet.mychatapp.model.entity.Post
import com.dragontelnet.mychatapp.model.entity.User
import com.dragontelnet.mychatapp.utils.MyConstants
import io.paperdb.Paper

object MySharedPrefs {
    fun getUserRegistrationSharedPref(context: Context): SharedPreferences {
        return context
                .getSharedPreferences(MyConstants.LocalDB.MY__PREFS, Context.MODE_PRIVATE)
    }

    fun putRegistrationDone(context: Context?) {
        context?.let {
            getUserRegistrationSharedPref(context).edit()
                    .putBoolean(MyConstants.LocalDB.REG_DONE_KEY, true).apply()
        }
    }

    fun isRegistrationDoneExists(context: Context): Boolean {
        return getUserRegistrationSharedPref(context)
                .getBoolean(MyConstants.LocalDB.REG_DONE_KEY, false)
    }

    private fun getMyNotificationSharedPrefs(context: Context): SharedPreferences {
        return context
                .getSharedPreferences(MyConstants.LocalDB.MY_NOTIFICATION_PREFS, Context.MODE_PRIVATE)
    }

    fun putDoReceiverNotification(context: Context, flag: Boolean) {
        getMyNotificationSharedPrefs(context).edit()
                .putBoolean(MyConstants.LocalDB.RECEIVER_NOTIFICATION_FLAG, flag).apply()
    }

    fun getReceiverNotification(context: Context): Boolean {
        return getMyNotificationSharedPrefs(context)
                .getBoolean(MyConstants.LocalDB.RECEIVER_NOTIFICATION_FLAG, false)
    }

    fun putUserObjToBook(user: User?) {
        user?.let {
            Paper.book(User.USER_BOOK_KEY).write("user", it)
        }
    }

    val getCurrentOfflineUserFromBook: User?
        get() = Paper.book(User.USER_BOOK_KEY).read("user")


    fun getPostsBook() = Paper.book(Post.POSTS_BOOK_KEY)
    fun getUsersBook() = Paper.book(User.USER_BOOK_KEY)

    fun putPostObjToBook(post: Post?) {
        post?.let {
            Paper.book(Post.POSTS_BOOK_KEY).write(it.postId, post)
        }
    }

    fun getPostObjFromBook(postId: String): Post? = Paper.book(Post.POSTS_BOOK_KEY).read<Post>(postId)

    fun destroyPostFromBook(postId: String) = Paper.book(Post.POSTS_BOOK_KEY).delete(postId)
}