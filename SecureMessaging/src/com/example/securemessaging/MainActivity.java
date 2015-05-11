package com.example.securemessaging;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Time;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity 
{
	private ListView mainListView ;  
	private CustomListViewAdapter listAdapter ;
	private List<RowItem> rowItems;  
	private EditText inputSearch;

	public static SharedPreferences s;
	public static EncryptionManager encryptionManager;
	private static DatabaseHandler dbHandler;
	public static GPSTracker mGPS;


	public static String private_key = "Private key";
	public static String public_key = "Public key";
	public static String phone_no = "phone number";
	public static String KEY_number = "number";
	public static String KEY_imei = "imei";
	public static String KEY_public_key = "public_key";
	public static String HEADER_METADATA = "meta";
	public static String HEADER_REPLY_METADATA = "rep-meta";
	public static String HEADER_VERIFY_META = "verify-meta";

	public static String separator = "`~~`";

	private static final String URL = "http://172.26.234.59/SMS_Server";

	public static boolean exchangeKeys = false;
	private static boolean exchangeKeysTrying = false;
	static Context context;

	//for analysis//////////////////////////////////////////////////
	static boolean isDebugging = true;
	// Debugging
	private static final String TAG = "Bluetooth";
	private static final boolean D = true;

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;

	// Name of the connected device
	private String mConnectedDeviceName = null;
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	public static BluetoothCommandService mCommandService = null;

	/** Called when the activity is first created. */  
	@Override  
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);  
		setContentView(R.layout.activity_main);  

		if (isDebugging)
		{
			// Get local Bluetooth adapter
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

			// If the adapter is null, then Bluetooth is not supported
			if (mBluetoothAdapter == null) {
				Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
				finish();
				return;
			}
		}

		context = this;

		// Find the ListView resource.   
		mainListView = (ListView) findViewById( R.id.mainListView );  

		rowItems = new ArrayList<RowItem>();

		listAdapter = new CustomListViewAdapter(this, R.layout.simplerow, rowItems);
		inputSearch = (EditText) findViewById(R.id.inputSearch);

		readContacts();
		// Set the ArrayAdapter as the ListView's adapter.  
		mainListView.setAdapter( listAdapter );     
		mainListView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapter, View v, int position,
					long arg3) 
			{
				RowItem value = (RowItem)adapter.getItemAtPosition(position); 
				Log.d("selected item", value.getName()+" : "+value.getNumber());

				Intent intent = new Intent(getBaseContext(), DisplayOptions.class);
				intent.putExtra("name",value.getName());
				intent.putExtra("number",value.getNumber());
				startActivity(intent);
			}
		});

		inputSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) 
			{
				// When user changed the Text
				//MainActivity.this.listAdapter.getFilter().filter(cs);   
				Log.d("*** Search value changed: " , cs.toString());
				listAdapter.doFilter(mainListView, cs.toString(), rowItems);
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) 
			{
				Log.d("*** Search value changedjfjfgjfgjfgjfgj: " , s.toString());
				listAdapter.doFilter(mainListView, s.toString(), rowItems);                  
			}


		});

		s= getPreferences(Activity.MODE_PRIVATE);

		mGPS = new GPSTracker(this);

		if (s.getString(MainActivity.phone_no, null) == null)
		{
			Log.i("phone number", "getting phone number from user: should only happen once!!");
			getPhoneNo();
		}



		try 
		{
			encryptionManager = new EncryptionManager( s , "testpassword");

		} catch (ParseException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		dbHandler = new DatabaseHandler(this);


	}

	@Override
	protected void onStart() 
	{
		super.onStart();

		if (isDebugging)
		{
			// If BT is not on, request that it be enabled.
			// setupCommand() will then be called during onActivityResult
			if (!mBluetoothAdapter.isEnabled()) 
			{
				Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			}
			// otherwise set up the command service
			else 
			{
				if (mCommandService==null)
					setupCommand();
			}
		}


	}

	@Override
	protected void onResume()
	{
		super.onResume();

		if (isDebugging)
		{
			// Performing this check in onResume() covers the case in which BT was
			// not enabled during onStart(), so we were paused to enable it...
			// onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
			if (mCommandService != null) 
			{
				if (mCommandService.getState() == BluetoothCommandService.STATE_NONE) 
				{
					mCommandService.start();
				}
			}
		}
	}

	private void setupCommand() 
	{
		// Initialize the BluetoothChatService to perform bluetooth connections
		mCommandService = new BluetoothCommandService(this, mHandler);
	}

	@Override
	protected void onDestroy() 
	{
		super.onDestroy();

		if (isDebugging)
		{
			if (mCommandService != null)
				mCommandService.stop();
		}
	}

	private void ensureDiscoverable() 
	{
		if (mBluetoothAdapter.getScanMode() !=
				BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

	// The Handler that gets information back from the BluetoothChatService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case BluetoothCommandService.STATE_CONNECTED:
					/*mTitle.setText(R.string.title_connected_to);
                    mTitle.append(mConnectedDeviceName);*/
					Log.i("BT", "Connected to: " + mConnectedDeviceName);
					break;
				case BluetoothCommandService.STATE_CONNECTING:
					// mTitle.setText(R.string.title_connecting);
					Log.i("BT", "connecting...");

					break;
				case BluetoothCommandService.STATE_LISTEN:
				case BluetoothCommandService.STATE_NONE:
					//mTitle.setText(R.string.title_not_connected);
					Log.i("BT", "NOT CONNECTED");

					break;
				}
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(), "Connected to "
						+ mConnectedDeviceName, Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
						Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

	public void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		if (isDebugging)
		{
			switch (requestCode) 
			{
			case REQUEST_CONNECT_DEVICE:
				// When DeviceListActivity returns with a device to connect
				if (resultCode == Activity.RESULT_OK) {
					// Get the device MAC address
					String address = data.getExtras()
							.getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
					// Get the BLuetoothDevice object
					BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
					// Attempt to connect to the device
					mCommandService.connect(device);
				}
				break;
			case REQUEST_ENABLE_BT:
				// When the request to enable Bluetooth returns
				if (resultCode == Activity.RESULT_OK) {
					// Bluetooth is now enabled, so set up a chat session
					setupCommand();
				} else {
					// User did not enable Bluetooth or an error occured
					Toast.makeText(this, "R.string.bt_not_enabled_leaving", Toast.LENGTH_SHORT).show();
					finish();
				}
			}
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		if (isDebugging)
		{
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.option_menu, menu);

		}

		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.scan:
			// Launch the DeviceListActivity to see devices and do scan
			Intent serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			return true;
		case R.id.discoverable:
			// Ensure this device is discoverable by others
			ensureDiscoverable();
			return true;
		}
		return false;
	}


	private void getPhoneNo() 
	{
		Intent k = new Intent(this, GetPhoneNo.class);
		startActivity(k);

	}


	private void readContacts() 
	{
		ContentResolver cr = getContentResolver();
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,null, null, null, ContactsContract.Contacts.DISPLAY_NAME + " ASC");

		if (cur.getCount() > 0)
		{
			while (cur.moveToNext())
			{
				String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
				String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

				// Using the contact ID now we will get contact phone number
				Cursor cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
						new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},

						ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
								ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
								ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

								new String[]{id},
								null);

				String contactNumber = "000";
				if (cursorPhone.moveToFirst()) {
					contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
				}
				//Log.i("number", name +" : " + contactNumber);
				cursorPhone.close();

				int image = R.drawable.ic_contact_picture;


				RowItem item = new RowItem(image, name, contactNumber);
				rowItems.add(item);                



			}
		}
	}


	public static void exchangeKeys(String number) 
	{
		DisplayOptions.status.setText("Exchanging keys");
		exchangeKeysTrying = true;
		//generating nonce
		String nonce = getNonce();

		//generating meta-data
		String metadata = getMetaData(MainActivity.HEADER_METADATA);

		//fetching public key and public modulus
		String publicKey = getPublicKey();

		//store challenge with number
		String metaD =  metadata.replace(MainActivity.HEADER_METADATA, "");

		Log.i("meta after replacing", metaD);

		String challenge = metaD + "," + nonce;
		challenge = challenge.replace(MainActivity.separator, "");



		String challengeNo = number;
		MainActivity.addChallenge(challengeNo, challenge);

		String message = metadata + publicKey + nonce;

		Log.i("message to send", message);

		if (isDebugging)
		{
			byte [] b= (Constants.startA + "\n").getBytes();
			mCommandService.write(b);
		}
		
		sendLongSMS(message, number);


	}


	static void sendLongSMS(String msg, String to) 
	{
		Logger.i("message to send:", msg);
		//msg = StringCompressor.compress(msg);
		
		SmsManager smsManager = SmsManager.getDefault();
		ArrayList<String> parts = smsManager.divideMessage(msg); 
		smsManager.sendMultipartTextMessage(to, null, parts, null, null);

		//Toast.makeText(getBaseContext(), "Message Sent!", Toast.LENGTH_LONG).show();
	}


	private static void addChallenge(String no, String challenge) 
	{
		Log.i("number of the challange", no);
		Log.i("challange", challenge);

		dbHandler.addNewChallange(no, challenge);

	}



	private static String getPublicKey() 
	{
		return (MainActivity.encryptionManager.s.getString(MainActivity.public_key, null)+MainActivity.separator);
	}


	private static String getMetaData(String header) 
	{
		//phone No:
		String metadata = header;
		metadata += MainActivity.separator;
		metadata += MainActivity.s.getString(phone_no, null);
		metadata += "--";

		//location
		if(MainActivity.mGPS.canGetLocation )
		{
			Double mLat = MainActivity.mGPS.getLatitude();
			Double mLong = MainActivity.mGPS.getLongitude();

			metadata += Double.toString(mLat);
			metadata += ":";
			metadata += Double.toString(mLong);
			metadata += "--";
		}
		else
		{
			metadata += "null:null--";
			Log.e("Location", "can't get the location");
		}

		//DateTime
		final Calendar c = Calendar.getInstance();
		int mYear = c.get(Calendar.YEAR);
		int mMonth = c.get(Calendar.MONTH);
		int mDay = c.get(Calendar.DAY_OF_MONTH);
		metadata += mDay+"-"+mMonth+"-"+mYear;
		metadata += ":";

		Time time = new Time();
		time.setToNow();
		metadata += time.hour+":"+time.minute;
		metadata += MainActivity.separator;

		Log.i("final meta data", metadata);

		return metadata;
	}


	private static String getNonce() 
	{
		SecureRandom sr = new SecureRandom();
		byte[] _nonce = new byte[1024/8];
		sr.nextBytes(_nonce);

		return (_nonce.toString() + MainActivity.separator);
	}


	public static void addMsg(Message msg) throws Exception 
	{

		Log.i("in addMsg", msg.number);


		MainActivity.exchangeKeysTrying = true;

		//check what the message is about
		String[] split = msg.message.split(MainActivity.separator);
		int index = 0;

		String header = split[index++];
		if (header.equals(HEADER_METADATA))
		{
			if (isDebugging)
			{
				byte [] b= (Constants.startB + "\n").getBytes();
				mCommandService.write(b);
			}

			exchangeKeysTrying = true;
			Log.i("metadata", "Got meta-data    " +msg.message);

			//send a reply: new metadata, new nonce, public key, public modulus, signed metadata + nonce of sender
			replyMetadata(msg.message);


		}
		else if (header.equals(HEADER_REPLY_METADATA))
		{
			if (isDebugging)
			{
				byte [] b= (Constants.endA + "\n").getBytes();
				mCommandService.write(b);
			}

			Log.i("reply metadata", "Got reply meta-data");
			Log.i("reply meta data msg", msg.message);
			//metadata
			String encryptMsg = split[index++];
			//publickey
			String publicKeyOfSender = split[index++];
			//nonce
			encryptMsg += ",";
			encryptMsg += split[index++];

			//the encrytpted challenge reply
			String encChallange = split[index++];
			Log.i("got encrypted challange",encChallange );

			String challenge = encryptionManager.decrypt(encChallange, publicKeyOfSender);

			String actualChallenge = getChallenge(msg.number);

			dbHandler.deleteChallange(msg.number);

			Log.i("challenge",challenge);
			Log.i("actual challenge", actualChallenge);

			if (actualChallenge.equals(challenge))
			{
				Log.i("equal", "the challenge and the challenge reply are equal");

				RreplyMetadata(encryptMsg, msg.number);
				DisplayOptions.status.setText("Challenge equal!!");


				MainActivity.exchangeKeysTrying = false;
				MainActivity.exchangeKeys = true;
				//sendPicture(msg.number);
				//connectionText.setText("Connection established!");
				MainActivity.dbHandler.addNewKey(msg.number, publicKeyOfSender);

				Constants.number = msg.number;
				Intent k = new Intent(context, SendReadSMS.class);
				context.startActivity(k);
			}
			else
			{

				DisplayOptions.status.setText("Challenge NOT equal!!");
				Log.i("not equal", "the challenge and the challenge reply are not equal");
				exchangeKeysTrying = false;
				exchangeKeys = false;
			}

		}
		else if (header.equals(HEADER_VERIFY_META))
		{
			if (isDebugging)
			{
				byte [] b= (Constants.endB + "\n").getBytes();
				mCommandService.write(b);
			}

			Log.i("verify meta", "Got verify meta-data    "+msg.message);

			String encChallange = split[index++];

			String publicKey = dbHandler.getTempKey(msg.number);


			dbHandler.deleteTempKey(msg.number);
			String actualChallenge = getChallenge(msg.number);

			String challenge = encryptionManager.decrypt(encChallange, publicKey);

			dbHandler.deleteChallange(msg.number);

			Log.i("challenge",challenge);
			Log.i("actual challenge", actualChallenge);

			if (actualChallenge.equals(challenge))
			{
				Log.i("equal", "the challenge and the challenge reply are equal");

				Toast.makeText(context,"Challenge equal!!", Toast.LENGTH_LONG).show();

				MainActivity.exchangeKeysTrying = false;
				MainActivity.exchangeKeys = true;

				//connectionText.setText("Connection established!");
				MainActivity.dbHandler.addNewKey(msg.number, publicKey);
				Constants.number = msg.number;
				Intent k = new Intent(context, SendReadSMS.class);
				context.startActivity(k);
			}
			else
			{
				Toast.makeText(context,"Challenge NOT equal!!", Toast.LENGTH_LONG).show();
				Log.i("not equal", "the challenge and the challenge reply are not equal");
				exchangeKeys = false;
				exchangeKeysTrying = false;
			}


		}
		else
		{
			if (MainActivity.isDebugging)
			{
				byte [] b= (Constants.receivedMsg + "\n").getBytes();
				MainActivity.mCommandService.write(b);
			}
			Log.i("got encrypted message", msg.message);
			
			String publicKey = dbHandler.getKey(msg.number);
			
			if (MainActivity.isDebugging)
			{
				byte [] b= (Constants.startDec + "\n").getBytes();
				MainActivity.mCommandService.write(b);
			}
			
			String message =  encryptionManager.decrypt(msg.message, publicKey);
			
			if (MainActivity.isDebugging)
			{
				byte [] b= (Constants.endDec + "\n").getBytes();
				MainActivity.mCommandService.write(b);
			}
			
			Toast.makeText(context, message , Toast.LENGTH_LONG).show();
			
			
		}

	}


	private static void RreplyMetadata(String encryptMsg, String number) throws Exception 
	{
		Log.i("This is what is being encrypted here", encryptMsg);
		//encrypt using the user's own public key
		String encoded = MainActivity.encryptionManager.encrypt(encryptMsg);
		Log.i("Encoded string", encoded);

		String msg = MainActivity.HEADER_VERIFY_META +MainActivity.separator + encoded;

		if (isDebugging)
		{
			byte [] b= (Constants.a1 + "\n").getBytes();
			mCommandService.write(b);
		}
		sendLongSMS(msg, number) ;     
	}


	private static String getChallenge(String number) 
	{
		return dbHandler.getChallenge(number);
	}


	private static void replyMetadata(String message) throws Exception 
	{
		String metadata = getMetaData(MainActivity.HEADER_REPLY_METADATA);
		String _nonce = getNonce();
		String rep_metadata = metadata + getPublicKey() + _nonce;

		String[] split = message.split(MainActivity.separator);

		Log.i("Test Message", message);


		String meta = split[1];
		String pub = split[2];
		String nonce = split[3];


		String encryptMsg = meta + "," + nonce;

		Log.i("This is what is being encrypted here", encryptMsg);
		//encrypt using the user's own public key
		String encoded = null;
		try 
		{
			encoded = encryptionManager.encrypt(encryptMsg);
		} catch (UnsupportedEncodingException e) {

			e.printStackTrace();
		}


		Log.i("After encryption", encoded);


		String publicKeyOfSender = getPublicKey();
		publicKeyOfSender = publicKeyOfSender.replaceFirst(MainActivity.separator, "");


		rep_metadata += encoded;
		rep_metadata += MainActivity.separator;

		String[] split2 = meta.split("--");
		String fromNo = split2[0];

		//store challenge with number
		String metaD =  metadata.replaceFirst(MainActivity.HEADER_REPLY_METADATA+MainActivity.separator, "");
		String challenge = metaD + "," + _nonce;
		challenge = challenge.replace(MainActivity.separator, "");

		String challengeNo = fromNo;
		MainActivity.addChallenge(challengeNo, challenge);

		//storing keys temp
		MainActivity.addKeys(fromNo,pub);

		Log.i("Replying metadata", rep_metadata);
		Log.i("Replying to", fromNo);

		if (isDebugging)
		{
			byte [] b= (Constants.b1 + "\n").getBytes();
			mCommandService.write(b);
		}
		sendLongSMS(rep_metadata, fromNo) ;  
	}


	private static void addKeys(String fromNo, String pub) 
	{
		dbHandler.AddNewTempKeys(fromNo, pub);
	}


	public static void createIDOnline() throws ParseException, IOException 
	{

		Log.i("number", s.getString(MainActivity.phone_no, "null"));
		Log.i("imei", s.getString(MainActivity.KEY_imei, "null"));
		Log.i("public key", s.getString(MainActivity.public_key, "null"));
		//String number = Base64.encodeToString(s.getString(MainActivity.phone_no, null).getBytes(), Base64.DEFAULT);
		//String imei = Base64.encodeToString(s.getString(MainActivity.KEY_imei, null).getBytes(), Base64.DEFAULT);
		//String public_key = Base64.encodeToString(s.getString(MainActivity.public_key, null).getBytes(), Base64.DEFAULT);

		/*HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(MainActivity.URL + "/createID.php");
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(new BasicNameValuePair(MainActivity.KEY_number, number));
        pairs.add(new BasicNameValuePair(MainActivity.KEY_imei, imei));
        pairs.add(new BasicNameValuePair(MainActivity.KEY_public_key, public_key));

        post.setEntity(new UrlEncodedFormEntity(pairs));
        HttpResponse response = client.execute(post);

        HttpEntity resEntityGet = response.getEntity();  
        if (resEntityGet != null) 
        {  
            // do something with the response
            String reply = EntityUtils.toString(resEntityGet);
            Log.i("GET RESPONSE", reply);

            if (!(reply.contains("success")))
            {
            	Log.e("PHP error", "PHP upload fail \t" + reply);
            }
            else
            {
            	Log.i("php upload done!", "Created a profile successfully!");
            }
        }*/
	}



} 


