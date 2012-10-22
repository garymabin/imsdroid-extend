package org.doubango.imsdroid.Screens;

import java.io.IOException;

import javax.security.auth.PrivateCredentialPermission;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.Utils.VideoRecorder;
import org.doubango.imsdroid.Engine;
import org.doubango.ngn.services.INgnConfigurationService;

import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class ScreenVideoRecorder extends Activity implements
		Camera.PreviewCallback, OnClickListener, SurfaceHolder.Callback {

	public ScreenVideoRecorder() {
		// super(SCREEN_TYPE.VIDEO_RECORDER_T, TAG);
		// TODO Auto-generated constructor stub
	}

	/* package */static final String VIDEO_RECORDER_FPS = "org.doubango.imsdroid.Screens.ScreenVideoRecorder.fps";
	/* package */static final String VIDEO_RECORDER_MEDIA_DIR = "org.doubango.imsdroid.Screens.ScreenVideoRecorder.mediadir";
	/* package */static final String VIDEO_RECORDER_FORMAT = "org.doubango.imsdroid.Screens.ScreenVideoRecorder.fps";
	/* package */static final String VIDEO_RECORDER_ENCODING = "org.doubango.imsdroid.Screens.ScreenVideoRecorder.encoding";

	private static final String TAG = ScreenVideoRecorder.class
			.getCanonicalName();;
	private Camera mCamera = null;
	private Button mStartButton;
	private Button mStopButton;
	private boolean isCameraReady = false;
	private boolean hasRecordingStared = false;
	private VideoRecorder mRecorder;
	private SurfaceHolder mSurfaceHolder = null;

	private INgnConfigurationService mConfigurationService;

	private VideoRecorderParameters mParameters;

	private boolean mIsPreviewing = false;
	private boolean mOnResumePending = false;
	private int mOrientation;

	private class PreviewThread extends Thread {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			mCamera = Camera.open();
			mCamera.setOneShotPreviewCallback(ScreenVideoRecorder.this);
			mCamera.startPreview();
			mIsPreviewing = true;
		}
	};

	protected static class VideoRecorderParameters {
		public VideoRecorderParameters() {
			// TODO Auto-generated constructor stub
			mConfigurationService = ((Engine) Engine.getInstance())
					.getConfigurationService();
			update();
		}

		private INgnConfigurationService mConfigurationService;
		public int videoQualityLevel;
		public String mediaDir;
		public int format;
		public int videoEncoding;
		

		public void update() {
			videoQualityLevel = mConfigurationService.getInt(
					VIDEO_RECORDER_FPS, 20);
			mediaDir = mConfigurationService.getString(
					VIDEO_RECORDER_MEDIA_DIR, "/record_videos/");
			format = mConfigurationService.getInt(VIDEO_RECORDER_FORMAT,
					MediaRecorder.OutputFormat.MPEG_4);
			videoEncoding = mConfigurationService.getInt(
					VIDEO_RECORDER_ENCODING, MediaRecorder.VideoEncoder.H264);
		}

		public void commit() {
			mConfigurationService.putInt(VIDEO_RECORDER_FPS, videoQualityLevel);
			mConfigurationService.putString(VIDEO_RECORDER_MEDIA_DIR,mediaDir);
			mConfigurationService.putInt(VIDEO_RECORDER_FORMAT, format);
			mConfigurationService.putInt(VIDEO_RECORDER_ENCODING, videoEncoding);
			mConfigurationService.commit();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		PreviewThread previewThread = new PreviewThread();
		previewThread.start();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mOrientation = getRequestedOrientation();
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setContentView(R.layout.screen_video_recorder);
		mStartButton = (Button) findViewById(R.id.button_start);
		mStartButton.setOnClickListener(this);
		mStopButton = (Button) findViewById(R.id.button_stop);
		mStopButton.setOnClickListener(this);
		SurfaceView view = (SurfaceView) findViewById(R.id.surfaceView1);
		SurfaceHolder holder = view.getHolder();
		holder.addCallback(this);
		mRecorder = new VideoRecorder();
		mParameters = new VideoRecorderParameters();
		try {
			previewThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Log.i(TAG, "onDestroy");
		super.onDestroy();
		if (mOrientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
			setRequestedOrientation(mOrientation);
		}
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		Log.i(TAG, "onStart");
		super.onStart();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if (hasFocus && mOnResumePending) {
			doOnResume();
			mOnResumePending = false;
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		Log.i(TAG, "onResume()");
		super.onResume();
		if (mCamera == null && !hasWindowFocus() && isKeyguardLocked()) {
			Log.v(TAG, "onRsume. mOnResumePending=true");
			mOnResumePending = true;
		} else {
			Log.v(TAG, "onRsume. mOnResumePending=false");
			doOnResume();
			mOnResumePending = false;
		}
	}

	private void doOnResume() {
		mParameters.update();
		if (!mIsPreviewing) {
			mCamera = Camera.open();
			mCamera.startPreview();
			mIsPreviewing = true;
		}
		if (mIsPreviewing) {
			mCamera.setOneShotPreviewCallback(ScreenVideoRecorder.this);
			try {
				Log.i(TAG, "mMediaDirPath = " + mParameters.mediaDir
						+ " videoEncoding = " + mParameters.videoEncoding);
				mRecorder.setCamera(mCamera).setCapureLevel(100)
						.setMediaDir(mParameters.mediaDir)
						.setVideoEncoder(mParameters.videoEncoding)
						.setOutputFormat(mParameters.format);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onPause() {
		Log.i(TAG, "onPause()");
		// TODO Auto-generated method stub
		super.onPause();
		mOnResumePending = false;
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
			mIsPreviewing = false;
		}
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		stopRecoding();
	}

	@Override
	protected void onStop() {
		Log.d(TAG, "onStop()");
		super.onStop();
		stopRecoding();
	}

	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// // getMenuInflater().inflate(R.menu.activity_main, menu);
	// menu.add(0, 0, 0, R.string.recorder_settings);
	// return true;
	// }
	//
	// @Override
	// public boolean onOptionsItemSelected(MenuItem item) {
	// // TODO Auto-generated method stub
	// switch (item.getItemId()) {
	// case 0:
	// break;
	//
	// default:
	// break;
	// }
	// return super.onOptionsItemSelected(item);
	// }

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		// TODO Auto-generated method stub
		// this is an one-short call back for notifying camera is ready
		isCameraReady = true;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.button_start:
			handleRecoding();
			break;
		case R.id.button_stop:
			stopRecoding();
		default:
			break;
		}
	}

	private void handleRecoding() {
		// TODO Auto-generated method stub
		if (!isCameraReady) {
			Toast.makeText(this, "Camera is not ready yet!", Toast.LENGTH_SHORT)
					.show();
			Log.d(TAG, "Camera is not ready yet!");
			return;
		}
		synchronized (this) {
			if (!hasRecordingStared) {
				mRecorder.start();
				hasRecordingStared = true;
				mStartButton.setEnabled(false);
				mStopButton.setEnabled(true);
			}
		}
	}

	private void stopRecoding() {
		synchronized (this) {
			if (hasRecordingStared) {
				mRecorder.stop();
				hasRecordingStared = false;
				mStopButton.setEnabled(false);
				mStartButton.setEnabled(true);
				Toast.makeText(
						this,
						"last record file saved in "
								+ mRecorder.getLastRecordFileName(),
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		if (holder.getSurface() == null) {
			return;
		}

		mSurfaceHolder = holder;
		try {
			mCamera.setPreviewDisplay(mSurfaceHolder);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		mSurfaceHolder = null;
	}

	private boolean isKeyguardLocked() {
		KeyguardManager kgm = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
		// isKeyguardSecure excludes the slide lock case.
		return (kgm != null) && kgm.isKeyguardLocked()
				&& kgm.isKeyguardSecure();
	}
}
