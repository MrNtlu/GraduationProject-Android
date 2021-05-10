package com.mrntlu.localsocialmedia.view.`interface`

interface Interaction<T> {
    fun onItemSelected(position: Int, item: T)
    fun onErrorRefreshPressed()
}