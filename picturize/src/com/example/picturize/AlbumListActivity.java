package com.example.picturize;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;

public class AlbumListActivity extends ListActivity {

    SimpleCursorAdapter mAdapter;
    private String graphPath = "/me/albums";
    private JSONArray albumList;
	private int IMAGE_MAX_SIZE = 1024;
    
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
		
		Session session = Session.getActiveSession();
		Log.d("Session", session.getAccessToken());
	
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		final Context context = this.getApplicationContext();
		
		final Request albumRequest = Request.newGraphPathRequest(session, graphPath, new Request.Callback() {

			@Override
			public void onCompleted(Response response) {
				ArrayList<String> array = new ArrayList<String>();
				Log.d("AlbumRequest", response.toString());
				GraphObject graphObject = response.getGraphObject();
				if (graphObject != null) {
					JSONObject jsonObject = graphObject.getInnerJSONObject();
                    try {
                    	albumList = jsonObject.getJSONArray("data");
                    	for (int i = 0; i < albumList.length(); i++) {
                    		JSONObject object = (JSONObject) albumList.get(i);
                    		array.add((String) object.get("name"));
                    		Log.d("JSON", "name = "+object.get("name"));
                    	}
                    } catch (JSONException e) {
                    	e.printStackTrace();
                    }
				}
				setListAdapter(new ArrayAdapter<String>(context, R.layout.albumtext, array));
			}
		});

		albumRequest.executeAsync();
	}
	
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent intent = new Intent(this.getBaseContext(), PhotosGridActivity.class);
		JSONObject object;
		try {
			object = (JSONObject) albumList.get(position);
			intent.putExtra("albumId", object.get("id").toString());
			startActivity(intent);
		} catch (JSONException e) {
			e.printStackTrace();
		}
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
			 final RequestAsyncTask uploadRequest = Request.executeUploadPhotoRequestAsync(Session.getActiveSession(), bi, new Request.Callback() {
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
