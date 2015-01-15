package com.jonassjoberg.phonesagainsthumanity;

import java.util.ArrayList;

import Utils.Constants;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class GameActivity extends Activity {
	public static ClientThread clientThread;
	private ListView listViewCards, listViewVoteCards;
	private ArrayAdapter<String> mArrayAdapterCards, mArrayAdapterVoteCards;
	
	private ArrayList<TextView> cards;
	private LinearLayout cardField;
	private TextView textViewPoints;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
		
		cards = new ArrayList<TextView>();
		cardField = (LinearLayout)findViewById(R.id.card_field);
		textViewPoints = (TextView) findViewById(R.id.textViewPoints);

		clientThread.setGameActivity(this);

	}
	
	public void addCard(final String text) {
		TextView tw = (TextView)getLayoutInflater().inflate(R.layout.layout_white_card, null);
		tw.setText(text);
		tw.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//TODO skicka + dra nytt kort
				clientThread.write((Constants.RESPONSE_CARD + ((TextView) v).getText() + Constants.CARD_END_TAG).getBytes());
				cardField.removeView(v);
				cards.remove(v);
			}
		});
		cards.add(tw);
		cardField.addView(tw);
	}
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.game, menu);
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
	
	public void addToAdapterCards(String s) {
		mArrayAdapterCards.add(s);
	}
	
	public void addToAdapterVoteCards(String s) {
		mArrayAdapterVoteCards.add(s);
	}
	
	public void updatePoints(int p) {
		textViewPoints.setText(p);
	}
}
