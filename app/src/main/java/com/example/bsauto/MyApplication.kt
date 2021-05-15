package com.example.bsauto

import android.Manifest
import android.app.Application
import android.widget.Toast
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

class MyApplication : Application() {

    var PERMISSIONSCAMERA = false
    var PERMISSIONSGALLERY = false

    override fun onCreate() {
        super.onCreate()

        initPermisos()

    }

    /**
     * Comprobamos los permisos de la aplicación
     */
    private fun initPermisos() {

        Dexter.withContext(this)
                //Lista de permisos
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                )
                // Listener a ejecutar
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        // comprbamos si tenemos los permisos de todos ellos
                        if (report.areAllPermissionsGranted()) {
                            PERMISSIONSCAMERA = true
                            PERMISSIONSGALLERY = true
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                            permissions: List<PermissionRequest?>?,
                            token: PermissionToken
                    ) {
                        token.continuePermissionRequest()
                    }
                }).withErrorListener { Toast.makeText(applicationContext, "Existe errores! ", Toast.LENGTH_SHORT).show() }
            .onSameThread()
            .check()
    }
}