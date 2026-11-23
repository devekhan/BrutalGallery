package com.brutalbeats.gallery.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

/** How much of the library the user shared with the app. */
enum class MediaAccess { NONE, PARTIAL, FULL }

private fun granted(context: Context, permission: String) =
    ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

fun mediaAccess(context: Context): MediaAccess = when {
    Build.VERSION.SDK_INT >= 33 -> when {
        granted(context, Manifest.permission.READ_MEDIA_IMAGES) ||
            granted(context, Manifest.permission.READ_MEDIA_VIDEO) -> MediaAccess.FULL
        Build.VERSION.SDK_INT >= 34 &&
            granted(context, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) -> MediaAccess.PARTIAL
        else -> MediaAccess.NONE
    }
    granted(context, Manifest.permission.READ_EXTERNAL_STORAGE) -> MediaAccess.FULL
    else -> MediaAccess.NONE
}

private fun permissionsToRequest(): Array<String> = when {
    Build.VERSION.SDK_INT >= 34 -> arrayOf(
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VIDEO,
        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
    )
    Build.VERSION.SDK_INT >= 33 -> arrayOf(
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VIDEO
    )
    else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
}

tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

class MediaPermissions(
    val access: MediaAccess,
    /** True once the user refused for good: only Android Settings can grant it now. */
    val blocked: Boolean,
    private val onRequest: () -> Unit,
    private val onOpenSettings: () -> Unit
) {
    fun request() = onRequest()
    fun openSettings() = onOpenSettings()
}

@Composable
fun rememberMediaPermissions(): MediaPermissions {
    val context = LocalContext.current
    var access by remember { mutableStateOf(mediaAccess(context)) }
    var asked by rememberSaveable { mutableStateOf(false) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { access = mediaAccess(context) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { access = mediaAccess(context) }

    val activity = context.findActivity()
    val blocked = asked && access == MediaAccess.NONE && activity != null &&
        !ActivityCompat.shouldShowRequestPermissionRationale(activity, permissionsToRequest().first())

    return MediaPermissions(
        access = access,
        blocked = blocked,
        onRequest = {
            asked = true
            launcher.launch(permissionsToRequest())
        },
        onOpenSettings = {
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.packageName, null)
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    )
}
