package examples.my.com.lection_p2_7;

import android.Manifest;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ActionMode;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import static android.R.attr.data;
import static android.R.id.switch_widget;
import static android.R.id.toggle;
import static android.R.id.widget_frame;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
import static android.os.Build.VERSION_CODES.N;

public class MainActivity extends AppCompatActivity
		implements NavigationView.OnNavigationItemSelectedListener {

	ActionBarDrawerToggle _actionBarDrawerToggle;
	private static final String TAG = "MainActivity";

	private final static int MY_REQUEST_PERMISSION = 1002;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		_actionBarDrawerToggle = new ActionBarDrawerToggle(
				this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.setDrawerListener(_actionBarDrawerToggle);
		_actionBarDrawerToggle.syncState();

		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);

		if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
			showRequestPermissionRationale();
		} else {
			requestPermission();
		}



		Intent intent = getIntent();
		Log.d(TAG, "onCreate() called with: savedInstanceState = [" + savedInstanceState + "]");
		if (intent != null)
			processIntent(intent);


	}

	@Override
	protected void onNewIntent(Intent intent) {
		Log.d(TAG, "onNewIntent() called with: intent = [" + intent + "]");
		super.onNewIntent(intent);
		if (intent != null)
			processIntent(intent);
	}

	public void processIntent(Intent intent) {
		Log.d(TAG, "processIntent() called with: intent = [" + intent + "]");

		if (intent == null)
			return;
		String action = intent.getAction();
		if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
				|| NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
				|| NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
			startFragment(R.id.nav_nfc);
			Fragment frag = getSupportFragmentManager().findFragmentById(R.id.main_fragment);
			if (frag instanceof NfcFragment) {
				((NfcFragment)frag).processIntent(intent);
			}
		}
	}

	private void showRequestPermissionRationale() {
		AlertDialog.Builder builder;
		builder = new AlertDialog.Builder(this);
		builder.setMessage("Need camera!")
				.setTitle("Permission")
				//.setIcon(android.R.drawable.ic_dialog_info)
				.setCancelable(true)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						requestPermission();
					}
				})
				.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				})
				.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						finish();
					}
				});
		AlertDialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}


	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		if (requestCode == MY_REQUEST_PERMISSION) {
			if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
					grantResults[1] == PackageManager.PERMISSION_GRANTED ) {
			} else {
				finish();
			}
		} else {
			super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}

	private void requestPermission() {

		if (PermissionChecker.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PermissionChecker.PERMISSION_GRANTED
			|| PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PermissionChecker.PERMISSION_GRANTED ) {

			ActivityCompat.requestPermissions(this,
					new String[]{
							Manifest.permission.READ_PHONE_STATE,
							Manifest.permission.ACCESS_COARSE_LOCATION,
					},
					MY_REQUEST_PERMISSION);
		}
	}

	@Override
	public void onBackPressed() {
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		Fragment frag = getSupportFragmentManager().findFragmentById(R.id.main_fragment);
		if (frag == null) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.add(R.id.main_fragment, new DialFragment());
			ft.commit();
			//if (null != getSupportActionBar()) getSupportActionBar().setTitle(R.string.dictionaries);
			_actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Fragment frag = getSupportFragmentManager().findFragmentById(R.id.main_fragment);
		if (frag != null)
			frag.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void clearBackStack() {
		FragmentManager fm = getSupportFragmentManager();
		int cnt = fm.getBackStackEntryCount();
		for (int i = 0; i < cnt; ++i) {
			fm.popBackStackImmediate();
		}
	}

	private void startFragment(int id) {
		Fragment frag = getSupportFragmentManager().findFragmentById(R.id.main_fragment);
		Fragment newFrag = null;

	    switch(id) {
			case R.id.nav_dialer:
				if (!(frag instanceof DialFragment)) {
					newFrag = new DialFragment();
				}
				break;
			case R.id.nav_tele:
				if (!(frag instanceof TeleFragment)) {
					newFrag = new TeleFragment();
				}
				break;
			case R.id.nav_bt:
				if (!(frag instanceof BTFragment)) {
					newFrag = new BTFragment();
				}
				break;
			case R.id.nav_wifi:
				if (!(frag instanceof WiFiFragment)) {
					newFrag = new WiFiFragment();
				}
				break;
			case R.id.nav_nfc:
				if (!(frag instanceof NfcFragment)) {
					newFrag = new NfcFragment();
				}
				break;
		}

		if (frag != newFrag && newFrag != null) {
			clearBackStack();
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.add(R.id.main_fragment, newFrag);
			ft.commit();
			_actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
		}
	}

	@SuppressWarnings("StatementWithEmptyBody")
	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		// Handle navigation view item clicks here.
		int id = item.getItemId();
		startFragment(id);

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START);
		return true;
	}
}
