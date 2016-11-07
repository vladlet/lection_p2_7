package examples.my.com.lection_p2_7;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by vlad on 01/11/16.
 */

public class BTFragment extends Fragment {
	protected static final String TAG = "bluetoothdemo";
	int REQUEST_ENABLE_BT = 1;
	EditText main;
	private ArrayAdapter<String> mNewDevicesArrayAdapter;

	View _root;

	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		_root = inflater.inflate(R.layout.bt_layout, container, false);
		if (_root == null) return null;
		main = (EditText) _root.findViewById(R.id.mainTextArea);
		mNewDevicesArrayAdapter = new ArrayAdapter<String>(getContext(), R.string.app_name);

		// Hook up the Discover button to its handler
		Button discover = (Button) _root.findViewById(R.id.discoverButton);
		discover.setOnClickListener(discoverButtonHandler);

		// Hook up the ArrayAdapter to the ListView
		ListView lv = (ListView) _root.findViewById(R.id.pairedBtDevices);
		lv.setAdapter(mNewDevicesArrayAdapter);

		BluetoothAdapter BT = BluetoothAdapter.getDefaultAdapter();
		if (BT == null) {
			String noDevMsg = "This device does not appear to have a Bluetooth adapter, sorry";
			main.setText(noDevMsg);
			Toast.makeText(getContext(), noDevMsg, Toast.LENGTH_LONG).show();
		}
		if (BT != null && !BT.isEnabled()) {
			// Ask user's permission to switch the Bluetooth adapter On.
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		}
		return _root;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode==REQUEST_ENABLE_BT) {
			if (resultCode== Activity.RESULT_OK) {
				BluetoothAdapter BT = BluetoothAdapter.getDefaultAdapter();
				String address = BT.getAddress();
				String name = BT.getName();
				String connectedMsg = "BT is on; your device is "  + name + " : " + address;
				main.setText(connectedMsg);
				Toast.makeText(getContext(), connectedMsg, Toast.LENGTH_LONG).show();
				Button discoverButton = (Button) _root.findViewById(R.id.discoverButton);
				discoverButton.setOnClickListener(discoverButtonHandler);
			} else {
				Toast.makeText(getContext(), "Failed to enable Bluetooth adapter!", Toast.LENGTH_LONG).show();
			}
		} else {
			Toast.makeText(getContext(), "Unknown RequestCode " + requestCode, Toast.LENGTH_LONG).show();
		}
	}

	/** When the user clicks the Discover button, get the list of paired devices
	 */
	View.OnClickListener discoverButtonHandler = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Log.d(TAG, "in onClick(" + v + ")");
			// IntentFilter for found devices
			IntentFilter foundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
			// Broadcast receiver for any matching filter
			BTFragment.this.getContext().registerReceiver(mReceiver, foundFilter);

			IntentFilter doneFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
			BTFragment.this.getContext().registerReceiver(mReceiver, doneFilter);
		}
	};

	public void onDetach() {
		getContext().unregisterReceiver(mReceiver);
		super.onDetach();
	}

	/** Receiver for the BlueTooth Discovery Intent; put the paired devices
	 * into the viewable list.
	 */
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d(TAG, "in onReceive, action = " + action);

			if (BluetoothDevice.ACTION_FOUND.equals(action)){
				BluetoothDevice btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

				if(btDevice.getBondState() != BluetoothDevice.BOND_BONDED){
					// XXX use a better type and a Layout in the visible list
					mNewDevicesArrayAdapter.add(btDevice.getName()+"\n"+btDevice.getAddress());
				}
			}
			else
			if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
				getActivity().setProgressBarIndeterminateVisibility(false);
				getActivity().setTitle(R.string.select_device);
				if (mNewDevicesArrayAdapter.getCount() == 0){
					String noDevice = getResources().getText(R.string.none_paired).toString();
					mNewDevicesArrayAdapter.add(noDevice);
				}
			}
		}
	};



	private class AcceptThread extends Thread {
		private final BluetoothServerSocket mmServerSocket;

		private final BluetoothAdapter mBluetoothAdapter;
		private final String NAME = "LectionBT";
		private final UUID MY_UUID = UUID.randomUUID();

		public AcceptThread(BluetoothAdapter bta) {
			mBluetoothAdapter = bta;
			// Use a temporary object that is later assigned to mmServerSocket,
			// because mmServerSocket is final
			BluetoothServerSocket tmp = null;
			try {
				// MY_UUID is the app's UUID string, also used by the client code
				tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
			} catch (IOException e) { }
			mmServerSocket = tmp;
		}

		public void run() {
			BluetoothSocket socket = null;
			// Keep listening until exception occurs or a socket is returned
			while (true) {
				try {
					socket = mmServerSocket.accept();
				} catch (IOException e) {
					break;
				}
				// If a connection was accepted
				if (socket != null) {
					// Do work to manage the connection (in a separate thread)
					manageConnectedSocket(socket);
					try {
						mmServerSocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				}
			}
		}

		/** Will cancel the listening socket, and cause the thread to finish */
		public void cancel() {
			try {
				mmServerSocket.close();
			} catch (IOException e) { }
		}

		public void manageConnectedSocket(BluetoothSocket socket) {
			new ConnectedThread(socket).start();
		}
	}

	private class ConnectThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;
		private final UUID MY_UUID = UUID.randomUUID();
		private final BluetoothAdapter mBluetoothAdapter;

		public ConnectThread(BluetoothDevice device, BluetoothAdapter adapter) {
			mBluetoothAdapter = adapter;
			// Use a temporary object that is later assigned to mmSocket,
			// because mmSocket is final
			BluetoothSocket tmp = null;
			mmDevice = device;

			// Get a BluetoothSocket to connect with the given BluetoothDevice
			try {
				// MY_UUID is the app's UUID string, also used by the server code
				tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
			} catch (IOException e) { }
			mmSocket = tmp;
		}

		public void run() {
			// Cancel discovery because it will slow down the connection
			mBluetoothAdapter.cancelDiscovery();

			try {
				// Connect the device through the socket. This will block
				// until it succeeds or throws an exception
				mmSocket.connect();
			} catch (IOException connectException) {
				// Unable to connect; close the socket and get out
				try {
					mmSocket.close();
				} catch (IOException closeException) { }
				return;
			}

			// Do work to manage the connection (in a separate thread)
			manageConnectedSocket(mmSocket);
		}

		/** Will cancel an in-progress connection, and close the socket */
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) { }
		}

		public void manageConnectedSocket(BluetoothSocket socket) {
			new ConnectedThread(socket).start();
		}
	}

	private class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		public ConnectedThread(BluetoothSocket socket) {
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the input and output streams, using temp objects because
			// member streams are final
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) { }

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run() {
			byte[] buffer = new byte[1024];  // buffer store for the stream
			int bytes; // bytes returned from read()

			// Keep listening to the InputStream until an exception occurs
			while (true) {
				try {
					// Read from the InputStream
					bytes = mmInStream.read(buffer);
					// Send the obtained bytes to the UI activity
					//mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
					//		.sendToTarget();
				} catch (IOException e) {
					break;
				}
			}
		}

		/* Call this from the main activity to send data to the remote device */
		public void write(byte[] bytes) {
			try {
				mmOutStream.write(bytes);
			} catch (IOException e) { }
		}

		/* Call this from the main activity to shutdown the connection */
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) { }
		}
	}
}
