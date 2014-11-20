package com.jonassjoberg.phonesagainsthumanity;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	private Button buttonJoin, buttonHost;
	private BluetoothAdapter mBluetoothAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		buttonJoin = (Button) findViewById(R.id.buttonJoin);
		buttonHost = (Button) findViewById(R.id.buttonHost);

		// Check if the device has a Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter != null) {
			// Start Bluetooth if it isn't on
			// Make the device visible to others
			if (!mBluetoothAdapter.isEnabled()) {
				Intent discoverableIntent = new	Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
				discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
				startActivity(discoverableIntent);
			}
		} else {
			// Device does not support Bluetooth
			// The device should not continue
			Log.d(this.getPackageName(), "No Bluetooth on this device");

			Context context = getApplicationContext();
			String text = "Sorry, your phone does not support Bluetooth";

			Toast.makeText(context, text, Toast.LENGTH_LONG).show();
			finish();
		}


		buttonJoin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(), ClientActivity.class);
				startActivity(intent);
			}
		});

//		buttonHost.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				Intent intent = new Intent(v.getContext(), ServerActivity.class);
//				startActivity(intent);
//			}
//		});
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
}
