package com.zeoflow.flowly.kotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Toast
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

const val AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

class MainActivity : AppCompatActivity() {

    private var TAG = "MainActivity"
    private var mInterstitialAd: InterstitialAd? = null
    private var mAdIsLoading: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Log the Mobile Ads SDK version.
        Log.d(TAG, "Google Mobile Ads SDK Version: " + MobileAds.getVersion())

        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this) {}

        loadAd()
    }

    private fun loadAd() {
        var adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            this,
            AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, adError.message)
                    mInterstitialAd = null
                    mAdIsLoading = false
                    val error =
                        "domain: ${adError.domain}, code: ${adError.code}, " + "message: ${adError.message}"
                    Toast.makeText(
                        this@MainActivity,
                        "onAdFailedToLoad() with error $error",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d(TAG, "Ad was loaded.")
                    mInterstitialAd = interstitialAd
                    mAdIsLoading = false
                    Toast.makeText(this@MainActivity, "onAdLoaded()", Toast.LENGTH_SHORT).show()
                    showInterstitial()
                }
            }
        )
    }

    // Show the ad if it's ready. Otherwise toast and restart the game.
    private fun showInterstitial() {
        if (mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        Log.d(TAG, "Ad was dismissed.")
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        mInterstitialAd = null
                        loadAd()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        Log.d(TAG, "Ad failed to show.")
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        mInterstitialAd = null
                    }

                    override fun onAdShowedFullScreenContent() {
                        Log.d(TAG, "Ad showed fullscreen content.")
                        // Called when ad is dismissed.
                    }
                }
            mInterstitialAd?.show(this)
        } else {
            Toast.makeText(this, "Ad wasn't loaded.", Toast.LENGTH_SHORT).show()
        }
    }
}