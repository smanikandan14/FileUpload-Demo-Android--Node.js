package com.mani.fileupload;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class AudioRecord extends Activity
{
    private static final String LOG_TAG = "AudioRecordTest";
    private MediaRecorder mRecorder = null;
    private Button startRecording;
    private Button stopRecording;
    private TextView timerText;
    private SeekBar mSeekBar;
    private String mCurrentfilePath;
    
    private boolean isRecording = false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audio_record_layout);
        
        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        startRecording = (Button) findViewById(R.id.record);
        stopRecording = (Button) findViewById(R.id.stop);
        timerText = (TextView) findViewById(R.id.timetext);
        
        startRecording.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if( !isRecording ) {
					isRecording = true;
					startRecording();
					startTime = System.currentTimeMillis();
					startTimer();
					startSeekBarTimer();
					startRecording.setEnabled(false);
				}
			}
		});
        
        stopRecording.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(isRecording) {
					stopRecording();
					isRecording = false;
					Intent intent = new Intent();
					Uri uri = Uri.parse(mCurrentfilePath);
					intent.setData(uri);
					setResult(Activity.RESULT_OK,intent);
					finish();
				}
			}
		});
    }

    private void startRecording() {
    	try {
	        mRecorder = new MediaRecorder();
	        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
	        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
	        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
	        mCurrentfilePath = createAudioFile().getAbsolutePath();
	        mRecorder.setOutputFile(mCurrentfilePath);	        
            mRecorder.prepare();
            mRecorder.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }
    }
    
	  private File createAudioFile() throws IOException {
	        boolean externalStorageAvailable = false;
	        boolean externalStorageWriteable = false;
	        File root = null;
	        File tempFile = null;
	        try {
	            String state = Environment.getExternalStorageState();
	            if (Environment.MEDIA_MOUNTED.equals(state)) {
	                externalStorageAvailable = externalStorageWriteable = true;
	            } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	                externalStorageAvailable = true;
	                externalStorageWriteable = false;
	            } else {
	                externalStorageAvailable = externalStorageWriteable = false;
	            }

	            if (externalStorageAvailable && externalStorageWriteable) {
	                root = new File(Environment.getExternalStorageDirectory(), "mani");
	                if (!root.exists()) {
	                    root.mkdirs();
	                }
	    		    // Create an image file name
	    		    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date());
	    		    String imageFileName = "Audio_" + timeStamp + "_";
	    		    tempFile = File.createTempFile(imageFileName,".3gp",root); 
	             }
	            } catch (Exception e) {
	            	e.printStackTrace();
	            }
		    return tempFile;
	   }
	  
	    private void startTimer() {
	    	CountDownTimer timer = new SecondsTimer(60000, 1000L);
	    	timer.start();
	    }
	    
	    int timerCount = 0;
	    long startTime;
	    
	    private class SecondsTimer extends CountDownTimer {
	        public SecondsTimer(long millisInFuture, long countDownInterval) {
	            super(millisInFuture, countDownInterval);
	        }

	        public void onFinish() {
	        	startTimer();
	        }

	        public void onTick(long millisUntilFinished) {
	        	long millis = System.currentTimeMillis() - startTime;
	            int seconds = (int) (millis / 1000);
	            int minutes = seconds / 60;
	            seconds     = seconds % 60;

	            timerText.setText(String.format("%d:%02d", minutes, seconds));
	        }
	    }

	    private void startSeekBarTimer() {
	    	CountDownTimer timer = new SeekBarTimer(60000, 100L);
	    	timer.start();
	    }
	    
	    int seekProgress = 0;
	    private class SeekBarTimer extends CountDownTimer {
	        public SeekBarTimer(long millisInFuture, long countDownInterval) {
	            super(millisInFuture, countDownInterval);
	        }

	        public void onFinish() {
	        	startSeekBarTimer();
	        }

	        public void onTick(long millisUntilFinished) {
	        	if(seekProgress % 100 == 0) {
	        		seekProgress = 0;
	        	}
	        	seekProgress++;
	        	mSeekBar.setProgress(seekProgress);
	        }
	    }

}