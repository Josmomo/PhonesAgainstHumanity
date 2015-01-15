package com.jonassjoberg.phonesagainsthumanity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class JoinActivity extends Activity {

	private ClientThread clientThread;
	private ArrayAdapter<String> mArrayAdapterSearchResults, mArrayAdapterCards, mArrayAdapterVoteCards;
	private Button buttonBluetoothSearch;
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothDevice mBluetoothDevice;
	private String address = "E4:B0:21:B7:9F:65";
	private ListView listViewSearchResults, listViewCards, listViewVoteCards;
	private JoinActivity myActivity;
	private int points = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_join);

		// Set title to phone name
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		setTitle(mBluetoothAdapter.getRemoteDevice(mBluetoothAdapter.getAddress()).getName());

		myActivity = this;

		// Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy


		mArrayAdapterSearchResults = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		mArrayAdapterCards = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		mArrayAdapterVoteCards = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

		listViewSearchResults = (ListView) findViewById(R.id.listViewBluetoothSearchResult);
		listViewSearchResults.setAdapter(mArrayAdapterSearchResults);
		listViewSearchResults.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
				// Get clicked address
				String s[] = mArrayAdapterSearchResults.getItem(pos).split("\n");
				address = s[1];


				if (address != "") {
					mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(address);
					if (clientThread != null) {
						clientThread.cancel();
					}
					clientThread = new ClientThread(mBluetoothDevice, myActivity, new Handler());
					GameActivity.clientThread = clientThread;
					clientThread.start();
				}
			}

		});
		
		buttonBluetoothSearch = (Button) findViewById(R.id.buttonBluetoothSearch);

		buttonBluetoothSearch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Remove earlier search results and start a new search
				mArrayAdapterSearchResults.clear();

				if (mBluetoothAdapter.isDiscovering()) {
					mBluetoothAdapter.cancelDiscovery();
				}
				mBluetoothAdapter.startDiscovery();
			}
		});
		

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.join, menu);
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

	@Override
	protected void onDestroy() {
		unregisterReceiver(mReceiver);
		mBluetoothAdapter.cancelDiscovery();
		super.onDestroy();
	}
	
	/*
	public void testThis(View v) {
		GameActivity.clientThread = clientThread;
		Intent i = new Intent(this, GameActivity.class);
		startActivity(i);
	}
	*/
	

	// Create a BroadcastReceiver for ACTION_FOUND
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {

				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

				// Add the name and address to an array adapter to show in a ListView
				mArrayAdapterSearchResults.add(device.getName() + "\n" + device.getAddress()); // "E4:B0:21:B7:9F:65"
			}
		}
	};
}
