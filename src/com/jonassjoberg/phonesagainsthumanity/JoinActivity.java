package com.jonassjoberg.phonesagainsthumanity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

public class JoinActivity extends Activity {

	private ClientThread clientThread;
	private ArrayAdapter<String> mArrayAdapterSearchResults, mArrayAdapterCards;
	private Button buttonBluetoothSearch, buttonBluetoothSend;
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothDevice mBluetoothDevice;
	private String address = "E4:B0:21:B7:9F:65";
	private ListView listViewSearchResults, listViewCards;
	private Activity myActivity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_join);
		
		myActivity = this;

		mArrayAdapterSearchResults = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		mArrayAdapterCards = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

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
					clientThread = new ClientThread(mBluetoothDevice);
					clientThread.start();
				}
			}

		});
		listViewCards = (ListView) findViewById(R.id.listViewCards);
		listViewCards.setAdapter(mArrayAdapterCards);
		listViewCards.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
				
			}

		});
		
		
		buttonBluetoothSearch = (Button) findViewById(R.id.buttonBluetoothSearch);
		buttonBluetoothSend = (Button) findViewById(R.id.buttonBluetoothSend);
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

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
		buttonBluetoothSend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String bytes = "Sending from Client";
				clientThread.write(bytes.getBytes());
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
				mArrayAdapterSearchResults.add(device.getName() + "\n" + device.getAddress()); // "E4:B0:21:B7:9F:65"
			}
		}
	};



	/**
	 * The ClientThread will handle all bluetooth communication to a host server.
	 * @author Jonas
	 *
	 */
	private class ClientThread extends Thread implements Runnable {

		private BluetoothAdapter mBluetoothAdapter;
		private BluetoothDevice mBluetoothDevice;
		private BluetoothSocket mBluetoothSocket;
		private final InputStream mInputStream;
		private final OutputStream mOutputStream;
		private final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

		private byte[] readBuffer = new byte[128];

		public ClientThread(BluetoothDevice device) {
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			mBluetoothDevice = device;
			BluetoothSocket tmp = null;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get a BluetoothSocket to connect with the given BluetoothDevice
			try {
				tmp = mBluetoothDevice.createRfcommSocketToServiceRecord(uuid);
			} catch (IOException e) {
				e.printStackTrace();
			}
			mBluetoothSocket = tmp;

			// Try to get the input and outputstream, otherwise the will be set to null
			if (mBluetoothSocket != null) {
				try {
					tmpIn = mBluetoothSocket.getInputStream();
					tmpOut = mBluetoothSocket.getOutputStream();
				} catch (IOException e) { }
			}
			mInputStream = tmpIn;
			mOutputStream = tmpOut;
		}

		@Override
		public void run() {
			mBluetoothAdapter.cancelDiscovery();

			try {
				// Blocks
				mBluetoothSocket.connect();
				Log.d(getName(), "Connected to server");
			} catch (IOException connectException) {
				// Unable to connect, close the socket and get out
				try {
					mBluetoothSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return;
			}
			
			while (true) {
				read();
			}
		}

		/**
		 * Writes to the outputstream
		 * @param bytes to write
		 */
		public void write(byte[] bytes) {
			try {
				if (mOutputStream != null) {
					mOutputStream.write(bytes);
				}
			} catch (IOException e) { 
				
			}
		}

		/**
		 * 
		 * @return
		 */
		public boolean read() {
			try {
				if (mInputStream != null) {
					mInputStream.read(readBuffer);
					// Add the card to hand
					myActivity.runOnUiThread(new Runnable() {
					    public void run() {
					    	try {
								String s = new String(readBuffer, "UTF-8");
								mArrayAdapterCards.add(s);
								// Reset the buffer so that the old text won't be left to the next reading
								for (int i=0; i<readBuffer.length; i++) {
									readBuffer[i] = 0;
								}
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							}
					    }
					});
					return true;
				}
			} catch (IOException e) {

			}
			return false;
		}

		/** Will cancel an in-progress connection, and close the socket */
		public void cancel() {
			try {
				mBluetoothSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
