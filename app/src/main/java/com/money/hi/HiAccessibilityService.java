package com.money.hi;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.graphics.Rect;


import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;


import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.os.Handler;

import java.io.IOException;
import java.util.List;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.LinearLayout;

import android.graphics.PixelFormat;

import android.os.SystemClock;

public class HiAccessibilityService extends BaseAccessibilityService {

    private static final String TAG = "xxx";

    private boolean isChatWindow = false;
    private long chatWindowCode = 0;

    private long lastTop = -1;

    private long lastEvent = 0;

    private boolean isWork = false;

    @Override
    public void onCreate() {
        createToucher();
    }


    @Override
    public void onInterrupt() {
        super.onInterrupt();

        Log.e(TAG, "onInterrupt ");
        goAccess();
    }

    @Override
    public void onAccessibilityEvent(final AccessibilityEvent event) {

        int eventType = event.getEventType();
        String className;

        Log.e(TAG, "eventType " + eventType);

        lastEvent = System.currentTimeMillis();

        checkEvent();

        switch (eventType) {

            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                className = event.getClassName().toString();
                Log.e(TAG, "window state change " + className);

                if(className.indexOf("activities.Chat") != -1){
                    isChatWindow = true;

                    if(imageButton1 != null){
                        isWork = true;
                        imageButton1.setBackgroundResource(R.drawable.green);
                        imageButton1.setKeepScreenOn(true);
                    }

                    AccessibilityNodeInfo root = getRootInActiveWindow();

                    if(root != null) {
                        chatWindowCode = root.hashCode();
                    }

                    scrollChatView(root);

                }else if(className.indexOf("hi.ui.MainActivity") != -1){
                    lastTop = -3;
                    isChatWindow = false;
                    chatWindowCode = 0;

                    if(imageButton1 != null){
                        isWork = false;
                        imageButton1.setBackgroundResource(R.drawable.red);
                        imageButton1.setKeepScreenOn(false);
                    }

                }else if(className.indexOf("LuckyMoneyActivity") != -1){
                    getLuckMoney();
                    delayBackToChat();
                }

//                AccessibilityNodeInfo root = getRootInActiveWindow();
//                HiAccessibilityService.printPacketInfo(root);

                break;

            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                className = event.getClassName().toString();
                Log.e(TAG, "type window content change " + isChatWindow);

                if(isChatWindow){

                    findLuckMoney();
                    getLuckMoney();
                    backToChat();
                    backToChat2();

                }

                break;

            default:
                break;

        }
    }

    private void scrollChatView(AccessibilityNodeInfo root){
        AccessibilityNodeInfo listView = getNodeByClassname(root, "ListView");

        if(listView != null) {
            Log.e(TAG, "scrollChatView");
            listView.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
        }
    }

    private boolean findLuckMoney(){
        AccessibilityNodeInfo root = getRootInActiveWindow();

        if(root != null) {
            List<AccessibilityNodeInfo> infos = root.findAccessibilityNodeInfosByText("领取红包");

            if (infos != null && infos.size() > 0) {

                AccessibilityNodeInfo info = infos.get(infos.size() - 1);
                Rect rect = new Rect();
                info.getBoundsInScreen(rect);

                if(rect.top <= lastTop){
                }else{
                    performViewClick(info);
                    Log.e(TAG, "findLuckMoney click");
                }
                lastTop = rect.top;

                return true;

            }else{

            }
        }

        return false;
    }

    private void getLuckMoney(){
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if(root != null) {
            List<AccessibilityNodeInfo> infos = root.findAccessibilityNodeInfosByText("拆红包");

            if (infos != null && infos.size() > 0) {
                AccessibilityNodeInfo info = infos.get(infos.size() - 1);
                performViewClick(infos.get(infos.size() - 1));
                Log.e(TAG, "getLuckMoney click ");
            }
        }
    }

    private void backToChat(){
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if(root != null) {
            List<AccessibilityNodeInfo> infos = root.findAccessibilityNodeInfosByText("查看我的红包记录");
            Log.e(TAG, "backToChat infos " + infos);

            if (infos != null && infos.size() > 0) {

                AccessibilityNodeInfo btn = getNodeByClassname(root, "ImageButton");

//                List<AccessibilityNodeInfo> btns = root.findAccessibilityNodeInfosByText("百度红包");
//                Log.e(TAG, "backToChat btns " + btn);
//
                if(btn != null){
                    performViewClick(btn);
                    Log.e(TAG, "backToChat click ");
                }

//                performBackClick();

            }
        }
    }

    private void backToChat2(){
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if(root != null) {
            List<AccessibilityNodeInfo> infos = root.findAccessibilityNodeInfosByText("手慢了，红包派完了");

            if (infos != null && infos.size() > 0) {
//                performBackClick();
                performViewClick(infos.get(infos.size() - 1));
                Log.e(TAG, "backToChat2 click");
            }

            if (infos != null && infos.size() == 0){
                infos = root.findAccessibilityNodeInfosByText("已失效");
                if (infos != null && infos.size() > 0) {
//                performBackClick();
                    performViewClick(infos.get(infos.size() - 1));
                    Log.e(TAG, "backToChat3 click");
                }
            }
        }
    }


