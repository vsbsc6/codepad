package net.chittu.codepad;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.util.Objects;

public class Utils {
    public static String getFilename(Context context, Uri uri){
        if(context == null)
            throw new IllegalArgumentException("Context is null.");

        if(uri == null)
            throw new IllegalArgumentException("Uri is null.");

        String result = null;
        if (Objects.equals(uri.getScheme(), ContentResolver.SCHEME_CONTENT)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if(displayNameIndex >= 0){
                    result = cursor.getString(displayNameIndex);
                }
            }

            if(cursor != null){
                cursor.close();
            }
        }

        if(result == null){
            result = uri.getPath();
            if(result != null){
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }
        return result;
    }

    public static String getBaseName(String filename){
        if(filename == null)
            return null;

        int cut = filename.lastIndexOf('.');
        if (cut == -1)
            return filename;

        return filename.substring(0, cut - 1);
    }

    public static String getExtension(String filename){
        if(filename == null)
            return null;

        int cut = filename.lastIndexOf('.');
        if (cut == -1)
            return "";

        return filename.substring(cut + 1);
    }



}
