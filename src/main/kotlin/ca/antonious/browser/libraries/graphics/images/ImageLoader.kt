package ca.antonious.browser.libraries.graphics.images

import ca.antonious.browser.libraries.graphics.core.Bitmap
import java.io.ByteArrayOutputStream
import java.net.URI
import java.util.concurrent.Executors
import javax.imageio.ImageIO

class ImageLoader {

    private val backgroundExecutor = Executors.newCachedThreadPool()

    fun loadImage(url: String, callback: (Result<Bitmap>) -> Unit) {
        backgroundExecutor.submit {
            try {
                val image = ImageIO.read(URI.create(url).toURL())
                val byteArrayOutputStream = ByteArrayOutputStream()
                ImageIO.write(image, "bmp", byteArrayOutputStream)
                byteArrayOutputStream.flush()
                val bytes = byteArrayOutputStream.toByteArray()
                byteArrayOutputStream.close()
                callback.invoke(Result.Success(Bitmap(width = image.width, height = image.height, bytes = bytes)))
            } catch (ex: Exception) {
                callback.invoke(Result.Failure(ex))
            }
        }
    }
}

sealed class Result<T> {
    data class Success<T>(val value: T) : Result<T>()
    data class Failure<T>(val reason: Exception) : Result<T>()
}
