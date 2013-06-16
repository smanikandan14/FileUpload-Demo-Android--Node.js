package com.mani.fileupload;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.mani.fileupload.http.EasySSLSocketFactory;


public class MainActivity extends Activity {


	  String uploadurl = "https://192.168.1.106:4000/upload";
	  //String uploadurl = "http://ec2-54-242-153-20.compute-1.amazonaws.com:4000/upload";
	  ProgressDialog mProgress;
	  RelativeLayout takeImage;
	  RelativeLayout takeVideo;
	  RelativeLayout takeAudio;
	  ImageView imageTaken;
	  private static TelephonyManager telephonyManager;
	  private static Map<String,String> countryISOMap = new HashMap<String,String>();
	    
	  final int TAKE_CAMERA_PIC_CODE = 100;
	  final int TAKE_CAMERA_VIDEO_CODE = 101;
	  final int RECORD_AUDIO_CODE = 102;
	  
	  private String currentPath = null;
	  @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        this.setContentView(R.layout.main_layout);
	        mProgress = null;
	        
	        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
	        
	        takeImage = (RelativeLayout) findViewById(R.id.take_picture);
	        takeVideo = (RelativeLayout) findViewById(R.id.take_video);
	        takeAudio = (RelativeLayout) findViewById(R.id.take_audio);
	        
	        imageTaken = (ImageView) findViewById(R.id.image);
	        
