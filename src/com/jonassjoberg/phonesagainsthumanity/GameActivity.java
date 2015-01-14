package com.jonassjoberg.phonesagainsthumanity;

import Utils.Constants;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class GameActivity extends Activity {
	public static ClientThread clientThread;
	private ListView listViewCards, listViewVoteCards;
	private ArrayAdapter<String> mArrayAdapterCards, mArrayAdapterVoteCards;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		clientThread.setGameActivity(this);
		
		mArrayAdapterCards = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		mArrayAdapterVoteCards = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

		listViewCards = (ListView) findViewById(R.id.listViewCards);
		listViewCards.setAdapter(mArrayAdapterCards);
		listViewCards.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
				// Send the picked card as response to the black card
				while (!clientThread.write((Constants.RESPONSE_CARD + mArrayAdapterCards.getItem(pos)).getBytes())) {}
			}

		});

		listViewVoteCards = (ListView) findViewById(R.id.listViewVoteCards);
		listViewVoteCards.setAdapter(mArrayAdapterVoteCards);
		listViewVoteCards.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
				// Send the picked card as response to the black card
				while (!clientThread.write((Constants.VOTE_CARD + mArrayAdapterCards.getItem(pos)).getBytes())) {}
			}

		});

		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
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
}
