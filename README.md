简单封装权限库，体积非常小。

使用：

✅ 第一步：在目标项目中添加 JitPack 仓库
修改 settings.gradle（推荐方式，适用于 AGP 7.0+）

// settings.gradle
pluginManagement {
    repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
    }
    }
    
dependencyResolutionManagement {

    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    
    repositories {
    
        google()
        
        mavenCentral()
        
        maven { url 'https://jitpack.io' } // 👈 关键：添加 JitPack
        
    }
    
}

⚠️ 不要加在 buildscript 的 repositories 中！

✅ 第二步：添加你的库依赖

修改 app/build.gradle 🔍 依赖格式

dependencies {

    implementation 'com.github.manxzc:zcPermissionHelperLib:v1.0.4'
    
}


✅ 第三步：Sync 并使用   mainactivity


import com.zc.permissionhelper.PermissionHelper

// 示例：申请相机权限

val permissions = arrayOf(Manifest.permission.CAMERA)

PermissionHelper.requestPermissions(this, permissions, object : PermissionHelper.PermissionCallback {

    override fun onGranted() {
    
        // 权限已授权
        
    }

    override fun onDenied(deniedPermissions: List<String>) {
    
        // 权限被拒绝
        Log.e("PermissionHelper", "❌ 以下权限被拒绝：")
        deniedPermissions.forEach { permission ->
            Log.e("PermissionHelper", "  - $permission")
        }
    }
})
