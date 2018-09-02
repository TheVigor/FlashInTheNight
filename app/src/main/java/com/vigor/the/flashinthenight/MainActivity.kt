package com.vigor.the.flashinthenight

import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import com.vigor.the.flashinthenight.flashlight.Flashlight
import com.vigor.the.flashinthenight.flashlight.FlashlightFactory
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var mImageViewOnOff : ImageView

    private lateinit var mFlashlight: Flashlight

    private var isFlashlightSupported = true
    private var isFlashlightEnabled = false

    private var isStroboscopeEnabled = false

    private var onPermissionsGranted = BehaviorSubject.create<Unit>()

    private var permissionDialog: AlertDialog? = null

    private var onStart = PublishSubject.create<Unit>()
    private var onStop = PublishSubject.create<Unit>()

    private var stroboscopeDisposable: Disposable? = null

    private companion object {
        private const val PERMISSION_REQUEST_CODE = 777
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mImageViewOnOff = findViewById(R.id.imageViewOnOff) as ImageView
        changeIconColor(Color.RED, mImageViewOnOff)


        mImageViewOnOff.setOnClickListener { onFlashlightOnOffClicked() }
        mImageViewOnOff.isEnabled = isFlashlightSupported

        mFlashlight = FlashlightFactory.newInstance(this)
        mFlashlight.isSupported()
                .subscribeOn(Schedulers.io())
                .filter { !it }
                .take(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { onFlashlightNotSupported() }
    }

    private fun checkCameraPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            onPermissionsGranted.onNext(Unit)
            return
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            val listener = DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> requestCameraPermissions()
                }
                dialog.dismiss()
            }
            if (permissionDialog == null) {
                permissionDialog = AlertDialog.Builder(this)
                        .setTitle("Camera Permissions")
                        .setMessage("Please grant the permission to your camera so that application can access flashlight of your device.")
                        .setPositiveButton("Continue", listener)
                        .setNegativeButton("Cancel", listener)
                        .setOnDismissListener { permissionDialog = null }
                        .show()
            }
            return
        }
        requestCameraPermissions()
    }

    private fun requestCameraPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), PERMISSION_REQUEST_CODE)
    }

    override fun onStart() {
        super.onStart()
        // call flashlight's onStart only after permissions granted and onStart called
        Observable
                .zip(onPermissionsGranted, onStart, BiFunction<Unit, Unit, Unit> { _, _ -> })
                .takeUntil(onStop)
                .subscribe {
                    mFlashlight.onStart()
                }
        Observable
                .zip(mFlashlight.onInitialized, onPermissionsGranted, BiFunction<Unit, Unit, Unit> { _, _ -> })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    mImageViewOnOff.isEnabled = isFlashlightSupported
                })
        onStart.onNext(Unit)
        checkCameraPermissions()
    }

    override fun onStop() {
        super.onStop()
        onStop.onNext(Unit)
        mFlashlight.onStop()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onPermissionsGranted.onNext(Unit)
                }
            }
        }
    }

    private fun onFlashlightOnOffClicked() {
        // disable flashlight if enabled
        if (isFlashlightEnabled) {
            stopStroboscopeTask()
            toggleFlashlight(false)
            return
        }
        // start stroboscope task
        if (isStroboscopeEnabled) {
            startStroboscopeTask()
            return
        }
        // just enable flashlight
        toggleFlashlight(true)
    }

    private fun startStroboscopeTask() {
        stroboscopeDisposable = Observable
                .interval(0, 100, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.io())
                .subscribe { toggleFlashlight() }
    }

    private fun stopStroboscopeTask() {
        stroboscopeDisposable?.let {
            if (!it.isDisposed) {
                it.dispose()
            }
        }
        stroboscopeDisposable = null
    }

    private fun onStroboscopeEnableChanged(enabled: Boolean) {
        isStroboscopeEnabled = enabled
        mImageViewOnOff.isEnabled = isFlashlightSupported && !enabled
        if (isStroboscopeEnabled) {
            startStroboscopeTask()
        } else {
            stopStroboscopeTask()
            toggleFlashlight(false)
        }
    }

    private fun toggleFlashlight(enabled: Boolean = !isFlashlightEnabled) {
        if (isFlashlightEnabled)
            changeIconColor(Color.RED, mImageViewOnOff)
        else
            changeIconColor(Color.GREEN, mImageViewOnOff)

        isFlashlightEnabled = enabled
        mFlashlight.enable(isFlashlightEnabled)
    }

    private fun onFlashlightNotSupported() {
        isFlashlightSupported = false
        mImageViewOnOff.isEnabled = false
    }

    private fun changeIconColor(color: Int, imageView: ImageView?) {
        imageView!!.background.mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN)
    }

}
