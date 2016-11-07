package examples.my.com.lection_p2_7;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import examples.my.com.lection_p2_7.record.NdefMessageParser;
import examples.my.com.lection_p2_7.record.ParsedNdefRecord;
import examples.my.com.lection_p2_7.record.SmartPoster;
import examples.my.com.lection_p2_7.record.TextRecord;
import examples.my.com.lection_p2_7.record.UriRecord;

import static android.R.attr.process;
import static android.os.Build.VERSION_CODES.M;

/**
 * Created by vlad on 01/11/16.
 */

public class NfcFragment extends Fragment {

	private static final String TAG = "NfcFragment";

	private static final DateFormat TIME_FORMAT = SimpleDateFormat.getDateTimeInstance();
	private LinearLayout mTagContent;
	PendingIntent pendingIntent;

	NfcAdapter mAdapter;

	View _root;
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		_root = inflater.inflate(R.layout.nfc_layout, container, false);
		mTagContent = (LinearLayout)_root.findViewById(R.id.list);

		mAdapter = NfcAdapter.getDefaultAdapter(getActivity());
		pendingIntent = PendingIntent.getActivity(
				getActivity(), 0, new Intent(getActivity(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		return _root;
	}

	public void processIntent(Intent intent) {

		String action = intent.getAction();
		if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
				|| NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
				|| NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
			Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
			NdefMessage[] msgs;
			if (rawMsgs != null) {
				msgs = new NdefMessage[rawMsgs.length];
				for (int i = 0; i < rawMsgs.length; i++) {
					msgs[i] = (NdefMessage) rawMsgs[i];
				}
			} else {
				// Unknown tag type
				byte[] empty = new byte[0];
				byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
				Parcelable tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
				byte[] payload = dumpTagData(tag).getBytes();
				NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, id, payload);
				NdefMessage msg = new NdefMessage(new NdefRecord[] { record });
				msgs = new NdefMessage[] { msg };
			}
			// Setup the views
			buildTagViews(msgs);
		}
	}

	@Override
	public void onPause() {
		mAdapter.disableForegroundDispatch(getActivity());
		super.onPause();
	}

	@Override
	public void onResume() {
		mAdapter.enableForegroundDispatch(getActivity(), pendingIntent, null, null);
		super.onResume();
	}

	private String dumpTagData(Parcelable p) {
		StringBuilder sb = new StringBuilder();
		Tag tag = (Tag) p;
		byte[] id = tag.getId();
		sb.append("Tag ID (hex): ").append(getHex(id)).append("\n");
		sb.append("Tag ID (dec): ").append(getDec(id)).append("\n");
		sb.append("ID (reversed): ").append(getReversed(id)).append("\n");

		String prefix = "android.nfc.tech.";
		sb.append("Technologies: ");
		for (String tech : tag.getTechList()) {
			sb.append(tech.substring(prefix.length()));
			sb.append(", ");
		}
		sb.delete(sb.length() - 2, sb.length());
		for (String tech : tag.getTechList()) {
			if (tech.equals(MifareClassic.class.getName())) {
				sb.append('\n');
				MifareClassic mifareTag = MifareClassic.get(tag);
				String type = "Unknown";
				switch (mifareTag.getType()) {
					case MifareClassic.TYPE_CLASSIC:
						type = "Classic";
						break;
					case MifareClassic.TYPE_PLUS:
						type = "Plus";
						break;
					case MifareClassic.TYPE_PRO:
						type = "Pro";
						break;
				}
				sb.append("Mifare Classic type: ");
				sb.append(type);
				sb.append('\n');

				sb.append("Mifare size: ");
				sb.append(mifareTag.getSize() + " bytes");
				sb.append('\n');

				sb.append("Mifare sectors: ");
				sb.append(mifareTag.getSectorCount());
				sb.append('\n');

				sb.append("Mifare blocks: ");
				sb.append(mifareTag.getBlockCount());
			}

			if (tech.equals(MifareUltralight.class.getName())) {
				sb.append('\n');
				MifareUltralight mifareUlTag = MifareUltralight.get(tag);
				String type = "Unknown";
				switch (mifareUlTag.getType()) {
					case MifareUltralight.TYPE_ULTRALIGHT:
						type = "Ultralight";
						break;
					case MifareUltralight.TYPE_ULTRALIGHT_C:
						type = "Ultralight C";
						break;
				}
				sb.append("Mifare Ultralight type: ");
				sb.append(type);
			}
		}

		return sb.toString();
	}

	private String getHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (int i = bytes.length - 1; i >= 0; --i) {
			int b = bytes[i] & 0xff;
			if (b < 0x10)
				sb.append('0');
			sb.append(Integer.toHexString(b));
			if (i > 0) {
				sb.append(" ");
			}
		}
		return sb.toString();
	}

	private long getDec(byte[] bytes) {
		long result = 0;
		long factor = 1;
		for (int i = 0; i < bytes.length; ++i) {
			long value = bytes[i] & 0xffl;
			result += value * factor;
			factor *= 256l;
		}
		return result;
	}

	private long getReversed(byte[] bytes) {
		long result = 0;
		long factor = 1;
		for (int i = bytes.length - 1; i >= 0; --i) {
			long value = bytes[i] & 0xffl;
			result += value * factor;
			factor *= 256l;
		}
		return result;
	}

	void buildTagViews(NdefMessage[] msgs) {
		if (msgs == null || msgs.length == 0) {
			return;
		}
		LayoutInflater inflater = LayoutInflater.from(getContext());
		LinearLayout content = mTagContent;

		// Parse the first message in the list
		// Build views for all of the sub records
		Date now = new Date();
		List<ParsedNdefRecord> records = NdefMessageParser.parse(msgs[0]);
		final int size = records.size();
		for (int i = 0; i < size; i++) {
			TextView timeView = new TextView(getContext());
			timeView.setText(TIME_FORMAT.format(now));
			content.addView(timeView, 0);
			ParsedNdefRecord record = records.get(i);
			content.addView(record.getView(getActivity(), inflater, content, i), 1 + i);
			content.addView(inflater.inflate(R.layout.tag_divider, content, false), 2 + i);
		}
	}

}
