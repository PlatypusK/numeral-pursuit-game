package com.games.customviews;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;

import com.google.ads.AdSize;
import com.google.ads.AdView;



public class MyAdView extends AdView {
	

	public MyAdView(Activity activity, AdSize adSize, String adUnitId) {
		super(activity, adSize, adUnitId);
		// TODO Auto-generated constructor stub
	}

	public MyAdView(Activity activity, AdSize[] adSizes, String adUnitId) {
		super(activity, adSizes, adUnitId);
		// TODO Auto-generated constructor stub
	}

	public MyAdView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public MyAdView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec){
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		Context ctx=getContext();
	    AdSize testSize=AdSize.createAdSize(AdSize.SMART_BANNER, ctx); //Need to do this because the final static variable SMART_BANNER has buggy behaviour and returns a negative size if you don't.
		int height=testSize.getHeightInPixels(ctx);
		int width=testSize.getWidthInPixels(ctx);
		setMeasuredDimension(width, height);
	}
}
