package com.mrntlu.localsocialmedia.view.ui.main

import android.os.Bundle
import android.view.*
import android.widget.AbsListView
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenResumed
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mrntlu.localsocialmedia.R
import com.mrntlu.localsocialmedia.databinding.FragmentSearchBinding
import com.mrntlu.localsocialmedia.service.model.UserModel
import com.mrntlu.localsocialmedia.utils.Constants
import com.mrntlu.localsocialmedia.utils.MaterialDialogUtil
import com.mrntlu.localsocialmedia.view.`interface`.CoroutinesErrorHandler
import com.mrntlu.localsocialmedia.view.adapter.UserAdapter
import com.mrntlu.localsocialmedia.view.adapter.UserInteraction
import com.mrntlu.localsocialmedia.viewmodel.UserViewModel
import kotlinx.coroutines.launch

class SearchFragment : BaseFragment<FragmentSearchBinding>(), CoroutinesErrorHandler {

    private val viewModel: UserViewModel by viewModels()
    private var isLoading = false
    private var pageNum = 1
    private val userList: ArrayList<UserModel> = arrayListOf()
    private var userAdapter: UserAdapter? = null
    private var search: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSearchBinding.inflate(layoutInflater, container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        setRecyclerView()
        setObserver()
    }

    private fun setRecyclerView() {
        binding.searchRV.apply {
            val linearLayoutManager = LinearLayoutManager(context)
            layoutManager = linearLayoutManager
            userAdapter = UserAdapter(object: UserInteraction {
                override fun onItemSelected(position: Int, item: UserModel) {
                    navController.navigate(R.id.action_searchFragment_to_profileFragment,
                        bundleOf(ProfileFragment.USER_ARG to item)
                    )
                }

                override fun onFollowPressed(position: Int, userModel: UserModel) {

                }

                override fun onErrorRefreshPressed() {
                    userAdapter?.submitLoading()
                    setData()
                }
            })
            adapter = userAdapter
            userAdapter?.submitList(arrayListOf())

            var isScrolling = false
            addOnScrollListener(object: RecyclerView.OnScrollListener(){
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    isScrolling = newState != AbsListView.OnScrollListener.SCROLL_STATE_IDLE
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    userAdapter?.let {
                        if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == it.itemCount - 1 && isScrolling && !isLoading) {
                            isLoading = true
                            pageNum++
                            it.submitPaginationLoading()
                            this@apply.scrollToPosition(it.itemCount - 1)
                            setData()
                        }
                    }
                }
            })
        }
    }

    private fun setObserver() {
        viewModel.searchUserObserver().observe(viewLifecycleOwner){ response ->
            response.results.map {
                it._image = it._image?.replaceFirst(Constants.URL,"")
            }
            if (pageNum <= 1){
                userList.clear()
                userList.addAll(response.results)
                userAdapter?.submitList(userList)
            }else {
                userList.addAll(response.results)
                userAdapter?.updateList(response.results)
            }
        }
    }

    private fun setData(){
        viewModel.searchUser(search, pageNum, token, this@SearchFragment)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        val searchView = menu.findItem(R.id.searchUser).actionView as SearchView
        searchView.apply {
            queryHint = "Search User"
            setIconifiedByDefault(true)
            isIconified = true

            setOnQueryTextListener(object: SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(query: String?): Boolean {
                    search = query ?: ""
                    clearFocus()
                    userAdapter?.submitLoading()
                    pageNum = 1
                    setData()

                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    return true
                }
            })
        }
    }

    override fun onError(message: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            whenResumed {
                context?.let {
                    MaterialDialogUtil.showErrorDialog(it, message)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}