//package com.zc.permissionhelper// PermissionHelper.kt
//
//import android.Manifest
//import android.app.Activity
//import android.content.Context
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.net.Uri
//import android.os.Build
//import android.os.Bundle
//import android.provider.Settings
//import androidx.appcompat.app.AlertDialog
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import androidx.fragment.app.Fragment
//
//object PermissionHelper {
//
//    private const val REQUEST_CODE = 1001
//
//    interface PermissionCallback {
//        fun onGranted()
//        fun onDenied(deniedPermissions: List<String>)
//    }
//
//    /**
//     * 请求权限（从 Activity 调用）
//     */
//    fun requestPermissions(
//        activity: AppCompatActivity,
//        permissions: Array<String>,
//        callback: PermissionCallback
//    ) {
//        val notGranted = getNotGrantedPermissions(activity, permissions)
//        if (notGranted.isEmpty()) {
//            callback.onGranted()
//        } else {
//            // 存储回调（使用 Activity 的 setResult 方式或 Fragment tag）
//            val fragment = PermissionFragment().apply {
//                setCallback(callback)
//                setPermissions(notGranted.toTypedArray())
//            }
//            activity.supportFragmentManager
//                .beginTransaction()
//                .add(fragment, "PermissionFragment")
//                .commitAllowingStateLoss()
//        }
//    }
//
//    /**
//     * 显示权限用途说明对话框（通用版，仅依赖 androidx.appcompat）
//     *
//     * @param activity 上下文（需为 Activity）
//     * @param message 权限用途说明（必填）
//     * @param title 标题（可选，默认“需要权限”）
//     * @param positiveText 确认按钮文字（默认“允许”）
//     * @param negativeText 取消按钮文字（默认“取消”）
//     * @param onConfirmed 用户点击“确定”后的回调
//     */
//    fun show(
//        activity: Activity,
//        message: String,
//        title: String = "需要权限",
//        positiveText: String = "允许",
//        negativeText: String = "取消",
//        onConfirmed: () -> Unit
//    ) {
//        if (activity.isFinishing || activity.isDestroyed) return
//
//        AlertDialog.Builder(activity)
//            .setTitle(title)
//            .setMessage(message)
//            .setPositiveButton(positiveText) { _, _ ->
//                onConfirmed()
//            }
//            .setNegativeButton(negativeText) { dialog, _ ->
//                dialog.dismiss()
//            }
//            .setCancelable(false)
//            .show()
//    }
//
//    /**
//     * 请求权限（从 Fragment 调用）
//     */
//    fun requestPermissions(
//        fragment: Fragment,
//        permissions: Array<String>,
//        callback: PermissionCallback
//    ) {
//        val notGranted = getNotGrantedPermissions(fragment.requireContext(), permissions)
//        if (notGranted.isEmpty()) {
//            callback.onGranted()
//        } else {
//            val permFragment = PermissionFragment().apply {
//                setCallback(callback)
//                setPermissions(notGranted.toTypedArray())
//            }
//            fragment.childFragmentManager
//                .beginTransaction()
//                .add(permFragment, "PermissionFragment")
//                .commitAllowingStateLoss()
//        }
//    }
//
//    private fun getNotGrantedPermissions(context: Context, permissions: Array<String>): List<String> {
//        return permissions.filter { permission ->
//            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
//        }
//    }
//
//
//    // ==============================
//    // 2. Android 15+ 媒体文件选择（无需权限）
//    // ==============================
//    fun pickMedia(
//        activity: Activity,
//        mimeType: String = "image/*"
//    ): Intent {
//        return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
//            addCategory(Intent.CATEGORY_OPENABLE)
//            type = mimeType
//        }
//    }
//
//    // ==============================
//    // 3. 精确闹钟权限引导（Android 12+）
//    // ==============================
//    fun requestExactAlarm(context: Context) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
//            if (!alarmManager.canScheduleExactAlarms()) {
//                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
//                    data = Uri.parse("package:${context.packageName}")
//                }
//                if (context is Activity) {
//                    context.startActivity(intent)
//                } else {
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                    context.startActivity(intent)
//                }
//            }
//        }
//    }
//
//    // 内部无 UI 的 Fragment，用于接收权限回调
//    class PermissionFragment : Fragment() {
//
//        private var callback: PermissionCallback? = null
//        private var permissions: Array<String> = emptyArray()
//
//        fun setCallback(callback: PermissionCallback) {
//            this.callback = callback
//        }
//
//        fun setPermissions(permissions: Array<String>) {
//            this.permissions = permissions
//        }
//
//        override fun onCreate(savedInstanceState: Bundle?) {
//            super.onCreate(savedInstanceState)
//            requestPermissions(permissions, REQUEST_CODE)
//        }
//
//        override fun onRequestPermissionsResult(
//            requestCode: Int,
//            permissions: Array<out String>,
//            grantResults: IntArray
//        ) {
//            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//            if (requestCode == REQUEST_CODE) {
//                val denied = mutableListOf<String>()
//                val granted = mutableListOf<String>()
//
//                permissions.forEachIndexed { index, permission ->
//                    if (grantResults.getOrNull(index) == PackageManager.PERMISSION_GRANTED) {
//                        granted.add(permission)
//                    } else {
//                        denied.add(permission)
//                    }
//                }
//
//                if (denied.isEmpty()) {
//                    callback?.onGranted()
//                } else {
//                    callback?.onDenied(denied)
//                }
//
//                // 清理自己
//                fragmentManager?.beginTransaction()?.remove(this)?.commitAllowingStateLoss()
//            }
//        }
//    }
//}
package com.zc.permissionhelper

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

