package com.valency.sensormonitor;

import com.valency.sensormonitor.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.BitmapFactory.Options;
import android.graphics.Paint;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.DisplayMetrics;
import android.view.View;

public class SensorMonitorActivity extends Activity {

	private SimulationView mSimulationView;
	private SensorManager mSensorManager;
	private PowerManager mPowerManager;
	private WakeLock mWakeLock;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
		mWakeLock = mPowerManager.newWakeLock(
				PowerManager.SCREEN_BRIGHT_WAKE_LOCK, getClass().getName());
		mSimulationView = new SimulationView(this);
		setContentView(mSimulationView);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mWakeLock.acquire();
		mSimulationView.startSimulation();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mSimulationView.stopSimulation();
		mWakeLock.release();
	}

	class SimulationView extends View implements SensorEventListener {
		private Sensor mAccelerometer;
		private Sensor mGyroscope;

		private Bitmap mWood;
		private float mAccX;
		private float mAccY;
		private float mAccZ;
		private float mGyrX;
		private float mGyrY;
		private float mGyrZ;
		private long mSensorTimeStamp;
		private long mCpuTimeStamp;

		public void startSimulation() {
			mSensorManager.registerListener(this, mAccelerometer,
					SensorManager.SENSOR_DELAY_UI);
			mSensorManager.registerListener(this, mGyroscope,
					SensorManager.SENSOR_DELAY_UI);
		}

		public void stopSimulation() {
			mSensorManager.unregisterListener(this);
		}

		public SimulationView(Context context) {
			super(context);
			mAccelerometer = mSensorManager
					.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);

			Options opts = new Options();
			opts.inDither = true;
			opts.inPreferredConfig = Bitmap.Config.RGB_565;
			mWood = BitmapFactory.decodeResource(getResources(),
					R.drawable.wood, opts);
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		}

		public void onSensorChanged(SensorEvent event) {
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				mAccX = event.values[0];
				mAccY = event.values[1];
				mAccZ = event.values[2];
			} else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
				mGyrX = event.values[0];
				mGyrY = event.values[1];
				mGyrZ = event.values[2];
			}
			mSensorTimeStamp = event.timestamp;
			mCpuTimeStamp = System.nanoTime();
		}

		@Override
		protected void onDraw(Canvas canvas) {
			canvas.drawBitmap(mWood, 0, 0, null);

			final long now = mSensorTimeStamp
					+ (System.nanoTime() - mCpuTimeStamp);

			Paint paint = new Paint();
			paint.setColor(Color.WHITE);
			paint.setTextSize(24.0f);

			canvas.drawText("TIME: " + now, 10.0f, 35.0f, paint);

			canvas.drawText("ACCELEROMETER X: " + mAccX, 10.0f, 70.0f, paint);
			canvas.drawText("ACCELEROMETER Y: " + mAccY, 10.0f, 105.0f, paint);
			canvas.drawText("ACCELEROMETER Z: " + mAccZ, 10.0f, 140.0f, paint);

			canvas.drawText("GYROSCOPE X: " + mGyrX, 10.0f, 175.0f, paint);
			canvas.drawText("GYROSCOPE Y: " + mGyrY, 10.0f, 210.0f, paint);
			canvas.drawText("GYROSCOPE Z: " + mGyrZ, 10.0f, 245.0f, paint);

			invalidate();
		}

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	}
}
