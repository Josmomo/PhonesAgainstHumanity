package com.jonassjoberg.phonesagainsthumanity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class HostActivity extends ActionBarActivity {

	private ServerThread serverThread;
	private ClientThread clientThread;
	private BluetoothDevice mBluetoothDevice;
	private BluetoothAdapter mBluetoothAdapter;
	
	private Button buttonNextCard;
	private TextView t;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_host);
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(mBluetoothAdapter.getAddress()); // "E4:B0:21:B7:9F:65"

		t = (TextView) findViewById(R.id.textViewTemp);

		buttonNextCard = (Button) findViewById(R.id.buttonNextCard);
		buttonNextCard.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				t.setText("Fungerar");
			}
		});

		serverThread = new ServerThread();
		clientThread = new ClientThread(mBluetoothDevice);
		serverThread.start();
		clientThread.start();
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

}
