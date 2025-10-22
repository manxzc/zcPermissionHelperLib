package com.zc.permissionhelper// PermissionHelper.kt

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

object PermissionHelper {

    private const val REQUEST_CODE = 1001

    interface PermissionCallback {
        fun onGranted()
        fun onDenied(deniedPermissions: List<String>)
    }

    /**
     * 请求权限（从 Activity 调用）
     */
    fun requestPermissions(
        activity: AppCompatActivity,
        permissions: Array<String>,
        callback: PermissionCallback
    ) {
        val notGranted = getNotGrantedPermissions(activity, permissions)
        if (notGranted.isEmpty()) {
            callback.onGranted()
        } else {
            // 存储回调（使用 Activity 的 setResult 方式或 Fragment tag）
            val fragment = PermissionFragment().apply {
                setCallback(callback)
                setPermissions(notGranted.toTypedArray())
            }
            activity.supportFragmentManager
                .beginTransaction()
                .add(fragment, "PermissionFragment")
                .commitAllowingStateLoss()
        }
    }

    /**
     * 请求权限（从 Fragment 调用）
     */
    fun requestPermissions(
        fragment: Fragment,
        permissions: Array<String>,
        callback: PermissionCallback
    ) {
        val notGranted = getNotGrantedPermissions(fragment.requireContext(), permissions)
        if (notGranted.isEmpty()) {
            callback.onGranted()
        } else {
            val permFragment = PermissionFragment().apply {
                setCallback(callback)
                setPermissions(notGranted.toTypedArray())
            }
            fragment.childFragmentManager
                .beginTransaction()
                .add(permFragment, "PermissionFragment")
                .commitAllowingStateLoss()
        }
    }

    private fun getNotGrantedPermissions(context: Context, permissions: Array<String>): List<String> {
        return permissions.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }
    }

    // 内部无 UI 的 Fragment，用于接收权限回调
    class PermissionFragment : Fragment() {

        private var callback: PermissionCallback? = null
        private var permissions: Array<String> = emptyArray()

        fun setCallback(callback: PermissionCallback) {
            this.callback = callback
        }

        fun setPermissions(permissions: Array<String>) {
            this.permissions = permissions
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            requestPermissions(permissions, REQUEST_CODE)
        }

        override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
        ) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if (requestCode == REQUEST_CODE) {
                val denied = mutableListOf<String>()
                val granted = mutableListOf<String>()

                permissions.forEachIndexed { index, permission ->
                    if (grantResults.getOrNull(index) == PackageManager.PERMISSION_GRANTED) {
                        granted.add(permission)
                    } else {
                        denied.add(permission)
                    }
                }

                if (denied.isEmpty()) {
                    callback?.onGranted()
                } else {
                    callback?.onDenied(denied)
                }

                // 清理自己
                fragmentManager?.beginTransaction()?.remove(this)?.commitAllowingStateLoss()
            }
        }
    }
}