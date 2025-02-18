package net.chittu.codepad;

import static android.content.ContentResolver.SCHEME_CONTENT;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.Objects;
import java.util.concurrent.Executor;

public class CodeFile {
    private static int file = 1;

    private Uri uri;
    private String name;
    private CodeFileReadState readState;
    private boolean modified;
    private String content;
    private File autosavedFile;

    private int untitled;

    private boolean active;

    private CodeFile(){}

    public CodeFile(Context context, String filename){
        name = filename;

        File root = context.getFilesDir();
        autosavedFile = new File(root, "File"  + file++);
        uri = Uri.parse(autosavedFile.toString());
        readState = CodeFileReadState.READ;
        active = false;
        String string = filename.substring(0, 8);
        if(string.equals("Untitled")){
            try{
                untitled = Integer.parseInt(filename.substring(9));
            }catch(Exception ignored){

            }
        }
    }

    public CodeFile(Context context, Uri uri){
        this.uri = uri;
        this.name = Utils.getFilename(context, uri);
        this.readState = CodeFileReadState.UNREAD;
        this.modified = false;
        this.content = "";
        File root = context.getFilesDir();

        autosavedFile = new File(root, "File"  + file++);

        active = false;
    }

    public String getName() {
        return name;
    }

    public Uri getUri() {
        return uri;
    }

    public boolean isModified(){
        return modified;
    }

    public File getAutosavedFile(){
        return autosavedFile;
    }

    public int getUntitled(){
        return untitled;
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
        setModified(true);
    }

    public void read(Context context, Runnable callback){
        Executor executor = context.getMainExecutor();

        if(readState == CodeFileReadState.READ){
            if(callback != null){
                executor.execute(callback);
            }
            return;
        }

        if(Objects.equals(uri.getScheme(), SCHEME_CONTENT)){
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
        else {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    if(autosavedFile.exists()){
                        readState = CodeFileReadState.READING;
                        try{
                            InputStream inputStream = new FileInputStream(autosavedFile);
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
                    }
                    if(callback != null){
                        executor.execute(callback);
                    }
                }
            });

            thread.start();
        }

    }

    public boolean write(){
        return false;
    }

    public static String serialize(CodeFile codeFile){
        return codeFile.getName() + "|" + codeFile.getUri() + "|" + codeFile.autosavedFile.getName() + "|" + codeFile.untitled;
    }

    public static CodeFile deserialize(Context context, String serialized){
        String[] parts = serialized.split("\\|");
        CodeFile codeFile = new CodeFile();
        codeFile.name = parts[0];
        codeFile.uri = Uri.parse(parts[1]);
        codeFile.autosavedFile = new File(context.getFilesDir(), parts[2]);
        codeFile.content = "";

        try{
            codeFile.untitled = Integer.parseInt(parts[3]);
            int f = Integer.parseInt(parts[2].substring(4));
            if(f >= file){
                file = f + 1;
            }
        }catch(Exception ignored){

        }

        codeFile.active = false;
        if(parts.length == 5){
            codeFile.active = true;
        }
        codeFile.readState = CodeFileReadState.UNREAD;
        return codeFile;
    }
}
