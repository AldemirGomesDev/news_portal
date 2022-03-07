package com.aldemir.newsportal.ui.detail

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.http.SslError
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import com.aldemir.newsportal.R
import com.aldemir.newsportal.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    companion object {
        const val PAGE_URL = "pageUrl"
        const val MAX_PROGRESS = 100
        fun newIntent(context: Context, pageUrl: String): Intent {
            val intent = Intent(context, DetailActivity::class.java)
            intent.putExtra(PAGE_URL, pageUrl)
            return intent
        }
    }

    private lateinit var pageUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = applicationContext.getString(R.string.title_detail)

        pageUrl = intent.getStringExtra(PAGE_URL)
            ?: throw IllegalStateException("field $PAGE_URL missing in Intent")
        initWebView()
        setWebClient()
        loadUrl(pageUrl)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.loadWithOverviewMode = true
        binding.webView.settings.useWideViewPort = true
        binding.webView.settings.domStorageEnabled = true
        binding.webView.webViewClient = object : WebViewClient() {
            override
            fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                handler?.proceed()
            }
        }
    }

    private fun setWebClient() {
        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(
                view: WebView,
                newProgress: Int
            ) {
                super.onProgressChanged(view, newProgress)
                binding.progressBarDetail.progress = newProgress
                if (newProgress < MAX_PROGRESS && binding.progressBarDetail.visibility == ProgressBar.GONE) {
                    binding.progressBarDetail.visibility = ProgressBar.VISIBLE
                }
                if (newProgress == MAX_PROGRESS) {
                    binding.progressBarDetail.visibility = ProgressBar.GONE
                }
            }
        }
    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && binding.webView.canGoBack()) {
            binding.webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
    private fun loadUrl(pageUrl: String) {
        binding.webView.loadUrl(pageUrl)
    }
}