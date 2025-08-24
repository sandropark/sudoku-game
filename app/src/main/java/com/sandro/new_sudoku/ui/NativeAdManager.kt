package com.sandro.new_sudoku.ui

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.sandro.new_sudoku.BuildConfig

class NativeAdManager(private val context: Context) {

    private var nativeAd: NativeAd? = null
    private var isLoading = false

    companion object {
        private const val AD_UNIT_ID = BuildConfig.AD_NATIVE_UNIT_ID // 테스트/실제 환경에 따라 자동 설정
    }

    /**
     * 네이티브 광고 로딩
     */
    fun loadAd(onAdLoaded: (NativeAd) -> Unit, onAdFailedToLoad: (LoadAdError) -> Unit) {
        println("🔄 NativeAd loadAd() 호출됨")
        if (isLoading || nativeAd != null) {
            println("⚠️ loadAd() 중단: isLoading=$isLoading, nativeAd=${if (nativeAd != null) "존재" else "null"}")
            return // 이미 로딩 중이거나 광고가 준비된 상태
        }

        isLoading = true
        val adRequest = AdRequest.Builder().build()
        println("📝 네이티브 광고 요청 생성, AD_UNIT_ID: $AD_UNIT_ID")

        val builder = AdLoader.Builder(context, AD_UNIT_ID)

        builder.forNativeAd { ad: NativeAd ->
            println("✅ NativeAd 로딩 성공")
            // If this callback occurs after the activity is destroyed, you must call
            // destroy and return or you may get a memory leak.
            // Note `isDestroyed()` is a method on Activity.
            if (context is Activity && context.isDestroyed) {
                ad.destroy()
                return@forNativeAd
            }

            // You must call destroy on old ads when you are done with them,
            // otherwise you will have a memory leak.
            nativeAd?.destroy()
            nativeAd = ad
            isLoading = false
            onAdLoaded(ad)
        }

        val adLoader = builder.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                println("❌ NativeAd 로딩 실패: ${adError.message}")
                println("❌ 오류 코드: ${adError.code}")
                println("❌ 오류 도메인: ${adError.domain}")
                isLoading = false
                onAdFailedToLoad(adError)
            }
        }).withNativeAdOptions(
            NativeAdOptions.Builder()
                // Methods in the NativeAdOptions.Builder class can be
                // used here to specify individual options settings.
                .build()
        ).build()

        adLoader.loadAd(adRequest)
    }

    /**
     * 광고가 준비되었는지 확인
     */
    fun isReady(): Boolean {
        return nativeAd != null
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
        nativeAd?.destroy()
        nativeAd = null
        isLoading = false
    }
}

/**
 * 힌트 사용을 위한 네이티브 광고 다이얼로그
 */
@Composable
fun HintNativeAdDialog(
    onHintUnlocked: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }
    var adLoadingState by remember { mutableStateOf<AdLoadingState>(AdLoadingState.Loading) }
    val nativeAdManager = remember { NativeAdManager(context) }

    // 광고 로딩
    LaunchedEffect(Unit) {
        nativeAdManager.loadAd(
            onAdLoaded = { ad ->
                nativeAd = ad
                adLoadingState = AdLoadingState.Loaded
            },
            onAdFailedToLoad = { error ->
                adLoadingState = AdLoadingState.Failed(error.message)
            }
        )
    }

    // 컴포넌트가 제거될 때 광고 정리
    DisposableEffect(Unit) {
        onDispose {
            nativeAdManager.destroy()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 제목
                Text(
                    text = "💡 힌트 보기",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                // 설명
                Text(
                    text = "광고를 시청하시면 선택한 셀의 정답을 확인할 수 있습니다.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                // 광고 영역
                when (adLoadingState) {
                    is AdLoadingState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("광고 로딩 중...", color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    is AdLoadingState.Loaded -> {
                        nativeAd?.let { ad ->
                            AndroidView(
                                factory = { ctx ->
                                    NativeAdView(ctx).apply {
                                        // 네이티브 광고 뷰 설정 (간단한 버전)
                                        setNativeAd(ad)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            )
                        }
                    }

                    is AdLoadingState.Failed -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .background(
                                    MaterialTheme.colorScheme.errorContainer,
                                    RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "광고 로딩에 실패했습니다.\n무료로 힌트를 제공합니다.",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                // 버튼들
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("취소")
                    }

                    Button(
                        onClick = {
                            onHintUnlocked()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = adLoadingState is AdLoadingState.Loaded || adLoadingState is AdLoadingState.Failed
                    ) {
                        Text(
                            text = when (adLoadingState) {
                                is AdLoadingState.Loading -> "로딩 중..."
                                is AdLoadingState.Loaded -> "힌트 보기"
                                is AdLoadingState.Failed -> "힌트 보기"
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 광고 로딩 상태
 */
sealed class AdLoadingState {
    object Loading : AdLoadingState()
    object Loaded : AdLoadingState()
    data class Failed(val message: String) : AdLoadingState()
}