package com.jonassjoberg.phonesagainsthumanity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import Utils.Constants;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;

/**
 * The ServerThread will handle all bluetooth communcation to devices.
 * @author Jonas
 * 
 */
public class ServerThread extends Thread implements Runnable  {
	private final int NEW_GAME = 1;
	private final int NEW_TURN = 2;
	private final int WAITING_FOR_CLIENT_RESPONSE = 3;
	private final int WAITING_FOR_SERVER_PICK = 4;
	private final int WAITING_FOR_CLIENT_STARTUP = 5;
	private int gameState = WAITING_FOR_CLIENT_STARTUP;

	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothSocket mBluetoothSocket;
	private BluetoothServerSocket mBluetoothServerSocket;
	private ArrayList<BluetoothSocket> socketList;
	private ArrayList<InputStream> inputStreamList;
	private ArrayList<OutputStream> outputStreamList;
	private final UUID uuid = UUID.fromString(Constants.UUID);

	private byte[] readBuffer = new byte[Constants.READ_BUFFER_SIZE];
	private Deck deckWhite;
	private HostActivity hostActivity;
	private GameHostActivity gameHostActivity;
	private Context hostActivityContext;
	private boolean allPlayersAdded = false;
	private boolean pickedWinningCard = false;
	private boolean wait = true;
	private int numberOfPlayers = 0;
	private HashMap<String, InputStream> cardToInputStream;

	public ServerThread(HostActivity a) {
		hostActivity = a;
		hostActivityContext = hostActivity.getApplicationContext();
		mBluetoothServerSocket = null;
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		socketList = new ArrayList<BluetoothSocket>();
		inputStreamList = new ArrayList<InputStream>();
		outputStreamList = new ArrayList<OutputStream>();
		cardToInputStream = new HashMap<String, InputStream>();
		deckWhite = new Deck(hostActivityContext, Color.WHITE, "deck_white.properties");

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
				for (int i=0; i<socketList.size(); i++) {
					for (int j=0; j<10; j++) {
						String text = deckWhite.nextCard().getText();
						while(!write((Constants.DECK_CARD + text + Constants.CARD_END_TAG).getBytes(), outputStreamList.get(i))) {}
					}
				}
				gameState = WAITING_FOR_CLIENT_RESPONSE;
				break;
			case NEW_TURN:
				for (int i=0; i<socketList.size(); i++) {
					String text = deckWhite.nextCard().getText();
					while(!write((Constants.DECK_CARD + text + Constants.CARD_END_TAG).getBytes(), outputStreamList.get(i))) {}
				}
				gameHostActivity.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						gameHostActivity.updateCardInHand();
					}
				});
				gameState = WAITING_FOR_CLIENT_RESPONSE;
				break;
			case WAITING_FOR_CLIENT_RESPONSE:
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				int count = 0;
				// Read one command from each player
				while (count < numberOfPlayers) {
					if (read(inputStreamList.get(count))) {
						count++;
					}
				}
				// All responses received
				gameState = WAITING_FOR_SERVER_PICK;
				break;
			case WAITING_FOR_SERVER_PICK:
				while (!pickedWinningCard) {
					try {
						sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				gameState = NEW_TURN;
				break;
			case WAITING_FOR_CLIENT_STARTUP:
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				int count2 = 0;
				// Read one command from each player
				while (count2 < numberOfPlayers) {
					if (read(inputStreamList.get(count2))) {
						count2++;
					}
				}
				// All responses received
				gameState = NEW_GAME;
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

			// Run on UI-thread
			hostActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// Create an alertdialog and show it
					AlertDialog.Builder builder = new AlertDialog.Builder(hostActivity);
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
							for (int i=0; i<socketList.size(); i++) {
								while (!write((Constants.START_GAME + "." + Constants.CARD_END_TAG).getBytes(), outputStreamList.get(i))) {}
							}

							hostActivity.startTheGame();
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
	public boolean read(final InputStream in) {

		try {
			if (in != null) {
				in.read(readBuffer);
				//				int skip = (int) in.skip(Constants.READ_BUFFER_SIZE);

				// Add the card to hand

				try {
					String s = new String(readBuffer, "UTF-8");
					String[] cards = s.split(Constants.CARD_END_TAG);
					for (String card : cards) {
						String command = card.substring(0, 3);
						final String message = card.substring(3);
	
						switch (command) {
						case Constants.RESPONSE_CARD:
							// TODO
							// Add message to this turns responslist
							gameHostActivity.runOnUiThread(new Runnable() {
								
								@Override
								public void run() {
									gameHostActivity.addCard(message);
									cardToInputStream.put(message, in);
								}
							});
							
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
					}
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}

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

	public void setGameHostActivity(GameHostActivity a) {
		gameHostActivity = a;
	}
	
	public void pickedWinningCard(String s) {
		for (int i=0; i<inputStreamList.size(); i++) {
			if (inputStreamList.get(0).equals(cardToInputStream.get(s))) {
				while (!write((Constants.POINT + "1." + Constants.CARD_END_TAG).getBytes(), outputStreamList.get(i))) {}
				break;
			}
		}
		pickedWinningCard = true;
	}
}
