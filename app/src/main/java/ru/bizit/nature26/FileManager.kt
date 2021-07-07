package ru.bizit.nature26

import android.graphics.Bitmap
import android.net.Uri
import java.io.*
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class FileManager() {

    private val BUFFER_SIZE = 8000

    fun bitmapToFile(
        bitmap: Bitmap,
        path: String,
        formatType: Bitmap.CompressFormat,
        fileExtension: String
    ): String {
        val file = File(path, "${UUID.randomUUID()}" + fileExtension)

        try {
            // Compress the bitmap and save in jpg format
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(formatType, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return Uri.parse(file.absolutePath).toString()
    }

    fun zipFile(inputFile: String): String {
        val date = Calendar.getInstance().time.toString();
        val zipFileName = inputFile.substring(0, inputFile.lastIndexOf("/") + 1) + date + ".zip"
        try {
            ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFileName))).use { out ->
                FileInputStream(inputFile).use { fi ->
                    BufferedInputStream(fi).use { origin ->
                        val entry = ZipEntry(inputFile.substring(inputFile.lastIndexOf("/")))
                        out.putNextEntry(entry)
                        origin.copyTo(out, BUFFER_SIZE)
                        origin.close()
                    }
                    fi.close()
                }
                out.closeEntry()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return zipFileName
    }


}