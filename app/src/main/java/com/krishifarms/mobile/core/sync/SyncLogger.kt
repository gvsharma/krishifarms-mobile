package com.krishifarms.mobile.core.sync

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

interface SyncLogger {
    fun d(message: String)
    fun w(message: String, throwable: Throwable? = null)
    fun e(message: String, throwable: Throwable? = null)
}

@Singleton
class AndroidSyncLogger @Inject constructor() : SyncLogger {
    override fun d(message: String) {
        Log.d(TAG, message)
    }

    override fun w(message: String, throwable: Throwable?) {
        if (throwable != null) Log.w(TAG, message, throwable) else Log.w(TAG, message)
    }

    override fun e(message: String, throwable: Throwable?) {
        if (throwable != null) Log.e(TAG, message, throwable) else Log.e(TAG, message)
    }

    private companion object {
        const val TAG = "SyncEngine"
    }
}
