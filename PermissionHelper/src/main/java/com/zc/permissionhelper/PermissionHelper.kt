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
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

data class PermissionRequest(
    val permission: String,
    val rationale: String? = null
)

open class PermissionHelper private constructor(
    private val launcher: ActivityResultLauncher<Array<String>>,
    private val context: android.content.Context
) {

    // ====== 对外统一入口：请求权限 ======
    open fun requestPermissionsWithRationale(
        requests: List<PermissionRequest>,
        onGranted: () -> Unit,
        onDenied: (List<String>) -> Unit
    ) {
        val pendingRequests = requests.filter {
            ContextCompat.checkSelfPermission(context, it.permission) != PackageManager.PERMISSION_GRANTED
        }

        if (pendingRequests.isEmpty()) {
            onGranted()
            return
        }

        val explanations = pendingRequests.mapNotNull { it.rationale?.let { r -> it.permission to r } }
        if (explanations.isNotEmpty()) {
            val message = explanations.joinToString("\n\n") { "• ${it.second}" }
            showRationaleDialog(message) {
                launcher.launch(pendingRequests.map { it.permission }.toTypedArray())
            }
        } else {
            launcher.launch(pendingRequests.map { it.permission }.toTypedArray())
        }
    }

    private fun showRationaleDialog(message: String, onConfirm: () -> Unit) {
        val activity = context as? Activity ?: return
        if (activity.isFinishing || activity.isDestroyed) return

        AlertDialog.Builder(activity)
            .setTitle("需要权限")
            .setMessage(message)
            .setPositiveButton("允许") { _, _ -> onConfirm() }
            .setNegativeButton("取消", null)
            .setCancelable(false)
            .show()
    }

    // ====== 工厂方法：Activity ======
    companion object {
        fun create(activity: ComponentActivity): PermissionHelper {
            val launcher = activity.registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { result ->
                // 这个回调里无法直接拿到 onGranted/onDenied，所以我们不在这处理逻辑
                // 而是在 launch 时把回调存起来 —— 但这样有风险（多请求覆盖）
                // 所以更好的方式：**不在 helper 内部处理结果，而是由外部监听**
                // ❌ 但我们想要“一次调用+回调”，所以换思路 ↓
            }
            // ⚠️ 上面的方式行不通！因为 launcher 回调和 request 请求无法一一对应

            // ✅ 正确做法：**每次 request 都创建一个临时 launcher？不行，会报错！**

            // 🔄 所以我们退一步：**只支持单次并发请求**，用成员变量暂存回调
            return ActivityBasedHelper(activity)
        }

        fun create(fragment: Fragment): PermissionHelper {
            return FragmentBasedHelper(fragment)
        }
    }

    // ====== 内部实现：Activity 版（带回调暂存） ======
    private class ActivityBasedHelper(activity: ComponentActivity) : PermissionHelper(
        launcher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            val denied = result.filterValues { !it }.keys.toList()
            if (denied.isEmpty()) {
                currentCallback?.first?.invoke()
            } else {
                currentCallback?.second?.invoke(denied)
            }
            currentCallback = null
        },
        context = activity
    ) {
        companion object {
            var currentCallback: Pair<(() -> Unit), ((List<String>) -> Unit)>? = null
        }

        override fun requestPermissionsWithRationale(
            requests: List<PermissionRequest>,
            onGranted: () -> Unit,
            onDenied: (List<String>) -> Unit
        ) {
            currentCallback = Pair(onGranted, onDenied)
            super.requestPermissionsWithRationale(requests, onGranted, onDenied)
        }
    }

    // ====== 内部实现：Fragment 版 ======
    private class FragmentBasedHelper(fragment: Fragment) : PermissionHelper(
        launcher = fragment.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            val denied = result.filterValues { !it }.keys.toList()
            if (denied.isEmpty()) {
                currentCallback?.first?.invoke()
            } else {
                currentCallback?.second?.invoke(denied)
            }
            currentCallback = null
        },
        context = fragment.requireContext()
    ) {
        companion object {
            var currentCallback: Pair<(() -> Unit), ((List<String>) -> Unit)>? = null
        }

        override fun requestPermissionsWithRationale(
            requests: List<PermissionRequest>,
            onGranted: () -> Unit,
            onDenied: (List<String>) -> Unit
        ) {
            currentCallback = Pair(onGranted, onDenied)
            super.requestPermissionsWithRationale(requests, onGranted, onDenied)
        }
    }
}