// ====== 核心数据结构：权限请求单元 ======
data class PermissionRequest(
    val permission: String,
    val rationale: String? = null
)

// ====== 权限帮助类 ======
object PermissionHelper {

    interface PermissionCallback {
        fun onGranted()
        fun onDenied(deniedPermissions: List<String>)
    }

    // ------------------ Activity ------------------
    fun requestPermissionsWithRationale(
        activity: AppCompatActivity,
        requests: List<PermissionRequest>,
        callback: PermissionCallback
    ) {
        val context = activity.applicationContext
        val pendingRequests = requests.filter {
            ContextCompat.checkSelfPermission(context, it.permission) != PackageManager.PERMISSION_GRANTED
        }

        if (pendingRequests.isEmpty()) {
            callback.onGranted()
            return
        }

        val explanations = pendingRequests.mapNotNull { it.rationale?.let { rationale -> it.permission to rationale } }

        if (explanations.isNotEmpty()) {
            val message = explanations.joinToString("\n\n") { "• ${it.second}" }
            showRationaleDialog(activity, message) {
                launch(activity, pendingRequests.map { it.permission }.toTypedArray(), callback)
            }
        } else {
            launch(activity, pendingRequests.map { it.permission }.toTypedArray(), callback)
        }
    }

    // ------------------ Fragment ------------------
    fun requestPermissionsWithRationale(
        fragment: Fragment,
        requests: List<PermissionRequest>,
        callback: PermissionCallback
    ) {
        val context = fragment.requireContext().applicationContext
        val pendingRequests = requests.filter {
            ContextCompat.checkSelfPermission(context, it.permission) != PackageManager.PERMISSION_GRANTED
        }

        if (pendingRequests.isEmpty()) {
            callback.onGranted()
            return
        }

        val explanations = pendingRequests.mapNotNull { it.rationale?.let { rationale -> it.permission to rationale } }

        if (explanations.isNotEmpty()) {
            val message = explanations.joinToString("\n\n") { "• ${it.second}" }
            showRationaleDialog(fragment.requireActivity(), message) {
                launch(fragment, pendingRequests.map { it.permission }.toTypedArray(), callback)
            }
        } else {
            launch(fragment, pendingRequests.map { it.permission }.toTypedArray(), callback)
        }
    }

    // ------------------ 私有工具 ------------------
    private fun showRationaleDialog(activity: Activity, message: String, onConfirm: () -> Unit) {
        if (activity.isFinishing || activity.isDestroyed) return
        AlertDialog.Builder(activity)
            .setTitle("需要权限")
            .setMessage(message)
            .setPositiveButton("允许") { _, _ -> onConfirm() }
            .setNegativeButton("取消", null)
            .setCancelable(false)
            .show()
    }

    private fun launch(activity: AppCompatActivity, permissions: Array<String>, callback: PermissionCallback) {
        activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val denied = result.filterValues { !it }.keys.toList()
            if (denied.isEmpty()) callback.onGranted() else callback.onDenied(denied)
        }.launch(permissions)
    }

    private fun launch(fragment: Fragment, permissions: Array<String>, callback: PermissionCallback) {
        fragment.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val denied = result.filterValues { !it }.keys.toList()
            if (denied.isEmpty()) callback.onGranted() else callback.onDenied(denied)
        }.launch(permissions)
    }
}