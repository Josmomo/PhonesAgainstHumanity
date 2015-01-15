package com.jonassjoberg.phonesagainsthumanity;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GameHostActivity extends Activity {
	public static ServerThread serverThread;
	private ArrayList<TextView> cards;
	private LinearLayout cardField;
	private HorizontalScrollView scrollField;
	private TextView cardInHand;
	private Button changeView;
	private boolean cardInHandVisible = true;
	private Deck deckBlack;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game_host);
		deckBlack = new Deck(this.getApplicationContext(), -1, "deck_black.properties");
		
		serverThread.setGameHostActivity(this);
		
		cards = new ArrayList<TextView>();
		scrollField = (HorizontalScrollView) findViewById(R.id.card_scroll);
		cardField = (LinearLayout)findViewById(R.id.card_field);
		changeView = (Button) findViewById(R.id.change_view);
		cardInHand = (TextView) findViewById(R.id.card_in_hand);
		cardInHand.setText(deckBlack.nextCard().getText());
		
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
	
	public void changeView(View v) {
		//Change between watching the response cards ore the question
		if(cardInHandVisible) {
			cardInHand.setVisibility(View.GONE);
			scrollField.setVisibility(View.VISIBLE);
			changeView.setText("Check question");
		} else {
			cardInHand.setVisibility(View.VISIBLE);
			scrollField.setVisibility(View.GONE);
			changeView.setText("Check responses");
		}
		cardInHandVisible = !cardInHandVisible;
	}
	
	public void addCard(String text) {
		//Create a text view with the white card design
		TextView tw = (TextView)getLayoutInflater().inflate(R.layout.layout_white_card, null);
		tw.setText(text);
		tw.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//TODO Säg att kortet vann
				serverThread.pickedWinningCard((String) ((TextView) v).getText());
				cards.clear();
				cardField.removeAllViews();
			}
		});
		//Add the card to the scroll view
		cards.add(tw);
		cardField.addView(tw);
	}
	
	public void updateCardInHand() {
		cardInHand.setText(deckBlack.nextCard().getText());
	}
}
