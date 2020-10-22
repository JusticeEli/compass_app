package com.justice.compassapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final String TAG = "MainActivity";
    //compass is updated after every 250 seconds
    public static final int UPDATE_COMPASS_AFTER_EVERY = 250;
    public static final int ANIMATION_DURATION = 250;

    private TextView textView;
    private ImageView imageView;
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private Sensor magnetometerSensor;
    private float[] lastAccelerometer = new float[3];
    private float[] lastMagnetometer = new float[3];
    private float[] rotationMatrix = new float[9];
    private float[] orientation = new float[3];

    boolean isLastAccelerometerArrayCopied = false;
    boolean isLastMagnetometerArrayCopied = false;
    long lastUpdatedTime = 0;
    float currentDegrees = 0f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initWidgets();
        initSensors();
        checkIfTheDeviceContains_Sensors();
    }

    private void checkIfTheDeviceContains_Sensors() {
        if (accelerometerSensor == null) {
            Log.d(TAG, "checkIfTheDeviceContains_Sensors: device does not contain accelerometer sensor");
            Toast.makeText(this, "This device does not contain an Acceleremeter", Toast.LENGTH_LONG).show();
            finish();
        }
        if (magnetometerSensor == null) {
            Log.d(TAG, "checkIfTheDeviceContains_Sensors: device does not contain magnetometer sensor");
            Toast.makeText(this, "This device does not contain a Magnetometer", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initSensors() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);


    }

    private void initWidgets() {
        textView = findViewById(R.id.textView);
        imageView = findViewById(R.id.imageView);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        Log.d(TAG, "onSensorChanged: ");
        if (event.sensor == accelerometerSensor) {
            System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.length);
            isLastAccelerometerArrayCopied = true;
            Log.d(TAG, "onSensorChanged: isLastAccelerometerArrayCopied true");
        } else if (event.sensor == magnetometerSensor) {
            System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.length);
            isLastMagnetometerArrayCopied = true;
            Log.d(TAG, "onSensorChanged: isLastMagnetometerArrayCopied true");
        }

        if (isLastAccelerometerArrayCopied && isLastMagnetometerArrayCopied && System.currentTimeMillis() - lastUpdatedTime > UPDATE_COMPASS_AFTER_EVERY) {
            Log.d(TAG, "onSensorChanged: all conditions for updating compass are true");

            SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer);
            SensorManager.getOrientation(rotationMatrix, orientation);
            float azimuthInRadian = orientation[0];

            float azimuthInDegrees = (float) Math.toDegrees(azimuthInRadian);
            RotateAnimation rotateAnimation = new RotateAnimation(currentDegrees, -azimuthInDegrees, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setDuration(ANIMATION_DURATION);
            rotateAnimation.setFillAfter(true);
            imageView.startAnimation(rotateAnimation);
            currentDegrees = -azimuthInDegrees;
            lastUpdatedTime = System.currentTimeMillis();

            int degrees = (int) azimuthInDegrees;
            textView.setText(degrees + "°");
            Log.d(TAG, "onSensorChanged: " + degrees + "°");
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magnetometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this, accelerometerSensor);
        sensorManager.unregisterListener(this, magnetometerSensor);
    }
}
