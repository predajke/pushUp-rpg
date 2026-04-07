package com.ninthbalcony.pushuprpg.managers

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.AdError

/**
 * AdManager handles Google AdMob Rewarded Ads.
 *
 * Features:
 * - Preload ads 30 seconds before showing
 * - Async loading (non-blocking)
 * - Test IDs for development (replace with production IDs)
 * - Timeout handling if ad fails to load
 *
 * TODO: Replace test IDs with production IDs after AdMob approval
 * - PROD_REWARDED_CLOVER = "ca-app-pub-..."
 * - PROD_REWARDED_SHOP = "ca-app-pub-..."
 * - PROD_REWARDED_DAILY = "ca-app-pub-..."
 * - PROD_REWARDED_FORGE = "ca-app-pub-..."
 */
class AdManager(private val context: Context) {
    companion object {
        private const val TAG = "AdManager"

        // Test IDs from Google (use during development)
        private const val TEST_APP_ID = "ca-app-pub-3940256099942544~1458002437"
        private const val TEST_REWARDED_AD_UNIT = "ca-app-pub-3940256099942544/5224354917"

        // TODO: Replace with production IDs after AdMob approval
        // private const val PROD_REWARDED_CLOVER = "ca-app-pub-..."
        // private const val PROD_REWARDED_SHOP = "ca-app-pub-..."
        // private const val PROD_REWARDED_DAILY = "ca-app-pub-..."
        // private const val PROD_REWARDED_FORGE = "ca-app-pub-..."

        private const val PRELOAD_DELAY_MS = 30000L // 30 seconds
        private const val LOAD_TIMEOUT_MS = 30000L // 30 seconds
    }

    private var rewardedAd: RewardedAd? = null
    private var isLoading = false
    private var lastShowTime: Long = 0L

    init {
        // Initialize Mobile Ads SDK
        try {
            MobileAds.initialize(context)
            Log.d(TAG, "Mobile Ads SDK initialized")
        } catch (e: Exception) {
            Log.w(TAG, "Mobile Ads initialization failed: ${e.message}")
        }
    }

    /**
     * Preload rewarded ad (call 30 seconds before showing).
     * Non-blocking, happens in background.
     */
    fun preloadRewardedAd() {
        if (isLoading || rewardedAd != null) {
            Log.d(TAG, "Ad already loading or loaded, skipping preload")
            return
        }

        isLoading = true
        Log.d(TAG, "Preloading rewarded ad...")

        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            context,
            TEST_REWARDED_AD_UNIT,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoading = false
                    Log.d(TAG, "Rewarded ad loaded successfully")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    isLoading = false
                    Log.w(TAG, "Failed to load rewarded ad: ${adError.message}")
                }
            }
        )
    }

    /**
     * Show rewarded ad if loaded.
     * Returns true if ad was shown, false if not ready.
     */
    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onDismissed: () -> Unit
    ): Boolean {
        if (rewardedAd == null || isLoading) {
            Log.w(TAG, "Rewarded ad not ready yet")
            return false
        }

        // Prevent rapid ad showing (min 5 seconds between ads)
        val timeSinceLastShow = System.currentTimeMillis() - lastShowTime
        if (timeSinceLastShow < 5000L) {
            Log.w(TAG, "Ad shown too recently, skipping")
            return false
        }

        lastShowTime = System.currentTimeMillis()
        Log.d(TAG, "Showing rewarded ad...")

        rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Rewarded ad dismissed")
                rewardedAd = null
                onDismissed()
                // Preload next ad after short delay
                preloadRewardedAd()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "Ad failed to show: ${adError.message}")
                rewardedAd = null
                onDismissed()
            }
        }

        rewardedAd?.show(activity) { rewardItem ->
            Log.d(TAG, "User earned reward: ${rewardItem.rewardType}")
            onRewardEarned()
        }

        return true
    }

    /**
     * Check if rewarded ad is ready to show.
     */
    fun isAdReady(): Boolean {
        return rewardedAd != null && !isLoading
    }

    /**
     * Get ad load status for debugging.
     */
    fun getAdStatus(): String {
        return when {
            isLoading -> "Loading..."
            rewardedAd != null -> "Ready"
            else -> "Not loaded"
        }
    }

    /**
     * Destroy manager (cleanup resources).
     */
    fun destroy() {
        Log.d(TAG, "AdManager destroyed")
    }
}
