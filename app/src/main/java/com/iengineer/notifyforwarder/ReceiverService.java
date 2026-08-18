package com.iengineer.notifyforwarder;

import android.app.*;
import android.content.*;
import android.os.IBinder;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class ReceiverService extends Service {
    private volatile boolean running=false;
    private Thread worker;
    private static final int ID=101;
    private static final String CH="receiver";

    @Override public void onCreate() {
        super.onCreate();
        NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= 26)
            nm.createNotificationChannel(new NotificationChannel(CH,"Receiver",NotificationManager.IMPORTANCE_LOW));
        Notification n=new Notification.Builder(this,CH)
            .setContentTitle("WA Receiver active")
            .setContentText("Listening for forwarded messages")
            .setSmallIcon(android.R.drawable.ic_dialog_info).build();
        startForeground(ID,n);
    }

    @Override public int onStartCommand(Intent i,int flags,int id) {
        if (!running) {
            running=true;
            worker=new Thread(this::listen);
            worker.start();
        }
        return START_STICKY;
    }

    private void listen() {
        while(running) {
            HttpURLConnection c=null;
            try {
                String topic=Config.topic(this);
                if (topic.isEmpty()) { Thread.sleep(3000); continue; }
                URL u=new URL("https://ntfy.sh/"+topic+"/sse");
                c=(HttpURLConnection)u.openConnection();
                c.setRequestProperty("Accept","text/event-stream");
                c.setConnectTimeout(15000);
                c.setReadTimeout(0);
                BufferedReader br=new BufferedReader(new InputStreamReader(c.getInputStream(),StandardCharsets.UTF_8));
                String line, event="";
                while(running && (line=br.readLine())!=null) {
                    if (line.startsWith("data: ")) {
                        event=line.substring(6);
                        if (event.contains("\"message\"")) showMessage(extract(event, "message"));
                    }
                }
            } catch(Exception e) {
                try { Thread.sleep(3000); } catch(Exception ignored) {}
            } finally { if(c!=null)c.disconnect(); }
        }
    }

    private String extract(String json,String key) {
        String k="\""+key+"\":\"";
        int s=json.indexOf(k);
        if(s<0) return json;
        s+=k.length();
        StringBuilder out=new StringBuilder();
        boolean esc=false;
        for(int i=s;i<json.length();i++){
            char ch=json.charAt(i);
            if(esc){ out.append(ch); esc=false; }
            else if(ch=='\\') esc=true;
            else if(ch=='\"') break;
            else out.append(ch);
        }
        return out.toString().replace("\\n","\n");
    }

    private void showMessage(String msg) {
        NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        Notification n=new Notification.Builder(this,CH)
            .setContentTitle("Forwarded WhatsApp message")
            .setContentText(msg)
            .setStyle(new Notification.BigTextStyle().bigText(msg))
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setAutoCancel(true).build();
        nm.notify((int)(System.currentTimeMillis() & 0x7fffffff),n);
    }

    @Override public void onDestroy() {
        running=false;
        if(worker!=null) worker.interrupt();
        super.onDestroy();
    }
    @Override public IBinder onBind(Intent i){ return null; }
}
