package com.noble.activity.compactflashlight

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build

object FlashlightFactory {


    @SuppressLint("NewApi")
    fun newInstance(context: Context, sdkVersion: Int = Build.VERSION.SDK_INT): Flashlight {
        return when {
            sdkVersion.isLollipop() -> LollipopFlashlight(context)
            sdkVersion.isMarshmallowAndUpper() -> MarshmallowFlashlight(context)
            else -> KitKatFlashlight()
        }
    }
}