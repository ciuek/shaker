package com.example.shaker_p;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    SensorManager sm = null;
    TextView az = null;
    TextView pt = null;
    List list1, list2, loglist;
    int READINGRATE = 20000;    //20 ms

    SensorEventListener sel = new SensorEventListener(){
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        public void onSensorChanged(SensorEvent event) {
            float[] values = event.values;
            az.setText("x: "+values[0]+"\ny: "+values[1]+"\nz: "+values[2]);
            loglist.add("ACC "+values[0]+" "+values[1]+" "+values[2]);
        }
    };
    SensorEventListener sel2 = new SensorEventListener(){
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        public void onSensorChanged(SensorEvent event) {
            float[] values = event.values;
            pt.setText("x: "+values[0]+"\ny: "+values[1]+"\nz: "+values[2]);
            loglist.add("GY "+values[0]+" "+values[1]+" "+values[2]);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button mButton = findViewById(R.id.button_first);
        mButton.setOnClickListener(this);
    }

    @Override
    protected void onStop() {
        if(list1.size()>0){
            sm.unregisterListener(sel);
        }
        if(list2.size()>0){
            sm.unregisterListener(sel2);
        }
        super.onStop();
    }
    @Override
    public void onClick(View v) {
        sm = (SensorManager)getSystemService(SENSOR_SERVICE);

        az = (TextView) findViewById(R.id.azimuth);
        pt = (TextView) findViewById(R.id.pitch);

        loglist = new ArrayList<String>();

        list1 = sm.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if(list1.size()>0){
            sm.registerListener(sel, (Sensor) list1.get(0), READINGRATE);
        }else{
            Toast.makeText(getBaseContext(), "Error: No Accelerometer.", Toast.LENGTH_LONG).show();
        }
        list2 = sm.getSensorList(Sensor.TYPE_GYROSCOPE);
        if(list2.size()>0){
            sm.registerListener(sel2, (Sensor) list2.get(0), READINGRATE);
        }
        else{
            Toast.makeText(getBaseContext(), "Error: No Gyroscope.", Toast.LENGTH_LONG).show();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(list1.size()>0){
                    sm.unregisterListener(sel);
                }
                if(list2.size()>0){
                    sm.unregisterListener(sel2);
                }
                File file = new File(Environment.getExternalStorageDirectory() + "/Download/" + File.separator + "test.txt");
               /* if(file.exists())
                {*/
                    Context context = getApplicationContext();
                    int duration = Toast.LENGTH_SHORT;
                    try {
                        BufferedWriter writer = new BufferedWriter(new FileWriter(file));

                        for (int x=0;x<loglist.size();x++)
                        {
                            writer.append((CharSequence) loglist.get(x));
                            writer.append('\n');
                        }
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast toast = Toast.makeText(context, e.toString(), duration);
                        toast.show();
                        System.out.println("file created: "+file);
                    }
                    Toast toast = Toast.makeText(context, "file created: "+file, duration);
                    toast.show();
                    System.out.println("file created: "+file);
                    loglist.clear();
                }
           /* }*/
        }, 5000);
    }

}