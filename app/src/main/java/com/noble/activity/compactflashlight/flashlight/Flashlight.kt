package com.noble.activity.compactflashlight

import io.reactivex.Observable

interface Flashlight {

    fun onStart()
    fun onStop()

    fun enable(state: Boolean)

    fun isSupported(): Observable<Boolean>

    val onInitialized: Observable<Unit>

}