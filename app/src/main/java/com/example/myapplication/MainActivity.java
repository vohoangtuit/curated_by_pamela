package com.example.myapplication;

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

public class MainActivity extends AppCompatActivity {

    private EditText urlEditText;
    private WebView webView;
    private Button backButton, goButton;

    private InterstitialAd mInterstitialAd;
    private static final String TAG = "MainActivity";
    private String finalUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Init AdMob

    }

    @Override
    protected void onStart() {
        super.onStart();
        _init();

    }
    void _init(){
        urlEditText = findViewById(R.id.editTextUrl);
        webView = findViewById(R.id.webView);
        backButton = findViewById(R.id.backButton);
        goButton = findViewById(R.id.goButton);

        urlEditText.setText("https://curatedbypamela.com");
        MobileAds.initialize(this, initializationStatus -> {
        });
        loadInterstitialAd();
        _initWebView();
        _onClick();
    }
    void _onClick(){
        goButton.setOnClickListener(v -> {
            String enteredUrl = urlEditText.getText().toString().trim();
            finalUrl = (enteredUrl.startsWith("http://") || enteredUrl.startsWith("https://"))
                    ? enteredUrl : "https://" + enteredUrl;
            webView.loadUrl(finalUrl); // ← Load trước

        });
        backButton.setOnClickListener(v -> {
            setResult(Activity.RESULT_OK);
            finish();
        });
    }
    void _initWebView(){
        // WebView setup
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setSupportMultipleWindows(true);
        webView.setWebViewClient(new WebViewClient());

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d(TAG, "WebView finished loading: " + url);

                // Show ad **sau khi WebView load xong**
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
        });
    }

    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
// ca-app-pub-7104190631117613/5458637393
        // todo test: ca-app-pub-7104190631117613/8953560665
        InterstitialAd.load(this,
                "ca-app-pub-7104190631117613/8953560665", // ✅ Interstitial Ad Unit ID
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
}