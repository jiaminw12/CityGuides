package com.orbital.cityguide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.text.method.LinkMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class EditProfile extends FragmentActivity implements OnDateSetListener {

	ImageView mProfilePic;
	TextView mUsername;
	TextView mEmail;
	TextView mDate;
	Spinner mGender;
	ImageButton pickdate;
	Button mTakePic;
	Button mUpdate;
	Button mCancel;

	String title, alertboxmsg, name_profile, id;
	int success;

	Bitmap bmp;

	protected ArrayAdapter<CharSequence> genderAdapter;
	int yy, mm, dd;

	Uri outputFileUri;

	static int LOAD_IMAGE = 1;
	static int LOAD_IMAGE_KITKAT = 2;
	static int TAKE_PICTURE = 3;

	static final String appDirectoryName = "City Guides Images";
	static final File imageRoot = new File(
			Environment
					.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
			appDirectoryName);

	private static final String GETUSR_URL = "http://192.168.1.5/City_Guide/getUser.php";
	private static final String UPDATEUSR_URL = "http://192.168.1.5/City_Guide/updateUser.php";

	private static final String TAG_SUCCESS = "success";
	private static final String TAG_MESSAGE = "message";
	private static final String TAG_USER = "userprofile";
	private static final String TAG_USERID = "user_id";
	private static final String TAG_USERNAME = "username";
	private static final String TAG_EMAIL = "emailAddress";
	private static final String TAG_DATE = "date";
	private static final String TAG_IMAGE = "image";
	private static final String TAG_GENDER = "gender";

	// An array of all of our attractions
	private JSONArray mUsers = null;

	// JSON parser class
	JSONParser jsonParser = new JSONParser();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editprofile);

		Bundle bundle = getIntent().getExtras();
		name_profile = bundle.getString("profile_username", name_profile);

		mUsername = (TextView) findViewById(R.id.username);
		mEmail = (TextView) findViewById(R.id.email);
		mDate = (TextView) findViewById(R.id.date);
		mGender = (Spinner) findViewById(R.id.gender);
		mProfilePic = (ImageView) findViewById(R.id.imgView);
		final Calendar c = Calendar.getInstance();
		yy = c.get(Calendar.YEAR);
		mm = c.get(Calendar.MONTH);
		dd = c.get(Calendar.DAY_OF_MONTH);
		updateDisplay();

		pickdate = (ImageButton) findViewById(R.id.pickDate);
		/** Listener for click event of the button */
		pickdate.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				DatePickerFragment newFragment = new DatePickerFragment();
				String choseDate = mDate.getText().toString();
				Bundle args = new Bundle();
				args.putString("dateText", choseDate);
				newFragment.setArguments(args);
				newFragment.show(getSupportFragmentManager(), "datePicker");
			}
		});

		getUserDetails();
		addListenerOnButton();
	}

	public void getUserDetails() {
		String username = name_profile;

		if (username.matches("")) {
			title = "Error Message";
			alertboxmsg = "Required field(s) is missing.";
			popupMessage(title, alertboxmsg);
		} else {
			try {
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("username", username));

				StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
						.permitAll().build();
				StrictMode.setThreadPolicy(policy);

				JSONObject json = jsonParser.makeHttpRequest(GETUSR_URL,
						"POST", params);
				if (json != null) {
					success = json.getInt(TAG_SUCCESS);
					if (success == 1) {

						mUsers = json.getJSONArray(TAG_USER);

						// looping through All Attractions
						for (int i = 0; i < mUsers.length(); i++) {
							JSONObject c = mUsers.getJSONObject(i);

							// Storing each json item in variable
							id = c.getString(TAG_USERID);
							String name = c.getString(TAG_USERNAME);
							String email = c.getString(TAG_EMAIL);
							String birthDate = c.getString(TAG_DATE);
							String image_pic = c.getString(TAG_IMAGE);
							String gender = c.getString(TAG_GENDER);

							if (!image_pic.equalsIgnoreCase("null")) {
								byte[] image = Base64.decode(image_pic,
										Base64.DEFAULT);
								Bitmap bitmap = BitmapFactory.decodeByteArray(
										image, 0, image.length);
								mProfilePic.setImageBitmap(bitmap);
							} else {
								mProfilePic.setVisibility(View.GONE);
							}

							mUsername.setText(name);
							mEmail.setText(email);
							mDate.setText(birthDate);
							genderAdapter = ArrayAdapter.createFromResource(this,
									R.array.genderArray,
									android.R.layout.simple_spinner_item);
							mGender.setAdapter(this.genderAdapter);
							int pposition = genderAdapter.getPosition(gender);
							mGender.setSelection(pposition);
						}
					} else if (success == 0) {
						title = "Message";
						alertboxmsg = json.getString("Error!");
						popupMessage(title, alertboxmsg);

					} else if (success == 0) {
						title = "Message";
						alertboxmsg = "Invalid Credentials!";
						popupMessage(title, alertboxmsg);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	public void addListenerOnButton() {

		mCancel = (Button) findViewById(R.id.cancel);
		mCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});

		mUpdate = (Button) findViewById(R.id.update);
		mUpdate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				String username = mUsername.getText().toString();
				String emailAddress = mEmail.getText().toString();
				String date = mDate.getText().toString();
				String gender = mGender.getSelectedItem().toString();
				String image = "null";

				boolean hasDrawable = (mProfilePic.getDrawable() != null);
				if (hasDrawable) {
					Bitmap bitmap = ((BitmapDrawable) mProfilePic.getDrawable())
							.getBitmap();
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
					byte[] imageArr = stream.toByteArray();
					image = Base64.encodeToString(imageArr, Base64.DEFAULT);
				}

				if (!username.matches("") && !emailAddress.matches("") && !date.matches("")
						&& !gender.matches("") && !image.matches("")) {

					try {
						List<NameValuePair> params = new ArrayList<NameValuePair>();
						params.add(new BasicNameValuePair("user_id", id));
						params.add(new BasicNameValuePair("username", username));
						params.add(new BasicNameValuePair("emailAddress",
								emailAddress));
						params.add(new BasicNameValuePair("date", date));
						params.add(new BasicNameValuePair("image", image));
						params.add(new BasicNameValuePair("gender", gender));

						StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
								.permitAll().build();
						StrictMode.setThreadPolicy(policy);

						JSONObject json = jsonParser.makeHttpRequest(UPDATEUSR_URL,
								"POST", params);
						if (json != null) {
							success = json.getInt(TAG_SUCCESS);
							if (success == 1) {
								title = "Message";
								alertboxmsg = "User's credentials updated!";
								popupMessage(title, alertboxmsg);
								Bundle bundle = new Bundle();
								bundle.putString("edttext", "From Activity");
								Fragment fragment = new ViewProfileFragment();
								bundle = new Bundle();
								bundle.putString("profile_username", name_profile);
								fragment.setArguments(bundle);
								finish();
							} else if (success == 0) {
								title = "Message";
								alertboxmsg = json.getString(TAG_MESSAGE);
								popupMessage(title, alertboxmsg);
							}
						}
					} catch (Exception e) {
						if (e.getMessage() != null) {
							Log.d("Error", e.getMessage());
						} else {
							e.printStackTrace();
						}
					}
				} else {
					title = "Error Message";
					alertboxmsg = "Can't be blank.";
					popupMessage(title, alertboxmsg);
				}
			}
		});

		Button Browse = (Button) findViewById(R.id.browse);
		Browse.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
				photoPickerIntent.setType("image/*");
				if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
					startActivityForResult(photoPickerIntent, LOAD_IMAGE_KITKAT);
				} else {
					startActivityForResult(photoPickerIntent, LOAD_IMAGE);
				}
			}
		});

		mTakePic = (Button) findViewById(R.id.takePicture);
		mTakePic.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

				File file = new File(Environment.getExternalStorageDirectory(),
						"MyPhoto.jpg");
				outputFileUri = Uri.fromFile(file);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

				startActivityForResult(intent, TAKE_PICTURE);
			}
		});

	}

	// PictureUpload
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			// LOAD_IMAGE
			if (requestCode == 1 && null != data) {

				Uri selectedImage = data.getData();
				String[] filePathColumn = { MediaStore.Images.Media.DATA };

				Cursor cursor = getContentResolver().query(selectedImage,
						filePathColumn, null, null, null);
				cursor.moveToFirst();

				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				String picturePath = cursor.getString(columnIndex);
				cursor.close();

				Log.e("path", picturePath); // use selectedImagePath

				mProfilePic = (ImageView) findViewById(R.id.imgView);
				bmp = BitmapFactory.decodeFile(picturePath);
				bmp = Shrink(picturePath, 100, 200);
				mProfilePic.setImageBitmap(bmp);

			} else if (requestCode == 2) {
				// LOAD_IMAGE_KITKAT
				Uri selectedImage = data.getData();
				String id = selectedImage.getLastPathSegment().split(":")[1];
				String[] filePathColumn = { MediaStore.Images.Media.DATA };
				final String imageOrderBy = null;

				Uri uri = getUri();
				String selectedImagePath = "path";

				Cursor imageCursor = getContentResolver().query(uri,
						filePathColumn, MediaStore.Images.Media._ID + "=" + id,
						null, imageOrderBy);

				if (imageCursor.moveToFirst()) {
					selectedImagePath = imageCursor.getString(imageCursor
							.getColumnIndex(MediaStore.Images.Media.DATA));
				}
				Log.e("path", selectedImagePath); // use selectedImagePath

				mProfilePic = (ImageView) findViewById(R.id.imgView);
				bmp = BitmapFactory.decodeFile(selectedImagePath);
				bmp = Shrink(selectedImagePath, 100, 200);
				mProfilePic.setImageBitmap(bmp);

			} else if (requestCode == 3) {
				// TAKE_PICTURE
				mProfilePic = (ImageView) findViewById(R.id.imgView);
				BitmapDrawable bmpd = new BitmapDrawable(getResources(),
						outputFileUri.getPath());
				mProfilePic.setImageDrawable(bmpd);
			}
		}
	}

	// By using this method get the Uri of Internal/External Storage for Media
	private Uri getUri() {
		String state = Environment.getExternalStorageState();
		if (!state.equalsIgnoreCase(Environment.MEDIA_MOUNTED))
			return MediaStore.Images.Media.INTERNAL_CONTENT_URI;

		return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
	}

	public static Bitmap Shrink(String file, int width, int height) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(file, options);
		options.inSampleSize = calcSize(options, width, height);
		options.inJustDecodeBounds = false;
		Bitmap bmp = BitmapFactory.decodeFile(file, options);
		return bmp;
	}

	public static int calcSize(BitmapFactory.Options options, int width,
			int height) {
		final int uHeight = options.outHeight;
		final int uWidth = options.outWidth;
		int inSampleSize = 1;
		if (uHeight > height || uWidth > width) {
			if (uWidth > uHeight) {
				inSampleSize = Math.round((float) uHeight / (float) height);
			} else {
				inSampleSize = Math.round((float) uWidth / (float) width);
			}
		}
		return inSampleSize;
	}

	/*----------------------DATEPICKER------------------------------*/
	@Override
	public void onDateSet(DatePicker view, int year, int month, int day) {
		yy = year;
		mm = month;
		dd = day;
		updateDisplay();
	}

	// Updates the date in the TextView
	private void updateDisplay() {
		mDate.setText(new StringBuilder()
				// Month is 0 based, just add 1
				.append(yy).append("-")
				.append(mm < 9 ? "0" + (mm + 1) : mm + 1).append("-")
				.append(dd < 10 ? "0" + dd : dd));
	}

	public void popupMessage(String title, String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
