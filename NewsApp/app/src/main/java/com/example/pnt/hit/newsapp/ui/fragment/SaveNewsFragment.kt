package com.example.pnt.hit.newsapp.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.pnt.hit.newsapp.R
import com.example.pnt.hit.newsapp.ui.NewsActivity
import com.example.pnt.hit.newsapp.ui.NewsViewModel

class SaveNewsFragment : Fragment(R.layout.fragment_saved_news) {
    lateinit var viewModel: NewsViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (activity as NewsActivity).viewModel
    }
}