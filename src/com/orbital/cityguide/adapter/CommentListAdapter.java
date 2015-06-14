package com.orbital.cityguide.adapter;

import com.orbital.cityguide.R;
import com.orbital.cityguide.model.CommentItem;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

public class CommentListAdapter extends ArrayAdapter<CommentItem> {

	private Context context;
	private ArrayList<CommentItem> commentItem;
	int resource;
	int count;
	
	CommentItem cm = new CommentItem();

	public CommentListAdapter(Context context, int resource,
			ArrayList<CommentItem> commentItem) {
		super(context, resource, commentItem);
		this.context = context;
		this.resource = resource;
		this.commentItem = commentItem;
		this.count = commentItem.size();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater mInflater = (LayoutInflater) context
					.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			convertView = mInflater.inflate(R.layout.list_item_comment, null);
		}

		ImageView mImg = (ImageView) convertView.findViewById(R.id.profile_pic);
		TextView mTitle = (TextView) convertView
				.findViewById(R.id.comment_title);
		RatingBar mRate = (RatingBar) convertView.findViewById(R.id.ratingBar);
		TextView mName = (TextView) convertView.findViewById(R.id.comment_name);
		TextView mDesc = (TextView) convertView.findViewById(R.id.comment_desc);
		TextView mDate = (TextView) convertView.findViewById(R.id.comment_date);

		byte[] image = Base64.decode(commentItem.get(position).getUsr_img(),
				Base64.DEFAULT);
		Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
		mImg.setImageBitmap(bitmap);
		mTitle.setText(commentItem.get(position).getTitle());
		mRate.setRating(Float.parseFloat(commentItem.get(position).getRate()));
		mName.setText(commentItem.get(position).getUsername());
		mDesc.setText(commentItem.get(position).getDescription());
		mDate.setText(commentItem.get(position).getDate());
		
		return convertView;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return count;
	}

/*	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return commentItem.get(position);
	}*/

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

}
