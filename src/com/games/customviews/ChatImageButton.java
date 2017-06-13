package com.games.customviews;

import com.games.numeral.pursuit.adfree.R;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;

public class ChatImageButton extends ImageButton{

	private static final int[] STATE_NEW_MESSAGE = {R.attr.state_new_message};
	private boolean hasNewChat=false;
	
	public ChatImageButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	@Override
	public int[] onCreateDrawableState(int extraSpace) {
	    final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
	    if (hasNewChat) {
	        mergeDrawableStates(drawableState, STATE_NEW_MESSAGE);
	    }
	    return drawableState;
	}
	public boolean hasNewChat() {
		return hasNewChat;
	}
	public void setHasNewChat(boolean hasNewChat) {
		this.hasNewChat = hasNewChat;
	}
}
