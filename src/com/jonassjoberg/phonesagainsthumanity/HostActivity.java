package com.jonassjoberg.phonesagainsthumanity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class HostActivity extends Activity {	

	
	private final int NEW_GAME = 1;
	private final int NEW_TURN = 2;
	private final int WAITING_FOR_CLIENT_RESPONSE = 3;
	private int gameState = NEW_GAME;

	private ServerThread serverThread;
	private BluetoothDevice mBluetoothDevice;
	private BluetoothAdapter mBluetoothAdapter;
	private ArrayAdapter<String> mArrayAdapter, mArrayAdapterCards;
	private ListView listView, listViewRespondCards;

	private Button buttonBluetoothSendCommand;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_host);

		// Set title to phone name
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(mBluetoothAdapter.getAddress()); // "E4:B0:21:B7:9F:65"
		setTitle(mBluetoothDevice.getName());

		Intent discoverableIntent = new	Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
		startActivity(discoverableIntent);

		mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		mArrayAdapterCards = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

		listView = (ListView) findViewById(R.id.listViewBluetoothConnectedDevices);
		listView.setAdapter(mArrayAdapter);


		serverThread = new ServerThread(this);
		GameHostActivity.serverThread = serverThread;
		serverThread.start();
	}
	
	public void startTheGame() {
		Intent i = new Intent(this, com.jonassjoberg.phonesagainsthumanity.GameHostActivity.class);
		this.startActivity(i);
	}
	
	public void testThis(View v) {
		startTheGame();
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
	
	public void addToAdapter(String s) {
		mArrayAdapter.add(s);
	}

}
