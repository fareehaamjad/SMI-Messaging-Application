package com.example.securemessaging;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class GetPhoneNo extends Activity
{

	public EditText phoneNumberField;
	public String imei;
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.input_phone_number);
		phoneNumberField = (EditText)findViewById(R.id.number);

		TelephonyManager mngr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE); 
		imei = mngr.getDeviceId();
		
	}
	
	@Override
	public void onBackPressed() {
		
		Log.i("back presses", "back Button pressed; killing the process");
		android.os.Process.killProcess(android.os.Process.myPid());
	}
	
	public void saveNo(View view)
	{
		showDialog();
		

	}

	private void showDialog() 
	{
		new AlertDialog.Builder(this)
		.setTitle("Please conform")
		.setMessage("Is " +phoneNumberField.getText().toString() +" your number?")
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

		    public void onClick(DialogInterface dialog, int whichButton) 
		    {
		    	Log.i("test",  "test etst");
				SharedPreferences.Editor e = MainActivity.s.edit();
				e.putString(MainActivity.phone_no, phoneNumberField.getText().toString());
				e.putString(MainActivity.KEY_imei, imei);
				
				Log.i("Phone No",  phoneNumberField.getText().toString());
				Log.i("IMEI number",  imei);
				while (!e.commit());
				finish();
		    }})
		 .setNegativeButton(android.R.string.no, null).show();
		
	}

}
