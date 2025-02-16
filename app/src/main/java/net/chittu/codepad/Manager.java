package net.chittu.codepad;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;

public class Manager {
    private WeakReference<Context> mContext;
    private static Manager mManager;
    private Handler mHandler;
    private Manager(Context context){
        mContext = new WeakReference<>(context);
    }

    public static void initialize(Context context){
        mManager = new Manager(context);
    }

    public Context getAppContext(){
        return mContext.get();
    }

    public static Manager getInstance() {
        return mManager;
    }

    public static Manager getInstance(Context context) {
        mManager = new Manager(context);
        return mManager;
    }

    public String getFileNameFromUri(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    columnIndex = Math.max(columnIndex, 0);
                    result = cursor.getString(columnIndex);
                }
            } finally {
                if(cursor != null)
                    cursor.close();
            }
        }

        if (result == null) {
            result = uri.getPath();
            assert result != null;
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void startThread(){
        if(mHandler != null)
            return;
        HandlerThread handlerThread = new HandlerThread("Worker");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
    }

    public void readUri(Uri uri, Editable editable){
        try{
            editable.clear();
            InputStream inputStream = getAppContext().getContentResolver().openInputStream(uri);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                editable.append(line);
                editable.append('\n');
            }
        }
        catch (Exception e){
            log(e.getMessage());
        }

    }

    public static void log(String msg){
        Log.d("Codepad", msg);
    }

}

