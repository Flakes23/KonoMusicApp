package com.example.konomusic.core.app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.media.AudioAttributes;
import android.os.Build;

import androidx.multidex.MultiDexApplication;

public class ApplicationClass extends MultiDexApplication {

    public static final String CHANNEL_ID_1 = "channel1";
    public static final String CHANNEL_ID_2 = "channel2";
    public static final String ACTION_PREVIOUS = "actionprevious";
    public static final String ACTION_NEXT = "actionnext";
    public static final String ACTION_PLAY = "actionplay";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel1 =
                    new NotificationChannel(CHANNEL_ID_1,
                            "Channel(1)", NotificationManager.IMPORTANCE_HIGH);
            channel1.setDescription("Channel 1 Desc..");

            // Playback channel should be silent to avoid loud alert sounds.
            NotificationChannel channel2 =
                    new NotificationChannel(CHANNEL_ID_2,
                            "Channel(2)", NotificationManager.IMPORTANCE_LOW);
            channel2.setDescription("Channel 2 Desc..");
            channel2.enableVibration(false);
            channel2.enableLights(false);
            channel2.setSound(null, new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build());

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                // Recreate channel2 so old noisy settings do not persist on device/emulator.
                notificationManager.deleteNotificationChannel(CHANNEL_ID_2);
                notificationManager.createNotificationChannel(channel1);
                notificationManager.createNotificationChannel(channel2);
            }
        }
    }
}
