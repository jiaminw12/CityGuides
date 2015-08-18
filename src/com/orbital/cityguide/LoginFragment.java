package com.orbital.cityguide;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class LoginFragment extends Fragment implements
		GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener {

	public static final String TAG = LoginFragment.class.getSimpleName();

	Button register;
	Button clickHere;
	Button login;

	EditText userName;
	EditText pw;

	String title, alertboxmsg;
	int success;

	static ConnectToWebServices mConnect = new ConnectToWebServices();
	static String ipadress = mConnect.GetIPadress();

	private static final String LOGIN_URL = "http://" + ipadress + "/login.php";
	private static final String CHECKUSER_URL = "http://" + ipadress + "/checkUser.php";
	private static final String REGISTER_URL = "http://" + ipadress + "/registration.php";
	private static final String TAG_SUCCESS = "success";

	private static final int RC_SIGN_IN = 0;
	// Profile pic image size in pixels
    private static final int PROFILE_PIC_SIZE = 400;
	// Google client to interact with Google API
	private GoogleApiClient mGoogleApiClient;
	private boolean mIntentInProgress;

	private boolean mSignInClicked;
	private ConnectionResult mConnectionResult;
	private SignInButton btnGoogleSignIn;
	private Button btnSignOut, btnRevokeAccess;

	// JSON parser class
	JSONParser jsonParser = new JSONParser();

	public LoginFragment() {
	}

	public static LoginFragment newInstance() {
		return new LoginFragment();
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_login, container,
				false);
		register = (Button) rootView.findViewById(R.id.register);
		login = (Button) rootView.findViewById(R.id.login);
		userName = (EditText) rootView.findViewById(R.id.username);
		pw = (EditText) rootView.findViewById(R.id.password);
		btnGoogleSignIn = (SignInButton) rootView
				.findViewById(R.id.btn_google_sign_in);

		mGoogleApiClient = new GoogleApiClient.Builder(rootView.getContext())
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).addApi(Plus.API)
				.addScope(Plus.SCOPE_PLUS_LOGIN).build();

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
				} else if (result == true) {
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
				} else if (result == false) {
					title = "Message";
					alertboxmsg = "Please enable internet.";
					popupMessage(title, alertboxmsg);
				}
			}
		});

		btnGoogleSignIn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				signInWithGplus();
			}
		});

	}

	/**
	 * Sign-in into google
	 * */
	private void signInWithGplus() {
		if (!mGoogleApiClient.isConnecting()) {
			mSignInClicked = true;
			resolveSignInError();
		}
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

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (!result.hasResolution()) {
			GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(),
					getActivity(), 0).show();
			return;
		}

		if (!mIntentInProgress) {
			// Store the ConnectionResult for later usage
			mConnectionResult = result;

			if (mSignInClicked) {
				// The user has already clicked 'sign-in' so we attempt to
				// resolve all
				// errors until the user is signed in, or they cancel.
				resolveSignInError();
			}
		}

	}

	@Override
	public void onConnected(Bundle arg0) {
		mSignInClicked = false;
		Log.v("fhsgfj","jdfsdg");
		// Get user's information
		getProfileInformation();
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		mGoogleApiClient.connect();
	}

	/**
	 * Method to resolve any signin errors
	 * */
	private void resolveSignInError() {
		if (mConnectionResult.hasResolution()) {
			try {
				mIntentInProgress = true;
				mConnectionResult.startResolutionForResult(getActivity(),
						RC_SIGN_IN);
			} catch (SendIntentException e) {
				mIntentInProgress = false;
				mGoogleApiClient.connect();
			}
		}
	}
	
	/**
	 * Fetching user's information name, email, profile pic
	 * */
	private void getProfileInformation() {
	    try {
	        if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
	            Person currentPerson = Plus.PeopleApi
	                    .getCurrentPerson(mGoogleApiClient);
	            String personName = currentPerson.getDisplayName();
	            String personPhotoUrl = currentPerson.getImage().getUrl();
	            String personGooglePlusProfile = currentPerson.getUrl();
	            String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
	 
	            Log.e(TAG, "Name: " + personName + ", plusProfile: "
	                    + personGooglePlusProfile + ", email: " + email
	                    + ", Image: " + personPhotoUrl);
	 
	            // by default the profile url gives 50x50 px image only
	            // we can replace the value with whatever dimension we want by
	            // replacing sz=X
	            personPhotoUrl = personPhotoUrl.substring(0,
	                    personPhotoUrl.length() - 2)
	                    + PROFILE_PIC_SIZE;
	            
	            try {
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("username", personName));
					
					StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
							.permitAll().build();
					StrictMode.setThreadPolicy(policy);

					JSONObject json = jsonParser.makeHttpRequest(
							CHECKUSER_URL, "POST", params);
					if (json != null) {
						success = json.getInt(TAG_SUCCESS);
						if (success == 1) {
							
							Bitmap bitmap = BitmapFactory.decodeFile(personPhotoUrl);
							ByteArrayOutputStream stream = new ByteArrayOutputStream();
							bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
							byte[] imageFile = stream.toByteArray();
							String image = Base64
									.encodeToString(imageFile, Base64.DEFAULT);
							
							try {
								List<NameValuePair> mParams = new ArrayList<NameValuePair>();
								mParams.add(new BasicNameValuePair("username", personName));
								mParams.add(new BasicNameValuePair("emailAddress",
										email));
								mParams.add(new BasicNameValuePair("password", "nil"));
								mParams.add(new BasicNameValuePair("date", "nil"));
								mParams.add(new BasicNameValuePair("image", image));
								mParams.add(new BasicNameValuePair("gender", "nil"));

								StrictMode.ThreadPolicy policy1 = new StrictMode.ThreadPolicy.Builder()
										.permitAll().build();
								StrictMode.setThreadPolicy(policy1);

								JSONObject json1 = jsonParser.makeHttpRequest(
										REGISTER_URL, "POST", mParams);
								if (json1 != null) {
									success = json1.getInt(TAG_SUCCESS);
									if (success == 1) {
										Intent nextActivity = new Intent(getActivity(),
												AfterLoginNavigationList.class);
										Bundle extras = new Bundle();
										extras.putString("profile_username", personName);
										nextActivity.putExtras(extras);
										startActivity(nextActivity);
									} 
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						} else if (success == 0) {
							Intent nextActivity = new Intent(getActivity(),
									AfterLoginNavigationList.class);
							Bundle extras = new Bundle();
							extras.putString("profile_username", personName);
							nextActivity.putExtras(extras);
							startActivity(nextActivity);
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	 	
	@Override
	public void onStart() {
	    super.onStart();
	    mGoogleApiClient.connect();
	}

	@Override
	public void onStop() {
	    super.onStop();
	    if (mGoogleApiClient.isConnected()) {
	        mGoogleApiClient.disconnect();
	    }
	}

}
