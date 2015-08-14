package com.orbital.cityguide.adapter;

import com.orbital.cityguide.R;
import com.orbital.cityguide.model.CommentItem_Home;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

public class CommentListAdapter_Home extends ArrayAdapter<CommentItem_Home> {

	private Context context;
	private ArrayList<CommentItem_Home> commentItem;
	int resource;
	int count;

	CommentItem_Home cm = new CommentItem_Home();

	public CommentListAdapter_Home(Context context, int resource,
			ArrayList<CommentItem_Home> commentItem) {
		super(context, resource, commentItem);
		this.context = context;
		this.resource = resource;
		this.commentItem = commentItem;
		this.count = commentItem.size();
	}

	public void updateResults(ArrayList<CommentItem_Home> commentItem) {
		this.commentItem = commentItem;
		// Triggers the list update
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater mInflater = (LayoutInflater) context
					.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			convertView = mInflater.inflate(R.layout.list_item_comment_home, null);
		}

		ImageView mImg = (ImageView) convertView.findViewById(R.id.profile_pic);
		TextView mTitle = (TextView) convertView.findViewById(R.id.comment_title);
		TextView mAttrTitle = (TextView) convertView.findViewById(R.id.comment_attr_title);
		mAttrTitle.setPaintFlags(mAttrTitle.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
		RatingBar mRate = (RatingBar) convertView.findViewById(R.id.ratingBar);
		TextView mDesc = (TextView) convertView.findViewById(R.id.comment_desc);
		mDesc.setTypeface(null, Typeface.ITALIC);
		TextView mAttrID = (TextView) convertView.findViewById(R.id.attr_id);
		TextView mCommID = (TextView) convertView.findViewById(R.id.comment_id);

		byte[] image = Base64.decode(commentItem.get(position).getUsr_img(),
				Base64.DEFAULT);
		Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
		mImg.setImageBitmap(bitmap);
		mTitle.setText(commentItem.get(position).getTitle()); // comment title
		mAttrTitle.setText(commentItem.get(position).getAttrTitle());;
		mRate.setRating(Float.parseFloat(commentItem.get(position).getRate()));
		mDesc.setText(commentItem.get(position).getDescription());
		mAttrID.setText(commentItem.get(position).getAttrID());
		mCommID.setText(commentItem.get(position).getCommentID());
		
		return convertView;
	}
	
	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return count;
	}

	@Override
	public CommentItem_Home getItem(int position) {
		// TODO Auto-generated method stub
		return commentItem.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

}
