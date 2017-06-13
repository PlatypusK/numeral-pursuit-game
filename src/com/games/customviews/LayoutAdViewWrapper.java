package com.games.customviews;

import com.google.ads.AdSize;

import android.content.Context;
import android.widget.LinearLayout;

public class LayoutAdViewWrapper extends LinearLayout {

	public LayoutAdViewWrapper(Context context) {super(context);
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
