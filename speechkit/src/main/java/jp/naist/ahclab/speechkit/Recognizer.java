package jp.naist.ahclab.speechkit;

import android.os.Handler;
import android.os.Message;
import jp.naist.ahclab.speechkit.logs.MyLog;
import com.codebutler.android_websockets.WebSocketClient;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("ALL")
public class Recognizer implements RecorderListener{
	public interface Listener{
		abstract void onRecordingBegin();
		abstract void onRecordingDone();
		abstract void onError(Exception error);
		abstract void onPartialResult(String result);
		abstract void onFinalResult(String result);
		abstract void onFinish(String reason);
        abstract void onReady(String reason);
        abstract void onNotReady(String reason);
        abstract void onUpdateStatus(SpeechKit.Status status);
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
    private Handler _handle_Ready;
    private Handler _handle_NotReady;
    private Handler _handle_Status;

    private Transcription transcription;

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
        transcription = new Transcription();
	}
    private void handelResult(String text){
    	Message msg = new Message();
		msg.obj = text;
		_handler_partialResult.sendMessage(msg);
    }
    private void handelReady(String text){
        Message msg = new Message();
        msg.obj = text;
        _handle_Ready.sendMessage(msg);
    }

    private void handelNotReady(String text){
        Message msg = new Message();
        msg.obj = text;
        _handle_NotReady.sendMessage(msg);
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
    private void handelStatus(SpeechKit.Status status){
        Message msg = new Message();
        msg.obj = status;
        _handle_Status.sendMessage(msg);
    }
	public Recognizer(ServerInfo serverInfo, Listener _listener) {
		// TODO Auto-generated constructor stub
		this.recogListener = _listener;
        this.transcription = new Transcription();
        _handle_Ready = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String text = (String)msg.obj;
                recogListener.onReady(text);
            }
        };

        _handle_Status = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                SpeechKit.Status status = (SpeechKit.Status)msg.obj;
                recogListener.onUpdateStatus(status);
            }
        };

        _handle_NotReady = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String text = (String)msg.obj;
                recogListener.onNotReady(text);
            }
        };

		_handler_partialResult = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String text = (String)msg.obj;
                Result tmpResult = null;
				try {
					tmpResult = Result.parseResult(text);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();

				}
				
				if (tmpResult == null){
					recogListener.onError(new Exception(text));
				}else
                if (tmpResult.isFinal()) {
                    transcription.add_text(tmpResult.getText());
                    MyLog.i("Final: " + transcription.getTranscript());
                    recogListener.onFinalResult(transcription.getTranscript());
                }
                else {
                    recogListener.onPartialResult(transcription.getTranscript()+" "+tmpResult.getText());
                }
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
				MyLog.d(message);
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
                handelStatus(SpeechKit.Status.READY);
				MyLog.d("Disconnect! " + reason);
				handelFinish(reason);
			}
			
			@Override
			public void onConnect() {
				// TODO Auto-generated method stub
                handelStatus(SpeechKit.Status.RECOGNIZING);
                startRecord();
                recogListener.onRecordingBegin();
			}
		};
		
		server_status_listener = new WebSocketClient.Listener() {
			
			@Override
			public void onMessage(byte[] data) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onMessage(String message) {
                MyLog.d(message);
                ServerStatus serverStatus = null;
                try {
                    serverStatus = ServerStatus.parseResult(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                if (serverStatus.isReady()){
                    handelReady("Ok");
                    handelStatus(SpeechKit.Status.READY);
                }
				if (! serverStatus.isReady()) {
                    handelNotReady(message);
                }
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
				MyLog.d("Connected to status server");
			}
		};

        MyLog.d("Initialzized recognizer with server ");
        MyLog.d(serverInfo.getSpeechServerUrl());
        MyLog.d(serverInfo.getStatusServerUrl());
        ws_client_speech = new WebSocketClient(URI.create(serverInfo.getSpeechServerUrl()),
                server_speech_listener, recogListener,
                extraHeaders);
        ws_client_status = new WebSocketClient(URI.create(serverInfo.getStatusServerUrl()),
				server_status_listener,
				extraHeaders);
		// Initial recorder
		recorderInstance = new PcmRecorder();
		recorderInstance.setListener(Recognizer.this);

	}
		
    public void connect(){

        if (! ws_client_status.isConnected()) {
            MyLog.d("Connecting to status server ");
            ws_client_status.connect();
        }
    }
	public void start(){
        transcription.renew();
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
        handelStatus(SpeechKit.Status.STOPPING);
		if (ws_client_speech.isConnected())
			ws_client_speech.send("EOS");
		// Notify
		recogListener.onRecordingDone();
	}
	
	void startRecord(){
				// TODO Auto-generated method stub			
			thRecord = new Thread(recorderInstance);
			thRecord.start();
			recorderInstance.setRecording(true);
			
	}
		
	
	void stopRecord(){
		recorderInstance.setRecording(false);
		
	}
	@Override
	public void onRecorderBuffer(byte[] buffer) {
		// TODO Auto-generated method stub
		ws_client_speech.send(buffer);
	}
	
}
