package org.koitharu.kotatsu.core.ui.util

import android.animation.TimeInterpolator
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.PathInterpolator
import androidx.annotation.AnimRes
import androidx.core.view.doOnLayout
import org.koitharu.kotatsu.R

/**
 * iOS-inspired UI utilities: spring animations, blur effects, and transitions.
 */
object IosUiHelper {

	val springInterpolator: TimeInterpolator by lazy {
		PathInterpolator(0.175f, 0.885f, 0.32f, 1.275f)
	}

	val easeOutInterpolator: TimeInterpolator by lazy {
		PathInterpolator(0.4f, 0f, 0.2f, 1f)
	}

	fun View.applyBlurEffect(radius: Float = 24f) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			setRenderEffect(
				RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.CLAMP),
			)
		}
	}

	fun View.clearBlurEffect() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			setRenderEffect(null)
		}
	}

	fun View.playIosScaleIn() {
		startAnimation(AnimationUtils.loadAnimation(context, R.anim.ios_scale_fade_in))
	}

	fun View.playIosScaleOut(@AnimRes animRes: Int = R.anim.ios_scale_fade_out) {
		startAnimation(AnimationUtils.loadAnimation(context, animRes))
	}

	fun View.animateSpringShow(duration: Long = 350L) {
		alpha = 0f
		scaleX = 0.92f
		scaleY = 0.92f
		visibility = View.VISIBLE
		animate()
			.alpha(1f)
			.scaleX(1f)
			.scaleY(1f)
			.setDuration(duration)
			.setInterpolator(springInterpolator)
			.start()
	}

	fun View.animateSpringHide(duration: Long = 250L, onEnd: (() -> Unit)? = null) {
		animate()
			.alpha(0f)
			.scaleX(0.95f)
			.scaleY(0.95f)
			.setDuration(duration)
			.setInterpolator(easeOutInterpolator)
			.withEndAction {
				visibility = View.GONE
				alpha = 1f
				scaleX = 1f
				scaleY = 1f
				onEnd?.invoke()
			}
			.start()
	}

	fun View.animatePress(scale: Float = 0.96f) {
		animate()
			.scaleX(scale)
			.scaleY(scale)
			.setDuration(100)
			.setInterpolator(easeOutInterpolator)
			.start()
	}

	fun View.animateRelease() {
		animate()
			.scaleX(1f)
			.scaleY(1f)
			.setDuration(200)
			.setInterpolator(springInterpolator)
			.start()
	}

	fun View.setupPressAnimation() {
		isClickable = true
		setOnTouchListener { v, event ->
			when (event.actionMasked) {
				android.view.MotionEvent.ACTION_DOWN -> v.animatePress()
				android.view.MotionEvent.ACTION_UP,
				android.view.MotionEvent.ACTION_CANCEL,
				-> v.animateRelease()
			}
			false
		}
	}

	fun View.crossfadeWith(other: View, duration: Long = 300L) {
		other.alpha = 0f
		other.visibility = View.VISIBLE
		animate().alpha(0f).setDuration(duration).withEndAction {
			visibility = View.GONE
		}.start()
		other.animate().alpha(1f).setDuration(duration).start()
	}

	fun View.doOnLayoutSpring(action: (View) -> Unit) {
		doOnLayout { view ->
			view.alpha = 0f
			view.translationY = 24f
			action(view)
			view.animate()
				.alpha(1f)
				.translationY(0f)
				.setDuration(400)
				.setInterpolator(springInterpolator)
				.start()
		}
	}
}
