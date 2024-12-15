package com.example.cinemate.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.cinemate.R
import com.example.cinemate.auth.LoginActivity
import com.example.cinemate.databinding.ActivityOnboardingBinding
import kotlin.math.abs
import kotlin.math.max

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var sliderAdapter: SliderAdapter
    private lateinit var onboardingItems: List<OnboardingItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        prepareOnboardingItems()
        setupViewPager()
        setupStartButton()
    }

    private fun prepareOnboardingItems() {
        onboardingItems = listOf(
            OnboardingItem(
                R.drawable.img_onboarding_1,
                "Welcome to Cinemate",
                "Your favorite ticket movies in one app"
            ),
            OnboardingItem(
                R.drawable.img_onboarding_2,
                "Discover Movies",
                "Browse through thousands of movies and shows"
            ),
            OnboardingItem(
                R.drawable.img_onboarding_3,
                "Join Us Now",
                "Create an account to get started with Cinemate"
            )
        )
    }

    private fun setupViewPager() {
        sliderAdapter = SliderAdapter(onboardingItems)

        binding.apply {
            viewPager.apply {
                orientation = ViewPager2.ORIENTATION_HORIZONTAL
                adapter = sliderAdapter
                offscreenPageLimit = onboardingItems.size
                currentItem = 0
            }

            wormDotsIndicator.setViewPager2(viewPager)

            val pageMargin = resources.getDimensionPixelOffset(R.dimen.pageMargin).toFloat()
            val pageOffset = resources.getDimensionPixelOffset(R.dimen.offset).toFloat()

            viewPager.setPageTransformer { page, position ->
                val myOffset = position * -(2 * pageOffset + pageMargin)
                if (position < -1) {
                    page.translationX = -myOffset
                } else if (position <= 1) {
                    val scaleFactor = max(0.7f, 1 - abs(position - 0.14285715f))
                    page.translationX = myOffset
                    page.scaleY = scaleFactor
                    page.alpha = scaleFactor
                } else {
                    page.alpha = 0f
                    page.translationX = myOffset
                }
            }

            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    val currentItem = onboardingItems[position]
                    tvTitle.text = currentItem.title
                    tvDescription.text = currentItem.description
                }
            })

            // Initial setup
            val initialItem = onboardingItems[0]
            tvTitle.text = initialItem.title
            tvDescription.text = initialItem.description
        }
    }

    private fun setupStartButton() {
        binding.btnHome.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}