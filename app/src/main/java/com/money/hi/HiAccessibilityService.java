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


import java.io.IOException;
import java.util.List;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;


public class HiAccessibilityService extends BaseAccessibilityService {

    private boolean isChatWindow = false;
    private long chatWindowCode = 0;

    private long lastTop = -1;

    @Override
    public void onAccessibilityEvent(final AccessibilityEvent event) {

        int eventType = event.getEventType();
        String className;

        Log.e("xxx", "eventType " + eventType);



        switch (eventType) {

            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                className = event.getClassName().toString();
                Log.e("xxx", "window state change " + className);

                if(className.indexOf("activities.Chat") != -1){
                    isChatWindow = true;

                    AccessibilityNodeInfo root = getRootInActiveWindow();

                    if(root != null) {
                        chatWindowCode = root.hashCode();
                    }
                }else if(className.indexOf("hi.ui.MainActivity") != -1){
                    lastTop = -3;
                    isChatWindow = false;
                    chatWindowCode = 0;
                }else if(className.indexOf("LuckyMoneyActivity") != -1){
                    getLuckMoney();
                }

                AccessibilityNodeInfo root = getRootInActiveWindow();

                HiAccessibilityService.printPacketInfo(root);

                break;

            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                className = event.getClassName().toString();
                Log.e("xxx", "type window content change " + isChatWindow);

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

    private boolean findLuckMoney(){
        AccessibilityNodeInfo root = getRootInActiveWindow();

//        if(root != null){
//            Log.e("xxx", "findLuckMoney root.hashCode()  " + root.hashCode());
//            Log.e("xxx", "findLuckMoney chatWindowCode  " + chatWindowCode);
//        }

        if(root != null) {
            List<AccessibilityNodeInfo> infos = root.findAccessibilityNodeInfosByText("领取红包");
//            Log.e("xxx", "findLuckMoney infos " + infos);

            if (infos != null && infos.size() > 0) {

                AccessibilityNodeInfo info = infos.get(infos.size() - 1);
                Rect rect = new Rect();
                info.getBoundsInScreen(rect);

                Log.e("xxx", "findLuckMoney top = " + rect.top);
                Log.e("xxx", "findLuckMoney lastTop = " + lastTop);

                if(rect.top <= lastTop){
                }else{
                    performViewClick(info);
                    Log.e("xxx", "findLuckMoney click");
                }
                lastTop = rect.top;

                return true;

            }else{
//                lastTop = -2;
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
                Log.e("xxx", "getLuckMoney click ");
            }
        }
    }

    private void backToChat(){
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if(root != null) {
            List<AccessibilityNodeInfo> infos = root.findAccessibilityNodeInfosByText("查看我的红包记录");
            Log.e("xxx", "backToChat infos " + infos);

            if (infos != null && infos.size() > 0) {

                AccessibilityNodeInfo btn = getNodeByClassname(root, "ImageButton");

//                List<AccessibilityNodeInfo> btns = root.findAccessibilityNodeInfosByText("百度红包");
                Log.e("xxx", "backToChat btns " + btn);
//
                if(btn != null){
                    performViewClick(btn);
                    Log.e("xxx", "backToChat click ");
                }

//                performBackClick();

            }
        }
    }

    private void backToChat2(){
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if(root != null) {
            List<AccessibilityNodeInfo> infos = root.findAccessibilityNodeInfosByText("手慢了，红包派完了");
            Log.e("xxx", "backToChat2 infos " + infos);
            if (infos != null && infos.size() > 0) {
//                performBackClick();
                performViewClick(infos.get(infos.size() - 1));
                Log.e("xxx", "backToChat2 click");
            }
        }
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
        Log.d("xxx", sb.toString());
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

}
