package com.rahullohra.photogallert

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Environment.getExternalStoragePublicDirectory
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.coroutines.experimental.bg
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.BitmapFactory
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth




class MainActivity : AppCompatActivity() {

    val TAG = "MainActivity"
    val MY_CAMERA_PICTURE = 101
    var mCurrentPhotoPath: String? = null
    var photoFileName = "hola.jpg"
    var tempFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rxPermissions = RxPermissions(this)

        btn_folders.setOnClickListener {
            rxPermissions
                    .request(Manifest.permission.READ_EXTERNAL_STORAGE)
                    .subscribe { granted ->
                        if (granted) {
//                            fetchDirectories()
                            fetchAppDirs()
                        }
                    }
        }

        btn_image.setOnClickListener {
            rxPermissions
                    .request(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .subscribe { granted ->
                        if (granted) {
                            launchCamera()
//                            launchCameraNew()
                        }
                    }
        }
    }


    fun launchCamera() {

        val cameraIntent = Intent()
        cameraIntent.action = (MediaStore.ACTION_IMAGE_CAPTURE)

        val photoFile = createImageFile()
        if (photoFile != null) {
            val authorities = applicationContext.packageName + ".fileprovider"
            val photoURI = FileProvider.getUriForFile(this, authorities, photoFile)
            Log.wtf(TAG, "uri = "+photoURI)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)

            startActivityForResult(cameraIntent, MY_CAMERA_PICTURE)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "IMAGE_" + timeStamp + "_"
        val storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)

        val image = File.createTempFile(imageFileName, ".jpg", storageDirectory)
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.absolutePath
        Log.wtf(TAG, "storage path = " + mCurrentPhotoPath)
        return image
    }

    fun fetchDirectories() {

        val root = File(Environment.getExternalStorageDirectory().getAbsolutePath())

        val directories = root.listFiles()

        for (file in directories) {
            Log.d(TAG, "file name: " + file.name)
        }
    }

    fun fetchAppDirs() {

        val root = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath())

        val directories = root.listFiles()

        for (file in directories) {
            Log.d(TAG, "file name: " + file.name)
        }
    }


    @Throws(IOException::class)
    private fun createImageFileNew(): File {
        val mediaStorageDir = File(getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), TAG)

        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(TAG, "failed to create directory")
        }
        val file = File(mediaStorageDir.path + File.separator + photoFileName)

        return file

    }

    fun handleImageFromCamera(data: Intent?) {
        if (data != null && data.extras != null) {
            val bm = data.extras.get("data") as Bitmap
            if (bm != null) {
                Toast.makeText(this, "Got the bitmap", Toast.LENGTH_SHORT).show()
//                addThisPicToGallery()
//                galleryAddPic()
                show()
                bg {
                    //                    saveImageToSDCard(bm)
//                    saveOriginalImage(bm)
//                    saveTempFile()
                }

            } else {
                Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show()
            }
        }
    }


    fun saveImageToSDCard(bitmap: Bitmap) {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val outputFile = File(Environment.getExternalStorageDirectory(), "photo_$timeStamp.png")
        try {
            val fileOutputStream = FileOutputStream(outputFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun saveTempFile() {

        val outputFile = File(Environment.getExternalStorageDirectory(), "output.png")
        tempFile?.copyTo(outputFile)

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            MY_CAMERA_PICTURE -> handleImageFromCamera(data)
        }
    }

    fun File.copyTo(file: File) {
        inputStream().use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    private fun galleryAddPic() {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val f = File(mCurrentPhotoPath)
        val contentUri = Uri.fromFile(f)
        mediaScanIntent.data = contentUri
        this.sendBroadcast(mediaScanIntent)
    }

    fun show(){
        // Get the dimensions of the View
        val targetW = image.getWidth()
        val targetH = image.getHeight()

        // Get the dimensions of the bitmap
        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions)
        val photoW = bmOptions.outWidth
        val photoH = bmOptions.outHeight

        // Determine how much to scale down the image
        val scaleFactor = Math.min(photoW / targetW, photoH / targetH)

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false
        bmOptions.inSampleSize = scaleFactor

        val bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions)
        image.setImageBitmap(bitmap)
    }


}
