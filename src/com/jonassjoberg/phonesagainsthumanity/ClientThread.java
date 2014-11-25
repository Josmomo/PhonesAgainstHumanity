package com.jonassjoberg.phonesagainsthumanity;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class ClientThread extends Thread implements Runnable {

	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothDevice mBluetoothDevice;
	private BluetoothSocket mBluetoothSocket;
	private final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	public ClientThread(BluetoothDevice device) {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mBluetoothDevice = device;
		BluetoothSocket tmp = null;

		// Get a BluetoothSocket to connect with the given BluetoothDevice
		try {
			tmp = mBluetoothDevice.createRfcommSocketToServiceRecord(uuid);
		} catch (IOException e) {
			e.printStackTrace();
		}
		mBluetoothSocket = tmp;
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
		
		
		while(true) {
			// TODO Do something with the socket
		}
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