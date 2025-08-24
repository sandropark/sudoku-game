package com.sandro.new_sudoku.ui

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.sandro.new_sudoku.BuildConfig

class InterstitialAdManager(private val context: Context) {

    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false

    companion object {
        private const val AD_UNIT_ID = BuildConfig.AD_INTERSTITIAL_UNIT_ID // 테스트/실제 환경에 따라 자동 설정
    }

    /**
     * 전면 광고 로딩
     */
    fun loadAd() {
        println("🔄 InterstitialAd loadAd() 호출됨")
        if (isLoading || interstitialAd != null) {
            println("⚠️ loadAd() 중단: isLoading=$isLoading, interstitialAd=${if (interstitialAd != null) "존재" else "null"}")
            return // 이미 로딩 중이거나 광고가 준비된 상태
        }

        isLoading = true
        val adRequest = AdRequest.Builder().build()
        println("📝 광고 요청 생성, AD_UNIT_ID: $AD_UNIT_ID")

        InterstitialAd.load(
            context,
            AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoading = false
                    println("✅ InterstitialAd 로딩 성공")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                    isLoading = false
                    println("❌ InterstitialAd 로딩 실패: ${loadAdError.message}")
                    println("❌ 오류 코드: ${loadAdError.code}")
                    println("❌ 오류 도메인: ${loadAdError.domain}")
                }
            }
        )
    }

    /**
     * 전면 광고 표시
     * @param activity 광고를 표시할 Activity
     * @param onAdClosed 광고가 닫힌 후 실행될 콜백
     */
    fun showAd(activity: Activity, onAdClosed: () -> Unit = {}) {
        println("🎬 showAd() 호출됨")
        val ad = interstitialAd

        if (ad != null) {
            println("✅ 광고 준비됨 - 표시 시작")
            // 전면 광고 표시 콜백 설정
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdClicked() {
                    // 광고 클릭 시
                    println("👆 InterstitialAd 클릭됨")
                }

                override fun onAdDismissedFullScreenContent() {
                    // 광고가 닫힘 (사용자가 X 버튼 클릭 또는 뒤로가기)
                    println("🔚 InterstitialAd 닫힘")
                    interstitialAd = null
                    onAdClosed() // 콜백 실행

                    // 다음 광고 미리 로딩
                    loadAd()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    // 광고 표시 실패
                    println("❌ InterstitialAd 표시 실패: ${adError.message}")
                    println("❌ 표시 실패 코드: ${adError.code}")
                    interstitialAd = null
                    onAdClosed() // 실패해도 콜백 실행 (게임 진행)
                    
                    // 실패 후 다시 로드 시도
                    loadAd()
                }

                override fun onAdImpression() {
                    // 광고가 사용자에게 노출됨
                    println("👁️ InterstitialAd 노출")
                }

                override fun onAdShowedFullScreenContent() {
                    // 광고가 전체 화면으로 표시됨
                    println("📺 InterstitialAd 전체 화면 표시")
                }
            }

            // 광고 표시
            ad.show(activity)
        } else {
            // 광고가 준비되지 않은 경우 즉시 콜백 실행 (대기하지 않음)
            println("⚠️ 광고가 준비되지 않음 - 즉시 진행")
            onAdClosed()
            
            // 다음 번을 위해 광고 로드 시작
            if (!isLoading) {
                loadAd()
            }
        }
    }

    /**
     * 광고가 준비되었는지 확인
     */
    fun isReady(): Boolean {
        return interstitialAd != null
    }

    /**
     * 현재 로딩 중인지 확인
     */
    fun isCurrentlyLoading(): Boolean {
        return isLoading
    }

    /**
     * 광고 해제 (메모리 정리)
     */
    fun destroy() {
        interstitialAd = null
        isLoading = false
    }
}