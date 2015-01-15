package com.jonassjoberg.phonesagainsthumanity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import Utils.Constants;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final int REQUEST_ENABLE_BLUETOOTH = 1;
	private BluetoothAdapter mBluetoothAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Check if the device has a bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter != null) {

			// Restart bluetooth
			mBluetoothAdapter.disable();

			Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH);

			// TODO Use to activate discovery, copy to host activity
			//				Intent discoverableIntent = new	Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			//				discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
			//				startActivity(discoverableIntent);

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
		Intent intent = new Intent(v.getContext(), com.jonassjoberg.phonesagainsthumanity.JoinActivity.class);
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

		Intent intent = new Intent(v.getContext(), com.jonassjoberg.phonesagainsthumanity.HostActivity.class);
		startActivity(intent);
	}

	/**
	 * Start the download thread when button is pressed.
	 * @param v The button
	 */
	public void startDownload(View v) {
		new DownloadTask().execute(Constants.DOWNLOAD_URL);
	}














	private class DownloadTask extends AsyncTask<String, String, Void> {
		private ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
		private InputStream inputStream = null;
		private String downloadResult = "";

		private File fileWhite = new File(Environment.getExternalStorageDirectory(), "deck_white.properties");
		private File fileBlack = new File(Environment.getExternalStorageDirectory(), "deck_black.properties");
		private FileWriter fileWriterWhite, fileWriterBlack;

		@Override
		protected void onPreExecute() {
			// Create files if they don't exists already
			try {
				if (!fileWhite.exists()) {
					fileWhite.createNewFile();
				}
				if (!fileBlack.exists()) {
					fileBlack.createNewFile();
				}
				fileWriterWhite = new FileWriter(fileWhite);
				fileWriterBlack = new FileWriter(fileBlack);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		protected Void doInBackground(String... arg0) {
	        ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();

	        try {
	            // Set up HTTP post

	            // HttpClient is more then less deprecated. Need to change to URLConnection
	            HttpClient httpClient = new DefaultHttpClient();

	            HttpPost httpPost = new HttpPost(arg0[0]);
	            httpPost.setEntity(new UrlEncodedFormEntity(param));
	            HttpResponse httpResponse = httpClient.execute(httpPost);
	            HttpEntity httpEntity = httpResponse.getEntity();

	            // Read content & Log
	            inputStream = httpEntity.getContent();
	        } catch (UnsupportedEncodingException e) {
	            e.printStackTrace();
	        } catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} 
	        
	        // Read from the inputstream and convert response to string using StringBuilder
	        try {
	            BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"), 8);
	            StringBuilder sBuilder = new StringBuilder();

	            String line = bReader.readLine();
	            String[] tmp = line.split("masterCards = ");
	            sBuilder.append(tmp[1] + '\n');
	            while ((line = bReader.readLine()) != null) {
	                sBuilder.append(line + "\n");
	            }

	            inputStream.close();
	            downloadResult = sBuilder.toString();

	        } catch (Exception e) {
	            Log.e("StringBuilding & BufferedReader", "Error converting downloadResult " + e.toString());
	        }
			return null;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			// TODO
			// Update dialog
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO
			// Convert JSON and write to corresponding files
			
			try {
//				JSONObject jsonObject = new JSONObject(test);
				JSONArray jsonArray = new JSONArray(downloadResult);
				
				JSONObject tmp = null;
				for (int i=0; i<jsonArray.length(); i++) {
					try {
				        tmp = jsonArray.getJSONObject(i);
				        // Pulling items from the array
				        String cardType = tmp.getString("cardType");
				        String text = tmp.getString("text");
				        
				        if (cardType.equals("A")) {
				        	fileWriterWhite.write(i + "=" + text + '\n');
				        } else if (cardType.equals("Q")) {
				        	fileWriterBlack.write(i + "=" + text + '\n');
				        }
				        
				    } catch (JSONException e) {
				    	e.printStackTrace();
				    } catch (IOException e) {
						e.printStackTrace();
					}
				}
				fileWriterWhite.flush();
				fileWriterBlack.flush();
				fileWriterWhite.close();
				fileWriterBlack.close();
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}

	}
}
