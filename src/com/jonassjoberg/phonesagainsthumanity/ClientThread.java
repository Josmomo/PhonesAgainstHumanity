package com.jonassjoberg.phonesagainsthumanity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Stack;
import java.util.UUID;

import Utils.Constants;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

/**
 * The ClientThread will handle all bluetooth communication to a host server.
 * @author Jonas
 *
 */
public class ClientThread extends Thread implements Runnable {

	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothDevice mBluetoothDevice;
	private BluetoothSocket mBluetoothSocket;
	private final InputStream mInputStream;
	private final OutputStream mOutputStream;
	private final UUID uuid = UUID.fromString(Constants.UUID);

	private byte[] readBuffer = new byte[Constants.READ_BUFFER_SIZE];
	private JoinActivity joinActivity;
	private GameActivity gameActivity;
	private Handler mHandler;
	private String rest = "";
	private Stack<String> readStack;

	public ClientThread(BluetoothDevice device, JoinActivity a, Handler h) {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mBluetoothDevice = device;
		joinActivity = a;
		mHandler = h;
		BluetoothSocket tmp = null;
		InputStream tmpIn = null;
		OutputStream tmpOut = null;
		readStack = new Stack<String>();

		// Get a BluetoothSocket to connect with the given BluetoothDevice
		try {
			tmp = mBluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);
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
			connectException.printStackTrace();
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
	 * @return
	 */
	public boolean write(byte[] bytes) {
		try {
			if (mOutputStream != null) {
				mOutputStream.write(bytes);
				return true;
			}
		} catch (IOException e) { 
			e.printStackTrace();
		}
		return false;
	}

	public boolean read() {
		try {
			if (mInputStream != null) {
				mInputStream.read(readBuffer);
				//				int skip = (int) mInputStream.skip(Constants.READ_BUFFER_SIZE);
				readStack.add(new String(readBuffer, "UTF-8"));

				// Reset the buffer so that the old text won't be left to the next reading
				for (int i=0; i<readBuffer.length; i++) {
					readBuffer[i] = 0;
				}

				// Add the card to hand

				String s = readStack.pop();
				String[] cards = s.split(Constants.CARD_END_TAG);
				for (String card : cards) {
					if (card.endsWith(".")) {
						String command = card.substring(0, 3);
						String message = card.substring(3);

						switch (command) {
						case Constants.DECK_CARD:
							gameActivity.addCard(message);
							break;
						case Constants.VOTE_CARD:
							//gameActivity.addToAdapterVoteCards(message);
							break;
						case Constants.POINT:
//							gameActivity.addPoints(Integer.parseInt(message));
							break;
						case Constants.START_GAME:
							Intent i = new Intent(joinActivity, GameActivity.class);
							joinActivity.startActivity(i);
							break;
						default:
						}
					}
				}

				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
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

	public void setGameActivity(GameActivity a) {
		 gameActivity = a;
	}
}