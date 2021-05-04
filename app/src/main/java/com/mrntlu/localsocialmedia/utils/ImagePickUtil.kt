package com.mrntlu.localsocialmedia.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide

class ImagePickUtil {

    /*
    startActivityForResult(openFileChooser(), 1)
    if (resultCode != Activity.RESULT_CANCELED){
        profileImage = onActivityResultHandler(requestCode, resultCode, data, binding.registerImagePickButton)
    }
    */

    fun openFileChooser(): Intent {
        val intent = Intent()
        intent.apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        return intent
    }

    fun onActivityResultHandler(requestCode: Int, resultCode: Int, data: Intent?, image: ImageView?): Uri? {
        return if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            if (image != null)
                Glide.with(image.context).load(data.data).into(image)
            data.data
        } else {
            null
        }
    }
}