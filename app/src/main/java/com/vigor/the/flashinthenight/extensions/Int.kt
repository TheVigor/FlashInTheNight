package com.vigor.the.flashinthenight.extensions

import android.os.Build

fun Int.isLollipop() = this >= Build.VERSION_CODES.LOLLIPOP && this < Build.VERSION_CODES.M
fun Int.isMarshmallowAndUpper() = this >= Build.VERSION_CODES.M
