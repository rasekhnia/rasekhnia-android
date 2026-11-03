package ir.rasekhnia.app;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String HOME = "https://rasekhnia.ir/";
    private static final int FILE_REQUEST = 1001;
    private WebView webView;
    private ProgressBar progress;
    private LinearLayout errorPanel;
    private ValueCallback<Uri[]> fileCallback;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(10, 80, 105));
        getWindow().setNavigationBarColor(Color.rgb(10, 80, 105));
        FrameLayout root = new FrameLayout(this);
        webView = new WebView(this);
        root.addView(webView, new FrameLayout.LayoutParams(-1, -1));

        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        progress.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.rgb(0, 168, 180)));
        FrameLayout.LayoutParams progressParams = new FrameLayout.LayoutParams(-1, dp(3), Gravity.TOP);
        root.addView(progress, progressParams);

        errorPanel = new LinearLayout(this);
        errorPanel.setOrientation(LinearLayout.VERTICAL);
        errorPanel.setGravity(Gravity.CENTER);
        errorPanel.setPadding(dp(24), dp(24), dp(24), dp(24));
        errorPanel.setBackgroundColor(Color.WHITE);
        TextView message = new TextView(this);
        message.setText("اتصال به راسخ‌نیا برقرار نشد.\nاینترنت خود را بررسی کنید.");
        message.setTextSize(19);
        message.setGravity(Gravity.CENTER);
        message.setTextColor(Color.rgb(25, 70, 85));
        errorPanel.addView(message);
        TextView retry = new TextView(this);
        retry.setText("تلاش مجدد");
        retry.setTextSize(18);
        retry.setTextColor(Color.WHITE);
        retry.setGravity(Gravity.CENTER);
        retry.setPadding(dp(28), dp(14), dp(28), dp(14));
        retry.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(0, 145, 160)));
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(Color.rgb(0, 145, 160)); bg.setCornerRadius(dp(12));
        retry.setBackground(bg);
        LinearLayout.LayoutParams retryParams = new LinearLayout.LayoutParams(-2, -2);
        retryParams.topMargin = dp(24);
        errorPanel.addView(retry, retryParams);
        retry.setOnClickListener(v -> { errorPanel.setVisibility(View.GONE); webView.reload(); });
        errorPanel.setVisibility(View.GONE);
        root.addView(errorPanel, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setSupportZoom(true);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = callback;
                try {
                    Intent intent = params.createIntent();
                    startActivityForResult(intent, FILE_REQUEST);
                    return true;
                } catch (Exception ex) {
                    fileCallback = null;
                    Toast.makeText(MainActivity.this, "انتخاب فایل در دسترس نیست", Toast.LENGTH_LONG).show();
                    return false;
                }
            }
            @Override public void onProgressChanged(WebView view, int newProgress) {
                progress.setProgress(newProgress);
                progress.setVisibility(newProgress >= 100 ? View.GONE : View.VISIBLE);
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (!request.isForMainFrame()) return false;
                Uri uri = request.getUrl();
                String host = uri.getHost();
                String scheme = uri.getScheme();
                if ("https".equalsIgnoreCase(scheme) && host != null &&
                    (host.equalsIgnoreCase("rasekhnia.ir") || host.equalsIgnoreCase("www.rasekhnia.ir"))) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); }
                catch (Exception ex) { Toast.makeText(MainActivity.this, "امکان بازکردن این پیوند وجود ندارد", Toast.LENGTH_SHORT).show(); }
                return true;
            }
            @Override public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                errorPanel.setVisibility(View.GONE);
            }
            @Override public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) errorPanel.setVisibility(View.VISIBLE);
            }
            @Override public void onPageFinished(WebView view, String url) {
                CookieManager.getInstance().flush();
            }
        });
        webView.setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) -> {
            try {
                Uri uri = Uri.parse(url);
                String host = uri.getHost();
                if (!"https".equalsIgnoreCase(uri.getScheme()) || host == null) throw new IllegalArgumentException("Unsupported download URL");
                DownloadManager.Request req = new DownloadManager.Request(uri);
                req.setMimeType(mimeType);
                req.addRequestHeader("User-Agent", userAgent == null ? "" : userAgent);
                String cookies = CookieManager.getInstance().getCookie(url);
                if (cookies != null) req.addRequestHeader("Cookie", cookies);
                String filename = android.webkit.URLUtil.guessFileName(url, contentDisposition, mimeType);
                req.setTitle(filename);
                req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
                ((DownloadManager) getSystemService(DOWNLOAD_SERVICE)).enqueue(req);
                Toast.makeText(this, "دانلود آغاز شد", Toast.LENGTH_SHORT).show();
            } catch (Exception ex) {
                Toast.makeText(this, "دانلود مستقیم ممکن نشد؛ پیوند را در مرورگر باز کنید", Toast.LENGTH_LONG).show();
            }
        });
        if (savedInstanceState == null) webView.loadUrl(getIntent().getStringExtra("target_url") != null ? getIntent().getStringExtra("target_url") : HOME);
        else if (webView.restoreState(savedInstanceState) == null) webView.loadUrl(getIntent().getStringExtra("target_url") != null ? getIntent().getStringExtra("target_url") : HOME);
    }

    private int dp(int value) { return (int)(getResources().getDisplayMetrics().density * value + .5f); }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_REQUEST && fileCallback != null) {
            Uri[] result = WebChromeClient.FileChooserParams.parseResult(resultCode, data);
            fileCallback.onReceiveValue(result);
            fileCallback = null;
        }
    }
    @Override public void onBackPressed() {
        if (errorPanel.getVisibility() == View.VISIBLE) { errorPanel.setVisibility(View.GONE); webView.loadUrl(getIntent().getStringExtra("target_url") != null ? getIntent().getStringExtra("target_url") : HOME); }
        else if (webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }
    @Override protected void onSaveInstanceState(Bundle outState) {
        webView.saveState(outState); super.onSaveInstanceState(outState);
    }
    @Override protected void onDestroy() {
        if (fileCallback != null) { fileCallback.onReceiveValue(null); fileCallback = null; }
        if (webView != null) { ((ViewGroup)webView.getParent()).removeView(webView); webView.destroy(); }
        super.onDestroy();
    }
}
