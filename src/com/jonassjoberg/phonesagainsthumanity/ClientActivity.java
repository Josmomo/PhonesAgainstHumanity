package com.jonassjoberg.phonesagainsthumanity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class ClientActivity extends ActionBarActivity {

	private ArrayAdapter<String> mArrayAdapter;
	private Button buttonBluetoothSearch, buttonBluetoothConnect;
	private BluetoothSocket bSocket;
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothDevice mBluetoothDevice;
	private final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_client);

		mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

		ListView listView = (ListView) findViewById(R.id.listViewBluetoothSearchResult);
		listView.setAdapter(mArrayAdapter);

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
//		buttonBluetoothConnect.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				mBluetoothDevice = mBluetoothAdapter.getRemoteDevice("20:73:00:3A:E7:03");
//
//				try {
//					bSocket = mBluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);
//					if (bSocket != null) {
//						bSocket.connect();
//					}
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.client, menu);
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
				mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
			}
		}
	};
}
