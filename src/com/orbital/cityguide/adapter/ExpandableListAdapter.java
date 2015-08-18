package com.orbital.cityguide.adapter;

import java.util.ArrayList;
import java.util.HashMap;
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
import android.graphics.Typeface;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

	private Context _context;
	private List<String> _listDataHeader; // header titles
	// child data in format of header title, child title
	private HashMap<String, List<String>> _listDataChild;

	int success;

	DBAdapter dbAdaptor;
	Cursor cursor = null;

	ViewGroup mParent;

	public ExpandableListAdapter(Context context, List<String> listDataHeader,
			HashMap<String, List<String>> listChildData) {
		this._context = context;
		this._listDataHeader = listDataHeader;
		this._listDataChild = listChildData;
	}

	@Override
	public Object getChild(int groupPosition, int childPosititon) {
		return this._listDataChild.get(this._listDataHeader.get(groupPosition))
				.get(childPosititon);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, final int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {

		dbAdaptor = new DBAdapter(_context);

		final String childText = (String) getChild(groupPosition, childPosition);

		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) this._context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater
					.inflate(R.layout.list_item_expand, null);
		}

		final TextView txtListChild = (TextView) convertView
				.findViewById(R.id.name);
		txtListChild.setText(childText);

		final Button mPlanner = (Button) convertView
				.findViewById(R.id.btnPlanner);
		try {
			dbAdaptor.open();
			cursor = dbAdaptor.getAttrIDByTitle(childText);
			if (cursor != null) {
				cursor.moveToFirst();
				String mAttrID = cursor.getString(0);
				Log.v("mAttrID: ", mAttrID);
				boolean result = dbAdaptor.getAttrID(mAttrID);
				if (result) {
					mPlanner.setText("-");
				} else {
					mPlanner.setText("+");
				}
			}
		} catch (Exception e) {
			Log.e("City Guide", e.getMessage());
		} finally {
			if (cursor != null)
				cursor.close();

			if (dbAdaptor != null)
				dbAdaptor.close();
		}

		mPlanner.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				if (mPlanner.getText().toString().equalsIgnoreCase("+")) {
					String title = txtListChild.getText().toString();
					mPlanner.setText("-");
					try {
						dbAdaptor.open();
						Cursor cursor = dbAdaptor.getAttrIDByTitle(childText);
						if (cursor != null) {
							cursor.moveToFirst();
							String mAttrID = cursor.getString(0);
							dbAdaptor.insertPlannerList(mAttrID, "0");
							Toast.makeText(mParent.getContext(),
									"Successfully Added!", Toast.LENGTH_SHORT)
									.show();
						}
					} catch (Exception e) {
						Log.e("CityGuideSingapore", e.getMessage());
					} finally {
						if (dbAdaptor != null) {
							dbAdaptor.close();
						}
					}
				} else if (mPlanner.getText().toString().equalsIgnoreCase("-")) {
					String title = txtListChild.getText().toString();
					mPlanner.setText("+");
					dbAdaptor = new DBAdapter(_context);
					try {
						dbAdaptor.open();
						Cursor cursor = dbAdaptor.getAttrIDByTitle(childText);
						if (cursor != null) {
							cursor.moveToFirst();
							String mAttrID = cursor.getString(0);
							if (mAttrID != null) {
								dbAdaptor.deletePlannerItem(mAttrID);
							}
						}
						Toast.makeText(mParent.getContext(),
								"Successfully Removed!", Toast.LENGTH_SHORT)
								.show();
					} catch (Exception e) {
						Log.e("CityGuideSingapore", e.getMessage());
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

		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return this._listDataChild.get(this._listDataHeader.get(groupPosition))
				.size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return this._listDataHeader.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return this._listDataHeader.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		String headerTitle = (String) getGroup(groupPosition);
		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) this._context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.list_group, null);
		}

		TextView lblListHeader = (TextView) convertView
				.findViewById(R.id.lblListHeader);
		lblListHeader.setTypeface(null, Typeface.BOLD);
		lblListHeader.setText(headerTitle);

		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

}
