package com.iengineer.notifyforwarder;

import android.app.Notification;
import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class WaNotificationListener extends NotificationListenerService {
    private static volatile String lastKey = "";

    @Override public void onNotificationPosted(StatusBarNotification sbn) {
        if (!"SOURCE".equals(Config.mode(this))) return;

        String pkg=sbn.getPackageName();
        int n=0;
        if (pkg.equals(Config.pkg(this,1))) n=1;
        else if (pkg.equals(Config.pkg(this,2))) n=2;
        if (n==0) return;

        Notification no=sbn.getNotification();
        CharSequence title=no.extras.getCharSequence(Notification.EXTRA_TITLE);
        CharSequence text=no.extras.getCharSequence(Notification.EXTRA_TEXT);
        CharSequence big=no.extras.getCharSequence(Notification.EXTRA_BIG_TEXT);
        String body=big != null ? big.toString() : text != null ? text.toString() : "";
        String t=title != null ? title.toString() : "";

        if (TextUtils.isEmpty(body) && TextUtils.isEmpty(t)) return;
        String key=sbn.getKey()+":"+t+":"+body;
        if (key.equals(lastKey)) return;
        lastKey=key;

        String prefix=Config.prefix(this,n);
        String message=(prefix + "\n" + (TextUtils.isEmpty(t) ? "" : t + ": ") + body).trim();
        new Thread(() -> publish(message)).start();
    }

    private void publish(String message) {
        try {
            String topic=Config.topic(this);
            if (TextUtils.isEmpty(topic)) return;
            URL u=new URL("https://ntfy.sh/"+topic);
            HttpURLConnection c=(HttpURLConnection)u.openConnection();
            c.setRequestMethod("POST");
            c.setDoOutput(true);
            c.setRequestProperty("Title","WhatsApp notification");
            c.setRequestProperty("Content-Type","text/plain; charset=utf-8");
            try(OutputStream os=c.getOutputStream()) {
                os.write(message.getBytes(StandardCharsets.UTF_8));
            }
            c.getResponseCode();
            c.disconnect();
        } catch(Exception ignored) {}
    }
}
