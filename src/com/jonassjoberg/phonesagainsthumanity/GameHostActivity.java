package com.jonassjoberg.phonesagainsthumanity;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class GameHostActivity extends Activity {
	public static ServerThread serverThread;
	private Deck deckBlack;
	private TextView cardInHand;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game_host);
		cardInHand = (TextView) findViewById(R.id.card_in_hand);
		deckBlack = new Deck(this.getApplicationContext(), Color.BLACK, "deck_black.properties");
		serverThread.setGameHostActivity(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.game_host, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void updateCardInHand() {
		System.out.println(cardInHand);
		System.out.println(deckBlack);
		cardInHand.setText(deckBlack.nextCard().getText());
	}
}
