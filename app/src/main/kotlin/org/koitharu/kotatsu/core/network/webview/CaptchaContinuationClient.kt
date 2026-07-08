package org.koitharu.kotatsu.core.network.webview

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.webkit.CookieManager
import android.webkit.WebView
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.koitharu.kotatsu.core.network.cookies.MutableCookieJar
import org.koitharu.kotatsu.parsers.network.CloudFlareHelper
import kotlin.coroutines.Continuation

class CaptchaContinuationClient(
	private val cookieJar: MutableCookieJar,
	private val targetUrl: String,
	continuation: Continuation<Unit>,
) : ContinuationResumeWebViewClient(continuation) {

	private val oldClearance = CloudFlareHelper.getClearanceCookie(cookieJar, targetUrl)
	private val handler = Handler(Looper.getMainLooper())
	private val cookieCheckRunnable = object : Runnable {
		override fun run() {
			syncCookiesFromWebView()
			val clearance = CloudFlareHelper.getClearanceCookie(cookieJar, targetUrl)
			if (clearance != null && clearance != oldClearance) {
				webViewRef?.let { resumeContinuation(it) }
			} else {
				handler.postDelayed(this, COOKIE_CHECK_INTERVAL)
			}
		}
	}
	private var webViewRef: WebView? = null

	override fun onPageFinished(view: WebView?, url: String?) {
		super.onPageFinished(view, url)
		syncCookiesFromWebView()
		checkClearance(view)
	}

	override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
		super.onPageStarted(view, url, favicon)
		webViewRef = view
		syncCookiesFromWebView()
		checkClearance(view)
		// Start periodic cookie polling to catch Turnstile solutions
		handler.removeCallbacks(cookieCheckRunnable)
		handler.postDelayed(cookieCheckRunnable, COOKIE_CHECK_INTERVAL)
	}

	private fun checkClearance(view: WebView?) {
		val clearance = CloudFlareHelper.getClearanceCookie(cookieJar, targetUrl)
		if (clearance != null && clearance != oldClearance) {
			handler.removeCallbacks(cookieCheckRunnable)
			resumeContinuation(view)
		}
	}

	/**
	 * Sync cookies from Android WebView CookieManager back into OkHttp's CookieJar.
	 * This ensures cf_clearance obtained by the WebView is available to OkHttp requests.
	 */
	private fun syncCookiesFromWebView() {
		val url = targetUrl.toHttpUrlOrNull() ?: return
		val cookieManager = CookieManager.getInstance()
		val cookieString = cookieManager.getCookie(targetUrl) ?: return
		val cookies = cookieString.split(";").mapNotNull { raw ->
			val trimmed = raw.trim()
			if (trimmed.isEmpty()) return@mapNotNull null
			Cookie.parse(url, trimmed)
		}
		if (cookies.isNotEmpty()) {
			cookieJar.saveFromResponse(url, cookies)
		}
	}

	companion object {
		private const val COOKIE_CHECK_INTERVAL = 500L // Check every 500ms
	}
}
