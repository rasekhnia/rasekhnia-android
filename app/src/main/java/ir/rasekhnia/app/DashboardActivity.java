package ir.rasekhnia.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Native home screen. Account role is enforced on the website, not in this activity. */
public class DashboardActivity extends Activity {
    private final int navy = Color.rgb(16, 31, 52);
    private final int tile = Color.rgb(30, 57, 81);
    private final int cyan = Color.rgb(105, 219, 218);
    private int dp(float n) { return (int)(getResources().getDisplayMetrics().density*n+.5f); }
    private GradientDrawable background(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color); d.setCornerRadius(dp(radius)); return d;
    }
    private TextView label(String text, int size, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(text); t.setTextSize(size);
        t.setTextColor(color); t.setGravity(Gravity.CENTER); t.setTextDirection(View.TEXT_DIRECTION_RTL);
        if (bold) t.setTypeface(null, Typeface.BOLD);
        return t;
    }
    private void open(String url) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("target_url", url);
        startActivity(intent);
    }
    private void tile(LinearLayout parent, String title, String subtitle, String url) {
        LinearLayout box = new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER); box.setPadding(dp(14),dp(16),dp(14),dp(16));
        box.setBackground(background(tile, 16));
        TextView a=label(title,19,Color.WHITE,true), b=label(subtitle,13,Color.rgb(190,211,223),false);
        box.addView(a); LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,-2);bp.topMargin=dp(5);
        box.addView(b,bp);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(94)); lp.bottomMargin=dp(10);
        parent.addView(box,lp); box.setOnClickListener(v->open(url));
    }
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(navy);getWindow().setNavigationBarColor(navy);
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true);scroll.setBackgroundColor(navy);
        LinearLayout main = new LinearLayout(this); main.setOrientation(LinearLayout.VERTICAL);
        main.setPadding(dp(20),dp(30),dp(20),dp(20));
        main.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        TextView brand=label("⚖  راسخ‌نیا",29,Color.WHITE,true);
        main.addView(brand);
        TextView sub=label("سامانه هوشمند خدمات حقوقی",15,cyan,false);
        LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,-2);sp.bottomMargin=dp(22);main.addView(sub,sp);
        TextView welcome=label("خدمات حقوقی، آموزش و مدیریت پرونده‌ها",16,Color.WHITE,false);
        welcome.setPadding(dp(12),dp(20),dp(12),dp(20));welcome.setBackground(background(tile,14));
        LinearLayout.LayoutParams wp=new LinearLayout.LayoutParams(-1,-2);wp.bottomMargin=dp(18);main.addView(welcome,wp);
        tile(main,"مراجعین و مشتریان","سفارش‌ها، قراردادها و پرونده‌ها","https://rasekhnia.ir/my-account/");
        tile(main,"وکلا","ورود به حساب و خدمات وکالت","https://rasekhnia.ir/my-account/");
        tile(main,"بازاریابان","ورود به حساب همکاری و فروش","https://rasekhnia.ir/my-account/");
        tile(main,"خدمات حقوقی","قراردادها، دادخواست‌ها و خدمات","https://rasekhnia.ir/shop/");
        tile(main,"مقالات حقوقی","مطالعه و آموزش","https://rasekhnia.ir/blog/");
        tile(main,"سایر امکانات","تلویزیون حقوقی، کلاس‌ها و سایت","https://rasekhnia.ir/");
        TextView note=label("پنل هر نقش پس از احراز هویت و تأیید دسترسی در سایت قابل استفاده است.",12,Color.rgb(180,195,210),false);
        note.setPadding(0,dp(8),0,dp(16));main.addView(note);
        scroll.addView(main);setContentView(scroll);
    }
}
