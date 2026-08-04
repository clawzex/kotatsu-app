package org.koitharu.kotatsu.core.network.cookies

import android.webkit.CookieManager
import androidx.annotation.WorkerThread
import androidx.core.util.Predicate
import okhttp3.Cookie
import okhttp3.HttpUrl
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AndroidCookieJar : MutableCookieJar {

	private val cookieManager = CookieManager.getInstance()

	@WorkerThread
	override fun loadForRequest(url: HttpUrl): List<Cookie> {
		val rawCookie = runCatching { cookieManager.getCookie(url.toString()) }.getOrNull() ?: return emptyList()
		return rawCookie.split(';').mapNotNull {
			parseWebViewCookie(url, it)
		}
	}

	@WorkerThread
	override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
		if (cookies.isEmpty()) {
			return
		}
		val urlString = url.toString()
		runCatching {
			for (cookie in cookies) {
				cookieManager.setCookie(urlString, cookie.toString())
			}
			safeFlush(cookieManager)
		}
	}

	override fun removeCookies(url: HttpUrl, predicate: Predicate<Cookie>?) {
		val cookies = loadForRequest(url)
		if (cookies.isEmpty()) {
			return
		}
		val urlString = url.toString()
		runCatching {
			for (c in cookies) {
				if (predicate != null && !predicate.test(c)) {
					continue
				}
				val nc = c.newBuilder()
					.expiresAt(System.currentTimeMillis() - 100000)
					.build()
				cookieManager.setCookie(urlString, nc.toString())
			}
			safeFlush(cookieManager)
		}
	}

	override suspend fun clear() = suspendCoroutine<Boolean> { continuation ->
		runCatching {
			cookieManager.removeAllCookies(continuation::resume)
		}.onFailure {
			continuation.resume(false)
		}
	}

	companion object {
		fun safeFlush(cookieManager: CookieManager) {
			try {
				cookieManager.flush()
			} catch (e: Throwable) {
				runCatching {
					java.util.concurrent.Executors.newSingleThreadExecutor().execute {
						runCatching { cookieManager.flush() }
					}
				}
			}
		}

		fun parseWebViewCookie(url: HttpUrl, rawCookie: String): Cookie? {
			val trimmed = rawCookie.trim()
			if (trimmed.isEmpty()) return null
			val topDomain = runCatching { url.topPrivateDomain() }.getOrNull()
				?: extractRootDomain(url.host)
			val cookieWithDomain = if (trimmed.contains("domain=", ignoreCase = true)) {
				trimmed
			} else {
				"$trimmed; domain=.$topDomain"
			}
			return Cookie.parse(url, cookieWithDomain)
				?: Cookie.parse(url, trimmed)
		}

		private fun extractRootDomain(host: String): String {
			val parts = host.split('.')
			if (parts.size >= 2 && !host.matches(Regex("\\d+\\.\\d+\\.\\d+\\.\\d+"))) {
				return parts.takeLast(2).joinToString(".")
			}
			return host
		}
	}
}
