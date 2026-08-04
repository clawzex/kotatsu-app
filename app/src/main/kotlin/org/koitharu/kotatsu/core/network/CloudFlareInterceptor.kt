package org.koitharu.kotatsu.core.network

import okhttp3.CookieJar
import okhttp3.Interceptor
import okhttp3.Response
import okio.IOException
import org.koitharu.kotatsu.core.exceptions.CloudFlareBlockedException
import org.koitharu.kotatsu.core.exceptions.CloudFlareProtectedException
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.parsers.network.CloudFlareHelper

/**
 * Detects Cloudflare-protected responses and throws appropriate exceptions.
 *
 * This interceptor is intentionally non-blocking (no runBlocking).
 * Cloudflare protection exceptions are caught in coroutine scopes (e.g. [ParserMangaRepository],
 * [CaptchaHandler]) where [AutoCaptchaSolver] can run asynchronously without thread deadlocks.
 */
class CloudFlareInterceptor(
	private val cookieJar: CookieJar,
) : Interceptor {

	override fun intercept(chain: Interceptor.Chain): Response {
		val request = chain.request()
		val response = chain.proceed(request)
		return when (CloudFlareHelper.checkResponseForProtection(response)) {
			CloudFlareHelper.PROTECTION_BLOCKED -> response.closeThrowing(
				CloudFlareBlockedException(
					url = request.url.toString(),
					source = request.tag(MangaSource::class.java),
				),
			)

			CloudFlareHelper.PROTECTION_CAPTCHA -> {
				try {
					response.close()
				} catch (_: Exception) {
				}
				val source = request.tag(MangaSource::class.java)

				// Fast path: if another thread solved the captcha, retry immediately
				if (!CloudFlareHelper.getClearanceCookie(cookieJar, request.url.toString()).isNullOrEmpty()) {
					val retryResponse = chain.proceed(request)
					if (CloudFlareHelper.checkResponseForProtection(retryResponse) == CloudFlareHelper.PROTECTION_NOT_DETECTED) {
						return retryResponse
					}
					try {
						retryResponse.close()
					} catch (_: Exception) {
					}
				}

				// Retry once before throwing (standard OkHttp retry behavior)
				val retryResponse = chain.proceed(request)
				when (CloudFlareHelper.checkResponseForProtection(retryResponse)) {
					CloudFlareHelper.PROTECTION_NOT_DETECTED -> retryResponse
					CloudFlareHelper.PROTECTION_BLOCKED -> retryResponse.closeThrowing(
						CloudFlareBlockedException(
							url = request.url.toString(),
							source = source,
						),
					)

					else -> retryResponse.closeThrowing(
						CloudFlareProtectedException(
							url = request.url.toString(),
							source = source,
							headers = request.headers,
						),
					)
				}
			}

			else -> response
		}
	}

	private fun Response.closeThrowing(error: IOException): Nothing {
		try {
			close()
		} catch (e: Exception) {
			error.addSuppressed(e)
		}
		throw error
	}
}
