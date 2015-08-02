package com.orbital.cityguide.adapter;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.orbital.cityguide.ConnectToWebServices;
import com.orbital.cityguide.JSONParser;
import com.orbital.cityguide.R;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AlphabetListAdapter extends BaseAdapter {

	JSONParser jParser = new JSONParser();
	
	static ConnectToWebServices mConnect = new ConnectToWebServices();
	static String ipadress = mConnect.GetIPadress();
	
	private static final String RETRIEVEID_URL = "http://" + ipadress +"/City_Guide/getAttractionIDByTitle.php";
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_AID = "attr_id";
	private static final String TAG_ATTRACTION = "attractions";
	private JSONArray mAttrID = null;
	int success;

	DBAdapter dbAdaptor;
	Cursor cursor = null;

	Item item;
	ViewGroup mParent;

	String mID;

	public static abstract class Row {
	}

	public static final class Section extends Row {
		public final String text;

		public Section(String text) {
			this.text = text;
		}
	}

	public static final class Item extends Row {
		public final String text;

		public Item(String text) {
			this.text = text;
		}
	}

	private List<Row> rows;

	public void setRows(List<Row> rows) {
		this.rows = rows;
	}

	@Override
	public int getCount() {
		return rows.size();
	}

	@Override
	public Row getItem(int position) {
		return rows.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		if (getItem(position) instanceof Section) {
			return 1;
		} else {
			return 0;
		}
	}

	@Override
	public View getView(int position, View convertView, final ViewGroup parent) {

		dbAdaptor = new DBAdapter(parent.getContext());
		View view = convertView;
		mParent = parent;

		if (getItemViewType(position) == 0) { // Item
			if (view == null) {
				LayoutInflater inflater = (LayoutInflater) parent.getContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = (RelativeLayout) inflater.inflate(R.layout.list_item,
						parent, false);
			}

			item = (Item) getItem(position);
			final TextView mTitle = (TextView) view.findViewById(R.id.name);
			mTitle.setText(item.text);

			mID = retrieveIdByTitle(mTitle.getText().toString());
			final Button mPlanner = (Button) view.findViewById(R.id.btnPlanner);
			try {
				dbAdaptor.open();
				cursor = dbAdaptor.getAttrID();
				if (cursor != null && cursor.getCount() > 0) {
					cursor.moveToFirst();
					do {
						String mAttrID = cursor.getString(0);
						if (mID.equals(mAttrID)) {
							mPlanner.setText("-");
							break;
						} else {
							mPlanner.setText("+");
						}
					} while (cursor.moveToNext());
				} else {
				}
			} catch (Exception e) {
				Log.d("City Guide", e.getMessage());
			} finally {
				if (cursor != null)
					cursor.close();

				if (dbAdaptor != null)
					dbAdaptor.close();
			}

			mPlanner.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					View parentRow = (View) v.getParent();
					ListView listView = (ListView) parentRow.getParent();

					if (mPlanner.getText().toString().equalsIgnoreCase("+")) {
						String title = mTitle.getText().toString();
						String key = retrieveIdByTitle(title);
						mPlanner.setText("-");
						try {
							dbAdaptor.open();
							dbAdaptor.insertPlannerList(key, "1");
							Toast.makeText(mParent.getContext(),
									"Successfully Added!",
									Toast.LENGTH_SHORT).show();
						} catch (Exception e) {
							Log.e("CityGuideSingapore", e.getMessage());
						} finally {
							if (dbAdaptor != null) {
								dbAdaptor.close();
							}
						}
					} else if (mPlanner.getText().toString()
							.equalsIgnoreCase("-")) {
						String title = mTitle.getText().toString();
						String key = retrieveIdByTitle(title);
						mPlanner.setText("+");
						dbAdaptor = new DBAdapter(mParent.getContext());
						try {
							dbAdaptor.open();
							dbAdaptor.deletePlannerItem(key);
							Toast.makeText(mParent.getContext(),
									"Successfully Removed!",
									Toast.LENGTH_SHORT).show();
						} catch (Exception e) {
							Log.d("CityGuideSingapore", e.getMessage());
						} finally {
							if (dbAdaptor != null) {
								dbAdaptor.close();
							}
						}
					}
				}

				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub

				}

			});

		} else { // Section
			if (view == null) {
				LayoutInflater inflater = (LayoutInflater) parent.getContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = (LinearLayout) inflater.inflate(
						R.layout.row_section_search, parent, false);
			}

			Section section = (Section) getItem(position);
			TextView textView = (TextView) view.findViewById(R.id.textView1);
			textView.setText(section.text);
			view.setClickable(false);
			view.setEnabled(false);
			view.setOnClickListener(null);
		}

		return view;
	}
	
	public String retrieveIdByTitle(String attr_title) {
		String id = null;
		try {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("attr_title", attr_title));

			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);

			JSONObject json = jParser.makeHttpRequest(RETRIEVEID_URL, "POST",
					params);
			if (json != null) {
				success = json.getInt(TAG_SUCCESS);
				if (success == 1) {
					mAttrID = json.getJSONArray(TAG_ATTRACTION);
					JSONObject c = mAttrID.getJSONObject(0);
					id = c.getString(TAG_AID);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return id;
	}

}
