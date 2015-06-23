package com.orbital.cityguide;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class WebViewActivity extends Activity {
	
	private WebView mWebView;
	Bundle extras;
	String link;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.attr_webview);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		 
		mWebView = (WebView) findViewById(R.id.webview);
		WebSettings webSettings = mWebView.getSettings();
		mWebView.getSettings().setJavaScriptEnabled(true);
		
		extras = getIntent().getExtras();
		String link = extras.getString("url");
		mWebView.loadUrl(link);
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
