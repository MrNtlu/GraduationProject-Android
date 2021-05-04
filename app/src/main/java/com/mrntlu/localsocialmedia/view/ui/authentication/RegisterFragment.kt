package com.mrntlu.localsocialmedia.view.ui.authentication

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenResumed
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.mrntlu.localsocialmedia.R
import com.mrntlu.localsocialmedia.databinding.FragmentRegisterBinding
import com.mrntlu.localsocialmedia.service.model.retrofitmodel.retrofitbody.authentication.RegisterBody
import com.mrntlu.localsocialmedia.utils.MaterialDialogUtil
import com.mrntlu.localsocialmedia.utils.isNotEmptyOrBlank
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
        binding.registerLoginButton.setOnClickListener {
            //navController.popBackStack()
            navController.navigate(R.id.action_registerFragment_to_loginFragment)
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
                    clearFocus(view)
                    (activity as AuthenticationActivity).setLoadingVisibility(false)

                    if (it.status == 200){
                        lifecycleScope.launch {
                            MaterialDialogUtil.showInfoDialog(view.context,"Success","Successfully Registered.")
                            navController.navigate(R.id.action_registerFragment_to_loginFragment)
                        }
                    }else
                        onError(it.message)
                }
            }else
                onError(errorMessage)
        }

        binding.registerLayout.setOnClickListener {
            clearFocus(it)
        }
    }

    private fun clearFocus(view: View){
        HideUtil.hideSoftKeyboard(view)
        if (binding.registerNameEditText.hasFocus())
            binding.registerNameEditText.clearFocus()
        if (binding.registerMailEditText.hasFocus())
            binding.registerMailEditText.clearFocus()
        if (binding.registerPasswordEditText.hasFocus())
            binding.registerPasswordEditText.clearFocus()
        if (binding.registerPasswordAgainEditText.hasFocus())
            binding.registerPasswordAgainEditText.clearFocus()
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