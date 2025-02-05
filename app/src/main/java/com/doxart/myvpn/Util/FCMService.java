package com.doxart.myvpn.Util;


import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FCMService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        RemoteMessage.Notification notification = message.getNotification();

        if (notification == null) return;

        String title = message.getNotification().getTitle();
        String text = message.getNotification().getBody();

        NotificationUtils notify = new NotificationUtils(this);

        if (text != null & title != null) {
            notify.sendNotification(title, text, false);
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }
}
