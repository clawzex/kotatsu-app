package org.koitharu.kotatsu.core.network.cookies

import androidx.annotation.WorkerThread
import androidx.core.util.Predicate
import okhttp3.Cookie
import okhttp3.HttpUrl

/**
 * Keeps [AndroidCookieJar] as the primary store (shared with WebView after CAPTCHA)
 * and mirrors cookies to encrypted preferences so sessions survive restarts reliably.
 */
class PersistentCookieJar(
	private val primary: AndroidCookieJar,
	private val backup: PreferencesCookieJar,
) : MutableCookieJar {

	@WorkerThread
	override fun loadForRequest(url: HttpUrl): List<Cookie> {
		val fromPrimary = primary.loadForRequest(url)
		val fromBackup = backup.loadForRequest(url)
		if (fromBackup.isEmpty()) {
			return fromPrimary
		}
		if (fromPrimary.isEmpty()) {
			primary.saveFromResponse(url, fromBackup)
			return fromBackup
		}
		val merged = LinkedHashMap<String, Cookie>(fromBackup.size + fromPrimary.size)
		for (cookie in fromBackup) {
			val key = "${cookie.domain.removePrefix(".")}|${cookie.path}|${cookie.name}"
			merged[key] = cookie
		}
		for (cookie in fromPrimary) {
			val key = "${cookie.domain.removePrefix(".")}|${cookie.path}|${cookie.name}"
			merged[key] = cookie
		}
		val result = merged.values.toList()
		val missingInPrimary = result.filter { cookie ->
			fromPrimary.none { it.name == cookie.name && it.value == cookie.value }
		}
		if (missingInPrimary.isNotEmpty()) {
			primary.saveFromResponse(url, missingInPrimary)
		}
		return result
	}

	@WorkerThread
	override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
		if (cookies.isEmpty()) {
			return
		}
		primary.saveFromResponse(url, cookies)
		backup.saveFromResponse(url, cookies)
	}

	@WorkerThread
	override fun removeCookies(url: HttpUrl, predicate: Predicate<Cookie>?) {
		primary.removeCookies(url, predicate)
		backup.removeCookies(url, predicate)
	}

	override suspend fun clear(): Boolean {
		backup.clear()
		return primary.clear()
	}
}
