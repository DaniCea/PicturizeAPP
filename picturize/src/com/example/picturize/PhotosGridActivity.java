package com.example.picturize;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;

public class PhotosGridActivity extends Activity {
	
	private String albumID;
	private String graphPath;
	private JSONArray photoList;
	private Activity a;
	private Session session; 
	private int IMAGE_MAX_SIZE = 1024;
	
	ArrayList<getPhotos> arrPhotos;

	private static final int CAMERA_REQUEST = 0;
	
	private void createActionBar(Menu menu) {
		MenuItem item1 = menu.add(0, 0, 0, "Item 1");
		{
			item1.setIcon(R.drawable.android_device_access_camera);
			item1.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		}
	}
	
	private boolean selectedItemActionBar(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			
			String state = Environment.getExternalStorageState();
			File photo = null;
			if (Environment.MEDIA_MOUNTED.equals(state))  {
				photo = new File(Environment.getExternalStorageDirectory(), "Pic.jpg");
			}
			else {
				photo = new File(Environment.getDataDirectory(), "Pic.jpg");
			}
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
			startActivityForResult(intent, CAMERA_REQUEST);
			return true;
		}
		return false;
	}
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.photos);
	    
	    getActionBar().setDisplayHomeAsUpEnabled(true);
	    
	    a = this;
	    arrPhotos = new ArrayList<getPhotos>();
	    
	    albumID = getIntent().getStringExtra("albumId");
	    Log.d("AlbumID", albumID);
	    
	    graphPath = "/" + albumID + "/photos";
	    
		session = Session.getActiveSession();
		//Log.d("Session", session.getAccessToken());
		
		final Request albumRequest = Request.newGraphPathRequest(session, graphPath, new Request.Callback() {

			@Override
			public void onCompleted(Response response) {
				GraphObject graphObject = response.getGraphObject();
				if (graphObject != null) {
					JSONObject jsonObject = graphObject.getInnerJSONObject();
                    try {
                    	photoList = jsonObject.getJSONArray("data");
                    	getPhotos photos;
                    	for (int i = 0; i < photoList.length(); i++) {
                    		JSONObject object = photoList.getJSONObject(i);
                    		Log.d("BajandoCalidad", object.toString());
                    		photos = new getPhotos();
                    		if (object.has("id")) {
                    			photos.setPhotoID(object.getString("id"));
                    		} else {
                    			photos.setPhotoID(null);
                    		}
                    		
                    		if (object.has("name")) {
                    			photos.setPhotoName(object.getString("name"));
                    		} else {
                    			photos.setPhotoName(null);
                    		}
                    		
                    		if (object.has("picture")) {
                    			photos.setPhotoPicture(object.getString("picture"));
                    		} else {
                    			photos.setPhotoPicture(null);
                    		}
                    		
                    		if (object.has("source")) {
                    			photos.setPhotoSource(object.getString("source"));
                    		} else {
                    			photos.setPhotoSource(null);
                    		}
                    		arrPhotos.add(photos);
                    	}
                    } catch (JSONException e) {
                    	e.printStackTrace();
                    }
				}
				Log.d("ARRPHOTOS", arrPhotos.toString());
			    GridView gridview = (GridView) findViewById(R.id.photosgridview);
			    for (int i = 0; i < arrPhotos.size(); ++i) {
			    	Log.d("ARRSOURCE", arrPhotos.get(i).getPhotoSource());
			    }
			    gridview.setAdapter(new PhotosAdapter(a, arrPhotos, getApplicationContext()));
			    gridview.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
						Intent intent = new Intent(a.getBaseContext(), PhotoActivity.class);
						intent.putExtra("source", arrPhotos.get(position).getPhotoSource());
						startActivity(intent);
					}
			    });
			}
		});
		albumRequest.executeAsync();
	}
	
	public void onResume(Bundle savedInstanceState) {

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
			 File f = null;
			 String state = Environment.getExternalStorageState();
			 if (Environment.MEDIA_MOUNTED.equals(state)) 
				 f = new File(Environment.getExternalStorageDirectory() + "/pic.jpg");
			 else
				 f = new File(Environment.getDataDirectory() + "/pic.jpg");
			 Bitmap bi = decodeFile(f);
			 Log.d("SUBIDA", "HORA DE SUBIR LA FOTO");
			 final RequestAsyncTask uploadRequest = Request.executeUploadPhotoRequestAsync(session, bi, new Request.Callback() {
				@Override
				public void onCompleted(Response response) {
					Log.d("SUBIDA", "FOTO SUBIDA");
				}				 
			 });

			try {
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
