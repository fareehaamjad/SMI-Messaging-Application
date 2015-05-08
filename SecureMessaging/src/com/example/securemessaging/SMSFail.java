package com.example.securemessaging;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

public class SMSFail extends Activity
{
	String className = "SMSFail";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.sms_fail);
		
		Log.i(className, "On create");
	}
	
	
}