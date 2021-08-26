package com.perksy.takepicturedemo

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.perksy.takepicturedemo.ImageUri.latestTmpUri
import java.io.File

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    var bitmap: Bitmap? = null

    private val takeImageResult =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (isSuccess) {
                latestTmpUri?.let { uri ->
                    if (Build.VERSION.SDK_INT < 28) {
                        bitmap = MediaStore.Images
                            .Media.getBitmap(this.contentResolver, uri)

                    } else {
                        val source = ImageDecoder
                            .createSource(this.contentResolver, uri)
                        bitmap = ImageDecoder.decodeBitmap(source)
                    }
                    previewImage.setImageBitmap(bitmap)
                }
            }
            val intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)
        }

    private val selectImageFromGalleryResult =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                previewImage.setImageURI(uri)
            }
        }

    private val previewImage by lazy { findViewById<ImageView>(R.id.image_preview) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setClickListeners()
    }

    private fun setClickListeners() {
        findViewById<MaterialButton>(R.id.take_image_button).setOnClickListener { takeImage() }
        findViewById<MaterialButton>(R.id.select_image_button).setOnClickListener { selectImageFromGallery() }
    }

    private fun takeImage() {
        lifecycleScope.launchWhenStarted {
            getTmpFileUri().let { uri ->
                latestTmpUri = uri
                takeImageResult.launch(uri)
            }
        }
    }

    private fun selectImageFromGallery() = selectImageFromGalleryResult.launch("image/*")

    private fun getTmpFileUri(): Uri {
        val tmpFile = File.createTempFile("tmp_image_file", ".png", cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }

        return FileProvider.getUriForFile(
            applicationContext,
            "com.perksy.takepicturedemo.provider",
            tmpFile
        )
    }
}