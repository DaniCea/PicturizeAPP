package com.example.picturize;

import java.util.Arrays;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;

public class MainFragment extends Fragment {
	
	private static final String TAG = "MainFragment";
	private UiLifecycleHelper uiHelper;
	private TextView userInfoTextView;
	private Button enterButton;

	private String buildUserInfoDisplay(GraphUser user) {
	    StringBuilder userInfo = new StringBuilder("");

	    userInfo.append(String.format("Welcome %s\n\n", 
	        user.getName()));

	    return userInfo.toString();
	}
	
	public View onCreateView(LayoutInflater inflater, 
	        ViewGroup container, 
	        Bundle savedInstanceState) {
	    View view = inflater.inflate(R.layout.main, container, false);

	    LoginButton authButton = (LoginButton) view.findViewById(R.id.authButton);
	    authButton.setFragment(this);
	    authButton.setReadPermissions(Arrays.asList("user_location", "user_photos", "user_likes", "photo_upload"));
	    userInfoTextView = (TextView) view.findViewById(R.id.userInfoTextView);
	    enterButton = (Button) view.findViewById(R.id.enterButton);
	    
	    return view;
	}
	
	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
		Log.d(TAG, state.toString());
	    if (state.isOpened()) {
	        Log.i(TAG, "Logged in...");
	        userInfoTextView.setVisibility(View.VISIBLE);
	        enterButton.setVisibility(View.VISIBLE);
	        // Request user data and show the results
	        Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {

	            @Override
	            public void onCompleted(GraphUser user, Response response) {
	                if (user != null) {
	                    userInfoTextView.setText(buildUserInfoDisplay(user));
	                }
	            }
	        });
	        
	    } else if (state.isClosed()) {
	        Log.i(TAG, "Logged out...");
	        userInfoTextView.setVisibility(View.INVISIBLE);
	        enterButton.setVisibility(View.INVISIBLE);
	    }
	}
	
	private Session.StatusCallback callback = new Session.StatusCallback() {
	    @Override
	    public void call(Session session, SessionState state, Exception exception) {
	        onSessionStateChange(session, state, exception);
	    }
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    uiHelper = new UiLifecycleHelper(getActivity(), callback);
	    uiHelper.onCreate(savedInstanceState);
	}
	
	@Override
	public void onResume() {
	    super.onResume();
	    
	    Session session = Session.getActiveSession();
	    if (session != null &&
	           (session.isOpened() || session.isClosed()) ) {
	        onSessionStateChange(session, session.getState(), null);
	    }
	    
	    uiHelper.onResume();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onPause() {
	    super.onPause();
	    uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
	    super.onDestroy();
	    uiHelper.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    uiHelper.onSaveInstanceState(outState);
	}
}


