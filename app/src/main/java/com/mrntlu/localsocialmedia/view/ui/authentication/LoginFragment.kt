package com.mrntlu.localsocialmedia.view.ui.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenResumed
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.mrntlu.localsocialmedia.R
import com.mrntlu.localsocialmedia.databinding.FragmentLoginBinding
import com.mrntlu.localsocialmedia.service.UserManager
import com.mrntlu.localsocialmedia.service.model.retrofitbody.LoginBody
import com.mrntlu.localsocialmedia.utils.MaterialDialogUtil
import com.mrntlu.localsocialmedia.utils.isNotEmptyOrBlank
import com.mrntlu.localsocialmedia.utils.printLog
import com.mrntlu.localsocialmedia.view.`interface`.CoroutinesErrorHandler
import com.mrntlu.localsocialmedia.view.ui.main.MainActivity
import com.mrntlu.localsocialmedia.viewmodel.AuthenticationViewModel
import com.yinglan.keyboard.HideUtil
import kotlinx.coroutines.launch

class LoginFragment : Fragment(R.layout.fragment_login), CoroutinesErrorHandler {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private var isFragmentSet = false
    private lateinit var userManager: UserManager
    private lateinit var navController: NavController
    private val authenticationViewModel: AuthenticationViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    //https://www.mobiler.dev/post/jetpack-datastore-entegrasyonu
    //https://proandroiddev.com/welcome-datastore-good-bye-sharedpreferences-4bf68e70efdb
    //https://medium.com/android-news/token-authorization-with-retrofit-android-oauth-2-0-747995c79720
    //https://github.com/MrNtlu/MyAnimeInfo2/blob/master/app/src/main/java/com/mrntlu/myanimeinfo2/service/AnimeService.kt
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userManager = UserManager(view.context)
        lifecycleScope.launch {
            userManager.deleteDataStore()
        }
        navController = Navigation.findNavController(view)

        userManager.getUserInfo().asLiveData().observe(viewLifecycleOwner, {
            if (it.first != null && it.second != null) {
                val intent = Intent(activity, MainActivity::class.java)
                val bundle = bundleOf(
                        MainActivity.TOKEN_ARG to it.first,
                        MainActivity.ID_ARG to it.second
                )
                intent.putExtras(bundle)
                startActivity(intent)
            } else {
                setFragment()
            }
        })
    }

    private fun setFragment(){
        if (!isFragmentSet) {
            setListeners()
            setObservers()
            isFragmentSet = true
        }
    }

    private fun setListeners() {
        binding.loginLoginButton.setOnClickListener { view ->
            val email = binding.loginMailEditText.text.toString()
            val password = binding.loginPasswordEditText.text.toString()
            if (email.isNotEmptyOrBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches() && password.isNotEmptyOrBlank()){
                (activity as AuthenticationActivity).setLoadingVisibility(true)
                authenticationViewModel.loginUser(LoginBody(email, password), this).observe(viewLifecycleOwner){
                    (activity as AuthenticationActivity).setLoadingVisibility(false)
                    if (it.status == 200){
                        lifecycleScope.launch {
                            userManager.saveToken(it.data!!.token, it.data.id)
                        }
                    }else
                        onError(it.message)
                }
            }else{
                MaterialDialogUtil.showErrorDialog(view.context, "Please enter a valid email or don't leave anything empty.")
            }
        }

        binding.loginFPButton.setOnClickListener {
            TODO("Implement new page or dialog")
        }

        binding.loginRegisterButton.setOnClickListener {
            navController.navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.loginLayout.setOnClickListener {
            HideUtil.hideSoftKeyboard(it)
            if (binding.loginMailEditText.hasFocus())
                binding.loginMailEditText.clearFocus()
            if (binding.loginPasswordEditText.hasFocus())
                binding.loginPasswordEditText.clearFocus()
        }
    }

    private fun setObservers(){

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
        isFragmentSet = false
    }
}