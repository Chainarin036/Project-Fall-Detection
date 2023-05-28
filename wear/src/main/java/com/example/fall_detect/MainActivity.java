package com.example.fall_detect;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;
import android.os.Vibrator;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.Node;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutionException;

public class MainActivity extends Activity implements SensorEventListener {
    private TextView textView;
    Button talkButton;
    Vibrator v;
    int receivedMessageNumber = 1;
    int sentMessageNumber = 1;
    private SensorManager sensorManeger;
    private boolean mFallDetected = false;
    private boolean isCancle = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView =  findViewById(R.id.text);
//        talkButton =  findViewById(R.id.talkClick);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        sensorManeger = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor accleroSensor = sensorManeger.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManeger.registerListener(this, accleroSensor, SensorManager.SENSOR_DELAY_UI);
//Create an OnClickListener//
//        talkButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String onClickMessage = "I just sent the handheld a message " + sentMessageNumber++;
//                textView.setText(onClickMessage);
////Use the same path//
//                String datapath = "/my_path";
//                new SendMessage(datapath, onClickMessage).start();
//            }
//        });
//Register to receive local broadcasts, which we'll be creating in the next step//
        IntentFilter newFilter = new IntentFilter(Intent.ACTION_SEND);
        Receiver messageReceiver = new Receiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, newFilter);
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        //float[] val = new float[]{sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]};

//        textView.setText(val);

        float x = sensorEvent.values[0];
        float y = sensorEvent.values[1];
        float z = sensorEvent.values[2];
        float[] val1 = new float[]{sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]};
        String val = x +"|"+ y +"|"+  z;
        double magnitude = Math.sqrt(x * x + y * y + z * z);
        double ac = calAcc(val1);
        //String val = sensorEvent.values[0] +"|"+ sensorEvent.values[1] +"|"+  sensorEvent.values[2];
//        new SendMessage("/my_path", val).start();
        Log.e("SendMessage","SendMessage : " + val);


        if (ac > 40) {
            v.vibrate(2000);
            Dialog dialog = new Dialog(MainActivity.this);
            View myLayout = getLayoutInflater().inflate(R.layout.custom_dialog_layout, null);
            dialog.setContentView(myLayout);
            dialog.show();

            // start countdown timer
            int countdownTime = 10; // in seconds
            new CountDownTimer(countdownTime * 1000, 1000) {
                public void onTick(long millisUntilFinished) {
                    // update countdown display (if needed)
                    Button positiveButton = myLayout.findViewById(R.id.button_ok);
                    positiveButton.setOnClickListener(
                            v -> {
                                /* Your action on positive button clicked. */
                                new SendMessage("/my_path", "0").start();
                                dialog.dismiss();
                                cancel(); // cancel countdown timer
                            }
                    );

                    Button negativeButton = myLayout.findViewById(R.id.button_sos);
                    negativeButton.setOnClickListener(
                            v -> {
                                /* Your action on negative button clicked. */
                                new SendMessage("/my_path", "1").start();
                                isCancle = true;
                                dialog.dismiss();
                                cancel(); // cancel countdown timer
                            }
                    );
                }

                public void onFinish() {
                    // dismiss dialog box
                    if(!isCancle){
                        new SendMessage("/my_path", "2").start();
                    }
                    isCancle = false;
                    dialog.dismiss();
                }
            }.start();
        }


//        AsyncTask.execute(new Runnable() {
//            @Override
//            public void run() {
//                //TODO your background code
//
//        });



    }
    double calAcc(float[] inp) {
        double x_pow = Math.pow(inp[0], 2);
        double y_pow = Math.pow(inp[1], 2);
        double z_pow = Math.pow(inp[2], 2);
        double com = x_pow + y_pow + z_pow;
        double result = Math.sqrt(com);
        return result;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
//Display the following when a new message is received//
            String onMessageReceived = "I just received a message from the handheld " + receivedMessageNumber++;
            textView.setText(onMessageReceived);

        }
    }
    class SendMessage extends Thread {
        String path;
        String message;
        //Constructor for sending information to the Data Layer//
        SendMessage(String p, String m) {
            path = p;
            message = m;
        }
        public void run() {
//Retrieve the connected devices//
            Task<List<Node>> nodeListTask =
                    Wearable.getNodeClient(getApplicationContext()).getConnectedNodes();
            try {
//Block on a task and get the result synchronously//
                List<Node> nodes = Tasks.await(nodeListTask);
                for (Node node : nodes) {
//Send the message///
                    Task<Integer> sendMessageTask =
                            Wearable.getMessageClient(MainActivity.this).sendMessage(node.getId(), path, message.getBytes());
                    try {
                        Integer result = Tasks.await(sendMessageTask);
//Handle the errors//
                    } catch (ExecutionException exception) {
//TO DO//
                    } catch (InterruptedException exception) {
//TO DO//
                    }
                }
            } catch (ExecutionException exception) {
//TO DO//
            } catch (InterruptedException exception) {
//TO DO//
            }
        }
    }
}