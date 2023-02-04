package com.nyt.taxi.Services;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nyt.taxi.R;

import java.util.Map;

public class FirebaseHandler extends FirebaseMessagingService {
    private static final String CHANNEL_ID = "1250012";
    private static final String TAG = FirebaseHandler.class.getSimpleName();

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        Map<String, String> md = message.getData();
        JsonObject jm = JsonParser.parseString(md.get("data")).getAsJsonObject();
        JsonObject ji = jm.getAsJsonObject("payload");
//        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
//
//        Person sender = new Person.Builder().setName(getString(R.string.dispatcher))
//                .build();
//
//        Notification.Builder notification = new Notification.Builder(this, CHANNEL_ID)
//                .setSmallIcon(Icon.createWithBitmap(bmp))
//                .setContentTitle(ji.get("title").getAsString())
//                .setContentText(ji.get("body").getAsString())
//                .setLargeIcon(bmp)
//                .setCategory(Notification.CATEGORY_MESSAGE)
//                .setPriority(NotificationCompat.PRIORITY_MAX);
//
//        new Notification.MessagingStyle(sender).addMessage(ji.get("body").getAsString(), System.currentTimeMillis (), sender).setBuilder(notification);
//
//        Intent push = new Intent();
//        push.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        push.setClass(this, ChatActivity.class);
//        push.putExtra("info", true);
//
//        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(this, 0, push, PendingIntent.FLAG_CANCEL_CURRENT);
//        notification.setContentIntent(fullScreenPendingIntent);
//
//        NotificationManager notificationManager = (NotificationManager) getSystemService(Context. NOTIFICATION_SERVICE ) ;
//        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, TAG, NotificationManager.IMPORTANCE_HIGH);
//        notificationManager.createNotificationChannel(channel);
//        notificationManager.notify(( int ) System. currentTimeMillis () , notification.build());

//
//        Intent intent = new Intent(this, MainActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
//        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
//        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, TAG, NotificationManager.IMPORTANCE_DEFAULT);
//        notificationChannel.enableVibration(true);
//        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(notificationChannel);
//
//
//        notificationBuilder
//                .setContentTitle(ji.get("title").getAsString())
//                .setContentText(ji.get("body").getAsString())
//                // .setDefaults(DEFAULT_SOUND | DEFAULT_VIBRATE)
//                .setAutoCancel(true)
//                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
//                .setContentIntent(pendingIntent)
//                .setLargeIcon(icon)
//                .setColor(Color.RED)
//                .setPriority(NotificationManager.IMPORTANCE_HIGH)
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .setStyle(new NotificationCompat.BigTextStyle()
//                        .bigText(ji.get("body").getAsString())
//                )
//        ;
//
//
//        notificationBuilder.setDefaults(DEFAULT_VIBRATE);
//        notificationBuilder.setLights(Color.YELLOW, 1000, 300);
//
//        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        notificationManager.notify(0, notificationBuilder.build());
//
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, TAG, NotificationManager.IMPORTANCE_HIGH);
//            notificationManager.createNotificationChannel(channel);
//            Notification notification = new Notification.Builder(getApplicationContext(),CHANNEL_ID).build();
//            startForeground(1, notification);
//        }
    }
}
