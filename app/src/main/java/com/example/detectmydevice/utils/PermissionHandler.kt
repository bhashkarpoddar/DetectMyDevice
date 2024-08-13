package com.example.detectmydevice.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.detectmydevice.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun AppCompatActivity.goToAppDetailsSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
    }
    startActivityForResult(intent, 0)
}

/*fun AppCompatActivity.isGranted(permissions: Array<String>) = run {
    val notGrantedPermissions = permissions.filterNot { permission ->
        ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    if (notGrantedPermissions.isNotEmpty()){
        //check if permission was previously denied and return a boolean value
        val showRationale=notGrantedPermissions.any { permission->
            shouldShowRequestPermissionRationale(permission)
        }
        //if true, explain to user why granting this permission is important
        if (showRationale){

        }else{
            //launch the Permission ActivityResultContract
            videoImagesPermission.launch(notGrantedPermissions.toTypedArray())
        }
    }

    this.let {
        (ActivityCompat.checkSelfPermission(
            it, permissions
        ) == PermissionChecker.PERMISSION_GRANTED)
    } ?: false
}*/


const val DENIED ="denied"
const val EXPLAINED ="explained"

inline fun FragmentActivity.requestMultiplePermissions(
    permissions: Array<String>,
    crossinline allGranted: () -> Unit = {},
    crossinline denied: (List<String>) -> Unit = {},
    crossinline explained: (List<String>) -> Unit = {}
) {
    registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result->
        //Filter elements whose value is false and convert them to list
        val deniedList = result.filter { !it.value }.map { it.key }
        when {
            deniedList.isNotEmpty() -> {
                //Group the rejected all list, and the grouping condition is whether to check and do not ask again
                val map = deniedList.groupBy { permission ->
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) DENIED else EXPLAINED
                }
                // Rejected and unchecked Do not ask again
                map[DENIED]?.let { denied.invoke(it) }
                // Rejected and checked Do not ask again
                map[EXPLAINED]?.let { explained.invoke(it) }
            }
            else -> allGranted.invoke()
        }
    }.launch(permissions)
}

inline fun Context.showOkayAlertFunction(title: String, message: String, positiveBtnTxt: String = "Okay", crossinline positiveBtnClick: () -> Unit) {
    MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme)
        .setCancelable(false)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(positiveBtnTxt) { _, _ ->
            positiveBtnClick()
        }
        .show()
}
