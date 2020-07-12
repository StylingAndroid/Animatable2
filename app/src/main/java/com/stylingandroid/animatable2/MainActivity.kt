package com.stylingandroid.animatable2

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.stylingandroid.animatable2.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var animatable: AnimatableWrapper? = null

    private val callback = object : AnimatableWrapper.AnimationCallbackAdapter() {
        override fun onAnimationEnd(drawable: Drawable) {
            println("Animation ended")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        animatable = binding.animation.drawable.toAnimatableWrapper()?.apply {
            registerAnimationCallback(callback)
            binding.animation.setOnClickListener {
                start()
            }
        }
    }

    override fun onDestroy() {
        animatable?.unregisterAnimationCallback(callback)
        super.onDestroy()
    }
}
