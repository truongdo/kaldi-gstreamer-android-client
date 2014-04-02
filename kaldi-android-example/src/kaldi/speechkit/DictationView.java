package kaldi.speechkit;

import java.net.UnknownHostException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class DictationView extends Activity implements Recognizer.Listener
{
	protected static final String TAG = "DictationView";
    
	@SuppressLint("HandlerLeak")
	
    private Recognizer _currentRecognizer;
    EditText ed_text;
    private static SpeechKit _speechKit;
    
    private String addr="ahcclm01.naist.jp";
    private int port=8888;
	private Handler _handler;

    Button dictationButton;
    ListeningDialog dialog;
    public DictationView()
    {
        super();
        _currentRecognizer = null;
    }

	@SuppressLint("HandlerLeak")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setVolumeControlStream(AudioManager.STREAM_MUSIC); // So that the 'Media Volume' applies to this activity
        setContentView(R.layout.main);
        if (_speechKit == null)
        {
            _speechKit = SpeechKit.initialize(getApplication().getApplicationContext(),"","server",addr,port);
            _speechKit.connect();
        }
                
        dictationButton = (Button)findViewById(R.id.dictation);
        ed_text = (EditText) findViewById(R.id.result);
        dialog = new ListeningDialog(this);
        
        dialog.prepare(new Button.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				stopRecognizer();
			}
		});
        dialog.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
				// TODO Auto-generated method stub
				stopRecognizer();
			}
		});
        dialog.setOnCancelListener(new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				// TODO Auto-generated method stub
				stopRecognizer();
			}
		});
        _handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String text = (String)msg.obj; 
               dialog.setText(text);
            }
        };
        
        Button.OnClickListener startListener = new Button.OnClickListener()
        {
            @Override
            public void onClick(View v) {
            	dialog.setText("Initialzing");
            	dialog.setCancelable(false);
                dialog.show();
                _currentRecognizer.start(); // Connect to server
            }
        };

        dictationButton.setOnClickListener(startListener);
        
        if (_currentRecognizer == null)
    		_currentRecognizer = _speechKit.createRecognizer("client", DictationView.this);
        else
        	_currentRecognizer.setListener(this);
      
    }
    private void stopRecognizer(){
    	if (_currentRecognizer != null)
        {
            _currentRecognizer.stopRecording();
        }
    }
    @Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (_currentRecognizer != null)
        {
            _currentRecognizer.stopRecording();
        }
	}

	@Override
    protected void onDestroy() {
        super.onDestroy();
        if (_currentRecognizer !=  null)
        {
            _currentRecognizer.cancel();
            _currentRecognizer = null;
        }
        
    }
 
    private void setDialogText(String text){
    	Message msg = new Message();
		String textTochange = text;
		msg.obj = textTochange;
		_handler.sendMessage(msg);
    }
	@Override
	public void onRecordingBegin() {
		// TODO Auto-generated method stub
		setDialogText("Recording...");
		dialog.setCancelable(true);
	}

	@Override
	public void onRecordingDone() {
		// TODO Auto-generated method stub
		setDialogText("Processing...");
	}
	@Override
	public void onError(Exception error) {
		// TODO Auto-generated method stub
		error.printStackTrace();
		if (dialog.isShowing()){
			dialog.dismiss();
		}
		Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
		if (error instanceof UnknownHostException) {
			Log.e(TAG, "Host "+_speechKit.getHostAddr()+":"+_speechKit.getPort()+" is unavailable");
		}
		
	}


	@Override
	public void onPartialResult(Result result) {
		// TODO Auto-generated method stub
		ed_text.setText(ed_text.getText().toString()+result.getText());
	}

	@Override
	public void onFinalResult(Result result) {
		// TODO Auto-generated method stub
		ed_text.setText(ed_text.getText().toString()+result.getText());
		
	}

	@Override
	public void onFinish(String reason) {
		// TODO Auto-generated method stub
		Log.d(TAG, "finish "+reason);
		if (dialog.isShowing()){
			dialog.dismiss();
		}
	}
	
	
}
