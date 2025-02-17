package net.chittu.codepad;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.concurrent.Executor;

public class CodeFile {
    private Uri uri;
    private String name;
    private String baseName;
    private String extension;
    private CodeFileReadState readState;
    private boolean modified;
    private String content;

    public CodeFile(Context context, Uri uri){
        this.uri = uri;
        this.name = Utils.getFilename(context, uri);
        this.baseName = Utils.getBaseName(this.name);
        this.extension = Utils.getExtension(this.name);
        this.readState = CodeFileReadState.UNREAD;
        this.modified = false;
        this.content = "";
    }

    public CodeFile(Uri uri, String name, String baseName, String extension) {
        this.uri = uri;
        this.name = name;
        this.baseName = baseName;
        this.extension = extension;
        this.readState = CodeFileReadState.UNREAD;
        if(Objects.equals(uri.getScheme(), "codepad")){
            this.readState = CodeFileReadState.READ;
        }
        this.modified = false;
        this.content = name;
    }

    public String getName() {
        return name;
    }

    public String getExtension() {
        return extension;
    }


    public String getBaseName() {
        return baseName;
    }

    public Uri getUri() {
        return uri;
    }

    public boolean isModified(){
        return modified;
    }

    public void setModified(boolean modified){
        this.modified = modified;
    }

    public boolean isRead(){
        return readState == CodeFileReadState.READ;
    }

    public String getContent(){
        return content;
    }

    public void setContent(String content){
        this.content = content;
    }

    public void read(Context context, Runnable callback){
        Executor executor = context.getMainExecutor();

        if(readState == CodeFileReadState.READ){
            if(callback != null){
                executor.execute(callback);
            }
            return;
        }

        ContentResolver contentResolver = context.getContentResolver();

        readState = CodeFileReadState.READING;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    InputStream inputStream = contentResolver.openInputStream(uri);
                    InputStreamReader reader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(reader);
                    String line;
                    StringBuilder stringBuilder = new StringBuilder();
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                        stringBuilder.append('\n');
                    }

                    content = stringBuilder.toString();

                    if(inputStream != null){
                        inputStream.close();
                    }
                    readState = CodeFileReadState.READ;
                }
                catch(IOException e){
                    readState = CodeFileReadState.UNREAD;
                }

                if(callback != null){
                    executor.execute(callback);
                }
            }
        });

        thread.start();
    }

    public boolean write(){
        return false;
    }
}
