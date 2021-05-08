package com.example.bsauto.util

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Environment
import android.widget.ImageView
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import java.io.File
import java.util.*

object UtilImage {
    /**
     * Función para obtener el nombre del fichero
     */
    fun createNameFile(): String {
        return "camera-" + UUID.randomUUID().toString() + ".jpg"
    }

    /**
     * Función que salva un fichero en un directorio
     */
    fun salvarImagen(path: String, nombre: String, context: Context): File? {
        // Directorio publico
        // Almacenamos en nuestro directorio de almacenamiento externo asignado en Pictures
        val dirFotos = File((context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath) + path)

        // Si no existe el directorio, lo creamos solo si es publico
        if (!dirFotos.exists()) {
            dirFotos.mkdirs()
        }
        try {
            val f = File(dirFotos, nombre)
            f.createNewFile()
            return f
        } catch (e1: Exception) {
            e1.printStackTrace()
        }
        return null
    }

    /**
     * Función para escalar una imagen
     */
    fun scaleImage(foto: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {

        if (maxHeight > 0 && maxWidth > 0) {
            val width: Int = foto.width
            val height: Int = foto.height
            val ratioBitmap = width.toFloat() / height.toFloat()
            val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()
            var finalWidth = maxWidth
            var finalHeight = maxHeight
            if (ratioMax > ratioBitmap) {
                finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
            } else {
                finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
            }

            return Bitmap.createScaledBitmap(foto, finalWidth, finalHeight, false)

        } else {
            return foto
        }
    }

    /**
     * Función para redondear una imagen
     */
    fun redondearFoto(imagen: ImageView) {
        val originalDrawable: Drawable = imagen.drawable
        var originalBitmap: Bitmap = (originalDrawable as BitmapDrawable).bitmap

        if (originalBitmap.width > originalBitmap.height) {
            originalBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.height, originalBitmap.height);
        } else if (originalBitmap.width < originalBitmap.height) {
            originalBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.width);
        }
        val roundedDrawable: RoundedBitmapDrawable =
            RoundedBitmapDrawableFactory.create(Resources.getSystem(), originalBitmap)
        roundedDrawable.cornerRadius = originalBitmap.width.toFloat()
        imagen.setImageDrawable(roundedDrawable)
    }

}