package com.stylingandroid.animatable2

import android.annotation.TargetApi
import android.graphics.drawable.Animatable
import android.graphics.drawable.Animatable2
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.vectordrawable.graphics.drawable.Animatable2Compat

fun Drawable.toAnimatableWrapper(): AnimatableWrapper? =
    (this as? Animatable)?.let { AnimatableWrapper.wrap(it) }

interface AnimatableWrapper : Animatable {
    fun clearAnimationCallbacks()
    fun registerAnimationCallback(callback: AnimationCallback)
    fun unregisterAnimationCallback(callback: AnimationCallback): Boolean

    interface AnimationCallback {
        fun onAnimationEnd(drawable: Drawable)
        fun onAnimationStart(drawable: Drawable)
    }

    open class AnimationCallbackAdapter : AnimationCallback {
        override fun onAnimationEnd(drawable: Drawable) { /* NO-OP */ }

        override fun onAnimationStart(drawable: Drawable) { /* NO-OP */ }
    }

    companion object {
        fun wrap(animatable: Animatable): AnimatableWrapper =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                wrapM(animatable)
            } else {
                wrapLegacy(animatable)
            }

        @TargetApi(Build.VERSION_CODES.M)
        private fun wrapM(animatable: Animatable): AnimatableWrapper =
            when (animatable) {
                is Animatable2 -> Animatable2Wrapper(animatable)
                is Animatable2Compat -> Animatable2CompatWrapper(animatable)
                else -> AnimatableWrapperBase<Unit>(animatable)
            }

        private fun wrapLegacy(animatable: Animatable): AnimatableWrapper =
            when (animatable) {
                is Animatable2Compat -> Animatable2CompatWrapper(animatable)
                else -> AnimatableWrapperBase<Unit>(animatable)
            }
    }
}

private open class AnimatableWrapperBase<T> constructor(animatable: Animatable) :
    Animatable by animatable, AnimatableWrapper {

    protected val registeredCallbacks = mutableMapOf<AnimatableWrapper.AnimationCallback, T>()

    override fun clearAnimationCallbacks() { /* NO-OP */ }

    override fun registerAnimationCallback(callback: AnimatableWrapper.AnimationCallback) {
        /* NO-OP */
    }

    override fun unregisterAnimationCallback(
        callback: AnimatableWrapper.AnimationCallback
    ): Boolean = false
}

@TargetApi(Build.VERSION_CODES.M)
private class Animatable2Wrapper(private val animatable2: Animatable2) :
    AnimatableWrapperBase<Animatable2.AnimationCallback>(animatable2) {

    override fun registerAnimationCallback(callback: AnimatableWrapper.AnimationCallback) {
        Animatable2Callback(callback).also { innerCallback ->
            animatable2.registerAnimationCallback(innerCallback)
            registeredCallbacks += callback to innerCallback
        }
    }

    override fun unregisterAnimationCallback(
        callback: AnimatableWrapper.AnimationCallback
    ): Boolean {
        return registeredCallbacks.remove(callback)?.let { innerCallback ->
            animatable2.unregisterAnimationCallback(innerCallback)
        } ?: false
    }

    override fun clearAnimationCallbacks() {
        animatable2.clearAnimationCallbacks()
        registeredCallbacks.clear()
    }
}

@TargetApi(Build.VERSION_CODES.M)
private class Animatable2Callback(
    private val animatable2Callback: AnimatableWrapper.AnimationCallback
) : Animatable2.AnimationCallback() {
    override fun onAnimationStart(drawable: Drawable) {
        super.onAnimationStart(drawable)
        animatable2Callback.onAnimationStart(drawable)
    }

    override fun onAnimationEnd(drawable: Drawable) {
        super.onAnimationEnd(drawable)
        animatable2Callback.onAnimationEnd(drawable)
    }
}

private class Animatable2CompatWrapper(private val animatable2: Animatable2Compat) :
    AnimatableWrapperBase<Animatable2Compat.AnimationCallback>(animatable2) {
    override fun registerAnimationCallback(callback: AnimatableWrapper.AnimationCallback) {
        Animatable2CompatCallback(callback).also { innerCallback ->
            animatable2.registerAnimationCallback(innerCallback)
            registeredCallbacks += callback to innerCallback
        }
    }

    override fun unregisterAnimationCallback(
        callback: AnimatableWrapper.AnimationCallback
    ): Boolean {
        return registeredCallbacks.remove(callback)?.let { innerCallback ->
            animatable2.unregisterAnimationCallback(innerCallback)
        } ?: false
    }

    override fun clearAnimationCallbacks() {
        animatable2.clearAnimationCallbacks()
        registeredCallbacks.clear()
    }
}

private class Animatable2CompatCallback(
    private val animatable2Callback: AnimatableWrapper.AnimationCallback
) : Animatable2Compat.AnimationCallback() {
    override fun onAnimationStart(drawable: Drawable) {
        super.onAnimationStart(drawable)
        animatable2Callback.onAnimationStart(drawable)
    }

    override fun onAnimationEnd(drawable: Drawable) {
        super.onAnimationEnd(drawable)
        animatable2Callback.onAnimationEnd(drawable)
    }
}
