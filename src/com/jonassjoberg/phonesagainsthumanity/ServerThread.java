package com.jonassjoberg.phonesagainsthumanity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class ServerThread extends Thread implements Runnable  {

	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothSocket mBluetoothSocket;
	private BluetoothServerSocket mBluetoothServerSocket;
	private InputStream mInputStream;
    private OutputStream mOutputStream;
	private ArrayList<BluetoothSocket> socketList;
	private final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	public ServerThread() {
		mBluetoothServerSocket = null;
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		socketList = new ArrayList<BluetoothSocket>();
		InputStream mInputStream = null;
        OutputStream mOutputStream = null;
        
		try {
			mBluetoothServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("ServerThread", uuid);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		mBluetoothSocket = null;
		
		while (true) {
			try {
				// Listen for incoming connections
				Log.d(getName(), "Are listening");
				mBluetoothSocket = mBluetoothServerSocket.accept(30000);				 
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
			
			if (mBluetoothSocket != null) {
				Log.d(getName(), "Accepted connection");
				socketList.add(mBluetoothSocket);
				break;
			} else {
				Log.d(getName(), "mBluetoothSocket = null");
			}
		}
	}
	
    public void write(byte[] bytes) {
        try {
            mOutputStream.write(bytes);
        } catch (IOException e) { }
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
