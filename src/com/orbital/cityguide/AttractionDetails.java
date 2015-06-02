package com.orbital.cityguide;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;

public class AttractionDetails extends Activity {
	
	private ImageView mImageView;
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.attr_details);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		mImageView = (ImageView) findViewById(R.id.imageViewId);
		mImageView.setImageBitmap(BitmapFactory.decodeFile("pathToImageFile"));
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        // Respond to the action bar's Up/Home button
        case android.R.id.home:
        	this.finish();
        	return true;
        }
        return super.onOptionsItemSelected(item);
    }
}