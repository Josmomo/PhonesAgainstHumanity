package com.jonassjoberg.phonesagainsthumanity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class HostActivity extends ActionBarActivity {

	private ServerThread serverThread;
	private BluetoothDevice mBluetoothDevice;
	private BluetoothAdapter mBluetoothAdapter;
	private ArrayAdapter<String> mArrayAdapter;
	private ListView listView;
	private Activity myActivity;

	private Button buttonBluetoothSendCommand;
	private TextView t2;
	private boolean newTurn = true;
	private Deck deck;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_host);

		myActivity = this;
		deck = new Deck(this, Color.WHITE, "deck_white.properties");

		mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

		listView = (ListView) findViewById(R.id.listViewBluetoothConnectedDevices);
		listView.setAdapter(mArrayAdapter);


		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(mBluetoothAdapter.getAddress()); // "E4:B0:21:B7:9F:65"

		t2 = (TextView) findViewById(R.id.textViewReceive);

		buttonBluetoothSendCommand = (Button) findViewById(R.id.buttonBluetoothSendCommand);

		buttonBluetoothSendCommand.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
			}
		});

		serverThread = new ServerThread();
		serverThread.start();
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


	/**
	 * The ServerThread will handle all bluetooth communcation to devices.
	 * @author Jonas
	 *
	 */
	private class ServerThread extends Thread implements Runnable  {

		private BluetoothAdapter mBluetoothAdapter;
		private BluetoothSocket mBluetoothSocket;
		private BluetoothServerSocket mBluetoothServerSocket;
		private ArrayList<BluetoothSocket> socketList;
		private ArrayList<InputStream> inputStreamList;
		private ArrayList<OutputStream> outputStreamList;
		private final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

		private byte[] readBuffer = new byte[80];

		public ServerThread() {
			mBluetoothServerSocket = null;
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			socketList = new ArrayList<BluetoothSocket>();
			inputStreamList = new ArrayList<InputStream>();
			outputStreamList = new ArrayList<OutputStream>();

			try {
				mBluetoothServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("ServerThread", uuid);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {

			for (int i=0; i<1; i++) {
				mBluetoothSocket = null;
				InputStream tmpIn = null;
				OutputStream tmpOut = null;

				try {
					// Listen for incoming connections
					Log.d(getName(), "Are listening");
					mBluetoothSocket = mBluetoothServerSocket.accept();
					if (mBluetoothSocket != null) {
						Log.d(getName(), "Accepted connection");
						socketList.add(mBluetoothSocket);
					} else {
						Log.d(getName(), "mBluetoothSocket = null");
					}
				} catch (IOException e) {
					e.printStackTrace();
					break;
				}


				// Try to get the input and outputstream, otherwise the will be set to null
				if (mBluetoothSocket != null) {
					try {
						if (mBluetoothSocket.getInputStream() != null) {
							tmpIn = mBluetoothSocket.getInputStream();
							inputStreamList.add(tmpIn);
						}
						if (mBluetoothSocket.getOutputStream() != null) {
							tmpOut = mBluetoothSocket.getOutputStream();
							outputStreamList.add(tmpOut);
						}
					} catch (IOException e) {
					}
				}

				myActivity.runOnUiThread(new Runnable() {
					public void run() {
						mArrayAdapter.add(mBluetoothSocket.getRemoteDevice().getName());
					}
				});
			}

			// Close the serverSocket when all devices are accepted
			try {
				mBluetoothServerSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			while (true) {
				for (int i=0; i<socketList.size(); i++) {
					read(inputStreamList.get(i));
				}

				if (newTurn) {
					for (int i=0; i<socketList.size(); i++) {
						// Send a card
						write(deck.nextCard().getText().getBytes(), outputStreamList.get(i));
					}
					newTurn = false;
				}

				try {
					sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		public void write(byte[] bytes, OutputStream out) {
			try {
				out.write(bytes);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public boolean read(InputStream in) {
			try {
				in.read(readBuffer, 0, readBuffer.length);
				System.out.println(readBuffer);
				myActivity.runOnUiThread(new Runnable() {
					public void run() {
						try {
							String s = new String(readBuffer, "UTF-8");
							t2.setText(s);
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				});
				return true;
			} catch (IOException e) {

			}
			return false;
		}

		public void close() {
			try {
				// Close all sockets
				for (int i=0; i<socketList.size(); i++) {
					socketList.get(i).close();
				}
				mBluetoothServerSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
