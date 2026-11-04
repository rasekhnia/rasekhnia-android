package ir.rasekhnia.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DashboardActivity extends Activity {
    private static final String HOME="https://rasekhnia.ir/";
    private final Handler handler=new Handler(Looper.getMainLooper());
    private JSONObject config;
    private FrameLayout shell;
    private View drawer;
    private int generation=0;
    private boolean sessionVerified=false;
    private boolean sessionChecking=false;
    private int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    private int color(String k,int fallback){try{return Color.parseColor(config.getString(k));}catch(Exception e){return fallback;}}
    private String val(String k,String fallback){return config==null?fallback:config.optString(k,fallback);}
    private GradientDrawable bg(int c,int r){GradientDrawable d=new GradientDrawable();d.setColor(c);d.setCornerRadius(dp(r));return d;}
    private TextView text(String s,int size,int fg,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextColor(fg);v.setTextSize(size);v.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);v.setTextDirection(View.TEXT_DIRECTION_RTL);if(bold)v.setTypeface(null,Typeface.BOLD);return v;}
    private LinearLayout vertical(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);return l;}
    private LinearLayout horizontal(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
    private TextView button(String s,int size,int fg,int background,int radius){TextView t=text(s,size,fg,true);t.setGravity(Gravity.CENTER);t.setBackground(bg(background,radius));return t;}
    private boolean valid(String u){try{URL p=new URL(u);return "https".equalsIgnoreCase(p.getProtocol())&&(p.getHost().equalsIgnoreCase("rasekhnia.ir")||p.getHost().equalsIgnoreCase("www.rasekhnia.ir"));}catch(Exception e){return false;}}
    private void open(String url){if(!sessionVerified){SessionGate.openLogin(this);return;}Intent i=new Intent(this,MainActivity.class);i.putExtra("target_url",valid(url)?url:HOME);startActivity(i);}
    private String glyph(String k){switch(k){case "scale":return "⚖";case "users":return "♙";case "file-text":return "▤";case "book-open":return "▣";case "graduation-cap":return "✦";case "tv":return "▣";case "headphones":return "☎";case "calendar-days":return "▦";case "shopping-bag":return "▧";case "bell":return "♧";case "handshake":return "♧";case "folder-open":return "▤";case "shield-check":return "✓";default:return "⚖";}}
    private JSONArray items(){JSONArray a=config==null?null:config.optJSONArray("tiles");if(a!=null)return a;JSONArray d=new JSONArray();String[][] seed={{"قراردادساز","تنظیم قرارداد حرفه‌ای","file-text",HOME},{"دادخواست و شکوائیه","تنظیم اوراق قضایی","scale",HOME},{"مطالعه پرونده","بررسی تخصصی اسناد","folder-open",HOME},{"چکاپ حقوقی","ارزیابی کسب‌وکار","shield-check",HOME},{"مقالات حقوقی","آموزش و آگاهی","book-open",HOME},{"حساب کاربری","مدیریت سفارش‌ها","users",HOME+"my-account/"}};for(String[] x:seed){JSONObject o=new JSONObject();try{o.put("title",x[0]);o.put("subtitle",x[1]);o.put("icon",x[2]);o.put("url",x[3]);}catch(Exception ignored){}d.put(o);}return d;}
    private void addSection(LinearLayout body,String s){TextView h=text(s,18,0xFF183449,true);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=dp(22);p.bottomMargin=dp(12);body.addView(h,p);}
    private void render(){if(!sessionVerified)return;
        int navy=color("background",0xFF101F34),cardColor=color("card",0xFF1E3951),accent=color("accent",0xFF69DBDA);
        int radius=Math.max(8,Math.min(28,config==null?16:config.optInt("corner_radius",16)));
        getWindow().setStatusBarColor(navy);getWindow().setNavigationBarColor(navy);
        shell=new FrameLayout(this);shell.setBackgroundColor(0xFFF4F8FA);
        LinearLayout page=vertical();page.setBackgroundColor(0xFFF4F8FA);shell.addView(page,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout header=horizontal();header.setPadding(dp(16),dp(10),dp(16),dp(10));header.setBackgroundColor(navy);
        TextView menu=button("☰",25,Color.WHITE,0xFF24465C,12);header.addView(menu,new LinearLayout.LayoutParams(dp(46),dp(46)));menu.setOnClickListener(v->showDrawer());
        LinearLayout brand=vertical();brand.setPadding(dp(12),0,dp(12),0);brand.addView(text(getString(R.string.app_name),21,Color.WHITE,true));brand.addView(text("سامانه هوشمند خدمات حقوقی",11,accent,false));header.addView(brand,new LinearLayout.LayoutParams(0,-2,1));
        TextView profile=button("♙",23,accent,0xFF24465C,12);header.addView(profile,new LinearLayout.LayoutParams(dp(46),dp(46)));profile.setOnClickListener(v->open(HOME+"my-account/"));page.addView(header);
        ScrollView sc=new ScrollView(this);sc.setFillViewport(true);page.addView(sc,new LinearLayout.LayoutParams(-1,0,1));LinearLayout body=vertical();body.setPadding(dp(16),dp(16),dp(16),dp(22));sc.addView(body);
        LinearLayout hero=vertical();hero.setPadding(dp(18),dp(17),dp(18),dp(17));hero.setBackground(bg(navy,20));hero.addView(text("به سامانه حقوقی خوش آمدید",12,accent,true));
        TextView headline=text(val("welcome","راهکارهای حقوقی، در دسترس شما"),19,Color.WHITE,true);headline.setMaxLines(3);LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,-2);hp.topMargin=dp(7);hero.addView(headline,hp);
        TextView intro=text("از تنظیم قرارداد تا مدیریت پرونده، همه در یک مکان",12,0xFFD0E0E9,false);LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(-1,-2);ip.topMargin=dp(7);hero.addView(intro,ip);
        TextView go=button("مشاهده خدمات   ←",13,navy,accent,12);LinearLayout.LayoutParams gp=new LinearLayout.LayoutParams(-2,dp(40));gp.topMargin=dp(12);hero.addView(go,gp);go.setPadding(dp(18),0,dp(18),0);go.setOnClickListener(v->open(HOME));body.addView(hero);
        TextView search=button("⌕   جست‌وجوی خدمات و مقالات",14,0xFF526D7D,Color.WHITE,14);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,dp(48));sp.topMargin=dp(14);body.addView(search,sp);search.setOnClickListener(v->open(HOME+"?s="));
        addSection(body,"خدمات پرکاربرد");
        // Keep text-bearing cards at most two columns on phones. Three columns only on wider screens.
        String layout=val("layout","grid2");int screenDp=(int)(getResources().getDisplayMetrics().widthPixels/getResources().getDisplayMetrics().density);
        int cols=layout.equals("list")?1:(layout.equals("grid3")&&screenDp>=600?3:2);
        int gap=Math.max(8,Math.min(24,config==null?12:config.optInt("tile_gap",12)));
        JSONArray a=items();LinearLayout row=null;int inRow=0;
        for(int i=0;i<Math.min(a.length(),60);i++){
            JSONObject o=a.optJSONObject(i);if(o==null||!o.optBoolean("enabled",true))continue;
            if(inRow==0){row=horizontal();LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,-2);rp.bottomMargin=dp(gap);body.addView(row,rp);}
            LinearLayout card=vertical();card.setGravity(Gravity.CENTER_VERTICAL);card.setPadding(dp(12),dp(13),dp(12),dp(13));card.setBackground(bg(cardColor,radius));
            if(config==null||config.optBoolean("show_icons",true)){TextView icon=text(glyph(o.optString("icon","scale")),25,accent,true);card.addView(icon);}
            TextView name=text(o.optString("title","خدمات حقوقی"),cols==1?16:14,Color.WHITE,true);name.setMaxLines(2);card.addView(name);
            if(config==null||config.optBoolean("show_descriptions",true)){String description=o.optString("subtitle","");if(!description.isEmpty()){TextView sub=text(description,11,0xFFC1D8E3,false);sub.setMaxLines(2);sub.setEllipsize(android.text.TextUtils.TruncateAt.END);LinearLayout.LayoutParams subp=new LinearLayout.LayoutParams(-1,-2);subp.topMargin=dp(5);card.addView(sub,subp);}}
            String url=o.optString("url",HOME);card.setOnClickListener(v->open(url));
            LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,dp(cols==1?106:128),1);if(inRow>0)cp.rightMargin=dp(gap);row.addView(card,cp);
            inRow++;if(inRow==cols)inRow=0;
        }
        if(inRow!=0&&row!=null){for(int i=inRow;i<cols;i++){View filler=new View(this);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(1),1);p.rightMargin=dp(gap);row.addView(filler,p);}}
        addSection(body,"مجله و آموزش حقوقی");TextView magazine=button("▣   مقالات، دوره‌ها و تلویزیون حقوقی   ←",13,0xFF17546A,0xFFE1F0F2,14);body.addView(magazine,new LinearLayout.LayoutParams(-1,dp(58)));magazine.setOnClickListener(v->open(HOME));
        if(config!=null&&config.optBoolean("show_footer",true)){TextView footer=text(val("footer_text","© سامانه خدمات حقوقی"),11,0xFF78909C,false);footer.setGravity(Gravity.CENTER);LinearLayout.LayoutParams fp=new LinearLayout.LayoutParams(-1,-2);fp.topMargin=dp(18);body.addView(footer,fp);}
        LinearLayout nav=horizontal();nav.setBackgroundColor(Color.WHITE);nav.setPadding(dp(4),dp(7),dp(4),dp(7));String[] labels={"⌂\nخانه","▦\nخدمات","▣\nآموزش","♙\nحساب","☰\nبیشتر"};for(int i=0;i<labels.length;i++){final int idx=i;TextView t=button(labels[i],12,i==0?0xFF087F91:0xFF607D8B,i==0?0xFFE2F4F5:Color.WHITE,12);LinearLayout.LayoutParams np=new LinearLayout.LayoutParams(0,dp(55),1);np.leftMargin=dp(2);np.rightMargin=dp(2);nav.addView(t,np);t.setOnClickListener(v->{if(idx==0){sc.smoothScrollTo(0,0);}else if(idx==1){sc.smoothScrollTo(0,search.getBottom()+dp(10));}else if(idx==2){open(HOME+"?s=%D8%A2%D9%85%D9%88%D8%B2%D8%B4");}else if(idx==3){open(HOME+"my-account/");}else showDrawer();});}page.addView(nav);
        setContentView(shell);drawer=null;
    }
    private void showDrawer(){if(drawer!=null){shell.removeView(drawer);drawer=null;return;}FrameLayout overlay=new FrameLayout(this);overlay.setBackgroundColor(0x77000000);shell.addView(overlay,new FrameLayout.LayoutParams(-1,-1));overlay.setOnClickListener(v->{shell.removeView(overlay);drawer=null;});
        ScrollView scroll=new ScrollView(this);scroll.setBackgroundColor(Color.WHITE);FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(dp(285),-1,Gravity.RIGHT);overlay.addView(scroll,p);scroll.setOnClickListener(v->{});LinearLayout content=vertical();content.setPadding(dp(16),dp(24),dp(16),dp(20));scroll.addView(content);
        TextView close=button("✕   بستن فهرست",15,0xFF15394B,0xFFE7F1F4,12);content.addView(close,new LinearLayout.LayoutParams(-1,dp(48)));close.setOnClickListener(v->{shell.removeView(overlay);drawer=null;});addSection(content,"فهرست خدمات");
        String[][] entries={{"خانه",HOME},{"خدمات حقوقی",HOME},{"مقالات و آموزش",HOME+"?s=%D8%A2%D9%85%D9%88%D8%B2%D8%B4"},{"حساب کاربری",HOME+"my-account/"}};
        for(String[] e:entries){drawerEntry(content,e[0],e[1]);}TextView logout=button("خروج از حساب",15,0xFF9A2020,0xFFFCE8E8,12);content.addView(logout,new LinearLayout.LayoutParams(-1,dp(48)));logout.setOnClickListener(v->open(HOME+"my-account/"));JSONArray a=items();for(int i=0;i<Math.min(a.length(),60);i++){JSONObject o=a.optJSONObject(i);if(o!=null&&o.optBoolean("enabled",true))drawerEntry(content,o.optString("title","خدمات"),o.optString("url",HOME));}drawer=overlay;
    }
    private void drawerEntry(LinearLayout parent,String name,String url){TextView t=text("‹   "+name,15,0xFF1A4054,false);t.setPadding(dp(10),dp(12),dp(10),dp(12));parent.addView(t);t.setOnClickListener(v->open(url));}
    private void refresh(){if(!sessionVerified)return;final int id=++generation;new Thread(()->{HttpURLConnection c=null;try{c=(HttpURLConnection)new URL("https://rasekhnia.ir/wp-json/rki-android/v1/config").openConnection();c.setConnectTimeout(8000);c.setReadTimeout(8000);if(c.getResponseCode()!=200)return;ByteArrayOutputStream b=new ByteArrayOutputStream();try(InputStream in=c.getInputStream()){byte[] buf=new byte[4096];int n;while((n=in.read(buf))!=-1){if(b.size()+n>131072)return;b.write(buf,0,n);}}String raw=new String(b.toByteArray(),StandardCharsets.UTF_8);JSONObject received=new JSONObject(raw);handler.post(()->{if(id==generation&&!isFinishing()){String old=config==null?"":config.toString();if(!old.equals(received.toString())){config=received;getSharedPreferences("dashboard",MODE_PRIVATE).edit().putString("config",raw).apply();render();}}});}catch(Exception ignored){}finally{if(c!=null)c.disconnect();}}).start();}
    @Override public void onCreate(Bundle b){super.onCreate(b);try{config=new JSONObject(getSharedPreferences("dashboard",MODE_PRIVATE).getString("config","{}"));}catch(Exception ignored){}checkSession();}
    private void checkSession(){
        if(sessionChecking)return;
        sessionChecking=true;
        SessionGate.check(this,(authenticated,reachable)->{
            sessionChecking=false;
            if(authenticated){sessionVerified=true;if(shell==null){render();refresh();}}
            else if(reachable){sessionVerified=false;SessionGate.openLogin(this);}
            else {sessionVerified=false;android.widget.TextView message=new android.widget.TextView(this);
                message.setText("اتصال به سامانه احراز هویت برقرار نشد. برای تلاش دوباره لمس کنید.");
                message.setGravity(Gravity.CENTER);message.setTextSize(17);setContentView(message);
                message.setOnClickListener(v->checkSession());}
        });
    }
    @Override protected void onResume(){super.onResume();if(shell!=null)checkSession();}
}
