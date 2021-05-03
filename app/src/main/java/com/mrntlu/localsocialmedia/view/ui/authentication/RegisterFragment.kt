package com.mrntlu.localsocialmedia.view.ui.authentication

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenResumed
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.mrntlu.localsocialmedia.R
import com.mrntlu.localsocialmedia.databinding.FragmentRegisterBinding
import com.mrntlu.localsocialmedia.service.model.retrofitbody.RegisterBody
import com.mrntlu.localsocialmedia.utils.DialogButtons
import com.mrntlu.localsocialmedia.utils.MaterialDialogUtil
import com.mrntlu.localsocialmedia.utils.isNotEmptyOrBlank
import com.mrntlu.localsocialmedia.utils.printLog
import com.mrntlu.localsocialmedia.view.`interface`.CoroutinesErrorHandler
import com.mrntlu.localsocialmedia.viewmodel.AuthenticationViewModel
import com.yinglan.keyboard.HideUtil
import kotlinx.coroutines.launch
import java.util.*


class RegisterFragment : Fragment(), CoroutinesErrorHandler {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController
    private val authenticationViewModel: AuthenticationViewModel by viewModels()
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

        binding.registerRegisterButton.setOnClickListener {view ->
            val name = binding.registerNameEditText.text.toString()
            val username = binding.registerUserameEditText.text.toString()
            val email = binding.registerMailEditText.text.toString()
            val password = binding.registerPasswordEditText.text.toString()
            val passwordAgain = binding.registerPasswordAgainEditText.text.toString()
            val errorMessage = checkRegisterRules(name, username, email, password, passwordAgain)

            if (errorMessage == null){
                (activity as AuthenticationActivity).setLoadingVisibility(true)
                authenticationViewModel.registerUser(RegisterBody(username, email, name, password),this).observe(viewLifecycleOwner){
                    (activity as AuthenticationActivity).setLoadingVisibility(false)
                    printLog("$it")
                    if (it.status == 200){
                        lifecycleScope.launch {
                            MaterialDialogUtil.showInfoDialog(view.context,"Success","Successfully Registered.")
                            navController.popBackStack()
                        }
                    }else
                        onError(it.message)
                }
            }else
                onError(errorMessage)
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

    private fun checkRegisterRules(name: String, username: String, email: String, password: String, passwordAgain: String): String?{
        return if (!name.isNotEmptyOrBlank()){
            "${getString(R.string.register_please_enter_your_)} ${getString(R.string.name).decapitalize(Locale.getDefault())}."
        }else if (!username.isNotEmptyOrBlank()){
            "${getString(R.string.register_please_enter_your_)} ${getString(R.string.username).decapitalize(Locale.getDefault())}."
        }else if (!email.isNotEmptyOrBlank()){
            "${getString(R.string.register_please_enter_your_)} ${getString(R.string.email).decapitalize(Locale.getDefault())}"
        }else if (!password.isNotEmptyOrBlank()){
            "${getString(R.string.register_please_enter_your_)} ${getString(R.string.password).decapitalize(Locale.getDefault())}."
        }else if (password != passwordAgain){
            "Passwords do not match."
        }else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            "Invalid email adress."
        }else if (!binding.privacySwitch.isChecked){
            "Please accept the Privacy & Policy"
        }else if (!binding.termsSwitch.isChecked){
            "Please accept the Terms & Conditions"
        }else
            null
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

    override fun onError(message: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            whenResumed {
                (activity as AuthenticationActivity).setLoadingVisibility(false)
                context?.let {
                    MaterialDialogUtil.showErrorDialog(it, message)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}