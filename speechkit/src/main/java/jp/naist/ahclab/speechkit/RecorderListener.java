package jp.naist.ahclab.speechkit;

public interface RecorderListener {
	abstract void onRecorderBuffer(byte[] buffer);
}
