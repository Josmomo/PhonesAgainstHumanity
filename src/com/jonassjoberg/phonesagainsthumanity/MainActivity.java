package com.jonassjoberg.phonesagainsthumanity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	private static final int REQUEST_ENABLE_BLUETOOTH = 1;
	private BluetoothAdapter mBluetoothAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Check if the device has a bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter != null) {

			// Start bluetooth if it isn't on
			// Make the device visible to others
			if (!mBluetoothAdapter.isEnabled()) {
				// Start Bluetooth
				Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH);

				// TODO Use to activate discovery, copy to host activity
				//				Intent discoverableIntent = new	Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
				//				discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
				//				startActivity(discoverableIntent);
			}			
		} else {
			// Device does not support bluetooth
			// Close the application
			Log.d(this.getPackageName(), "No Bluetooth on this device");

			Toast.makeText(getApplicationContext(), "Sorry, your phone does not support Bluetooth", Toast.LENGTH_LONG).show();
			finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
		// Turn of bluetooth
		mBluetoothAdapter.disable();
		Toast.makeText(getApplicationContext(), "TURNING OFF BLUETOOTH", Toast.LENGTH_LONG).show();

		// TODO Auto-generated method stub
		super.onDestroy();
	}


	/**
	 * Start the join activity when the button is pressed.
	 * @param v The button
	 */
	public void joinActivity(View v) {
		Intent intent = new Intent(v.getContext(), JoinActivity.class);
		startActivity(intent);
	}

	/**
	 * Start the host activity when the button is pressed.
	 * @param v The button
	 */
	public void hostActivity(View v) {
		// TODO Only for printing purpose, remove later
		BluetoothDevice btd = mBluetoothAdapter.getRemoteDevice(mBluetoothAdapter.getAddress());
		System.out.println(btd.toString());

		Intent intent = new Intent(v.getContext(), HostActivity.class);
		startActivity(intent);
	}
}
