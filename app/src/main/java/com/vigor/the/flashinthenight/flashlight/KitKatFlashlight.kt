package com.vigor.the.flashinthenight.flashlight

import android.hardware.Camera
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.lang.RuntimeException


@Suppress("DEPRECATION")
class KitKatFlashlight : AbstractFlashlight() {

    private var mCamera: Camera? = null
    private var mFlashlightDisposable: Disposable? = null

    private val mFlashlightObservable = Observable.create<Int?> {
        val result = (0..Camera.getNumberOfCameras() - 1)
                .map {
                    try {
                        val camera = Camera.open(it)
                        val res = camera.parameters?.supportedFlashModes?.isNotEmpty() ?: false
                        camera.release()
                        if (res) it else null
                    } catch (e: RuntimeException) {
                        null
                    }
                }
                .filterNotNull()
                .firstOrNull()

        if (result != null) {
            it.onNext(result)
        }
        it.onComplete()
    }.cache()

    override fun onStart() {
        mFlashlightDisposable = mFlashlightObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    mCamera = it?.let { it1 -> Camera.open(it1) }
                    mCamera?.let {
                        with(it) {
                            startPreview()
                            _onInitialized.onNext(Unit)
                        }
                    }
                }
    }

    override fun onStop() {
        mFlashlightDisposable?.let {
            if (!it.isDisposed)
                it.dispose()
        }

        mCamera?.let {
            with (it) {
                stopPreview()
                release()
            }
        }
    }

    override fun enable(enable: Boolean) {
        val params = mCamera?.parameters
        try {
            if (enable) {
                params?.flashMode = Camera.Parameters.FLASH_MODE_TORCH
            } else {
                params?.flashMode = Camera.Parameters.FLASH_MODE_OFF
            }
            mCamera?.parameters = params
        } catch (e: RuntimeException) {
            e.printStackTrace()
        }
    }

    override fun isSupported(): Observable<Boolean> = mFlashlightObservable
            .map { true }
            .defaultIfEmpty(false)
}
