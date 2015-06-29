package com.orbital.cityguide;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginFragment extends Fragment {

	Button register;
	Button clickHere;
	Button login;
	private LoginButton loginFacebook;
	CallbackManager callbackManager;

	EditText userName;
	EditText pw;

	String title, alertboxmsg;
	int success;

	private static final String LOGIN_URL = "http://192.168.1.5/City_Guide/login.php";
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_MESSAGE = "message";

	// Your Facebook APP ID
	private static String APP_ID = "1599241106995105"; // Replace your App ID here

	// JSON parser class
	JSONParser jsonParser = new JSONParser();

	public LoginFragment() {
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_login, container,
				false);
		register = (Button) rootView.findViewById(R.id.register);
		login = (Button) rootView.findViewById(R.id.login);
		userName = (EditText) rootView.findViewById(R.id.username);
		pw = (EditText) rootView.findViewById(R.id.password);
		
		/*FacebookSdk.sdkInitialize(getActivity());
        callbackManager = CallbackManager.Factory.create();
        loginFacebook = (LoginButton) rootView.findViewById(R.id.FBlogin_button);
        loginFacebook.setReadPermissions(Arrays.asList("public_profile, email, user_birthday"));
        loginFacebook.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
	        @Override
	        public void onSuccess(LoginResult loginResult) {
	        	Toast.makeText(getActivity(),"Success",Toast.LENGTH_SHORT).show();
	        	GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(
                                    JSONObject object,
                                    GraphResponse response) {
                                // Application code
                                Log.v("LoginActivity", response.toString());
                                //String id = user.optString("id");
                                //String firstName = user.optString("first_name");
                                //String lastName = user.optString("last_name");
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender, birthday");
                request.setParameters(parameters);
                request.executeAsync();
	        }

	        @Override
	        public void onCancel() {
	        	Toast.makeText(getActivity(),"fail",Toast.LENGTH_SHORT).show();
	        }

	        @Override
	        public void onError(FacebookException exception) {
	        	Toast.makeText(getActivity(),"error",Toast.LENGTH_SHORT).show();
	        }
	    });    */
		
		setRetainInstance(true);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		init();
	}

	public void init() {
		
		final boolean result = isNetworkAvailable();

		register.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent nextActivity = new Intent(getActivity(), Register.class);
				startActivity(nextActivity);
			}
		});

		login.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				String username = userName.getText().toString();
				String password = pw.getText().toString();

				if (username.matches("") || password.matches("")) {
					title = "Error Message";
					alertboxmsg = "Required field(s) is missing.";
					popupMessage(title, alertboxmsg);
				} else if (result == true){
					try {
						List<NameValuePair> params = new ArrayList<NameValuePair>();
						params.add(new BasicNameValuePair("username", username));
						params.add(new BasicNameValuePair("password", password));

						StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
								.permitAll().build();
						StrictMode.setThreadPolicy(policy);

						JSONObject json = jsonParser.makeHttpRequest(LOGIN_URL,
								"POST", params);
						if (json != null) {
							success = json.getInt(TAG_SUCCESS);
							if (success == 1) {
								title = "Message";
								alertboxmsg = "Login successfully!";
								popupMessage(title, alertboxmsg);

								Intent nextActivity = new Intent(getActivity(),
										AfterLoginNavigationList.class);
								Bundle extras = new Bundle();
								extras.putString("profile_username", username);
								nextActivity.putExtras(extras);
								startActivity(nextActivity);
							} else if (success == 0) {
								title = "Message";
								alertboxmsg = "Invalid Credentials!";
								popupMessage(title, alertboxmsg);
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} else if (result == false){
					title = "Message";
					alertboxmsg = "Please enable internet.";
					popupMessage(title, alertboxmsg);
				}
			}
		});
	}
	
	public boolean isNetworkAvailable() {
		ConnectivityManager connManager = (ConnectivityManager) this
				.getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		// if no network is available networkInfo will be null
		// otherwise check if we are connected
		if (mWifi != null && mWifi.isConnected()) {
			return true;
		}
		return false;
	}

	public void popupMessage(String title, String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(title)
				.setMessage(msg)
				.setNegativeButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.cancel();
							}
						});

		AlertDialog alert = builder.create();
		alert.show();
	}

}
