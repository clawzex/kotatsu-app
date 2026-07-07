package org.koitharu.kotatsu

import android.content.Context
import android.os.Build
import android.os.StrictMode
import androidx.fragment.app.strictmode.FragmentStrictMode
import org.koitharu.kotatsu.core.BaseApp

class KotatsuApp : BaseApp() {

	override fun attachBaseContext(base: Context) {
		super.attachBaseContext(base)
		enableStrictMode()
	}

	private fun enableStrictMode() {
		val notifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
			StrictModeNotifier(this)
		} else {
			null
		}
		StrictMode.setThreadPolicy(
			StrictMode.ThreadPolicy.Builder().apply {
				detectNetwork()
				detectDiskWrites()
				detectCustomSlowCalls()
				detectResourceMismatches()
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) detectUnbufferedIo()
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) detectExplicitGc()
				penaltyLog()
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && notifier != null) {
					penaltyListener(notifier.executor, notifier)
				}
			}.build(),
		)
		StrictMode.setVmPolicy(
			StrictMode.VmPolicy.Builder().apply {
				detectActivityLeaks()
				detectLeakedSqlLiteObjects()
				detectLeakedClosableObjects()
				detectLeakedRegistrationObjects()
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					detectContentUriWithoutPermission()
				}
				detectFileUriExposure()
				penaltyLog()
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && notifier != null) {
					penaltyListener(notifier.executor, notifier)
				}
			}.build(),
		)
		FragmentStrictMode.defaultPolicy = FragmentStrictMode.Policy.Builder().apply {
			detectWrongFragmentContainer()
			detectFragmentTagUsage()
			detectRetainInstanceUsage()
			detectSetUserVisibleHint()
			detectWrongNestedHierarchy()
			detectFragmentReuse()
			penaltyLog()
			if (notifier != null) {
				penaltyListener(notifier)
			}
		}.build()
	}
}
