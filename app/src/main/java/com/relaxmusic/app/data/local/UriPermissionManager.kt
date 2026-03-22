package com.relaxmusic.app.data.local

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri

class UriPermissionManager {
    fun takePersistablePermission(context: Context, uri: Uri) {
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        context.contentResolver.takePersistableUriPermission(uri, flags)
    }

    fun hasPermission(context: Context, uri: Uri): Boolean {
        return context.contentResolver.persistedUriPermissions.any { it.uri == uri && it.isReadPermission }
    }
}
