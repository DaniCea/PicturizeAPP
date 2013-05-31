package com.example.picturize;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class PhotoActivity extends Activity {
	private static final int CAMERA_REQUEST = 0;
	private int IMAGE_MAX_SIZE = 1024;
	private String source = null;
	
	private void createActionBar(Menu menu) {
		MenuItem item1 = menu.add(0, 0, 0, "Item 1");
		{
			item1.setIcon(R.drawable.android_download);
			item1.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		}
		MenuItem item2 = menu.add(0, 1, 1, "Item 2");
		{
			item2.setIcon(R.drawable.android_device_access_camera);
			item2.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		}
	}
	
	private boolean selectedItemActionBar(MenuItem item) {
		if (item.getItemId() == 0) {
			Random rand = new Random();
			final int  n = rand.nextInt(1000) + 1;
			Toast.makeText(getApplicationContext(), "Image Downloaded at " + Environment.getExternalStorageDirectory() + "/myImage" + Integer.toString(n) + ".png", Toast.LENGTH_LONG).show();
			new Thread(new Runnable() {
		        public void run() {
					URL url;
					InputStream input = null;
					try {
						url = new URL (source);
						try {
							input = url.openStream();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					try {
					    //The sdcard directory e.g. '/sdcard' can be used directly, or 
					    //more safely abstracted with getExternalStorageDirectory()

					    File storagePath = Environment.getExternalStorageDirectory();
					    OutputStream output = null;
						try {
							output = new FileOutputStream (new File(storagePath,"myImage" + Integer.toString(n) + ".png"));
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					    try {
					        byte[] buffer = new byte[1024];
					        int bytesRead = 0;
					        try {
								while ((bytesRead = input.read(buffer, 0, buffer.length)) >= 0) {
								    output.write(buffer, 0, bytesRead);
								}
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
					    } finally {
					        try {
								output.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
					    }
					} finally {
					    try {
							input.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
		        }
			}).start();
		}
			
		else if (item.getItemId() == 1) {
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		    File photo =
		            new File(Environment.getExternalStorageDirectory(), "Pic.jpg");
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
			startActivityForResult(intent, CAMERA_REQUEST);
			return true;
		}
		return false;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.imagephoto);
	    
	    getActionBar().setDisplayHomeAsUpEnabled(true);
	    
	    Intent i = getIntent();
	    
	    source = (String) i.getExtras().get("source");
	   
        ScaleImageView imageView = (ScaleImageView) findViewById(R.id.photo);
        ImageLoader.getInstance().displayImage(source, imageView);
	}
	
	public boolean onCreateOptionsMenu (Menu menu) {
		super.onCreateOptionsMenu(menu);
		createActionBar(menu);
		return true;
	}
	
    public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	            finish();
	            return true;
	        default:
	            return selectedItemActionBar(item);
	    }
    }
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		 if ((requestCode == CAMERA_REQUEST) && (resultCode == Activity.RESULT_OK)) {
			 File f = new File(Environment.getExternalStorageDirectory() + "/pic.jpg");
			 Bitmap bi = decodeFile(f);
			 Log.d("SUBIDA", "HORA DE SUBIR LA FOTO");
			 final RequestAsyncTask uploadRequest = Request.executeUploadPhotoRequestAsync(Session.getActiveSession(), bi, new Request.Callback() {
				@Override
				public void onCompleted(Response response) {
					Log.d("SUBIDA", "FOTO SUBIDA");
					Toast.makeText(getApplicationContext(), "Photo has been uploaded to picturize album", Toast.LENGTH_LONG).show();
				}				 
			 });

			try {
				Toast.makeText(getApplicationContext(), "Wait while photo is uploaded...", Toast.LENGTH_LONG).show();
				uploadRequest.get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 }
	}
	
	 private Bitmap decodeFile(File f){
		    Bitmap b = null;

		        //Decode image size
		    BitmapFactory.Options o = new BitmapFactory.Options();
		    o.inJustDecodeBounds = true;

		    FileInputStream fis = null;
			try {
				fis = new FileInputStream(f);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    BitmapFactory.decodeStream(fis, null, o);
		    try {
				fis.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		    int scale = 1;
		    if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
		        scale = (int)Math.pow(2, (int) Math.round(Math.log(IMAGE_MAX_SIZE / 
		           (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
		    }

		    //Decode with inSampleSize
		    BitmapFactory.Options o2 = new BitmapFactory.Options();
		    o2.inSampleSize = scale;
		    try {
				fis = new FileInputStream(f);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    b = BitmapFactory.decodeStream(fis, null, o2);
		    try {
				fis.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		    return b;
		}
}