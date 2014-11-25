package com.jonassjoberg.phonesagainsthumanity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class JoinActivity extends ActionBarActivity {

	private ClientThread clientThread;
	private ArrayAdapter<String> mArrayAdapter;
	private Button buttonBluetoothSearch, buttonBluetoothConnect;
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothDevice mBluetoothDevice;
	private String address = "";
	private ListView listView;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_join);
		
		mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

		listView = (ListView) findViewById(R.id.listViewBluetoothSearchResult);
		listView.setAdapter(mArrayAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
				String s[] = mArrayAdapter.getItem(pos).split("\n");
				address = s[1];
			}
			
		});
		buttonBluetoothSearch = (Button) findViewById(R.id.buttonBluetoothSearch);
		buttonBluetoothConnect = (Button) findViewById(R.id.buttonBluetoothConnect);
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

		buttonBluetoothSearch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Remove earlier search results and start a new search
				mArrayAdapter.clear();
				
				if (mBluetoothAdapter.isDiscovering()) {
					mBluetoothAdapter.cancelDiscovery();
				}
				mBluetoothAdapter.startDiscovery();

			}
		});
		buttonBluetoothConnect.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (address != "")
				mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(address);
				clientThread = new ClientThread(mBluetoothDevice);
				clientThread.start();
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
		super.onDestroy();
		mBluetoothAdapter.cancelDiscovery();
	}

	// Create a BroadcastReceiver for ACTION_FOUND
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				
				// Add the name and address to an array adapter to show in a ListView
				mArrayAdapter.add(device.getName() + "\n" + device.getAddress()); // "E4:B0:21:B7:9F:65"
			}
		}
	};
}
