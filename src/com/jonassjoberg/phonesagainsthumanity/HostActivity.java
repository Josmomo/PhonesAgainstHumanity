package com.jonassjoberg.phonesagainsthumanity;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class HostActivity extends ActionBarActivity {

	private Deck deck;
	private Button buttonNextCard;
	TextView t;
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothServerSocket mBluetoothServerSocket;
	private int numPlayers = 1;
	private final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_host);

		t = (TextView) findViewById(R.id.textViewTemp);

		deck = new Deck(this, R.string.white_card, "deck_white.properties");
		buttonNextCard = (Button) findViewById(R.id.buttonNextCard);
		buttonNextCard.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				t.setText(deck.nextCard().getText());
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.host, menu);
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

}
