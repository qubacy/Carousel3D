package com.example.carousel3d.view.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.carousel3d.Application
import com.example.carousel3d.databinding.ActivityMainBinding
import com.example.carousel3d.view.main.adapter.UserCarouselAdapter
import com.example.carousel3d.view.main.model.MainActivityModel
import com.example.carousel3d.view.main.model.MainActivityModelFactory
import com.example.carousel3dlib.layoutmanager.Carousel3DHorizontalScrollCallback
import com.example.carousel3dlib.layoutmanager.Carousel3DLayoutManager
import kotlinx.coroutines.launch

class MainActivity(
    
) : AppCompatActivity(), Carousel3DHorizontalScrollCallback {
    companion object {
        const val TAG = "MAIN_ACTIVITY"
    }

    private lateinit var binding: ActivityMainBinding
    private val model: MainActivityModel by viewModels {
        MainActivityModelFactory((application as Application).appContainer.userDataRepository)
    }

    private var userListAdapter: UserCarouselAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        userListAdapter = UserCarouselAdapter(model)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        // It's crucial to attach an Adapter to a RecyclerView BEFORE providing any changes to the
        // first one!

        binding.userRecyclerView.apply {
            layoutManager = Carousel3DLayoutManager().apply {
                addHorizontalScrollCallback(this@MainActivity)
            }
            adapter = userListAdapter
        }

        lifecycleScope.launch {
            model.userFlow.collect {
                userListAdapter?.setItems(it)
            }
        }
    }

    override fun onHorizontalScroll(fraction: Float) {
        Log.d(TAG, "onHorizontalScroll(): fraction = $fraction")
    }
}