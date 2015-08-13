package com.orbital.cityguide;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.baoyz.swipemenulistview.SwipeMenuListView.OnMenuItemClickListener;
import com.orbital.cityguide.adapter.PlannerDragNDropListAdapter.Row;
import com.orbital.cityguide.adapter.DBAdapter;
import com.orbital.cityguide.adapter.PlannerDragNDropListAdapter;
import com.orbital.cityguide.adapter.PlannerDragNDropListAdapter.Item;
import com.orbital.cityguide.adapter.PlannerDragNDropListAdapter.Section;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class TripPlannerFragment extends ListFragment {

	public static final String TAG = TripPlannerFragment.class.getSimpleName();

	protected ArrayAdapter<CharSequence> dayAdapter, adultAdapter,
			childAdapter;

	// manages all of our attractions in a list.
	private ArrayList<HashMap<String, String>> mPlannerList = new ArrayList<HashMap<String, String>>();
	final PlannerDragNDropListAdapter adapter = new PlannerDragNDropListAdapter();

	private HashMap<String, Integer> sections = new HashMap<String, Integer>();
	List<Row> rows = new ArrayList<Row>();

	Cursor cursor = null;
	DBAdapter dbAdaptor;

	static String name_profile;

	String username;
	int success, start = 0;
	String previousLetter = null;

	Spinner daySpinner = null;
	Spinner adultSpinner = null;
	Spinner childSpinner = null;
	// Progress Dialog
	private ProgressDialog pDialog;
	CharSequence[] planner_list;
	TextView mFinalPrice;
	float mPriceAdult, mPriceChild, totalSinglePrice, totalPrice = 0;
	int num_Adult, num_Child;

	SharedPreferences pref;
	Context mContext;

	public TripPlannerFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity();
	}

	public static TripPlannerFragment newInstance(String nameProfile) {
		TripPlannerFragment myFragment = new TripPlannerFragment();
		Bundle args = new Bundle();
		args.putString("profile_username", nameProfile);
		myFragment.setArguments(args);
		name_profile = nameProfile;
		return myFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getActivity().setRequestedOrientation(
				ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		pref = this.getActivity().getSharedPreferences("PREFERENCE",
				Context.MODE_PRIVATE);

		Bundle bundle = this.getArguments();
		if (name_profile != null && isNetworkAvailable()) {
			name_profile = bundle.getString("profile_username");
			username = name_profile;
			// UploadPlannerList();
		} else {
			name_profile = null;
		}

		View rootView = inflater.inflate(R.layout.fragment_planner, container,
				false);
		dbAdaptor = new DBAdapter(getActivity());
		daySpinner = (Spinner) rootView.findViewById(R.id.day_spinner);
		this.dayAdapter = ArrayAdapter.createFromResource(this.getActivity(),
				R.array.arrayDay, android.R.layout.simple_spinner_item);
		daySpinner.setAdapter(this.dayAdapter);
		final int num = pref.getInt("NumOfDays", 0);
		daySpinner.setSelection(num);
		daySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				String value = daySpinner.getSelectedItem().toString();
				pref.edit().putInt("NumOfDays", (Integer.parseInt(value)) - 1)
						.commit();

				if (value.equalsIgnoreCase("1")) {
					addSectionHeader(1);
				} else if (value.equalsIgnoreCase("2")) {
					addSectionHeader(2);
				} else if (value.equalsIgnoreCase("3")) {
					addSectionHeader(3);
				} else if (value.equalsIgnoreCase("4")) {
					addSectionHeader(4);
				} else if (value.equalsIgnoreCase("5")) {
					addSectionHeader(5);
				} else if (value.equalsIgnoreCase("6")) {
					addSectionHeader(6);
				} else if (value.equalsIgnoreCase("7")) {
					addSectionHeader(7);
				} else if (value.equalsIgnoreCase("8")) {
					addSectionHeader(8);
				} else if (value.equalsIgnoreCase("9")) {
					addSectionHeader(9);
				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});

		adultSpinner = (Spinner) rootView.findViewById(R.id.adult_spinner);
		this.adultAdapter = ArrayAdapter.createFromResource(this.getActivity(),
				R.array.arrayPrice, android.R.layout.simple_spinner_item);
		adultSpinner.setAdapter(this.adultAdapter);
		final int numA = pref.getInt("NumOfAdult", 0);
		adultSpinner.setSelection(numA, false);
		num_Adult = Integer.parseInt(adultSpinner.getSelectedItem().toString());
		adultSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				num_Adult = Integer.parseInt(adultSpinner.getSelectedItem()
						.toString());
				pref.edit().putInt("NumOfAdult", num_Adult).commit();
				adapter.notifyDataSetChanged();
				updateTotalPrice();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}

		});

		childSpinner = (Spinner) rootView.findViewById(R.id.child_spinner);
		this.childAdapter = ArrayAdapter.createFromResource(this.getActivity(),
				R.array.arrayPrice, android.R.layout.simple_spinner_item);
		childSpinner.setAdapter(this.childAdapter);
		final int numC = pref.getInt("NumOfChild", 0);
		childSpinner.setSelection(numC, false);
		num_Child = Integer.parseInt(childSpinner.getSelectedItem().toString());
		childSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				num_Child = Integer.parseInt(childSpinner.getSelectedItem()
						.toString());
				pref.edit().putInt("NumOfChild", num_Child).commit();
				adapter.notifyDataSetChanged();
				updateTotalPrice();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		mFinalPrice = (TextView) rootView.findViewById(R.id.totalPrice);
		new LoadPlannerList().execute();
		return rootView;
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		SwipeMenuListView mListView = (SwipeMenuListView) getView()
				.findViewById(android.R.id.list);

		SwipeMenuCreator creator = new SwipeMenuCreator() {

			@Override
			public void create(SwipeMenu menu) {
				switch (menu.getViewType()) {
				case 0:
					// create menu of type 0
					// create "edit" item
					SwipeMenuItem openItem = new SwipeMenuItem(getActivity()
							.getApplicationContext());
					// set item background
					openItem.setBackground(new ColorDrawable(Color.rgb(0xC9,
							0xC9, 0xCE)));
					// set item width
					openItem.setWidth(dp2px(100));
					// set item title
					openItem.setTitle("Move");
					// set item title fontsize
					openItem.setTitleSize(18);
					// set item title font color
					openItem.setTitleColor(Color.WHITE);
					// add to menu
					menu.addMenuItem(openItem);

					// create "delete" item
					SwipeMenuItem deleteItem = new SwipeMenuItem(getActivity()
							.getApplicationContext());
					// set item background
					deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
							0x3F, 0x25)));
					// set item width
					deleteItem.setWidth(dp2px(100));
					// set a icon
					deleteItem.setIcon(R.drawable.trash);
					// add to menu
					menu.addMenuItem(deleteItem);
					break;
				case 1:
					// section
					// create menu of type 1
					break;
				}
			}
		};

		// set creator
		mListView.setMenuCreator(creator);
		mListView.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(int position, SwipeMenu menu,
					int index) {
				Item item;
				AlertDialog diaBox;
				switch (index) {
				case 0:
					// edit
					item = (Item) adapter.getItem(position);
					Log.v("id ; ", item.id);
					diaBox = AskOption_Update(item.id);
					diaBox.show();
					break;
				case 1:
					// delete
					item = (Item) adapter.getItem(position);
					diaBox = AskOption_Delete(item.id);
					diaBox.show();
					break;
				}
				// false : close the menu; true : not close
				// the menu
				return false;
			}
		});

	}

	class LoadPlannerList extends AsyncTask<Object, Object, Cursor> {

		DBAdapter dbAdaptor = new DBAdapter(getActivity());

		/* Before starting background thread Show Progress Dialog */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(getActivity());
			pDialog.setMessage("Loading the info. Please wait...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		/** Getting product details in background thread **/
		protected Cursor doInBackground(Object... params) {
			mPlannerList.clear();
			try {
				dbAdaptor.open();
				cursor = dbAdaptor.getAllPlanner();
				if (cursor != null && cursor.getCount() > 0) {
					cursor.moveToFirst();
					do {
						String mAttrID = cursor.getString(0);
						String tag_title = cursor.getString(1);
						Log.v("tag_title : ", tag_title);
						String attr_title = cursor.getString(2);
						float mPriceAdult = Float.valueOf(cursor.getString(3));
						float mPriceChild = Float.valueOf(cursor.getString(4));
						float singlePrice = (mPriceAdult * num_Adult)
								+ (mPriceChild * num_Child);
						totalPrice = totalPrice + singlePrice;
						HashMap<String, String> map = new HashMap<String, String>();
						map.put(tag_title, mAttrID+","+attr_title);
						mPlannerList.add(map);
					} while (cursor.moveToNext());
				}
			} catch (Exception e) {
				Log.e("City Guide", e.getMessage());
			}

			return null;
		}

		/** After completing background task Dismiss the progress dialog **/
		protected void onPostExecute(Cursor cursor) {

			if (cursor != null) {
				cursor.close();
			}

			if (dbAdaptor != null) {
				dbAdaptor.close();
			}

			int start = 0;
			String previousLetter = null;
			
			for (HashMap<String, String> map : mPlannerList) {
				for (String str : map.keySet()) {
					String key = str;
					String value = map.get(key);
					String firstTitle = key;
					
					// Check if we need to add a header row
					if (!firstTitle.equals(previousLetter)) {
						rows.add(new Section(firstTitle));
						sections.put(firstTitle, start);
					}
					
					String[] detail = value.split(","); 
					// Add the title to the list
					rows.add(new Item(detail[0], detail[1]));
					previousLetter = firstTitle;
				}
			}
			adapter.setRows(rows);
			setListAdapter(adapter);

			DecimalFormat df = new DecimalFormat("#0.00");
			df.setMaximumFractionDigits(2);
			mFinalPrice.setText("Total Price : $" + df.format(totalPrice));

			// dismiss the dialog once got all details
			pDialog.dismiss();
		}

	}

	public void addSectionHeader(int numofDay) {
		List<String> listItems = new ArrayList<String>();
		for (int i = 1; i < numofDay + 1; i++) {
			planner_list = new String[numofDay];
			String title = "Day " + i;
			listItems.add(title);
		}
		planner_list = listItems.toArray(new CharSequence[listItems.size()]);
	}

	// update planner item
	private AlertDialog AskOption_Update(final String attr_id) {
		Builder updateDialogBox = new AlertDialog.Builder(getActivity());
		// set message, title
		updateDialogBox.setTitle("Choose a day");

		// select the item
		DialogInterface.OnClickListener ListClick = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// your deleting code
				dialog.dismiss();
				String tag_id = null;
				String row_id = null;
				dbAdaptor = new DBAdapter(getActivity().getApplicationContext());
				Cursor cursor = null;
				Cursor cursor2 = null;
				try {
					dbAdaptor.open();
					cursor = dbAdaptor.getTagID(String
							.valueOf(planner_list[which]));
					cursor2 = dbAdaptor.getPlannerRowId(attr_id);
					if (cursor != null && cursor.getCount() > 0
							&& cursor2 != null && cursor2.getCount() > 0) {
						cursor.moveToFirst();
						cursor2.moveToFirst();
						do {
							tag_id = cursor.getString(0);
							row_id = cursor2.getString(0);
						} while (cursor.moveToNext() && cursor2.moveToNext());
					}

					dbAdaptor.updatePlannerItem(Integer.valueOf(row_id), attr_id,
							tag_id);
				} catch (Exception e) {
					Log.d("City Guide Singapore", e.getMessage());
				} finally {
					if (dbAdaptor != null) {
						dbAdaptor.close();
					}
					getActivity()
							.getSupportFragmentManager()
							.beginTransaction()
							.replace(
									R.id.frame_container,
									TripPlannerFragment
											.newInstance(name_profile),
									TripPlannerFragment.TAG)
							.commitAllowingStateLoss();
				}
			}
		};

		// cancel button
		DialogInterface.OnClickListener OkClick = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		};

		updateDialogBox.setItems(planner_list, ListClick);
		updateDialogBox.setNeutralButton("Cancel", OkClick);
		AlertDialog alertdialog = updateDialogBox.create();
		return alertdialog;
	}

	// delete planner item
	private AlertDialog AskOption_Delete(final String attr_id) {
		AlertDialog myQuittingDialogBox = new AlertDialog.Builder(getActivity())
				// set message, title
				.setTitle("Delete")
				.setMessage("Are you sure want to Delete?")

				.setPositiveButton("Delete",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int whichButton) {
								// your deleting code
								dialog.dismiss();

								dbAdaptor = new DBAdapter(getActivity()
										.getApplicationContext());

								try {
									dbAdaptor.open();

									dbAdaptor.deletePlannerItem(attr_id);

								} catch (Exception e) {
									Log.d("City Guide Singapore",
											e.getMessage());
								} finally {
									if (dbAdaptor != null) {
										dbAdaptor.close();
									}
									getActivity()
											.getSupportFragmentManager()
											.beginTransaction()
											.replace(
													R.id.frame_container,
													TripPlannerFragment
															.newInstance(name_profile),
													TripPlannerFragment.TAG)
											.commitAllowingStateLoss();
								}
							}

						})

				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {

								dialog.dismiss();

							}
						}).create();
		return myQuittingDialogBox;
	}

	public float retrievePrice(String attr_id) {
		float totalSinglePrice = 0;
		DBAdapter dbAdaptor = new DBAdapter(getActivity());
		try {
			dbAdaptor.open();
			cursor = dbAdaptor.getAllPlanner();
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				do {

				} while (cursor.moveToNext());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		totalSinglePrice = (mPriceAdult * num_Adult)
				+ (mPriceChild * num_Child);
		return totalSinglePrice;
	}

	public void updateTotalPrice() {
		mPlannerList.clear();
		totalPrice = 0;
		try {
			dbAdaptor.open();
			cursor = dbAdaptor.getAllPlanner();
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				do {
					String mAttrID = cursor.getString(0);
					String tag_title = cursor.getString(1);
					Log.v("tag_title : ", tag_title);
					String attr_title = cursor.getString(2);
					float mPriceAdult = Float.valueOf(cursor.getString(3));
					float mPriceChild = Float.valueOf(cursor.getString(4));
					float singlePrice = (mPriceAdult * num_Adult)
							+ (mPriceChild * num_Child);
					totalPrice = totalPrice + singlePrice;
					HashMap<String, String> map = new HashMap<String, String>();
					map.put(tag_title, mAttrID+","+attr_title);
					mPlannerList.add(map);
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {
			Log.e("City Guide", e.getMessage());
		} finally {
			if (cursor != null)
				cursor.close();

			if (dbAdaptor != null)
				dbAdaptor.close();
		}

		DecimalFormat df = new DecimalFormat("#0.00");
		df.setMaximumFractionDigits(2);
		mFinalPrice.setText("Total Price : $" + df.format(totalPrice));
	}

	private int dp2px(int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				getResources().getDisplayMetrics());
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

}
