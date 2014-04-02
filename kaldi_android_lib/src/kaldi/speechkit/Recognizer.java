package kaldi.speechkit;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.codebutler.android_websockets.WebSocketClient;

public class Recognizer implements RecorderListener{
	public interface Listener{
		abstract void onRecordingBegin();
		abstract void onRecordingDone();
		abstract void onError(Exception error);
		abstract void onPartialResult(Result text);
		abstract void onFinalResult(Result result);
		abstract void onFinish(String reason);
	}
	protected static final String TAG = "Recognizer";
	private WebSocketClient ws_client_speech;
	private WebSocketClient ws_client_status;
	private PcmRecorder recorderInstance;
	private WebSocketClient.Listener server_status_listener;
	private WebSocketClient.Listener server_speech_listener;
	private Thread thRecord;
	Listener recogListener;
	private Handler _handler_partialResult;
	private Handler _handler_Error;
	private Handler _handler_Finish;
	JSONObject message;
	int num_worker;
	String hypothesis = "";
	List<BasicNameValuePair> extraHeaders = Arrays.asList(
		    new BasicNameValuePair("Cookie", "session=abcd"),
		    new BasicNameValuePair("content-type", "audio/x-raw"),
		    new BasicNameValuePair("+layout", "(string)interleaved"),
		    new BasicNameValuePair("+rate", "16000"),
		    new BasicNameValuePair("+format", "S16LE"),
		    new BasicNameValuePair("+channels", "1")
		);
	
	public Recognizer(){
		
	}
    private void handelResult(String text){
    	Message msg = new Message();
		String textTochange = text;
		msg.obj = textTochange;
		_handler_partialResult.sendMessage(msg);
    }
    private void handelError(Exception error){
    	Message msg = new Message();
		msg.obj = error;
		_handler_Error.sendMessage(msg);
    }
    private void handelFinish(String reason){
    	Message msg = new Message();
		msg.obj = reason;
		_handler_Finish.sendMessage(msg);
    }
	public Recognizer(String serverAddr, int serverPort, String language, Listener _listener) {
		// TODO Auto-generated constructor stub
		this.recogListener = _listener;
		
		_handler_partialResult = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String text = (String)msg.obj; 
                Result tmpResult = null;
				try {
					tmpResult = Result.parseResult(text);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					
				}
				
				if (tmpResult == null){
					recogListener.onError(new Exception(text));
				}else
                if (tmpResult.isFinal())
                	recogListener.onFinalResult(tmpResult);
                else
                	recogListener.onPartialResult(tmpResult);
                
            }
        };
        
        _handler_Error = new Handler() {
            @Override
            public void handleMessage(Message msg) {
            	Exception error = (Exception)msg.obj; 
                recogListener.onError(error);
                
            }
        };
        _handler_Finish = new Handler() {
            @Override
            public void handleMessage(Message msg) {
            	String reason = (String)msg.obj; 
                recogListener.onFinish(reason);
                
            }
        };
		server_speech_listener = new WebSocketClient.Listener() {
			
			@Override
			public void onMessage(byte[] data) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onMessage(String message) {
				// TODO Auto-generated method stub
				Log.d(TAG, message);
				handelResult(message);
			}
			
			@Override
			public void onError(Exception error) {
				// TODO Auto-generated method stub
				//recogListener.onError(error);
				handelError(error);
			}
			
			@Override
			public void onDisconnect(int code, String reason) {
				// TODO Auto-generated method stub
				Log.d(TAG,"Disconnect! "+reason);
				handelFinish(reason);
			}
			
			@Override
			public void onConnect() {
				// TODO Auto-generated method stub
				recogListener.onRecordingBegin();
		        startRecord();
			}
		};
		
		server_status_listener = new WebSocketClient.Listener() {
			
			@Override
			public void onMessage(byte[] data) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onMessage(String message) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onError(Exception error) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onDisconnect(int code, String reason) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onConnect() {
				// TODO Auto-generated method stub
				
			}
		};
		
		ws_client_speech = new WebSocketClient(URI.create("ws://"+serverAddr+":"+serverPort+"/"+language+
				"/ws/speech?content-type=audio/x-raw,+layout=(string)interleaved,+rate=(int)16000,+format=(string)S16LE,+channels=(int)1"),
				server_speech_listener, recogListener,
				extraHeaders);
		ws_client_status = new WebSocketClient(URI.create("ws://"+serverAddr+":"+serverPort+"/"+language+"/ws/status"),
				server_status_listener,
				extraHeaders);
	
		// Initial recorder
		recorderInstance = new PcmRecorder();
		
		recorderInstance.setListener(Recognizer.this);
		
	}
		
	
	
	
	public void start(){
		
		//if (! ws_client_status.isConnected())
		//	ws_client_status.connect();
		if (! ws_client_speech.isConnected())
			ws_client_speech.connect();
	}

	public void setListener(Listener _listener) {
		// TODO Auto-generated method stub
		this.recogListener = _listener;
	}

	public void cancel() {
		// TODO Auto-generated method stub
		
		// Stop recording
		
		// Stop web socket
		if (ws_client_speech != null && ws_client_speech.isConnected()){
			ws_client_speech.disconnect();
		}
		if (ws_client_status != null && ws_client_speech.isConnected()){
			ws_client_status.disconnect();
		}
	}
	

	public float getAudioLevel() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void stopRecording() {
		// TODO Auto-generated method stub
		stopRecord();
		// Send EOS signal
		if (ws_client_speech.isConnected())
			ws_client_speech.send("EOS");
		// Notify
		recogListener.onRecordingDone();
	}
	
	public void startRecord(){
				// TODO Auto-generated method stub			
			thRecord = new Thread(recorderInstance);
			thRecord.start();
			recorderInstance.setRecording(true);
			
	}
		
	
	public void stopRecord(){
		recorderInstance.setRecording(false);
		
	}
	@Override
	public void onRecorderBuffer(byte[] buffer) {
		// TODO Auto-generated method stub
		Log.d(TAG,"read "+buffer.length);
		
		ws_client_speech.send(buffer);
	}
	
}
