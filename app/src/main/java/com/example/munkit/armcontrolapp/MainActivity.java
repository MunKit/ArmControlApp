package com.example.munkit.armcontrolapp;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.Sensor;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import org.xwalk.core.XWalkView;

import static android.R.color.holo_green_light;
import static android.R.color.holo_orange_light;
import static android.R.color.holo_red_light;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private static final String TAG = "Mymessage";

    private MqttAndroidClient mqttAndroidClient;
    private final String serverUri = "tcp://iot.eclipse.org:1883";
    private final String clientId = "myAndClient";
    private final String pubchannel = "RoboticArm/message";

    private long lasttime = System.currentTimeMillis();
    private long lasttime2 = System.currentTimeMillis();
    private SensorManager accSensorManager;
    private Sensor accAccelerometer;
    private SensorManager gyoSensorManager;
    private Sensor gyoAccelerometer;
    private double[] accdata = new double[3];
    private double[] angle = {90,90};
    private double[] langle = new double[3];

    private boolean terminate_tran = true;
    //true mean release
    private boolean clampstat = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        lasttime = System.currentTimeMillis();
        accSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accAccelerometer = accSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accSensorManager.registerListener(this, accAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        gyoSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gyoAccelerometer = gyoSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        gyoSensorManager.registerListener(this, gyoAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), serverUri, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

                TextView respondtext = (TextView) findViewById(R.id.respondtext);
                if (reconnect) {
                    respondtext.setText("Reconnected to : " + serverURI);
                    respondtext.setTextColor(getResources().getColor(holo_orange_light));
                    //Log.i(TAG,"Reconnected to : " + serverURI);
                    // Because Clean Session is true, we need to re-subscribe
                    //subscribeToTopic(subchannel);
                } else {
                    respondtext.setText("Connected to: " + serverURI);
                    respondtext.setTextColor(getResources().getColor(holo_green_light));
                    //Log.i(TAG,"Connected to: " + serverURI);
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                TextView respondtext = (TextView) findViewById(R.id.respondtext);
                respondtext.setText("The Connection was lost.");
                respondtext.setTextColor(getResources().getColor(holo_red_light));
                //Log.i(TAG,"The Connection was lost.");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                //Log.i(TAG,"Incoming message: " + new String(message.getPayload()));
                //set transition
                //transition = true;
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);

        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    //subscribeToTopic(subchannel);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    //Log.i(TAG,"Failed to connect to: " + serverUri);
                    //respondtext.setText("Failed to connect to: " + serverUri);
                    //respondtext.setTextColor(getResources().getColor(holo_red_light));
                }
            });


        } catch (MqttException ex){
            ex.printStackTrace();
        }

        final Button clampButton = (Button)findViewById(R.id.Clampbutton);
        clampButton.setOnClickListener(
                new Button.OnClickListener(){
                    public void onClick(View v){
                        //click to stop publish message
                        clampstat = !clampstat;

                        if (!terminate_tran) {
                            if (clampstat) {
                                clampButton.setText("Hold");
                                publishMessage(pubchannel, "release");
                            } else {
                                clampButton.setText("release");
                                publishMessage(pubchannel, "Hold");
                            }
                        }

                    }
                });

        final Button button1 = (Button)findViewById(R.id.connectbutton);

        button1.setOnClickListener(
                new Button.OnClickListener(){
                    public void onClick(View v){
                        //click to stop publish message
                        terminate_tran = !terminate_tran;
                        if (terminate_tran) {
                            button1.setText("Start");

                        }
                        else
                        {
                            button1.setText("stop");
                        }

                    }
                });

        SeekBar simpleSeekBar=(SeekBar)findViewById(R.id.seekBar4);
        // perform seek bar change listener event used for getting the progress value
        simpleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
                if (!terminate_tran)
                {
                    if (progressChangedValue<3)
                    {
                        langle[2] = -1;
                        publishMessage(pubchannel, Double.toString(langle[2])+" "+Double.toString(langle[1]));
                    }
                    else if (progressChangedValue>7)
                    {
                        langle[2] = 1;
                        publishMessage(pubchannel, Double.toString(langle[2])+" "+Double.toString(langle[1]));
                    }
                    else
                    {
                        langle[2] = 0;
                    }
                }

            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {


            }
        });
        //XWalkView mXWalkView = (XWalkView) findViewById(R.id.webview);
        //mXWalkView.load("https://webrtcsimple-error005.c9users.io/index.html", null);
    }

    //accelerometer event
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event)
    {
        // alpha is calculated as t / (t + dT)
        // with t, the low-pass filter's time-constant
        // and dT, the event delivery rate

        Sensor sensor = event.sensor;
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accdata[0] = event.values[0];
            accdata[1] = event.values[1];
            accdata[2] = event.values[2];
        }
        if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {

            long curtime = System.currentTimeMillis();
            long dt = (curtime - lasttime)/1000;
            angle[0] = 0.98*(angle[0] + event.values[1]*dt) + 0.02*Math.atan2(accdata[2],accdata[0]);
            angle[1] = 0.98*(angle[1] + event.values[0]*dt) + 0.02*Math.atan2(accdata[2],accdata[1]);

            //TextView Xval = (TextView) findViewById(R.id.textViewAccXval);
            //TextView Yval = (TextView) findViewById(R.id.textViewAccYval);
            //TextView Zval = (TextView) findViewById(R.id.textViewaccZval);

            //Xval.setText(Double.toString(Math.toDegrees(angle[0])));
            //Yval.setText(Double.toString(Math.toDegrees(angle[1])));
            //Zval.setText(double.toString(linear_acceleration[2]));

            if (Math.toDegrees(angle[0])< 50 )
            {
                //left
                langle[0] = -1;
            }
            else if (Math.toDegrees(angle[0])> 120)
            {
                //right
                langle[0] = 1;
            }
            else
            {
                langle[0] = 0;
            }
            if (Math.toDegrees(angle[1])< 50 )
            {
                //back
                langle[1] = 1;
            }
            else if (Math.toDegrees(angle[1])> 120)
            {
                //front
                langle[1] = -1;
            }
            else
            {
                langle[1] = 0;
            }
            if(curtime-lasttime2 > 500)
            {
                if (!terminate_tran) {
                    if (langle[0] == 1)
                        publishMessage(pubchannel, "x1");
                    else if (langle[0] == -1)
                        publishMessage(pubchannel, "x9");
                    else if (langle[1] !=0 ||  langle[2] !=0)
                        publishMessage(pubchannel, Double.toString(langle[2]) + " " + Double.toString(langle[1]));
                }
                lasttime2 = curtime;
            }

            lasttime = curtime;

        }

    }
    public void subscribeToTopic(String subscriptionTopic){
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG,"Subscribed!");

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(TAG,"Failed to subscribe");
                }
            });

        } catch (MqttException ex){
            Log.i(TAG,"Exception whilst subscribing");
            ex.printStackTrace();
        }
    }

    public void publishMessage(String Channel, String pubmessage){

        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(pubmessage.getBytes());
            mqttAndroidClient.publish(Channel, message);
            if(!mqttAndroidClient.isConnected()){
                Log.i(TAG,mqttAndroidClient.getBufferedMessageCount() + " messages in buffer.");
            }
        } catch (MqttException e) {
            Log.i(TAG,"Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        accSensorManager.registerListener(this, accAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        gyoSensorManager.registerListener(this, gyoAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        Log.i(TAG, "onResume");
    }


    @Override
    protected void onPause() {
        super.onPause();
        accSensorManager.unregisterListener(this);
        gyoSensorManager.unregisterListener(this);
        Log.i(TAG, "onPause");
    }

}
