package com.iengineer.notifyforwarder;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.*;

public class MainActivity extends Activity {
    EditText topic, prefix1, prefix2, package1, package2;
    RadioButton source, receiver;
    TextView status;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);

        status=findViewById(R.id.status);
        topic=findViewById(R.id.topic); prefix1=findViewById(R.id.prefix1);
        prefix2=findViewById(R.id.prefix2); package1=findViewById(R.id.package1);
        package2=findViewById(R.id.package2); source=findViewById(R.id.sourceMode);
        receiver=findViewById(R.id.receiverMode);

        topic.setText(Config.topic(this));
        prefix1.setText(Config.prefix(this,1)); prefix2.setText(Config.prefix(this,2));
        package1.setText(Config.pkg(this,1)); package2.setText(Config.pkg(this,2));

        String m=Config.mode(this);
        source.setChecked("SOURCE".equals(m)); receiver.setChecked("RECEIVER".equals(m));

        findViewById(R.id.save).setOnClickListener(v -> save());
        findViewById(R.id.activate).setOnClickListener(v -> activate());
        findViewById(R.id.notificationAccess).setOnClickListener(v ->
            startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")));

        if (android.os.Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 7);
        }
    }

    void save() {
        Config.p(this).edit()
            .putString("topic",topic.getText().toString().trim())
            .putString("prefix1",prefix1.getText().toString().trim())
            .putString("prefix2",prefix2.getText().toString().trim())
            .putString("pkg1",package1.getText().toString().trim())
            .putString("pkg2",package2.getText().toString().trim())
            .apply();
        status.setText("Settings saved.");
    }

    void activate() {
        save();
        String mode=source.isChecked() ? "SOURCE" : receiver.isChecked() ? "RECEIVER" : "NONE";
        Config.p(this).edit().putString("mode",mode).apply();

        if ("RECEIVER".equals(mode)) {
            startService(new Intent(this, ReceiverService.class));
            status.setText("RECEIVER active. Sender/source forwarding is OFF.");
        } else if ("SOURCE".equals(mode)) {
            stopService(new Intent(this, ReceiverService.class));
            status.setText("SOURCE active. Receiver service is OFF.");
        } else status.setText("Select a mode.");
    }
}