	        BitmapFactory.Options options;
	        try {
	          options = new BitmapFactory.Options();
	          options.inSampleSize = 2;
	  		  File file = new File ("/mnt/sdcard/mani/Mani.jpg");
			  Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(),options);
			  System.out.println("####### Bitmap ####### "+bitmap);
			  imageTaken.setImageBitmap(bitmap);
	        } catch(Exception e) {
	            e.printStackTrace();
	        }

	        takeImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					showCamera();
				}
			});
	        
	        takeVideo.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					showCameraVideo();
				}
			}); 

	        takeAudio.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					recordAudio();
				}
			}); 

	        BufferedReader br = null;
	        try {
	        	InputStream is = this.getResources().openRawResource(R.raw.iso);
	            InputStreamReader isr = new InputStreamReader(is);	        	
	            br = new BufferedReader(isr);
	            String line = br.readLine();
	            while (line != null) {
	                line = br.readLine();
	                String[] split = line.split("     ");
	                countryISOMap.put(split[0], split[1]);
	            }
	            br.close();
	        } catch (Exception e) {
	        	e.printStackTrace();
	        } finally {
	        }
	    }
	  
	  @Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
			// TODO Auto-generated method stub
			super.onActivityResult(requestCode, resultCode, data);
			if( resultCode == Activity.RESULT_OK) {
				if(requestCode == TAKE_CAMERA_PIC_CODE) {
					System.out.println("####### onActivityResult ####### "+data);
					handleCameraPhoto();
				} else if(requestCode == TAKE_CAMERA_VIDEO_CODE) {
					System.out.println("####### onActivityResult ####### "+data);
					handleCameraVideo(data);
				} else if(requestCode == RECORD_AUDIO_CODE) {
					System.out.println("####### onActivityResult ####### "+data);
					handleAudio(data);
				}				
			}
		}
	  
	  private void handleCameraPhoto() {
		  //Load picture from sdcard 
		  try {
			  File file = new File (currentPath);
			  System.out.println("####### handleSmallCameraPhoto ####### "+file.getAbsolutePath());
			  BitmapFactory.Options options = new BitmapFactory.Options();
			  options.inSampleSize = 2;
			  Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(),options);
			  System.out.println("####### handleSmallCameraPhoto ####### "+bitmap);
			  imageTaken.setImageBitmap(bitmap);
			  new MyTask("image").execute(file);
		  } catch (Exception e) {
			  e.printStackTrace();
		  }
	  }
	  
	  private void handleCameraVideo(Intent intent) {
		  System.out.println("####### handleCameraVideo ####### "+currentPath);
		  File file = new File (currentPath);
		  new MyTask("video").execute(file);
	  }

	  private void handleAudio(Intent intent) {
		  System.out.println("####### handleAudio ####### "+intent.getData().toString());
		  File file = new File (intent.getData().toString());
		  new MyTask("audio").execute(file);
	  }

	  private void showCamera() {
		  try {
			  Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			  File f = createImageFile();
			  currentPath = f.getAbsolutePath();
			  takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
			  startActivityForResult(takePictureIntent, TAKE_CAMERA_PIC_CODE);
		  } catch (Exception e) {
			  e.printStackTrace();
		  }
	  }
	  
	  private void showCameraVideo() {
		  try {
			  Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
			  File f = createVideoFile();
			  currentPath = f.getAbsolutePath();
			  takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
			  startActivityForResult(takeVideoIntent, TAKE_CAMERA_VIDEO_CODE);
		  } catch (Exception e) {
			  e.printStackTrace();
		  }
	  }
	  
	  private void recordAudio() {
		  Intent recordAudioIntent = new Intent(this,AudioRecord.class);
		  startActivityForResult(recordAudioIntent, RECORD_AUDIO_CODE);
	  }
	  
	  private File createImageFile() throws IOException {
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
	    		    String imageFileName = "Image_" + timeStamp + "_";
	    		    tempFile = File.createTempFile(imageFileName,".jpg",root); 
	             }
	            } catch (Exception e) {
	            	e.printStackTrace();
	            }
		    return tempFile;
	   }

	  private File createVideoFile() throws IOException {
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
	    		    String imageFileName = "Video_" + timeStamp + "_";
	    		    tempFile = File.createTempFile(imageFileName,".mp4",root); 
	             }
	            } catch (Exception e) {
	            	e.printStackTrace();
	            }
		    return tempFile;
	   }

	  class MyTask extends AsyncTask<File,Void,Void> {

		String type;
		public MyTask(String type) {
			this.type = type;
		}
  		@Override
        protected void onPreExecute()  {
			super.onPreExecute();
			mProgress = ProgressDialog.show(MainActivity.this, "", "Uploading...");
        }
		@Override
		protected Void doInBackground(File... params) {
			// TODO Auto-generated method stub
			try {
				System.out.println("@######## Http starting #########  "+params[0].getAbsolutePath() );
				uploadFile(type,params[0]);
			} catch ( Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		@Override
        protected void onPostExecute( Void result ) { 
            super.onPostExecute(result);
            System.out.println("@######## Http onPostExecute##########  " );
            mProgress.dismiss();
        }		  
	  }
	  
	  public void uploadFile(String type,File file) {

	        try {
	        	HttpParams httpParams = new BasicHttpParams();
	        	httpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
	            
	            SchemeRegistry registry = new SchemeRegistry();
	            registry.register(new Scheme("http", new PlainSocketFactory(), 80));
	            registry.register(new Scheme("https", new EasySSLSocketFactory(), 443));
	            
	            //http://download.java.net/jdk8/docs/technotes/guides/security/jsse/JSSERefGuide.html
	            
	            /* Make a thread safe connection manager for the client */
	            ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(httpParams, registry);
	            HttpClient httpClient = new DefaultHttpClient(manager, httpParams); 

	            
	            HttpPost httppost = new HttpPost(uploadurl);
	            MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);  
	            multipartEntity.addPart("upload", new FileBody(file));
	            multipartEntity.addPart("type", new StringBody(type));
	            multipartEntity.addPart("name", new StringBody(getUsername()));
	            multipartEntity.addPart("device", new StringBody(getBrand()+", "+getModelName()));
	            multipartEntity.addPart("location", new StringBody(getCountry()));
	            httppost.setEntity(multipartEntity);
	            httpClient.execute(httppost);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	  
	  public String getUsername(){
		    AccountManager manager = AccountManager.get(this); 
		    Account[] accounts = manager.getAccountsByType("com.google"); 
		    List<String> possibleEmails = new LinkedList<String>();

		    for (Account account : accounts) {
		      // TODO: Check possibleEmail against an email regex or treat
		      // account.name as an email address only for certain account.type values.
		      possibleEmails.add(account.name);
		    }

		    if(!possibleEmails.isEmpty() && possibleEmails.get(0) != null){
		        String email = possibleEmails.get(0);
		        String[] parts = email.split("@");
		        if(parts.length > 0 && parts[0] != null)
		            return parts[0];
		        else
		            return "undefined";
		    }else
		        return "undefined";
		}
	  
	    public static String getCountry() {
	        String country;
	        try {
	            country = telephonyManager.getNetworkCountryIso();
	            country = (country == null || "".equals(country)) ? "Unknown" : country;
	            if(!country.equals("Unknown")) {
	            	country = countryISOMap.get(country);
	            }
	        } catch (Exception exception) {
	            //Log.e(DEBUG_TAG, exception);
	        	exception.printStackTrace();
	            country = "Unknown";
	        }
	        return country;
	    }
	    
	    public static String getBrand() {
	        String brand;
	        try {
	            brand = android.os.Build.BRAND;
	        } catch (Exception exception) {
	            //Log.e(DEBUG_TAG, exception);
	        	exception.printStackTrace();
	            brand = "Unknown";
	        }

	        return brand;
	    }
	    
	    public static String getModelName() {
	        String modelName;
	        try {
	            modelName = android.os.Build.MODEL;
	        } catch (Exception exception) {
	            //Log.e(DEBUG_TAG, exception);
	        	exception.printStackTrace();
	            modelName = "Unknown";
	        }

	        return modelName;
	    }
	    
}