    private void checkEvent(){

        if(!isWork){
            isWork = true;
            if(imageButton1 != null){
                imageButton1.setBackgroundResource(R.drawable.green);
                imageButton1.setKeepScreenOn(true);
            }
        }

        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                long now = System.currentTimeMillis();
                Log.e(TAG, "checkEvent");

                if(now - lastEvent > 10000){
                    Log.e(TAG, "checkEvent delay");
                    lastEvent = now;
//                    goAccess();
                    performGlobalAction(GLOBAL_ACTION_RECENTS);
                    delayMenu();

                    if(imageButton1 != null){
                        isWork = false;
                        imageButton1.setBackgroundResource(R.drawable.red);
                        imageButton1.setKeepScreenOn(false);
                    }

                }
            }
        }, 11000);
    }

    private void delayBackToChat(){
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                Log.e(TAG, "delay click");

                backToChat();
            }
        }, 5000);
    }

    private void delayMenu(){
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                Log.e(TAG, "delay recents");

                performGlobalAction(GLOBAL_ACTION_RECENTS);
            }
        }, 1000);
    }

    private AccessibilityNodeInfo getNodeByClassname(AccessibilityNodeInfo info, String classname){
        String name = info.getClassName().toString();
        String[] split = name.split("\\.");
        name = split[split.length - 1];
        if (classname.equals(name)) {
            return info;
        }

        int count = info.getChildCount();
        if (count > 0) {

            for (int i = 0; i < count; i++) {
                AccessibilityNodeInfo n = getNodeByClassname(info.getChild(i), classname);

                if(n != null){
                    return n;
                }
            }
        }

        return null;
    }

    private static int tabcount = -1;
    private static StringBuilder sb;

    public static void printPacketInfo(AccessibilityNodeInfo root) {
        sb = new StringBuilder();
        tabcount = 0;
        int[] is = {};
        analysisPacketInfo(root, is);
        Log.d(TAG, sb.toString());
    }

    //打印此时的界面状况,便于分析
    private static void analysisPacketInfo(AccessibilityNodeInfo info, int... ints) {
        if (info == null) {
            return;
        }
        if (tabcount > 0) {
            for (int i = 0; i < tabcount; i++) {
                sb.append("\t\t");
            }
        }
        if (ints != null && ints.length > 0) {
            StringBuilder s = new StringBuilder();
            for (int j = 0; j < ints.length; j++) {
                s.append(ints[j]).append(".");
            }
            sb.append(s).append(" ");
        }
        String name = info.getClassName().toString();
        String[] split = name.split("\\.");
        name = split[split.length - 1];
        if ("TextView".equals(name)) {
            CharSequence text = info.getText();
            sb.append("text:").append(text);
            sb.append("text:").append(info.getViewIdResourceName());
        } else if ("Button".equals(name)) {
            CharSequence text = info.getText();
            sb.append("Button:").append(text);
        } else {
            sb.append(name);
        }
        sb.append("\n");

        int count = info.getChildCount();
        if (count > 0) {
            tabcount++;
            int len = ints.length + 1;
            int[] newInts = Arrays.copyOf(ints, len);

            for (int i = 0; i < count; i++) {
                newInts[len - 1] = i;
                analysisPacketInfo(info.getChild(i), newInts);
            }
            tabcount--;
        }

    }

    LinearLayout toucherLayout;
    WindowManager.LayoutParams params;
    WindowManager windowManager;

    ImageButton imageButton1;
    //状态栏高度.
    int statusBarHeight = -1;

    private void createToucher()
    {
        //赋值WindowManager&LayoutParam.
        params = new WindowManager.LayoutParams();
        windowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        //设置type.系统提示型窗口，一般都在应用程序窗口之上.
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        //设置效果为背景透明.
        params.format = PixelFormat.RGBA_8888;
        //设置flags.不可聚焦及不可使用按钮对悬浮窗进行操控.
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;// |WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

        //设置窗口初始停靠位置.
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.x = 0;
        params.y = 0;

        //设置悬浮窗口长宽数据.
        //注意，这里的width和height均使用px而非dp.这里我偷了个懒
        //如果你想完全对应布局设置，需要先获取到机器的dpi
        //px与dp的换算为px = dp * (dpi / 160).
        params.width = 300;
        params.height = 300;

        LayoutInflater inflater = LayoutInflater.from(getApplication());
        //获取浮动窗口视图所在布局.
        toucherLayout = (LinearLayout) inflater.inflate(R.layout.toucherlayout,null);
        //添加toucherlayout
        windowManager.addView(toucherLayout,params);


        //主动计算出当前View的宽高信息.
        toucherLayout.measure(View.MeasureSpec.UNSPECIFIED,View.MeasureSpec.UNSPECIFIED);

        //用于检测状态栏高度.
        int resourceId = getResources().getIdentifier("status_bar_height","dimen","android");
        if (resourceId > 0)
        {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }

        //浮动窗口按钮.
        imageButton1 = (ImageButton) toucherLayout.findViewById(R.id.imageButton1);

        imageButton1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.i(TAG,"点击了");
                goAccess();
            }
        });

        imageButton1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                params.x = (int) event.getRawX() - 150;
                params.y = (int) event.getRawY() - 150 - statusBarHeight;
                windowManager.updateViewLayout(toucherLayout,params);
                return false;
            }
        });
    }

    @Override
    public void onDestroy()
    {
        if (imageButton1 != null)
        {
            windowManager.removeView(toucherLayout);
        }
        super.onDestroy();
    }

}
