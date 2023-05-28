package com.example.fall_detect;

import android.content.BroadcastReceiver;
import android.util.Log;
import android.widget.Button;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
// import androidx.room.jarjarred.org.stringtemplate.v4.Interpreter;

import com.google.android.gms.wearable.Node;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Wearable;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
public class MainActivity extends AppCompatActivity {
    Button talkbutton;
    TextView textview;
    protected Handler myHandler;
    int receivedMessageNumber = 1;
    int sentMessageNumber = 1;

    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //talkbutton = findViewById(R.id.talkButton);
        textview = findViewById(R.id.textView);

        database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");
//        myRef.setValue("Fall, World!");

        //Create a message handler//

        myHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Bundle stuff = msg.getData();
                messageText(stuff.getString("messageText"));
                return true;
            }
        });
//Register to receive local broadcasts, which we'll be creating in the next step//
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        Receiver messageReceiver = new Receiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);
    }

    public void messageText(String newinfo) {
        if (newinfo.compareTo("") != 0) {
//            textview.append("\n" + newinfo);

            textview.setText("\n" + newinfo);
        }

    }

    //Define a nested class that extends BroadcastReceiver//
    public class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
//Upon receiving each message from the wearable, display the following text//
            String message = "I just received a message from the wearable " + receivedMessageNumber++;
            String m1 = "ผู้สูงอายุหกล้ม กำลังต้องการความช่วยเหลือ";
            String m2 = "ผู้สูงอายุหกล้มอาจจะ 'หมดสติ !' ต้องการความช่วยเหลือโดยด่วน";
            int value = Integer.parseInt(intent.getStringExtra("message"));
            textview.setText("VAL : " + value);

            DatabaseReference myRef = database.getReference("Alert");
            DatabaseReference myRef2 = database.getReference("status");

            myRef.setValue(value);

            Toast.makeText(context, "value : " + value, Toast.LENGTH_SHORT).show();
            if(value == 1){
                myRef2.setValue("1");
                myRef.setValue(m1);
            }
            if(value == 2){
                myRef2.setValue("2");
                myRef.setValue(m2);
            }
            if(value == 0){
                myRef2.setValue("0");
                myRef.setValue("0");
            }
//            if ("FALL Alert".equals(value)) {
//                DatabaseReference myRefx = database.getReference("message");
//                myRefx.setValue("ผู้สูงอายุหกล้ม ต้องการความช่วยเหลือ");
//            }
        }

        public void talkClick(View v) {
            String message = "Sending message.... ";
            textview.setText(message);
//Sending a message can block the main UI thread, so use a new thread//
            new NewThread("/my_path", message).start();
        }

        //Use a Bundle to encapsulate our message//
        public void sendmessage(String messageText) {
            Bundle bundle = new Bundle();
            bundle.putString("messageText", messageText);
            Message msg = myHandler.obtainMessage();
            msg.setData(bundle);
            myHandler.sendMessage(msg);
        }

        class NewThread extends Thread {
            String path;
            String message;

            //Constructor for sending information to the Data Layer//
            NewThread(String p, String m) {
                path = p;
                message = m;
            }

            public void run() {
//Retrieve the connected devices, known as nodes//
                Task<List<Node>> wearableList =
                        Wearable.getNodeClient(getApplicationContext()).getConnectedNodes();
                try {
                    List<Node> nodes = Tasks.await(wearableList);
                    for (Node node : nodes) {
                        Task<Integer> sendMessageTask =
//Send the message//
                                Wearable.getMessageClient(MainActivity.this).sendMessage(node.getId(), path, message.getBytes());
                        try {
//Block on a task and get the result synchronously//
                            Integer result = Tasks.await(sendMessageTask);
                            sendmessage("I just sent the wearable a message " + sentMessageNumber++);
                            //if the Task fails, then…..//
                        } catch (ExecutionException exception) {
                            //TO DO: Handle the exception//
                        } catch (InterruptedException exception) {
                            //TO DO: Handle the exception//
                        }
                    }
                } catch (ExecutionException exception) {
                    //TO DO: Handle the exception//
                } catch (InterruptedException exception) {
                    //TO DO: Handle the exception//
                }
            }
        }
    }
}
