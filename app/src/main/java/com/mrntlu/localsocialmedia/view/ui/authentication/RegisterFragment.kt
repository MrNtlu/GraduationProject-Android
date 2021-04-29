package com.mrntlu.localsocialmedia.view.ui.authentication

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.mrntlu.localsocialmedia.databinding.FragmentRegisterBinding
import com.yinglan.keyboard.HideUtil


class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController

    private var profileImage: Uri? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegisterBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        setListeners()
    }

    private fun setListeners(){
        binding.registerImagePickButton.setOnClickListener {
            startActivityForResult(openFileChooser(), 1)
        }

        binding.registerLoginButton.setOnClickListener {
            navController.popBackStack()
        }

        binding.registerRegisterButton.setOnClickListener {

        }

        binding.registerLayout.setOnClickListener {
            HideUtil.hideSoftKeyboard(it)
            if (binding.registerNameEditText.hasFocus())
                binding.registerNameEditText.clearFocus()
            if (binding.registerMailEditText.hasFocus())
                binding.registerMailEditText.clearFocus()
            if (binding.registerPasswordEditText.hasFocus())
                binding.registerPasswordEditText.clearFocus()
            if (binding.registerPasswordAgainEditText.hasFocus())
                binding.registerPasswordAgainEditText.clearFocus()
        }
    }

    private fun openFileChooser(): Intent {
        val intent = Intent()
        intent.apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        return intent
    }

    private fun onActivityResultHandler(requestCode: Int, resultCode: Int, data: Intent?, image: ImageView?): Uri? {
        return if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.data != null) {
            if (image != null)
                Glide.with(image.context).load(data.data).into(image)
            data.data
        } else {
            null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_CANCELED){
            profileImage = onActivityResultHandler(requestCode, resultCode, data, binding.registerImagePickButton)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}