package nz.mega.documentscanner.data

import android.graphics.PointF
import android.net.Uri
import androidx.core.net.toFile
import androidx.recyclerview.widget.DiffUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nz.mega.documentscanner.utils.FileUtils.rotate

data class Page constructor(
    val id: Long = System.currentTimeMillis(),
    val originalImageUri: Uri,
    val width: Int,
    val height: Int,
    val croppedImageUri: Uri? = null,
    val cropPoints: List<PointF>? = null,
    var rotation: Int = 0
) {

    suspend fun rotateFiles(degrees: Float = 90f) {
        withContext(Dispatchers.IO) {
            croppedImageUri?.toFile()?.rotate(degrees)
            originalImageUri.toFile().rotate(degrees)
        }
    }

    suspend fun deleteFiles() {
        withContext(Dispatchers.IO) {
            croppedImageUri?.toFile()?.delete()
            originalImageUri.toFile().delete()
        }
    }

    suspend fun deleteCroppedFile() {
        withContext(Dispatchers.IO) {
            croppedImageUri?.toFile()?.delete()
        }
    }

    fun getContourPoints(): List<PointF> =
        listOf(
            PointF(0f, 0f),
            PointF(width.toFloat(), 0f),
            PointF(0f, height.toFloat()),
            PointF(width.toFloat(), height.toFloat())
        )

    class ItemDiffUtil : DiffUtil.ItemCallback<Page>() {

        override fun areItemsTheSame(oldItem: Page, newItem: Page): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Page, newItem: Page): Boolean =
            oldItem == newItem
    }
}
