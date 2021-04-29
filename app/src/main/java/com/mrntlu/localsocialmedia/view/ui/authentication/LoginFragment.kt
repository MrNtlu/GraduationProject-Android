package com.mrntlu.localsocialmedia.view.ui.authentication

import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.mrntlu.localsocialmedia.R
import com.mrntlu.localsocialmedia.databinding.FragmentLoginBinding
import com.mrntlu.localsocialmedia.utils.MaterialDialogUtil
import com.mrntlu.localsocialmedia.utils.isNotEmptyOrBlank
import com.yinglan.keyboard.HideUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoginFragment : Fragment(R.layout.fragment_login) {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(layoutInflater, container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        setListeners()
    }

    private fun setListeners() {
        binding.loginLoginButton.setOnClickListener {
            val email = binding.loginMailEditText.text.toString()
            val password = binding.loginPasswordEditText.text.toString()
            lifecycleScope.launch {
                (activity as AuthenticationActivity).setLoadingVisibility(true)
                delay(2000L)
                (activity as AuthenticationActivity).setLoadingVisibility(false)
                if (email.isNotEmptyOrBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches() && password.isNotEmptyOrBlank()){
                    TODO("Redirect to content page")
                }else{
                    MaterialDialogUtil.showErrorDialog(it.context,"Please enter a valid email or don't leave anything empty.")
                }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}