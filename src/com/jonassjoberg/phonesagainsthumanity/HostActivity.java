package com.jonassjoberg.phonesagainsthumanity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class HostActivity extends Activity {

	private final int NEW_GAME = 1;
	private final int NEW_TURN = 2;
	private final int WAITING_FOR_CLIENT_RESPONSE = 3;

	private ServerThread serverThread;
	private BluetoothDevice mBluetoothDevice;
	private BluetoothAdapter mBluetoothAdapter;
	private ArrayAdapter<String> mArrayAdapter;
	private ListView listView;
	private Activity myActivity;
	private Handler mHandler;

	private Button buttonBluetoothSendCommand;
	private TextView t2;
	private Deck deck;

	private int gameState = NEW_GAME;
	public boolean allPlayersAdded = false;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_host);

		myActivity = this;
		deck = new Deck(this, Color.WHITE, "deck_white.properties");
		mHandler = new Handler();

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
				gameState = NEW_TURN;
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

		private byte[] readBuffer = new byte[128];

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
			Intent discoverableIntent = new	Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
			startActivity(discoverableIntent);

			addPlayersToHost();

			while (true) {
				switch (gameState) {
				case NEW_GAME:
					for (int i=0; i<socketList.size(); i++) {
						write(deck.nextCard().getText().getBytes(), outputStreamList.get(i));
					}
					gameState = WAITING_FOR_CLIENT_RESPONSE;
					break;
				case NEW_TURN:
					for (int i=0; i<socketList.size(); i++) {
						// Send a card
						write(deck.nextCard().getText().getBytes(), outputStreamList.get(i));
					}
					gameState = WAITING_FOR_CLIENT_RESPONSE;
					break;
				default:
					try {
						sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					//					for (int i=0; i<socketList.size(); i++) {
					//						read(inputStreamList.get(i));
					//					}
				}
			}
		}

		private void addPlayersToHost() {
			
			mBluetoothSocket = null;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;
			
			// Do for all players
			while (!allPlayersAdded ) {

				try {
					// Listen for incoming connections
					Log.d(getName(), "Are listening");
					mBluetoothSocket = mBluetoothServerSocket.accept();
					if (mBluetoothSocket != null) {
						Log.d(getName(), "Accepted connection");
					} else {
						Log.d(getName(), "mBluetoothSocket = null");
						continue;
					}
				} catch (IOException e) {
					e.printStackTrace();
					continue;
				}


				// Try to get the input and outputstream, otherwise they will be set to null
				if (mBluetoothSocket != null) {
					try {
						if (mBluetoothSocket.getInputStream() != null) {
							tmpIn = mBluetoothSocket.getInputStream();
						}
						if (mBluetoothSocket.getOutputStream() != null) {
							tmpOut = mBluetoothSocket.getOutputStream();
						}
					} catch (IOException e) {
						e.printStackTrace();
						continue;
					}
				}


				// Add everything to corresponding lists
				socketList.add(mBluetoothSocket);
				inputStreamList.add(tmpIn);
				outputStreamList.add(tmpOut);

				// List the device as connected
				mHandler.post(new Runnable() {
					
					@Override
					public void run() {
						mArrayAdapter.add(mBluetoothSocket.getRemoteDevice().getName());
						// TODO Ask if another player should be added
					}
				});
//				myActivity.runOnUiThread(new Runnable() {
//					public void run() {
//						Log.i("", mArrayAdapter.toString());
//						Log.i("", mBluetoothSocket.toString());
//						mArrayAdapter.add(mBluetoothSocket.getRemoteDevice().getName());
//						// TODO Ask if another player should be added
//					}
//				});


				// Run on UI-thread
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						// Create an alertdialog and show it
						AlertDialog.Builder builder = new AlertDialog.Builder(myActivity);
						builder.setMessage("Add another player?")
						.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// FIRE ZE MISSILES!
							}
						})
						.setNegativeButton("No", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								allPlayersAdded = true;
							}
						});
						// Create the AlertDialog object and return it
						builder.create().show();
					}
				});

			}

			// Close the serverSocket when all devices are accepted
			try {
				mBluetoothServerSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
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
							for (int i=0; i<readBuffer.length; i++) {
								readBuffer[i] = 0;
							}
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
