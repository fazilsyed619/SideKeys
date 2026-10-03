package com.studio.sidekeys;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityWindowInfo;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.os.*;
import android.view.*;
import java.util.List;

public class KeyAccessibilityService extends AccessibilityService {
    static boolean isRunning=false;
    static KeyAccessibilityService instance;
    WindowManager wm; OverlayView overlay; WindowManager.LayoutParams lp; AudioManager audio;
    SharedPreferences prefs; Handler handler=new Handler(Looper.getMainLooper()); boolean imeVisible=false;

    public static void refreshIfRunning(){ if(instance!=null) instance.refreshLayout(); }

    @Override protected void onServiceConnected(){
        super.onServiceConnected(); instance=this; isRunning=true;
        prefs=getSharedPreferences("settings",MODE_PRIVATE); audio=(AudioManager)getSystemService(AUDIO_SERVICE); wm=(WindowManager)getSystemService(WINDOW_SERVICE);
        overlay=new OverlayView(this); lp=new WindowManager.LayoutParams(-2,-2,WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, android.graphics.PixelFormat.TRANSLUCENT); lp.gravity=Gravity.TOP|Gravity.LEFT; refreshLayout(); wm.addView(overlay,lp);
    }
    void refreshLayout(){ if(overlay==null)return; overlay.rebuild(); moveOverlay(); }
    void moveOverlay(){
        if(lp==null||overlay==null)return;
        int[] wh=new int[2]; overlay.measure(View.MeasureSpec.makeMeasureSpec(1000,View.MeasureSpec.AT_MOST),View.MeasureSpec.makeMeasureSpec(2000,View.MeasureSpec.AT_MOST)); int w=overlay.getMeasuredWidth(), h=overlay.getMeasuredHeight();
        android.graphics.Rect ime=getImeBounds(); boolean lift=prefs.getBoolean("lift",true) && !ime.isEmpty();
        int screenH=getResources().getDisplayMetrics().heightPixels, screenW=getResources().getDisplayMetrics().widthPixels;
        lp.width=w; lp.height=h; lp.x=prefs.getBoolean("right",true)?Math.max(0,screenW-w-8):8;
        if(lift) lp.y=Math.max(16,ime.top-h-12); else lp.y=Math.max(16,(screenH-h)/2);
        wm.updateViewLayout(overlay,lp);
    }
    android.graphics.Rect getImeBounds(){
        android.graphics.Rect r=new android.graphics.Rect();
        try{ List<AccessibilityWindowInfo> ws=getWindows(); if(ws!=null) for(AccessibilityWindowInfo w:ws){ if(w.getType()==AccessibilityWindowInfo.TYPE_INPUT_METHOD){w.getBoundsInScreen(r); if(r.height()>100)return r;}} }catch(Exception ignored){}
        return r;
    }
    @Override public void onAccessibilityEvent(android.view.accessibility.AccessibilityEvent e){ if(e.getEventType()==android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || e.getEventType()==android.view.accessibility.AccessibilityEvent.TYPE_WINDOWS_CHANGED) handler.postDelayed(this::moveOverlay,80); }
    @Override public void onInterrupt(){}
    void volume(int delta){ audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,delta>0?AudioManager.ADJUST_RAISE:AudioManager.ADJUST_LOWER,AudioManager.FLAG_SHOW_UI); overlay.invalidate(); }
    void setVolume(float fraction){ int max=audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC); int v=Math.round(Math.max(0,Math.min(1,fraction))*max); audio.setStreamVolume(AudioManager.STREAM_MUSIC,v,AudioManager.FLAG_SHOW_UI); overlay.invalidate(); }
    void powerTap(){ performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN); }
    void powerLong(){ performGlobalAction(GLOBAL_ACTION_POWER_DIALOG); }
    @Override public void onDestroy(){ if(overlay!=null){try{wm.removeView(overlay);}catch(Exception ignored){}} overlay=null; instance=null; isRunning=false; super.onDestroy(); }

    class OverlayView extends View {
        Paint p=new Paint(3); float downX,downY; boolean draggingSlider=false; long powerDown;
        float d;
        OverlayView(Context c){super(c); setBackground(new ColorDrawable(Color.TRANSPARENT)); setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
        void rebuild(){d=getResources().getDisplayMetrics().density; requestLayout(); invalidate();}
        int S(){return prefs.getInt("size",72);}
        @Override protected void onMeasure(int ws,int hs){int s=S(); setMeasuredDimension((int)(2.15f*s*d),(int)(5.25f*s*d));}
        void circle(Canvas c,float cx,float cy,float r,String glyph){
            int alpha=Math.round(255*(prefs.getInt("opacity",90)/100f)); p.setAlpha(alpha); p.setColor(Color.rgb(36,118,232)); p.setStyle(Paint.Style.FILL); c.drawCircle(cx,cy,r,p);
            p.setColor(Color.WHITE); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(Math.max(3,r*.07f)); c.drawCircle(cx,cy,r,p);
            p.setStyle(Paint.Style.FILL); p.setTextAlign(Paint.Align.CENTER); p.setTypeface(Typeface.create("sans",Typeface.NORMAL)); p.setTextSize(r*1.05f); c.drawText(glyph,cx,cy-r*.30f,p);
        }
        @Override protected void onDraw(Canvas c){super.onDraw(c); float s=S()*d, w=getWidth(), h=getHeight(); float cx=s*.55f; float r=s*.46f; float top=s*.10f; circle(c,cx,top+r,"+"=="+"?r:r,"+"); circle(c,cx,top+r*3.15f,r,"−"); circle(c,cx,top+r*5.30f,r,"⏻");
            float barX=s*1.18f, barW=s*.78f, barTop=s*.06f, barBot=h-s*.06f, barH=barBot-barTop; float frac=(float)audio.getStreamVolume(AudioManager.STREAM_MUSIC)/Math.max(1,audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            p.setStyle(Paint.Style.FILL); p.setAlpha(Math.round(255*(prefs.getInt("opacity",90)/100f))); p.setColor(Color.rgb(238,238,240)); c.drawRoundRect(barX,barTop,barX+barW,barBot,barW/2,barW/2,p);
            p.setColor(Color.rgb(36,118,232)); float fy=barBot-barH*frac; c.drawRoundRect(barX,fy,barX+barW,barBot,barW/2,barW/2,p);
            p.setColor(Color.rgb(20,100,210)); p.setTextAlign(Paint.Align.CENTER); p.setTextSize(s*.30f); c.drawText(String.valueOf(audio.getStreamVolume(AudioManager.STREAM_MUSIC)),barX+barW/2,barTop+s*.62f,p);
            p.setColor(Color.WHITE); p.setTextSize(s*.55f); c.drawText("♪",barX+barW/2,barBot-s*.30f,p);
        }
        float dist(float x,float y,float cx,float cy){return (float)Math.hypot(x-cx,y-cy);}
        @Override public boolean onTouchEvent(android.view.MotionEvent e){float s=S()*d,r=s*.46f,cx=s*.55f; if(e.getAction()==MotionEvent.ACTION_DOWN){downX=e.getX();downY=e.getY(); float bx=s*1.18f, bw=s*.78f; if(e.getX()>=bx&&e.getX()<=bx+bw){draggingSlider=true; setVolume(1f-(e.getY()-s*.06f)/(getHeight()-s*.12f)); return true;} powerDown=SystemClock.uptimeMillis(); return true;}
            if(e.getAction()==MotionEvent.ACTION_MOVE){if(draggingSlider){setVolume(1f-(e.getY()-s*.06f)/(getHeight()-s*.12f));} return true;}
            if(e.getAction()==MotionEvent.ACTION_UP){if(draggingSlider){draggingSlider=false;return true;} float y=e.getY(); if(dist(e.getX(),y,cx,s*.10f+r)<r*1.25f){volume(1);return true;} if(dist(e.getX(),y,cx,s*.10f+r*3.15f)<r*1.25f){volume(-1);return true;} if(dist(e.getX(),y,cx,s*.10f+r*5.30f)<r*1.25f){if(SystemClock.uptimeMillis()-powerDown>600)powerLong();else powerTap();return true;} } return true; }
    }
}
