package com.dfrobot.angelo.blunobasicdemo;

import android.content.Context;
import android.os.Bundle;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Calendar;

public class MainActivity  extends BlunoLibrary implements OnClickListener {
	private final int HIGH = 1;
	private final int LOW = 0;
	private final String filePath = Environment.getExternalStorageDirectory().getPath() + "/log.txt" ;

	private static int subject_id = 0;
	private int previousState = HIGH;

	private Button buttonScan;
	private Button buttonSerialSend;
	private Button buttonAddSubject;
	private EditText serialSendText;
	private TextView serialReceivedText;
	private Button buttonClear;
	private Button buttonTrue;
	private Button buttonFalse;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        onCreateProcess();														//onCreate Process by BlunoLibrary


        serialBegin(115200);													//set the Uart Baudrate on BLE chip to 115200

        serialReceivedText=(TextView) findViewById(R.id.serialReveicedText);	//initial the EditText of the received data
        serialSendText=(EditText) findViewById(R.id.serialSendText);			//initial the EditText of the sending data

        buttonSerialSend = (Button) findViewById(R.id.buttonSerialSend);		//initial the button for sending the data
		buttonSerialSend.setOnClickListener(this);
        buttonScan = (Button) findViewById(R.id.buttonScan);					//initial the button for scanning the BLE device
		buttonScan.setOnClickListener(this);
		buttonAddSubject = (Button) findViewById(R.id.subject_delim);
		buttonAddSubject.setOnClickListener(this);
		buttonClear = (Button)findViewById(R.id.clear_text);
		buttonClear.setOnClickListener(this);
		buttonTrue = (Button)findViewById(R.id.true_btn);
		buttonTrue.setOnClickListener(this);
		buttonFalse = (Button)findViewById(R.id.false_btn);
		buttonFalse.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		Calendar c = Calendar.getInstance();
		switch(v.getId()) {
			case R.id.buttonSerialSend:
				serialSend(serialSendText.getText().toString());
				break;
			case R.id.buttonScan:
				buttonScanOnClickProcess();
				break;
			case R.id.subject_delim:
				writeSubjectDelim();
				break;
			case R.id.clear_text:
				serialReceivedText.setText("");
				break;
			case R.id.true_btn:
				writeToFile(String.format("%d-%d-%d:%d-%d-%d-%d, true\n",
						c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH),
						c.get(Calendar.HOUR), c.get(Calendar.MINUTE), c.get(Calendar.SECOND),
						c.get(Calendar.MILLISECOND)));
				break;
			case R.id.false_btn:
				writeToFile(String.format("%d-%d-%d:%d-%d-%d-%d, false\n",
						c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH),
						c.get(Calendar.HOUR), c.get(Calendar.MINUTE), c.get(Calendar.SECOND),
						c.get(Calendar.MILLISECOND)));
				break;
		}
	}

	protected void onResume(){
		super.onResume();
		System.out.println("BlUNOActivity onResume");
		onResumeProcess();														//onResume Process by BlunoLibrary
	}
	
	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		onActivityResultProcess(requestCode, resultCode, data);					//onActivityResult Process by BlunoLibrary
		super.onActivityResult(requestCode, resultCode, data);
	}
	
    @Override
    protected void onPause() {
        super.onPause();
        onPauseProcess();														//onPause Process by BlunoLibrary
    }
	
	protected void onStop() {
		super.onStop();
		onStopProcess();														//onStop Process by BlunoLibrary
	}
    
	@Override
    protected void onDestroy() {
        super.onDestroy();	
        onDestroyProcess();														//onDestroy Process by BlunoLibrary
    }

	@Override
	public void onConectionStateChange(connectionStateEnum theConnectionState) {//Once connection state changes, this function will be called
		switch (theConnectionState) {											//Four connection state
		case isConnected:
			buttonScan.setText("Connected");
			break;
		case isConnecting:
			buttonScan.setText("Connecting");
			break;
		case isToScan:
			buttonScan.setText("Scan");
			break;
		case isScanning:
			buttonScan.setText("Scanning");
			break;
		case isDisconnecting:
			buttonScan.setText("isDisconnecting");
			break;
		default:
			break;
		}
	}

	@Override
	public void onSerialReceived(String theString) {							//Once connection data received, this function will be called
		// TODO Auto-generated method stub
		serialReceivedText.append(theString);							//append the text into the EditText
		//The Serial data from the BLUNO may be sub-packaged, so using a buffer to hold the String is a good choice.
		try {

//			int state = Integer.parseInt(theString.trim());

//			if(state == LOW) {
				Calendar c = Calendar.getInstance();
				writeToFile(String.format("%d-%d-%d:%d-%d-%d-%d, %d\n",
						c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH),
						c.get(Calendar.HOUR), c.get(Calendar.MINUTE), c.get(Calendar.SECOND),
						c.get(Calendar.MILLISECOND), subject_id));
//			}
		} catch(Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	private void writeToFile(String data) {
		try {
			File directory = new File(Environment.getExternalStorageDirectory(), "bluetooth_log");
			if (!directory.exists()) {
				directory.mkdirs();
			}

			File log_file = new File(directory, "log.txt");
			FileOutputStream fOut = null;
			if(!log_file.exists()) {
				fOut = new FileOutputStream(log_file);
			} else {
				fOut = new FileOutputStream(log_file, true);
			}

			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fOut);
			outputStreamWriter.write(data);
			outputStreamWriter.close();
			Toast.makeText(getApplicationContext(), data, Toast.LENGTH_SHORT).show();
		}
		catch (IOException e) {
			Toast.makeText(getApplicationContext(), "fail", Toast.LENGTH_SHORT).show();
			Log.e("Exception", "File write failed: " + e.toString());
		}
	}

	private void writeSubjectDelim() {
		subject_id ++;
		Calendar c = Calendar.getInstance();
		String delim = String.format("=====================subject:%d;datetime:%d-%d-%d;%d-%d-%d-%d;=====================\n",
				subject_id, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH),
				c.get(Calendar.HOUR), c.get(Calendar.MINUTE), c.get(Calendar.SECOND), c.get(Calendar.MILLISECOND));
		writeToFile(delim);
	}
}