package org.koitharu.kotatsu.browser.cloudflare

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.webkit.CookieManager
import android.webkit.WebView
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.koitharu.kotatsu.browser.BrowserClient
import org.koitharu.kotatsu.core.network.cookies.AndroidCookieJar
import org.koitharu.kotatsu.core.network.cookies.MutableCookieJar
import org.koitharu.kotatsu.core.network.webview.CaptchaSolverScript
import org.koitharu.kotatsu.core.network.webview.adblock.AdBlock
import org.koitharu.kotatsu.core.util.ext.printStackTraceDebug
import org.koitharu.kotatsu.parsers.network.CloudFlareHelper

class CloudFlareClient(
	private val cookieJar: MutableCookieJar,
	private val callback: CloudFlareCallback,
	adBlock: AdBlock,
	private val targetUrl: String,
	private val userAgent: String = "",
) : BrowserClient(callback, adBlock) {

	private val handler = Handler(Looper.getMainLooper())
	private var webViewRef: WebView? = null
	private var checkPassedFired = false

	private val cookieCheckRunnable: Runnable = object : Runnable {
		override fun run() {
			if (checkPassedFired) return
			syncCookiesFromWebView(webViewRef)
			if (checkClearance(webViewRef)) {
				return
			}
			webViewRef?.let { tryAutoSolve(it) }
			handler.postDelayed(this, 300L)
		}
	}

	override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
		super.onPageStarted(view, url, favicon)
		webViewRef = view
		injectStealthScript(view)
		syncCookiesFromWebView(view)
		if (checkClearance(view)) return
		handler.removeCallbacks(cookieCheckRunnable)
		handler.postDelayed(cookieCheckRunnable, 300L)
	}

	override fun onPageCommitVisible(view: WebView, url: String) {
		super.onPageCommitVisible(view, url)
		callback.onPageLoaded()
	}

	override fun onPageFinished(webView: WebView, url: String) {
		super.onPageFinished(webView, url)
		webViewRef = webView
		callback.onPageLoaded()
		syncCookiesFromWebView(webView)
		if (checkClearance(webView)) return
		tryAutoSolve(webView)
	}

	override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
		super.doUpdateVisitedHistory(view, url, isReload)
		syncCookiesFromWebView(view)
		checkClearance(view)
	}

	fun reset() {
		checkPassedFired = false
		handler.removeCallbacks(cookieCheckRunnable)
	}

	private fun checkClearance(view: WebView?): Boolean {
		if (checkPassedFired) return true
		syncCookiesFromWebView(view)
		val clearance = CloudFlareHelper.getClearanceCookie(cookieJar, targetUrl)
		if (!clearance.isNullOrEmpty()) {
			checkPassedFired = true
			handler.removeCallbacks(cookieCheckRunnable)
			callback.onCheckPassed()
			return true
		}
		return false
	}

	private fun injectStealthScript(view: WebView?) {
		if (view == null) return
		try {
			view.evaluateJavascript(CaptchaSolverScript.stealthScript(userAgent), null)
		} catch (e: Exception) {
			e.printStackTraceDebug()
		}
	}

	private fun tryAutoSolve(view: WebView) {
		if (checkPassedFired) return
		try {
			view.evaluateJavascript(CaptchaSolverScript.SOLVE_SCRIPT) {
				syncCookiesFromWebView(view)
				checkClearance(view)
			}
			dispatchHardwareTouch(view)
		} catch (e: Exception) {
			e.printStackTraceDebug()
		}
	}

	private fun dispatchHardwareTouch(view: WebView) {
		try {
			view.evaluateJavascript(CaptchaSolverScript.GET_WIDGET_COORDINATES_SCRIPT) { res ->
				if (checkPassedFired) return@evaluateJavascript
				val coords = res?.trim('"')?.replace("\\", "")?.split(',')
				if (coords != null && coords.size == 2) {
					val xDp = coords[0].toFloatOrNull() ?: return@evaluateJavascript
					val yDp = coords[1].toFloatOrNull() ?: return@evaluateJavascript
					val density = view.resources.displayMetrics.density
					val xPx = xDp * density
					val yPx = yDp * density
					val downTime = android.os.SystemClock.uptimeMillis()
					val eventTime = android.os.SystemClock.uptimeMillis()
					val downEvent = android.view.MotionEvent.obtain(downTime, eventTime, android.view.MotionEvent.ACTION_DOWN, xPx, yPx, 0)
					val upEvent = android.view.MotionEvent.obtain(downTime, eventTime + 100, android.view.MotionEvent.ACTION_UP, xPx, yPx, 0)
					view.dispatchTouchEvent(downEvent)
					view.dispatchTouchEvent(upEvent)
					downEvent.recycle()
					upEvent.recycle()
				}
			}
		} catch (e: Exception) {
			e.printStackTraceDebug()
		}
	}

	private fun syncCookiesFromWebView(view: WebView?) {
		val httpUrl = targetUrl.toHttpUrlOrNull() ?: return
		val cookieManager = CookieManager.getInstance()
		val cookieString = runCatching { cookieManager.getCookie(targetUrl) }.getOrNull() ?: return
		val cookies = cookieString.split(";").mapNotNull { raw ->
			AndroidCookieJar.parseWebViewCookie(httpUrl, raw)
		}
		if (cookies.isNotEmpty()) {
			cookieJar.saveFromResponse(httpUrl, cookies)
		}
		AndroidCookieJar.safeFlush(cookieManager)
	}
}
