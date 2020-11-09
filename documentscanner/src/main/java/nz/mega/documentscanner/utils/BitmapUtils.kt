package nz.mega.documentscanner.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import androidx.annotation.IntRange
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.Rotate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Scalar

object BitmapUtils {

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun getBitmapFromUri(
        context: Context,
        uri: Uri,
        degreesToRotate: Int = 0,
        @IntRange(from = 0, to = 100) quality: Int = 100
    ): Bitmap = withContext(Dispatchers.Default) {
        Glide.with(context)
            .asBitmap()
            .load(uri)
            .encodeQuality(quality)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .apply {
                if (degreesToRotate != 0) {
                    transform(Rotate(degreesToRotate))
                }
            }
            .submit()
            .get()
    }

    fun Bitmap.toMat(): Mat {
        val mat = Mat(height, width, CvType.CV_8U, Scalar(4.0))
        val bitmap32 = copy(Bitmap.Config.ARGB_8888, true)
        Utils.bitmapToMat(bitmap32, mat)
        return mat
    }

    fun Mat.toBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(cols(), rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(this, bitmap)
        return bitmap
    }

    suspend fun Bitmap.rotate(degrees: Int): Bitmap = withContext(Dispatchers.Default) {
        if (degrees == 0) return@withContext this@rotate

        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        val rotated = Bitmap.createBitmap(this@rotate, 0, 0, width, height, matrix, true)
        recycle()
        return@withContext rotated
    }
}
