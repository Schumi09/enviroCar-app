/*
Service for detecting Emergency Brakes   
*/

package org.envirocar.app.application.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import org.envirocar.app.application.CommandListener;
import org.envirocar.app.storage.Measurement;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class AccelerometerService extends Service implements SensorEventListener {

    private final float NOISE = 1.2;
    private final long TERMINATE = 15000;
    private final long TIMER = 1000;
    private Calendar calendar;
    private Boolean detected; // boolean for avoiding multiple measurements at once
    private Handler handler;
    private ArrayList<Measurement> list;
    private Sensor mAccelerometer;
    private SensorManager mSensorManager;
    private SimpleDateFormat sdf;
    private Handler terminateHandler;
    private static File sdcard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
    

    public void onCreate()
    {
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(1);
        mSensorManager.registerListener(this, mAccelerometer, 3);
        sdf = new SimpleDateFormat("dd/MM/yyyy hh/mm/ss");
        calendar = Calendar.getInstance();
        handler = new Handler();
        terminateHandler = new Handler();
        list = new ArrayList<Measurement>();
        detected = false;
    }

    public void onDestroy()
    {
        mSensorManager.unregisterListener(this);
    }

    public void onSensorChanged(SensorEvent sensorevent)
    {
        float x = sensorevent.values[0];
        float y = sensorevent.values[1];
        float z = sensorevent.values[2];
        float g = (float)(Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2)) / 9.8100000000000005);
        //Log.d("Accelerometer","" + g);

        final String fname = (new StringBuilder()).append(c.getTimeInMillis()).append("").toString();

        if (g >= NOISE && detected == false)
        {
            detected = true;
            Measurement measurement = CommandListener.getCurrentMeasurement();
            if (measurement.getProperty(Measurement.PropertyKey.SPEED) >= 20)
            {
                list.add(measurement);
                createCSV(fname, g);
                addToCSV(fname, measurement.toString());

                // Gets car's data depending on the refresh rate TIMER
                final Runnable runnable = new Runnable() {
                    public void run()
                    {
                        Measurement measurement1 = CommandListener.getCurrentMeasurement();
                        Log.d("measure"," "+measurement1);
                        addToCSV(fname, measurement1.toString());
                        list.add(measurement1);
                        handler.postDelayed(this, TIMER);
                    }

                };

                // Starting after TIMER, first Measurement is already added 
                handler.postDelayed(runnable, TIMER);


                // Analysing Measurements after milliseconds defined by TERMINATE,    
                terminateHandler.postDelayed(new Runnable() {

                    public void run() {

                        handler.removeCallbacks(runnable);
                        
                        double first_speed = list.get(0).getProperty(Measurement.PropertyKey.SPEED); // km/h
                        double brake_length = 0.5 * Math.pow((first_speed / 10), 2); //m
                        first_speed = first_speed / 360 * 100; // m/s

                        double brake_duration = brake_length / first_speed; //in seconds
                        double brake_duration_i = Math.ceil(brake_duration);
                        int list_l = list.size();
                        int i = 0;
                        double speed_i = first_speed;


                        while(i <= brake_duration_i && i <= list_l && speed_i !=0){
                            speed_i = list.get(i).getProperty(Measurement.PropertyKey.SPEED);
                            i++;
                        }
                        if(speed_i == 0){
                            addToCSV(fname, "Emergency Brake detected");
                        }else{
                            addToCSV(fname, "False Detection");
                        }

                    }

                }, TERMINATE);


                detected = false;
                list = new ArrayList<Measurement>();
                Log.d("Fullbrake", (new StringBuilder()).append("").append(list).toString());
            }
        }
    }

    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    private void addToCSV(String filename, String data) {
        String s2 = (new StringBuilder()).append(filename).append(".txt").toString();
        File file = new File(sdcard, s2);
        try {
            FileOutputStream fileoutputstream = new FileOutputStream(file, true);
            OutputStreamWriter outputstreamwriter = new OutputStreamWriter(fileoutputstream);
            outputstreamwriter.append((new StringBuilder()).append(data).append("\n").toString());
            outputstreamwriter.flush();
            outputstreamwriter.close();
            fileoutputstream.flush();
            fileoutputstream.close();
            System.gc();
            Log.d("Inserted", (new StringBuilder()).append(data).append("").toString());
            return;
        } 
        catch (IOException ioexception) 
        {
            ioexception.printStackTrace();
        }
    }

    private void createCSV(String filename, double g)
    {
        String s1 = (new StringBuilder()).append(filename).append(".txt").toString();
        File file = new File(sdcard, s1);
        Log.d("File Creation", String.valueOf(System.currentTimeMillis()));
        try {
            file.createNewFile();
            FileOutputStream fileoutputstream = new FileOutputStream(file, true);
            OutputStreamWriter outputstreamwriter = new OutputStreamWriter(fileoutputstream);
            outputstreamwriter.append((new StringBuilder()).append(filename).append("\n").toString());
            outputstreamwriter.append((new StringBuilder()).append("G-Force: ").append(g).append("\n").toString());
            outputstreamwriter.close();
            fileoutputstream.close();
        }
        catch (IOException ioexception)
        {
            ioexception.printStackTrace();
        }
        Log.d("Created File at", String.valueOf(System.currentTimeMillis()));
    }

    static
    {
        sdcard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
    }
}
