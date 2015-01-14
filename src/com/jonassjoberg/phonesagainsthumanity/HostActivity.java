package com.jonassjoberg.phonesagainsthumanity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.UUID;

import Utils.Constants;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
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
		
		listViewRespondCards = (ListView) findViewById(R.id.ListViewRespondCards);
		listViewRespondCards.setAdapter(mArrayAdapterCards);

		buttonBluetoothSendCommand = (Button) findViewById(R.id.buttonBluetoothSendCommand);

		buttonBluetoothSendCommand.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				gameState = NEW_TURN;
			}
		});

		serverThread = new ServerThread(this, new Handler());
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





	private class ServerThread extends Thread implements Runnable  {
		
		private BluetoothAdapter mBluetoothAdapter;
		private BluetoothSocket mBluetoothSocket;
		private BluetoothServerSocket mBluetoothServerSocket;
		private ArrayList<BluetoothSocket> socketList;
		private ArrayList<InputStream> inputStreamList;
		private ArrayList<OutputStream> outputStreamList;
		private final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

		private byte[] readBuffer = new byte[Constants.READ_BUFFER_SIZE];
		private Object syncToken;
		private Deck deck;
		private Activity myActivity;
		private Context hostActivityContext;
		private boolean allPlayersAdded = false;
		private boolean wait = true;
		private Handler mHandler;
		private int numberOfPlayers = 0;

		public ServerThread(Activity a, Handler h) {
			myActivity = a;
			mHandler = h;
			hostActivityContext = myActivity.getApplicationContext();
			mBluetoothServerSocket = null;
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			socketList = new ArrayList<BluetoothSocket>();
			inputStreamList = new ArrayList<InputStream>();
			outputStreamList = new ArrayList<OutputStream>();
			deck = new Deck(hostActivityContext, Color.WHITE, "deck_white.properties");

			try {
				mBluetoothServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("ServerThread", uuid);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			addPlayersToHost();

			while (true) {
				switch (gameState) {
				case NEW_GAME:
					for (int i=0; i<3; i++) {
						for (int j=0; j<socketList.size(); j++) {
							while(!write((Constants.DECK_CARD + deck.nextCard().getText()).getBytes(), outputStreamList.get(j))) {}
						}
					}
					gameState = WAITING_FOR_CLIENT_RESPONSE;
					break;
				case NEW_TURN:
					for (int i=0; i<socketList.size(); i++) {
						// Send a card
						while(!write((Constants.DECK_CARD + deck.nextCard().getText()).getBytes(), outputStreamList.get(i))) {}
						try {
							sleep(2000); // TODO remove
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					gameState = WAITING_FOR_CLIENT_RESPONSE;
					break;
				case WAITING_FOR_CLIENT_RESPONSE:
					try {
						sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					int count = 0;
					while (count < numberOfPlayers) {
						if (read(inputStreamList.get(count))) {
							count++;
						}
					}
					// All responses received
					gameState = NEW_TURN; // TODO Change to right state later
					break;
					default:
						break;
				}
			}
		}

		/**
		 * 
		 * Blocks until all players have connected.
		 */
		private void addPlayersToHost() {

			mBluetoothSocket = null;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Do for all players
			while (numberOfPlayers < 8) {

				try {
					// Listen for incoming connections
					Log.d(getName(), "Are listening");
					mBluetoothSocket = mBluetoothServerSocket.accept();
					if (mBluetoothSocket != null) {
						Log.d(getName(), "Accepted connection");
					} else {
						Log.d(getName(), "mBluetoothSocket = null");
						break;
					}
				} catch (IOException e) {
					e.printStackTrace();
					break;
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
						break;
					}
				}


				// Add everything to corresponding lists
				socketList.add(mBluetoothSocket);
				inputStreamList.add(tmpIn);
				outputStreamList.add(tmpOut);
				numberOfPlayers++;

				// List the device as connected
				mHandler.post(new Runnable() {

					@Override
					public void run() {

						mArrayAdapter.add(mBluetoothSocket.getRemoteDevice().getName());
						// TODO Ask if another player should be added
					}
				});

				// Run on UI-thread
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						// Create an alertdialog and show it
						AlertDialog.Builder builder = new AlertDialog.Builder(myActivity);
						builder.setMessage("Add another player?")
						.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								wait = false;
							}
						})
						.setNegativeButton("No", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								allPlayersAdded = true;
								wait = false;
							}
						});
						// Create the AlertDialog object and return it
						builder.create().show();
					}
				});


				// Waiting for host answer
				while (wait) {
					try {
						sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				wait = true;
				if (allPlayersAdded) {
					break;
				}
			}

			// Close the serverSocket when all devices are accepted
			try {
				mBluetoothServerSocket.close();
				mBluetoothAdapter.cancelDiscovery();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		

		/**
		 * Tries to write to out.
		 * @param bytes What to write
		 * @param out The Outputstream to write to.
		 * @return True on success, otherwise false.
		 */
		public boolean write(byte[] bytes, OutputStream out) {
			try {
				if (out != null) {
					out.write(bytes);
					out.flush();
					return true;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return false;
		}

		/**
		 * Tries to read from in and then do appropriate work according to the command within the message.
		 * @param in The Inputstream to read from.
		 * @return True if the read is successful, otherwise false.
		 */
		public boolean read(InputStream in) {
			
			try {
				if (in != null) {
					in.read(readBuffer);
					int skip = (int) in.skip(Constants.READ_BUFFER_SIZE);
					
					// Add the card to hand
					mHandler.post(new Runnable() {
					    public void run() {
					    	try {
					    		String s = new String(readBuffer, "UTF-8");
								String command = s.substring(0, 3);
								String message = s.substring(3);
								
								switch (command) {
								case Constants.RESPONSE_CARD:
									// TODO
									// Add message to this turns responslist
									mArrayAdapterCards.add(message);
									break;
								case Constants.VOTE_CARD:
									// TODO
									break;
								case Constants.READ_CHECK:
									break;
								default:
									break;
								}
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
				e.printStackTrace();
			}
			return false;
		}

		public void close() {

			try {
				// Close all sockets
				for (int i=0; i<socketList.size(); i++) {
					socketList.get(i).close();
				}
				mBluetoothAdapter.cancelDiscovery();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}
