package com.orbital.cityguide;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.orbital.cityguide.adapter.CommentListAdapter_Home;
import com.orbital.cityguide.model.CommentItem;
import com.orbital.cityguide.model.CommentItem_Home;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.ListView;

public class HistoryFragment extends ListFragment {

	static ConnectToWebServices mConnect = new ConnectToWebServices();
	static String ipadress = mConnect.GetIPadress();

	private static final String GETUSR_URL = "http://" + ipadress + "/getUser.php";
	private static final String GET_COMM_URL = "http://" + ipadress
			+ "/getCommentByUser.php";
	private static final String UPDATE_COMM_URL = "http://" + ipadress
			+ "/updateComment.php";
	private static final String DELETE_COMM_URL = "http://" + ipadress
			+ "/deleteComment.php";

	private static final String TAG_SUCCESS = "success";
	private static final String TAG_MESSAGE = "message";
	private static final String TAG_COMMENT = "comments";
	private static final String TAG_USER = "userprofile";
	private static final String TAG_CT = "comment_title";
	private static final String TAG_TEXT = "comment_text";
	private static final String TAG_RATE = "rating";
	private static final String TAG_USR_IMG = "image";
	private static final String TAG_USR_NAME = "username";
	private static final String TAG_ATTR_ID = "attr_id";
	private static final String TAG_ATTR_TITLE = "attr_title";
	private static final String TAG_COMM_ID = "comment_id";
	private static final String TAG_USR_ID = "user_id";

	// An array of all of comments
	private JSONArray mComments = null;
	private JSONArray mUSERID = null;
	ArrayList<CommentItem_Home> commentItem = new ArrayList<CommentItem_Home>();
	CommentListAdapter_Home commentListAdapter;

	// JSON parser class
	JSONParser jsonParser = new JSONParser();

	String name_profile, title, alertboxmsg;;
	int success;

	ListView mListView;

