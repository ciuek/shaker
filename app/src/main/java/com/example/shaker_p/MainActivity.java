package com.example.shaker_p;

import static java.lang.Math.abs;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    SensorManager sm = null;
    EditText name = null;
    TextView az = null;
    TextView pt = null;
    List<Sensor> list1;
    List<Sensor> list2;
    ArrayList<Float> ACCx, ACCy, ACCz, GSCx, GSCy, GSCz;
    int READINGRATE = 20000;    //20 ms

    SensorEventListener sel = new SensorEventListener(){
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        public void onSensorChanged(SensorEvent event) {
            float[] values = event.values;
            az.setText("x: "+values[0]+"\ny: "+values[1]+"\nz: "+values[2]);
            ACCx.add(values[0]);
            ACCy.add(values[1]);
            ACCz.add(values[2]);
        }
    };
    SensorEventListener sel2 = new SensorEventListener(){
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        public void onSensorChanged(SensorEvent event) {
            float[] values = event.values;
            pt.setText("x: "+values[0]+"\ny: "+values[1]+"\nz: "+values[2]);
            GSCx.add(values[0]);
            GSCy.add(values[1]);
            GSCz.add(values[2]);
        }
    };

    private String createVector(ArrayList<Float> list){
        Float min = Collections.min(list),
                max = Collections.max(list),
                mean ,
                temp = 0f;
        double std,
                dtemp = 0.0;


        for (int x = 0; x < list.size(); x++)
            temp += list.get(x);
        mean = temp/list.size();

        for (int i = 0; i < list.size(); i++)
        {
            float val = list.get(i);
            double squrDiffToMean = Math.pow(val - mean, 2);
            dtemp += squrDiffToMean;
        }

        double meanOfDiffs = dtemp / (double) (list.size());
        std = Math.sqrt(meanOfDiffs);

        return min + " " + max + " " + mean + " " + std + " ";
    }

    private String avgVector(ArrayList<String> list) {
        double[] params = new double[24];
        for (String line : list) {
            String[] arr = line.split(" ");
            for (int i=0; i<arr.length; i++) {
                params[i] += Double.parseDouble(arr[i]);
            }
        }

        for(int i=0; i<params.length; i++)
        {
            params[i] = params[i]/list.size();
        }

        StringBuilder builder = new StringBuilder();

        for (double param : params)
            builder.append(param + " ");

        return  builder.toString();
    }

    private void compareVectors(String x, String y) {
        String[] file_arr = x.split(" ");
        String[] user_arr = y.split(" ");
        Double[] results = new Double[file_arr.length];
        Double sum = 0.0;

        if(file_arr.length == user_arr.length)
        {
            for (int i=0; i< file_arr.length; i++) {
                double a = Double.parseDouble(file_arr[i]),
                        b = Double.parseDouble(user_arr[i]);
                results[i] = abs(a-b);
                sum +=  results[i];
            }
            pt.setText(sum.toString());

        }
        else
            Toast.makeText(getBaseContext(), "Error: Wrong input.", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button measure = findViewById(R.id.button_first);
        Button compare = findViewById(R.id.button_compare);


        measure.setOnClickListener(v -> {
            name = findViewById(R.id.name);
            String namestring = name.getText().toString();
            if(namestring.equals("")) {
                Toast.makeText(getBaseContext(), "Error: Enter your name.", Toast.LENGTH_LONG).show();
                return;
            }

            sm = (SensorManager)getSystemService(SENSOR_SERVICE);

            az = findViewById(R.id.azimuth);
            pt = findViewById(R.id.pitch);

            ACCx = new ArrayList<>();
            ACCy = new ArrayList<>();
            ACCz = new ArrayList<>();
            GSCx = new ArrayList<>();
            GSCy = new ArrayList<>();
            GSCz = new ArrayList<>();

            list1 = sm.getSensorList(Sensor.TYPE_ACCELEROMETER);
            if(list1.size()>0){
                sm.registerListener(sel, list1.get(0), READINGRATE);
            }else{
                Toast.makeText(getBaseContext(), "Error: No Accelerometer.", Toast.LENGTH_LONG).show();
            }
            list2 = sm.getSensorList(Sensor.TYPE_GYROSCOPE);
            if(list2.size()>0){
                sm.registerListener(sel2, list2.get(0), READINGRATE);
            }
            else{
                Toast.makeText(getBaseContext(), "Error: No Gyroscope.", Toast.LENGTH_LONG).show();
            }

            new Handler().postDelayed(() -> {
                if(list1.size()>0){
                    sm.unregisterListener(sel);
                }
                if(list2.size()>0){
                    sm.unregisterListener(sel2);
                }

                File file = new File(Environment.getExternalStorageDirectory() + "/Documents/" + File.separator + namestring + ".txt");
                Context context = getApplicationContext();
                int duration = Toast.LENGTH_SHORT;
                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));

                    writer.append(createVector(ACCx));
                    writer.append(createVector(ACCy));
                    writer.append(createVector(ACCz));
                    writer.append(createVector(GSCx));
                    writer.append(createVector(GSCy));
                    writer.append(createVector(GSCz));
                    writer.append('\n');
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast toast = Toast.makeText(context, e.toString(), duration);
                    toast.show();
                    System.out.println("An error has occured");
                }
                Toast toast = Toast.makeText(context, "file created: " + file, duration);
                toast.show();
                System.out.println("file created: " + file);
            }, 5000);
        });

        compare.setOnClickListener(v -> {
            name = findViewById(R.id.name);
            String namestring = name.getText().toString();
            if(namestring.equals("")) {
                Toast.makeText(getBaseContext(), "Error: Enter your name.", Toast.LENGTH_LONG).show();
                return;
            }
            File file = new File(Environment.getExternalStorageDirectory() + "/Documents/" + File.separator + namestring + ".txt");
            ArrayList<String> inputlist = new ArrayList<>();

            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;

                while ((line = br.readLine()) != null) {
                    inputlist.add(line);
                }
                br.close();
            }
            catch (IOException e) {

            }

            String input = avgVector(inputlist);

            sm = (SensorManager)getSystemService(SENSOR_SERVICE);

            az = findViewById(R.id.azimuth);
            pt = findViewById(R.id.pitch);

            ACCx = new ArrayList<>();
            ACCy = new ArrayList<>();
            ACCz = new ArrayList<>();
            GSCx = new ArrayList<>();
            GSCy = new ArrayList<>();
            GSCz = new ArrayList<>();

            list1 = sm.getSensorList(Sensor.TYPE_ACCELEROMETER);
            if(list1.size()>0){
                sm.registerListener(sel, list1.get(0), READINGRATE);
            }else{
                Toast.makeText(getBaseContext(), "Error: No Accelerometer.", Toast.LENGTH_LONG).show();
            }
            list2 = sm.getSensorList(Sensor.TYPE_GYROSCOPE);
            if(list2.size()>0){
                sm.registerListener(sel2, list2.get(0), READINGRATE);
            }
            else{
                Toast.makeText(getBaseContext(), "Error: No Gyroscope.", Toast.LENGTH_LONG).show();
            }

            new Handler().postDelayed(() -> {
                if(list1.size()>0){
                    sm.unregisterListener(sel);
                }
                if(list2.size()>0){
                    sm.unregisterListener(sel2);
                }

                StringBuilder text2 = new StringBuilder();

                text2.append(createVector(ACCx));
                text2.append(createVector(ACCy));
                text2.append(createVector(ACCz));
                text2.append(createVector(GSCx));
                text2.append(createVector(GSCy));
                text2.append(createVector(GSCz));

                String usersInput = text2.toString();

                compareVectors(input, usersInput);


            }, 5000);

        });
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


}