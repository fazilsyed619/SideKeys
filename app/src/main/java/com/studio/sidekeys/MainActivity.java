package com.studio.sidekeys;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

public class MainActivity extends Activity {
    SharedPreferences prefs;
    LinearLayout root;
    TextView status;
    int blue = Color.rgb(36,118,232);

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences("settings", MODE_PRIVATE);
        buildUi();
    }

    TextView text(String s, float sp) { TextView t=new TextView(this); t.setText(s); t.setTextSize(sp); t.setTextColor(Color.WHITE); t.setPadding(0,8,0,8); return t; }
    Button button(String s) { Button b=new Button(this); b.setText(s); b.setAllCaps(false); return b; }

    void buildUi() {
        root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(32,28,32,24); root.setBackgroundColor(Color.rgb(23,25,30));
        TextView title=text("SideKeys",28); title.setTypeface(null,1); root.addView(title);
        TextView sub=text("Floating volume + power controls for phones with worn-out hardware buttons.",15); sub.setTextColor(Color.LTGRAY); root.addView(sub);

        status=text("Service: " + (KeyAccessibilityService.isRunning ? "ON" : "OFF"),16); status.setPadding(0,24,0,12); root.addView(status);
        Button access=button("Enable SideKeys Accessibility Service"); access.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))); root.addView(access);

        root.addView(text("Side",16));
        Spinner side=new Spinner(this); String[] sides={"Right side","Left side"}; side.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,sides)); side.setSelection(prefs.getBoolean("right",true)?0:1); side.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){ public void onNothingSelected(android.widget.AdapterView<?> p){} public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){prefs.edit().putBoolean("right",pos==0).apply(); KeyAccessibilityService.refreshIfRunning();}}); root.addView(side);

        root.addView(text("Button size",16));
        SeekBar size=new SeekBar(this); size.setMax(56); size.setProgress(prefs.getInt("size",28)-44); size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onStartTrackingTouch(SeekBar s){} public void onStopTrackingTouch(SeekBar s){} public void onProgressChanged(SeekBar s,int p,boolean f){prefs.edit().putInt("size",44+p).apply(); KeyAccessibilityService.refreshIfRunning();}}); root.addView(size);

        root.addView(text("Opacity",16));
        SeekBar opacity=new SeekBar(this); opacity.setMax(70); opacity.setProgress(prefs.getInt("opacity",90)-30); opacity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onStartTrackingTouch(SeekBar s){} public void onStopTrackingTouch(SeekBar s){} public void onProgressChanged(SeekBar s,int p,boolean f){prefs.edit().putInt("opacity",30+p).apply(); KeyAccessibilityService.refreshIfRunning();}}); root.addView(opacity);

        Switch lift=new Switch(this); lift.setText("Move controls upward while keyboard is open"); lift.setTextColor(Color.WHITE); lift.setChecked(prefs.getBoolean("lift",true)); lift.setOnCheckedChangeListener((v,c)->{prefs.edit().putBoolean("lift",c).apply(); KeyAccessibilityService.refreshIfRunning();}); root.addView(lift);
        TextView note=text("Power tap locks the screen. Long-press Power opens the system power menu. Android does not let normal third-party apps reproduce every hardware-power behavior.",14); note.setTextColor(Color.LTGRAY); note.setPadding(0,22,0,8); root.addView(note);
        setContentView(root);
    }
    @Override protected void onResume(){super.onResume(); if(status!=null) status.setText("Service: " + (KeyAccessibilityService.isRunning?"ON":"OFF"));}
}