	public HistoryFragment() {
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getActivity().setRequestedOrientation(
				ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		View rootView = inflater.inflate(R.layout.history_fragment, container,
				false);

		Bundle extras = getArguments();
		name_profile = extras.getString("profile_username");
		
		mListView = (ListView) rootView.findViewById(android.R.id.list);

		UpdateHistory();

		return rootView;
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				final View finalView = view;
				AlertDialog.Builder builder = new AlertDialog.Builder(
						getActivity());
				ArrayList<String> arrayList = new ArrayList<String>();
				arrayList.add("Edit");
				arrayList.add("Delete");
				arrayList.add("Cancel");
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(
						getActivity(), android.R.layout.simple_list_item_1,
						arrayList);
				DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:
							// Edit Clicked
							edit(finalView);
							break;
						case 1:
							// Delete clicked
							delete(finalView);
							break;
						case 2:
							// Cancel clicked
							break;
						default:
							break;
						}
					}
				};
				
				
				builder.setAdapter(adapter, listener);
				builder.show();

				return false;
			}
		});


	}

	public void UpdateHistory() {
		commentItem.clear();
		try {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("username", name_profile));

			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);

			JSONObject json_comm = jsonParser.makeHttpRequest(GET_COMM_URL,
					"POST", params);
			if (json_comm != null) {
				success = json_comm.getInt(TAG_SUCCESS);
				if (success == 1) {
					mComments = json_comm.getJSONArray(TAG_COMMENT);

					// looping through All Attractions
					for (int i = 0; i < mComments.length(); i++) {
						JSONObject c = mComments.getJSONObject(i);

						CommentItem_Home comItem = new CommentItem_Home();
						
						comItem.setCommentID(c.getString(TAG_COMM_ID));
						comItem.setAttrID(c.getString(TAG_ATTR_ID));
						comItem.setAttrTitle(c.getString(TAG_ATTR_TITLE));
						comItem.setUsr_img(c.getString(TAG_USR_IMG));
						comItem.setTitle(c.getString(TAG_CT));
						comItem.setRate(c.getString(TAG_RATE));
						comItem.setDescription(c.getString(TAG_TEXT));

						commentItem.add(comItem);
					}
					commentListAdapter = new CommentListAdapter_Home(
							getActivity().getApplicationContext(),
							R.layout.list_item_comment_home, commentItem);

					mListView.setAdapter(commentListAdapter);

				} else if (success == 0) {
					// Display no "HISTORY"
					popupMessage("History", "Sorry! currently no history.");
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void edit(View view) {
		TextView mTitle = (TextView) view.findViewById(R.id.comment_title);
		RatingBar mRate = (RatingBar) view.findViewById(R.id.ratingBar);
		TextView mDesc = (TextView) view.findViewById(R.id.comment_desc);
		final TextView mAttrID = (TextView) view.findViewById(R.id.attr_id);
		final TextView mCommID = (TextView) view.findViewById(R.id.comment_id);

		LayoutInflater li = LayoutInflater.from(getActivity());
		View promptsView = li.inflate(R.layout.insert_comment, null);
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				getActivity());
		alertDialogBuilder.setView(promptsView);

		final EditText mCommentTitle = (EditText) promptsView
				.findViewById(R.id.mainTitle);
		final EditText mCommentText = (EditText) promptsView
				.findViewById(R.id.comment_box);
		final RatingBar mRatingbar = (RatingBar) promptsView
				.findViewById(R.id.ratingBar);
		mRatingbar
				.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
					public void onRatingChanged(RatingBar ratingBar,
							float rating, boolean fromUser) {
					}
				});

		mCommentTitle.setText(mTitle.getText().toString());
		mCommentText.setText(mDesc.getText().toString());
		mRatingbar.setRating(mRate.getRating());

		// set dialog message
		alertDialogBuilder
				.setCancelable(false)
				.setPositiveButton("UPDATE",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// get user input and set it to result
								String comment_id = mCommID.getText()
										.toString();
								String attr_id = mAttrID.getText().toString();
								String comment_title = mCommentTitle.getText()
										.toString();
								String comment_text = mCommentText.getText()
										.toString();
								String rating = String.valueOf(mRatingbar
										.getRating());
								String username = name_profile;

								if (comment_title.matches("")
										|| comment_text.matches("")
										|| attr_id.matches("")) {
									title = "Error Message";
									alertboxmsg = "Required field(s) is missing.";
									popupMessage(title, alertboxmsg);
								} else {

									try {
										List<NameValuePair> params = new ArrayList<NameValuePair>();
										params.add(new BasicNameValuePair(
												"comment_id", comment_id));
										params.add(new BasicNameValuePair(
												"attr_id", attr_id));
										params.add(new BasicNameValuePair(
												"comment_title", comment_title));
										params.add(new BasicNameValuePair(
												"comment_text", comment_text));
										params.add(new BasicNameValuePair(
												"rating", rating));
										params.add(new BasicNameValuePair(
												"user_id", retrieveUserID(username)));

										StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
												.permitAll().build();
										StrictMode.setThreadPolicy(policy);

										JSONObject json = jsonParser
												.makeHttpRequest(
														UPDATE_COMM_URL,
														"POST", params);
										if (json != null) {
											success = json.getInt(TAG_SUCCESS);
											if (success == 1) {
												title = "Message";
												alertboxmsg = "Updated!";
												popupMessage(title, alertboxmsg);
												getActivity().getSupportFragmentManager()
												.beginTransaction()
												.replace(R.id.frame_container, HomeFragment_AfterLogin.newInstance(name_profile),
														HomeFragment_AfterLogin.TAG).commit();
											} else if (success == 0) {
												title = "Message";
												alertboxmsg = json
														.getString(TAG_MESSAGE);
												popupMessage(title, alertboxmsg);
											}
										}
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();

	}

	public void delete(View view) {
		final TextView mTitle = (TextView) view.findViewById(R.id.comment_title);
		final TextView mAttrID = (TextView) view.findViewById(R.id.attr_id);
		final TextView mCommID = (TextView) view.findViewById(R.id.comment_id);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				getActivity());

		// set dialog message
		alertDialogBuilder
				.setTitle("DELETE")
				.setMessage("Are you sure you want to delete ?")
				.setCancelable(false)
				.setPositiveButton("DELETE",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// get user input and set it to result
								String comment_id = mCommID.getText()
										.toString();
								String attr_id = mAttrID.getText().toString();
								String username = name_profile;

								if (comment_id.matches("")
										|| username.matches("")
										|| attr_id.matches("")) {
									title = "Error Message";
									alertboxmsg = "Required field(s) is missing.";
									popupMessage(title, alertboxmsg);
								} else {
									try {
										List<NameValuePair> params = new ArrayList<NameValuePair>();
										params.add(new BasicNameValuePair(
												"comment_title", mTitle.getText().toString()));
										params.add(new BasicNameValuePair(
												"comment_id", comment_id));
										params.add(new BasicNameValuePair(
												"attr_id", attr_id));
										params.add(new BasicNameValuePair(
												"user_id", retrieveUserID(username)));

										StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
												.permitAll().build();
										StrictMode.setThreadPolicy(policy);

										JSONObject json = jsonParser
												.makeHttpRequest(
														DELETE_COMM_URL,
														"POST", params);
										if (json != null) {
											success = json.getInt(TAG_SUCCESS);
											if (success == 1) {
												title = "Message";
												alertboxmsg = "Deleted!";
												popupMessage(title, alertboxmsg);
												getActivity().getSupportFragmentManager()
												.beginTransaction()
												.replace(R.id.frame_container, HomeFragment_AfterLogin.newInstance(name_profile),
														HomeFragment_AfterLogin.TAG).commit();
											} else if (success == 0) {
												title = "Message";
												alertboxmsg = json
														.getString(TAG_MESSAGE);
												popupMessage(title, alertboxmsg);
											}
										}
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
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
	
	public String retrieveUserID(String username) {
		String id = null;
		try {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("username", username));

			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);

			JSONObject json = jsonParser.makeHttpRequest(GETUSR_URL, "POST",
					params);
			if (json != null) {
				success = json.getInt(TAG_SUCCESS);
				if (success == 1) {
					mUSERID = json.getJSONArray(TAG_USER);
					JSONObject c = mUSERID.getJSONObject(0);
					id = c.getString(TAG_USR_ID);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return id;
	}

	@Override
	public void onResume() {
	    super.onResume();
	    commentListAdapter.notifyDataSetChanged();
	}
}
