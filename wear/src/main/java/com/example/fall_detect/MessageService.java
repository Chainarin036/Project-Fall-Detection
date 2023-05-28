package com.example.fall_detect;
import android.content.Intent;
import com.google.android.gms.wearable.MessageEvent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.wearable.WearableListenerService;
public class MessageService extends WearableListenerService {
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
//If the message’s path equals "/my_path"...//
        if (messageEvent.getPath().equals("/my_path")) {
//...retrieve the message//
            final String message = new String(messageEvent.getData());
            Intent messageIntent = new Intent();
            messageIntent.setAction(Intent.ACTION_SEND);
            messageIntent.putExtra("message", message);
//Broadcast the received Data Layer messages locally//
            LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
        }
        else {
            super.onMessageReceived(messageEvent);
        }
    }
}