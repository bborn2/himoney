package com.money.hi;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Rect;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Parcelable;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

public class HiMoneyService extends BaseAccessibilityService {

    private static final String TAG = "HiMoneyService";

    private static final String HIHONGBAO_NOTIFICATION_TIP = "[百度红包]";
    private static final int SCREEN_ON_TIME = 30000;

    private PowerManager.WakeLock mWakeLock;
    private boolean isChatWindow = true;
    private long lastTop = -1;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate");
    }

    @Override
    public void onInterrupt() {
        Log.e(TAG, "onInterrupt");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.e(TAG, "onServiceConnected");
    }

    @Override
    public void onAccessibilityEvent(final AccessibilityEvent event) {

        int eventType = event.getEventType();
        String className;

        if (watchNotifications(event)) {
            return;
        }

        switch (eventType) {

            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                className = event.getClassName().toString();
                Log.e(TAG, "window state change " + className);

                if (className.indexOf("activities.Chat") != -1) {
                    isChatWindow = true;

                    AccessibilityNodeInfo root = getRootInActiveWindow();

                    scrollChatView(root);

                    keepScreenOn(true);

                } else if (className.indexOf("hi.ui.MainActivity") != -1) {
                    lastTop = -3;
                    isChatWindow = false;

                    keepScreenOn(false);

                } else if (className.indexOf("LuckyMoneyActivity") != -1) {
                    getLuckMoney();
                    delayBackToChat();
                }

                break;

            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                Log.e(TAG, "type window content change " + isChatWindow);

                if (isChatWindow) {
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

    private void scrollChatView(AccessibilityNodeInfo root) {
        AccessibilityNodeInfo listView = getNodeByClassname(root, "ListView");

        if (listView != null) {
            Log.e(TAG, "scrollChatView");
            listView.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
        }
    }

    private boolean findLuckMoney() {
        AccessibilityNodeInfo root = getRootInActiveWindow();

        if (root != null) {
            List<AccessibilityNodeInfo> infos = root.findAccessibilityNodeInfosByText("领取红包");

            if (infos != null && infos.size() > 0) {

                AccessibilityNodeInfo info = infos.get(infos.size() - 1);
                Rect rect = new Rect();
                info.getBoundsInScreen(rect);

                if (rect.top <= lastTop) {
                } else {
                    performViewClick(info);
                    Log.e(TAG, "findLuckMoney click");
                }
                lastTop = rect.top;

                return true;
            }
        }

        return false;
    }

    private void getLuckMoney() {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root != null) {
            List<AccessibilityNodeInfo> infos = root.findAccessibilityNodeInfosByText("拆红包");

            if (infos != null && infos.size() > 0) {
                AccessibilityNodeInfo info = infos.get(infos.size() - 1);
                performViewClick(infos.get(infos.size() - 1));
                Log.e(TAG, "getLuckMoney click ");
            }
        }
    }

    private void backToChat() {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root != null) {
            List<AccessibilityNodeInfo> infos = root.findAccessibilityNodeInfosByText("查看我的红包记录");

            if (infos != null && infos.size() > 0) {
                AccessibilityNodeInfo btn = getNodeByClassname(root, "ImageButton");

                if (btn != null) {
                    performViewClick(btn);
                }
            }
        }
    }

    private void backToChat2() {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root != null) {
            List<AccessibilityNodeInfo> infos = root.findAccessibilityNodeInfosByText("手慢了，红包派完了");

            if (infos != null && infos.size() > 0) {
                performViewClick(infos.get(infos.size() - 1));
            }

            if (infos != null && infos.size() == 0) {
                infos = root.findAccessibilityNodeInfosByText("已失效");
                if (infos != null && infos.size() > 0) {
                    performViewClick(infos.get(infos.size() - 1));
                }
            }
        }
    }

    private void delayBackToChat() {
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                backToChat();
            }
        }, 5000);
    }

    private AccessibilityNodeInfo getNodeByClassname(AccessibilityNodeInfo info, String classname) {
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

                if (n != null) {
                    return n;
                }
            }
        }

        return null;
    }

    private boolean watchNotifications(AccessibilityEvent event) {
        // Not a notification
        if (event.getEventType() != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            return false;
        }

        // Not a hongbao
        String tip = event.getText().toString();

        if (!tip.contains(HIHONGBAO_NOTIFICATION_TIP)) {
            return false;
        }

        if (isKeyguardLocked()) {
            alarmHongbao();
        } else {
            Parcelable parcelable = event.getParcelableData();
            if (parcelable instanceof Notification) {
                Notification notification = (Notification) parcelable;
                try {
                    notification.contentIntent.send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }
        }

        return true;
    }

    private boolean isKeyguardLocked() {
        return ((KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE)).isKeyguardLocked();
    }

    private void alarmHongbao() {
        // ring
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        final Ringtone rt = RingtoneManager.getRingtone(getApplicationContext(), uri);
        rt.play();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                rt.stop();
            }
        }, 5000);

        // vibrate
        Vibrator vibrator = (Vibrator) this.getSystemService(this.VIBRATOR_SERVICE);
        vibrator.vibrate(5000);
    }

    @SuppressLint("InvalidWakeLockTag")
    private void keepScreenOn(boolean on) {
        if (mWakeLock == null) {
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            mWakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "WakeLock");
            mWakeLock.setReferenceCounted(false);
        }

        if (on) {
            mWakeLock.acquire(SCREEN_ON_TIME);
        } else {
            mWakeLock.release();
        }

    }
}
