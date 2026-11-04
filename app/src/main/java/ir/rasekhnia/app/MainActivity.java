package ir.rasekhnia.app;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.FrameLayout;

/** Authentication gate and authenticated site browser. */
public class MainActivity extends Activity {
    private static final String BASE="https://rasekhnia.ir/";
    private static final String LOGIN=BASE+"my-account/";
    private WebView webView;
    private boolean checking=false, browserMode=false;
    private String target;
    private TextView status;
    private boolean trusted(String url) {
        try { Uri u=Uri.parse(url);String h=u.getHost();return "https".equalsIgnoreCase(u.getScheme()) && ("rasekhnia.ir".equalsIgnoreCase(h)||"www.rasekhnia.ir".equalsIgnoreCase(h)); }catch(Exception e){return false;}
    }
    @Override public void onCreate(Bundle b){super.onCreate(b);
        target=getIntent().getStringExtra("target_url");browserMode=trusted(target);
        FrameLayout frame=new FrameLayout(this);status=new TextView(this);status.setText("در حال بررسی ورود امن…");status.setTextColor(Color.DKGRAY);status.setTextSize(17);status.setGravity(Gravity.CENTER);frame.addView(status,new FrameLayout.LayoutParams(-1,-1));setContentView(frame);
        check();
    }
    private void check(){if(checking)return;checking=true;SessionGate.check(this,(auth,reachable)->{
        checking=false;
        if(auth){if(browserMode)showWeb(target);else{startActivity(new Intent(this,DashboardActivity.class));finish();}}
        else if(!reachable){status.setText("اتصال به سامانه احراز هویت برقرار نشد. برای تلاش دوباره لمس کنید.");status.setOnClickListener(v->check());}
        else showWeb(LOGIN);
    });}
    private void showWeb(String url){
        if(webView!=null){if(!url.equals(webView.getUrl()))webView.loadUrl(url);return;}
        webView=new WebView(this);setContentView(webView);
        WebSettings s=webView.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);
        CookieManager.getInstance().setAcceptCookie(true);CookieManager.getInstance().setAcceptThirdPartyCookies(webView,true);
        webView.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){Uri u=r.getUrl();if(trusted(u.toString()))return false;
                String scheme=u.getScheme();
                if(scheme!=null && (scheme.equalsIgnoreCase("https") || scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("tel") || scheme.equalsIgnoreCase("mailto"))){
                    try{startActivity(new Intent(Intent.ACTION_VIEW,u));}catch(Exception ignored){}
                }
                return true;}
            @Override public void onPageFinished(WebView v,String url){
                CookieManager.getInstance().flush();
                if(!browserMode && !checking){checking=true;SessionGate.check(MainActivity.this,(auth,reachable)->{
                    checking=false;if(auth){startActivity(new Intent(MainActivity.this,DashboardActivity.class));finish();}
                });}
            }
        });
        webView.loadUrl(url);
    }
    @Override public void onBackPressed(){if(webView!=null && webView.canGoBack())webView.goBack();else super.onBackPressed();}
    @Override protected void onDestroy(){if(webView!=null)webView.destroy();super.onDestroy();}
}
