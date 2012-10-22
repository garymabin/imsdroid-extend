package org.doubango.imsdroid.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import android.R.integer;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Environment;
import android.renderscript.Int2;
import android.text.format.Time;
import android.util.Log;

/**
 * A wrapper class for MediaRecorder
 * 
 * @author mabin
 * 
 */
public class VideoRecorder {
	private final static String TAG = "VideoRecorder";
	private MediaRecorder mInternalRecorder = null;
	private static final String DEFAULT_PATH = "/recorder_videos/";

	private double mCaptureRate = 0;
	private int mAudioEncoder = MediaRecorder.AudioEncoder.AMR_NB;
	private int mOutputFormat = MediaRecorder.OutputFormat.MPEG_4;
	private int mAudioSource = MediaRecorder.AudioSource.MIC;
	private int mVideoSource = MediaRecorder.VideoSource.CAMERA;
	private int mVideoEncoder = MediaRecorder.VideoEncoder.H264;
	private String mOutputFile = "";
	private String mMediaDirPath = DEFAULT_PATH;
	private Camera mCamera;

	private boolean isRecording = false;

	public enum CaptureRateLevel {
		LOW, MEDIUM, HIGH,
	}

	public VideoRecorder() {
		// TODO Auto-generated constructor stub
		mInternalRecorder = new MediaRecorder();
	}

	public VideoRecorder setCamera(Camera camera) {
		mCamera = camera;
		return this;
	}

	public VideoRecorder setAudioEncoder(int encoder) {
		mAudioEncoder = encoder;
		return this;
	}

	public VideoRecorder setOutputFormat(int outputFormat) {
		mOutputFormat = outputFormat;
		return this;
	}

	public VideoRecorder setAudioSource(int source) {
		mAudioSource = source;
		return this;
	}

	public VideoRecorder setVideoSource(int source) {
		mVideoSource = source;
		return this;
	}

	public VideoRecorder setVideoEncoder(int encoder) {
		mVideoEncoder = encoder;
		return this;
	}

	public VideoRecorder setCapureLevel(int level) {
		if (mCamera != null) {
			int fpsRange [] = new int[2];
			mCamera.getParameters().getPreviewFpsRange(fpsRange);
			mCaptureRate = 
					(fpsRange[1] - fpsRange[0]) * level / 100.0 + fpsRange[0];
		}
		return this;
	}

	public VideoRecorder setMediaDir(String path) throws IOException {
		mMediaDirPath = path;
		File mediaDir = new File(Environment.getExternalStorageDirectory()
				.toString() + path);
		if (!mediaDir.exists()) {
			mediaDir.mkdir();
		}
		if (!mediaDir.isDirectory()) {
			throw new FileNotFoundException("invalid path");
		}
		return this;
	}

	public String getLastRecordFileName() {
		return mOutputFile;
	}

	private void initRecorder() {
		if (mCaptureRate > 0  && mCaptureRate < 1) {
			mInternalRecorder.setCaptureRate(mCaptureRate);
		}
		mInternalRecorder.setAudioSource(mAudioSource);
		mInternalRecorder.setVideoSource(mVideoSource);
		mInternalRecorder.setOutputFormat(mOutputFormat);
		mInternalRecorder.setAudioEncoder(mAudioEncoder);
		mInternalRecorder.setVideoEncoder(mVideoEncoder);
		Time currentTime = new Time();
		currentTime.setToNow();
		StringBuilder sb = new StringBuilder()
				.append(Environment.getExternalStorageDirectory().toString())
				.append(mMediaDirPath)
				.append(currentTime.format2445())
				.append(mOutputFormat == MediaRecorder.OutputFormat.THREE_GPP ? ".3gp"
						: ".mp4");
		mOutputFile = sb.toString();
		mInternalRecorder.setOutputFile(mOutputFile);
	}

	public void start() {
		synchronized (this) {
			if (!isRecording) {
				isRecording = true;
				mCamera.unlock();
				mInternalRecorder.setCamera(mCamera);

				initRecorder();

				try {
					mInternalRecorder.prepare();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					isRecording = false;
					return;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					isRecording = false;
					e.printStackTrace();
					return;
				}
				mInternalRecorder.start();
			}
		}
	}

	public void stop() {
		synchronized (this) {
			if (isRecording) {
				mInternalRecorder.stop();
				isRecording = false;
			}
		}
	}
}
