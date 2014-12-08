package jp.naist.ahclab.speechkit.logs;

import android.util.Log;
public class MyLog {
    public final static boolean DEBUG = true;
    public final static boolean INFO = true;
    public static void d(String message) {
        if (DEBUG) {
            String fullClassName = Thread.currentThread().getStackTrace()[2].getClassName();
            String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
            String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
            int lineNumber = Thread.currentThread().getStackTrace()[2].getLineNumber();

            Log.d(className + "." + methodName + "():" + lineNumber, message);
        }
    }
    public static void i(String message) {
        if (INFO) {
            String fullClassName = Thread.currentThread().getStackTrace()[2].getClassName();
            String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
            String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
            int lineNumber = Thread.currentThread().getStackTrace()[2].getLineNumber();

            Log.i(className + "." + methodName + "():" + lineNumber, message);
        }
    }
}