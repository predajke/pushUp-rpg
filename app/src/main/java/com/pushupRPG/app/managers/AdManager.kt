package com.pushupRPG.app.managers

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.*

class AdManager(private val context: Context) {
    companion object {
        private const val TAG = "AdManager"
        private const val AD_LOAD_TIMEOUT_MS = 30_000L

        // Google test Ad Unit ID for Rewarded Ads (for development/testing)
        private const val TEST_REWARDED_AD_UNIT = "ca-app-pub-3940256099942544/5224354917"

        // Production Ad Unit IDs (replace after AdMob approval)
        // const val PRODUCTION_CLOVER_BOX = "ca-app-pub-..."
        // const val PRODUCTION_SHOP = "ca-app-pub-..."
        // const val PRODUCTION_DAILY = "ca-app-pub-..."
        // const val PRODUCTION_FORGE = "ca-app-pub-..."
    }

    private var rewardedAd: RewardedAd? = null
    private var isAdLoading = false
    private var lastAdLoadTime = 0L

    fun preloadRewardedAd() {
        if (isAdLoading || rewardedAd != null) {
            Log.d(TAG, "Ad already loaded or loading - skipping preload")
            return
        }

        isAdLoading = true
        lastAdLoadTime = System.currentTimeMillis()

        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            context,
            TEST_REWARDED_AD_UNIT,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.w(TAG, "Failed to load rewarded ad: ${adError.message}")
                    isAdLoading = false
                    rewardedAd = null
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Rewarded ad loaded successfully")
                    isAdLoading = false
                    rewardedAd = ad
                }
            }
        )
    }

    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onAdDismissed: () -> Unit
    ) {
        // If no ad is preloaded, start loading
        if (rewardedAd == null && !isAdLoading) {
            Log.d(TAG, "No preloaded ad, starting load...")
            isAdLoading = true

            val adRequest = AdRequest.Builder().build()

            RewardedAd.load(
                activity,
                TEST_REWARDED_AD_UNIT,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.w(TAG, "Failed to load ad for display: ${adError.message}")
                        isAdLoading = false
                        onAdDismissed()
                    }

                    override fun onAdLoaded(ad: RewardedAd) {
                        Log.d(TAG, "Ad loaded, now showing...")
                        isAdLoading = false
                        displayAd(activity, ad, onRewardEarned, onAdDismissed)
                    }
                }
            )
        } else if (rewardedAd != null) {
            // Ad is already preloaded, show immediately
            Log.d(TAG, "Showing preloaded ad")
            displayAd(activity, rewardedAd!!, onRewardEarned, onAdDismissed)
            rewardedAd = null
        } else {
            Log.d(TAG, "Ad is loading, waiting...")
            onAdDismissed()
        }
    }

    private fun displayAd(
        activity: Activity,
        ad: RewardedAd,
        onRewardEarned: () -> Unit,
        onAdDismissed: () -> Unit
    ) {
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {
                Log.d(TAG, "Ad was clicked")
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Ad dismissed")
                rewardedAd = null
                onAdDismissed()
                // Start preloading next ad
                preloadRewardedAd()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.w(TAG, "Ad failed to show: ${adError.message}")
                rewardedAd = null
                onAdDismissed()
            }

            override fun onAdImpression() {
                Log.d(TAG, "Ad impression recorded")
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Ad showed full screen content")
            }
        }

        ad.show(activity) { rewardItem ->
            Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
            onRewardEarned()
        }
    }

    fun isAdReady(): Boolean {
        return rewardedAd != null
    }

    fun destroy() {
        rewardedAd = null
        isAdLoading = false
        Log.d(TAG, "AdManager destroyed")
    }
}
