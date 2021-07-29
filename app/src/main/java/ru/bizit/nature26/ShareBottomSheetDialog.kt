package ru.bizit.nature26

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.FileProvider
import dagger.Module
import dagger.android.ContributesAndroidInjector
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.bizit.nature26.common.Common
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.*
import javax.inject.Inject
import javax.inject.Named


@Module
abstract class ShareModule {
    @ContributesAndroidInjector
    abstract fun contributeActivityInjector(): ShareBottomSheetDialog?
}

class ShareBottomSheetDialog : DaggerBottomSheetDialogFragment() {
    @Inject
    @Named("Long Live")
    lateinit var appData: AppData
    lateinit var sheet: View
    lateinit var path: Uri
    lateinit var currentPhotoPath: String

    private var base64Images = mutableListOf<Image>()

    companion object {
        const val TAG = "LayersBottomSheetDialog"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sheet = inflater.inflate(R.layout.report_bottom_sheet_dialog, container, false)

        val addPhoto = sheet.findViewById<Button>(R.id.addPhoto)
        addPhoto.setOnClickListener {
            dispatchTakePictureIntent()
        }
        val send = sheet.findViewById<Button>(R.id.send)
        send.setOnClickListener {
            sendMessage(dialog)
        }
        val userNameView = sheet.findViewById<TextView>(R.id.userName)
        userNameView.text = appData.user.name
        val coordinatesView = sheet.findViewById<TextView>(R.id.coordinates)
        coordinatesView.text = "Координаты: ${appData.lat}, ${appData.lon}"

        return sheet
    }

    private fun sendMessage(mainDialog: Dialog?) {
        val textView = sheet.findViewById<EditText>(R.id.editTextTextMultiLine)
        if (textView.text.toString().isEmpty()) {
            return
        }

        val um = UserMessage(
            images = base64Images, text = textView.text.toString(), lat = appData.lat,
            lon = appData.lon, token = appData.user.token
        )
        Common.retrofitService.postMessage(um).enqueue(object : Callback<MutableList<String>> {
            override fun onFailure(
                call: Call<MutableList<String>>,
                t: Throwable
            ) {
            }

            override fun onResponse(
                call: Call<MutableList<String>>,
                response: Response<MutableList<String>>
            ) {
                val numbers = response.body() as MutableList<String>
                val builder = AlertDialog.Builder(sheet.context)
                with(builder)
                {
                    setTitle("Отправка обращения")
                    setMessage("Спасибо за обращение! Ваша заявка зарегистрирована под номером " + numbers[0])
                    setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                        mainDialog?.dismiss()
                    }
                    show()
                }
            }
        })
    }

    private fun dispatchTakePictureIntent() {
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            null
        }
        val photoURI: Uri = FileProvider.getUriForFile(
            sheet.context,
            "ru.bizit.nature26.FileProvider",
            photoFile!!
        )

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        startActivityForResult(cameraIntent, 1)
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = UUID.randomUUID().toString()
        val storageDir: File = sheet.context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            val imageView = ImageView(this.context)
            imageView.layoutParams = LinearLayout.LayoutParams(128, 128)

            val bmOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
                BitmapFactory.decodeFile(currentPhotoPath, this)

                val photoW: Int = outWidth
                val photoH: Int = outHeight
                val scaleFactor: Int = Math.max(1, Math.min(photoW / 700, photoH / 700))

                inJustDecodeBounds = false
                inSampleSize = scaleFactor
            }

            val bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions)
            imageView.setImageBitmap(bitmap)
            val images = sheet.findViewById<LinearLayout>(R.id.images)
            images.addView(imageView, 1)

            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            val imageBytes: ByteArray = byteArrayOutputStream.toByteArray()

            val base64 = Base64.encodeToString(imageBytes, Base64.DEFAULT)
            base64Images.add(Image(jpeg = base64))
        }
    }
}