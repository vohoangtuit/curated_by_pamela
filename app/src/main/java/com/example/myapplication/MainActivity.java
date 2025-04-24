package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
public class MainActivity extends AppCompatActivity {

    private EditText urlEditText;
    private WebView webView;
    private Button backButton, goButton;
    private AdView adView;


    private InterstitialAd mInterstitialAd;
    private RewardedAd mRewardedAd;

    private String finalUrl = "";
    private String pendingUrl = null;
    private static final String TAG = "MainActivity";

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
        adView = findViewById(R.id.adView);
        urlEditText.setText("https://curatedbypamela.com");

        // Khởi tạo Google Mobile Ads
        MobileAds.initialize(this, initializationStatus -> {});

        // Load ads trước
        loadInterstitialAd();
        loadRewardedAd();
        _loadBannerAd();
        _initWebView();
        _onClick();
    }

    void _onClick() {
        goButton.setOnClickListener(v -> {
            String enteredUrl = urlEditText.getText().toString().trim();
            finalUrl = (enteredUrl.startsWith("http://") || enteredUrl.startsWith("https://"))
                    ? enteredUrl : "https://" + enteredUrl;
            webView.loadUrl(finalUrl);

            // Load lại Interstitial cho lần sau
         //   loadInterstitialAd();
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
                super.onPageFinished(view, url);
                Log.d(TAG, "Page finished: " + url);

                if (mInterstitialAd != null) {
                    mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            Log.d(TAG, "Ad dismissed.");
                            mInterstitialAd = null;
                            loadInterstitialAd(); // Load ad mới
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(com.google.android.gms.ads.AdError adError) {
                            Log.d(TAG, "Ad failed to show: " + adError.getMessage());
                        }
                    });

                    mInterstitialAd.show(MainActivity.this);
                } else {
                    Log.d(TAG, "Interstitial ad not ready after page load.");
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d(TAG, "User clicked: " + url);

                if (url.equals(finalUrl)) return false;

                if (mRewardedAd != null) {
                    pendingUrl = url;

                    mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            Log.d(TAG, "Rewarded Ad dismissed.");
                            mRewardedAd = null;
                            loadRewardedAd();

                            if (pendingUrl != null) {
                                webView.loadUrl(pendingUrl);
                                pendingUrl = null;
                            }
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(com.google.android.gms.ads.AdError adError) {
                            Log.d(TAG, "Rewarded Ad failed to show.");
                            webView.loadUrl(url); // Load trực tiếp nếu lỗi
                        }
                    });

                    mRewardedAd.show(MainActivity.this, rewardItem -> {
                        Log.d(TAG, "User earned reward.");
                    });

                    return true; // Ngăn load trực tiếp
                } else {
                    loadRewardedAd(); // Load lại nếu chưa có
                    return false;
                }
            }
        });
    }

    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this,
                "ca-app-pub-7104190631117613/8953560665", // Test ID
                adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(InterstitialAd ad) {
                        mInterstitialAd = ad;
                        Log.d(TAG, "Interstitial ad loaded.");
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError adError) {
                        mInterstitialAd = null;
                        Log.d(TAG, "Failed to load interstitial ad: " + adError.getMessage());
                    }
                });
    }

    private void loadRewardedAd() {
        AdRequest adRequest = new AdRequest.Builder().build();

        RewardedAd.load(this,
                "ca-app-pub-7104190631117613/9836866103", // Test ID
                adRequest,
                new RewardedAdLoadCallback() {
                    @Override
                    public void onAdLoaded(RewardedAd ad) {
                        mRewardedAd = ad;
                        Log.d(TAG, "Rewarded ad loaded.");
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError adError) {
                        Log.d(TAG, "Failed to load rewarded ad: " + adError.getMessage());
                        mRewardedAd = null;
                    }
                });
    }

    private void _loadBannerAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }
}