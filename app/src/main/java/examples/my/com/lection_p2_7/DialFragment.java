package examples.my.com.lection_p2_7;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

/**
 * Created by vlad on 01/11/16.
 */

public class DialFragment extends Fragment {

	EditText _et;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		View root = inflater.inflate(R.layout.dial_layout, container, false);
		if (root == null) return null;

		int butIds[] = {
			R.id.button0,
			R.id.button1,
			R.id.button2,
			R.id.button3,
			R.id.button4,
			R.id.button5,
			R.id.button6,
			R.id.button7,
			R.id.button8,
			R.id.button9,
			R.id.button_p,
			R.id.button_c,
			R.id.button_b,
		};

		_et = (EditText)root.findViewById(R.id.editText);


		View.OnClickListener listener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
					case R.id.button0:
					case R.id.button1:
					case R.id.button2:
					case R.id.button3:
					case R.id.button4:
					case R.id.button5:
					case R.id.button6:
					case R.id.button7:
					case R.id.button8:
					case R.id.button9:
					case R.id.button_p: {
						String str = _et.getText().toString();
						String add = ((Button) v).getText().toString();
						_et.setText(str + add);
						break;
					}
					case R.id.button_b: {
						String str = _et.getText().toString();
						_et.setText(str.substring(0, str.length() - 1));
						break;
					}
					case R.id.button_c : {
						String numberToDial = "tel:"+_et.getText().toString();
						startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(numberToDial)));
					}
				}
			}
		};

		for (int id: butIds ) {
			Button b = (Button)root.findViewById(id);
			b.setOnClickListener(listener);
		}

		return root;
	}
}
