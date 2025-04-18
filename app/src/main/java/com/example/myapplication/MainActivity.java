package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

public class MainActivity extends AppCompatActivity {

    private EditText urlEditText;
    private WebView webView;
    private Button backButton, goButton;

    private InterstitialAd mInterstitialAd;
    private RewardedAd mRewardedAd;

    private static final String TAG = "MainActivity";
    private String finalUrl = "";
    private String pendingUrl = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        _init();
    }

    void _init() {
        urlEditText = findViewById(R.id.editTextUrl);
        webView = findViewById(R.id.webView);
        backButton = findViewById(R.id.backButton);
        goButton = findViewById(R.id.goButton);

        urlEditText.setText("https://curatedbypamela.com");

        MobileAds.initialize(this, initializationStatus -> {});
        loadRewardedAd(); // Load sẵn rewarded ad

        _initWebView();
        _onClick();
    }

    void _onClick() {
        goButton.setOnClickListener(v -> {
            String enteredUrl = urlEditText.getText().toString().trim();
            finalUrl = (enteredUrl.startsWith("http://") || enteredUrl.startsWith("https://"))
                    ? enteredUrl : "https://" + enteredUrl;
            webView.loadUrl(finalUrl);
        });

        backButton.setOnClickListener(v -> {
            setResult(Activity.RESULT_OK);
            finish();
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    void _initWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setSupportMultipleWindows(true);

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d(TAG, "Page loaded: " + url);

                // Khi trang chính (từ GO button) load xong thì hiển thị interstitial ad
                if (url.equals(finalUrl)) {
                    loadInterstitialAd(); // Load và show sau khi load xong
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d(TAG, "User clicked: " + url);

                if (url.equals(finalUrl)) {
                    // Nếu là link gốc đã load rồi thì không cần show rewarded
                    return false;
                }

                // Link khác → show rewarded ad
                if (mRewardedAd != null) {
                    pendingUrl = url;

                    mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            Log.d(TAG, "Rewarded Ad dismissed.");
                            mRewardedAd = null;
                            loadRewardedAd(); // Load ad tiếp theo

                            if (pendingUrl != null) {
                                webView.loadUrl(pendingUrl);
                                pendingUrl = null;
                            }
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(com.google.android.gms.ads.AdError adError) {
                            Log.d(TAG, "Rewarded Ad failed to show.");
                            webView.loadUrl(url);
                        }
                    });

                    mRewardedAd.show(MainActivity.this, rewardItem -> {
                        Log.d(TAG, "User earned reward.");
                    });

                    return true; // Ngăn WebView load link ngay
                }

                return false; // Không có ad → vẫn load link bình thường
            }
        });
    }

    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this,
                "ca-app-pub-7104190631117613/8953560665",
                adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(InterstitialAd ad) {
                        mInterstitialAd = ad;
                        Log.d(TAG, "Interstitial ad loaded.");

                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                Log.d(TAG, "Interstitial Ad dismissed.");
                                mInterstitialAd = null;
                            }
                        });

                        mInterstitialAd.show(MainActivity.this);
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError adError) {
                        mInterstitialAd = null;
                        Log.d(TAG, "Interstitial ad failed: " + adError.getMessage());
                    }
                });
    }

    private void loadRewardedAd() {
        AdRequest adRequest = new AdRequest.Builder().build();

        RewardedAd.load(this,
                "ca-app-pub-7104190631117613/4481463610",
                adRequest,
                new RewardedAdLoadCallback() {
                    @Override
                    public void onAdLoaded(RewardedAd ad) {
                        mRewardedAd = ad;
                        Log.d(TAG, "Rewarded ad loaded.");
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError adError) {
                        mRewardedAd = null;
                        Log.d(TAG, "Rewarded ad failed: " + adError.getMessage());
                    }
                });
    }
}