package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.Screens.ScreenVideoRecorder.VideoRecorderParameters;


import android.os.Bundle;
import android.widget.EditText;

public class ScreenRecorderSettings extends BaseScreen {
	private final static String TAG = ScreenRecorderSettings.class.getCanonicalName();
	
	private EditText mMediaDirText;
	private VideoRecorderParameters mPameters;
	
	public ScreenRecorderSettings() {
		super(SCREEN_TYPE.RECORDER_SETTINGS_T, TAG);
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_recorder_settings);
		
		mMediaDirText = (EditText)findViewById(R.id.screen_network_editText_pcscf_host);
		mPameters = new VideoRecorderParameters();
		mMediaDirText.setText(mPameters.mediaDir);
		super.addConfigurationListener(mMediaDirText);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mPameters.update();
		mMediaDirText.setText(mPameters.mediaDir);
		
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (mComputeConfiguration) {
			mPameters.mediaDir = mMediaDirText.getText().toString().trim();
			mPameters.commit();
		}
	}
}
