package jp.naist.ahclab.kaldigstreamerclient;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import jp.naist.ahclab.speechkit.Recognizer;
import jp.naist.ahclab.speechkit.ServerInfo;
import jp.naist.ahclab.speechkit.SpeechKit;
import jp.naist.ahclab.speechkit.view.ListeningDialog;


public class SimpleActivity extends Activity implements Recognizer.Listener{

    final String TAG = "SimpleActivity";

    private ListeningDialog lst_dialog;
    private Button btn_start;
    private EditText ed_result;

    protected ServerInfo serverInfo = new ServerInfo();
    Recognizer _currentRecognizer;

    void init_speechkit(ServerInfo serverInfo){
        SpeechKit _speechKit = SpeechKit.initialize(getApplication().getApplicationContext(), "", "", serverInfo);
        _currentRecognizer = _speechKit.createRecognizer(SimpleActivity.this);
        _currentRecognizer.connect();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictation);

        lst_dialog = new ListeningDialog(SimpleActivity.this);
        btn_start = (Button)findViewById(R.id.btn_start);
        ed_result = (EditText)findViewById(R.id.ed_result);

        serverInfo.setAddr(this.getResources().getString(R.string.default_server_addr));
        serverInfo.setPort(Integer.parseInt(this.getResources().getString(R.string.default_server_port)));
        serverInfo.setAppSpeech(this.getResources().getString(R.string.default_server_app_speech));
        serverInfo.setAppStatus(this.getResources().getString(R.string.default_server_app_status));

        init_speechkit(serverInfo);

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _currentRecognizer.start();
                lst_dialog.show();
            }
        });

        Button.OnClickListener stop_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _currentRecognizer.stopRecording();
            }
        };
        lst_dialog.prepare(stop_listener);

    }


    @Override
    public void onPartialResult(String result) {
        ed_result.setText(result);
    }

    @Override
    public void onFinalResult(String result) {
        ed_result.setText(result);
    }

    @Override
    public void onFinish(String reason) {
        if (lst_dialog.isShowing())
            lst_dialog.dismiss();
    }

    @Override
    public void onReady(String reason) {
        btn_start.setEnabled(true);
    }

    @Override
    public void onNotReady(String reason) {
        btn_start.setEnabled(false);
        Toast.makeText(getApplicationContext(),"Server connected, but not ready, reason: "+reason,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpdateStatus(SpeechKit.Status status) {

    }

    @Override
    public void onRecordingBegin() {
        lst_dialog.setText("Listening");
    }

    @Override
    public void onRecordingDone() {
        lst_dialog.setText("Please wait!");
    }

    @Override
    public void onError(Exception error) {

    }

}
