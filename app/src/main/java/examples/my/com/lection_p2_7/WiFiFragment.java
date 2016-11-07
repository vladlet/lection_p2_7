package examples.my.com.lection_p2_7;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

/**
 * Created by vlad on 01/11/16.
 */

public class WiFiFragment extends Fragment {
	ListView lv;
	WifiManager wifi;
	String wifis[];
	WifiScanReceiver wifiReciever;
	View _root;

	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		_root = inflater.inflate(R.layout.wifi_layout, container, false);
		if (_root == null) return null;

		lv=(ListView)_root.findViewById(R.id.listView);

		wifi=(WifiManager)getActivity().getSystemService(Context.WIFI_SERVICE);
		wifiReciever = new WifiScanReceiver();
		wifi.startScan();
		return _root;
	}

	public void onPause() {
		getActivity().unregisterReceiver(wifiReciever);
		super.onPause();
	}

	public void onResume() {
		getActivity().registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		super.onResume();
	}

	private class WifiScanReceiver extends BroadcastReceiver {
		public void onReceive(Context c, Intent intent) {
			List<ScanResult> wifiScanList = wifi.getScanResults();
			wifis = new String[wifiScanList.size()];

			for(int i = 0; i < wifiScanList.size(); i++){
				wifis[i] = ((wifiScanList.get(i)).toString());
			}
			lv.setAdapter(new ArrayAdapter<String>(getContext().getApplicationContext(),android.R.layout.simple_list_item_1,wifis));
		}
	}
}
