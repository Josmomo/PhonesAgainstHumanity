package com.jonassjoberg.phonesagainsthumanity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;

public class GameActivity extends Activity{
	
	LinearLayout cardField;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
		
		cardField = (LinearLayout)findViewById(R.id.card_field);
		for(int i = 0; i < 8; i++) {
			addCard("********" + i + "*************");
		}
	}
}
