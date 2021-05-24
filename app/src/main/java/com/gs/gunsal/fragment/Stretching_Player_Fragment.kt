package com.gs.gunsal.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.gs.gunsal.databinding.FragmentStretchingPlayerListBinding

/**
 * A fragment representing a list of Items.
 */
class Stretching_Player_Fragment : Fragment(){

    lateinit var binding: FragmentStretchingPlayerListBinding
    var start:Long = 0
    lateinit var callback: OnBackPressedCallback
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStretchingPlayerListBinding.inflate(layoutInflater, container, false)
        binding.webView.webChromeClient = WebChromeClient()
        binding.webView.settings.apply {
            javaScriptEnabled = true
            setAppCacheEnabled(true)
            pluginState = WebSettings.PluginState.ON
            useWideViewPort = true
            loadWithOverviewMode = true

        }
        binding.ddangsOne.setOnClickListener {
            binding.webView.loadUrl("https://www.youtube.com/embed/t70t-sklypk")
            binding.webView.visibility = View.VISIBLE
            binding.linear.visibility = View.GONE
            start = System.currentTimeMillis()
        }
        binding.ddangsTwo.setOnClickListener {
            binding.webView.loadUrl("https://www.youtube.com/embed/swRNeYw1JkY")
            binding.webView.visibility = View.VISIBLE
            binding.linear.visibility = View.GONE
            start = System.currentTimeMillis()
        }
        // Set the adapter
        return binding!!.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = object :OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                Log.d("frag", "back")
                var end = System.currentTimeMillis()
                Log.d("time", ((end-start)/1000).toString())
                binding.linear.visibility = View.VISIBLE
                binding.webView.visibility = View.GONE
            }

        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onDetach() {
        super.onDetach()
        callback.remove()
    }
    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            Stretching_Player_Fragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }

}