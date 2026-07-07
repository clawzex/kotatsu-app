package org.koitharu.kotatsu.core.network

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Adds browser-like headers so Cloudflare-protected sources treat requests as a normal client.
 */
class BrowserHeadersInterceptor : Interceptor {

	override fun intercept(chain: Interceptor.Chain): Response {
		val request = chain.request()
		val builder = request.newBuilder()
		if (request.header(CommonHeaders.ACCEPT) == null) {
			builder.header(
				CommonHeaders.ACCEPT,
				"text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8",
			)
		}
		if (request.header("Accept-Language") == null) {
			builder.header("Accept-Language", "en-US,en;q=0.9")
		}
		if (request.header("Upgrade-Insecure-Requests") == null) {
			builder.header("Upgrade-Insecure-Requests", "1")
		}
		// Do NOT set Accept-Encoding here — OkHttp adds it and transparently decompresses
		// only when the header is absent. Setting it manually breaks all parser responses.
		return chain.proceed(builder.build())
	}
}
