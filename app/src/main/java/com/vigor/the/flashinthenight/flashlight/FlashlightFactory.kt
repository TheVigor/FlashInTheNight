package com.vigor.the.flashinthenight.flashlight

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import com.vigor.the.flashinthenight.extensions.isLollipop
import com.vigor.the.flashinthenight.extensions.isMarshmallowAndUpper

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