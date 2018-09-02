package com.noble.activity.compactflashlight

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

abstract class AbstractFlashlight: Flashlight {

    protected val _onInitialized: Subject<Unit> by lazy { newOnInitialized() }

    override val onInitialized: Observable<Unit>
        get() = _onInitialized

    open protected fun newOnInitialized(): Subject<Unit> = PublishSubject.create<Unit>()
}