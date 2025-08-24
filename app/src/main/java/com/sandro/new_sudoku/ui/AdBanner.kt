package com.sandro.new_sudoku.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.sandro.new_sudoku.BuildConfig

@Composable
fun AdBanner(
    modifier: Modifier = Modifier
) {
    LocalContext.current
    var adView by remember { mutableStateOf<AdView?>(null) }

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp), // 배너 광고 표준 높이
        factory = { ctx ->
            AdView(ctx).apply {
                adUnitId = BuildConfig.AD_BANNER_UNIT_ID // 테스트/실제 환경에 따라 자동 설정

                setAdSize(AdSize.BANNER)

                adView = this
            }
        },
        update = { view ->
            val adRequest = AdRequest.Builder().build()
            view.loadAd(adRequest)
        }
    )

    // 컴포넌트가 제거될 때 광고 정리 (메모리 누수 방지)
    DisposableEffect(Unit) {
        onDispose {
            adView?.destroy()
        }
    }
}