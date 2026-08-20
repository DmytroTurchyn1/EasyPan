package com.cook.easypan.easypan.data.reviews

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.play.core.ktx.launchReview
import com.google.android.play.core.ktx.requestReview
import com.google.android.play.core.review.ReviewManagerFactory

private const val TAG = "ReviewClient"

class ReviewClient(context: Context) {
    private val manager = ReviewManagerFactory.create(context)
    suspend fun requestReview(activity: Activity) {
        runCatching {
            val info = manager.requestReview()
            manager.launchReview(activity, info)
        }.onFailure { Log.e(TAG, "Failed to launch the in-app review flow", it) }
    }
}