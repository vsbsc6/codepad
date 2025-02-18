package net.chittu.codepad;

import android.content.Context;
import android.net.Uri;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executor;

public class FileManager {
    public static final String AUTOSAVED_DIRECTORY_NAME = "autosaved";
    public static final String INDEX_FILE_NAME = "index.txt";
    private HashMap<String, CodeFile> codeFiles;
    private ArrayList<String> locations;

    private File indexFile;

    private File autosavedFilesDirectory;

    private int nextTitle;

    private int active;

    private FileManagerState state;

    public FileManager(Context context){
        codeFiles = new HashMap<>();
        locations = new ArrayList<>();
        File root = context.getFilesDir();
        indexFile = new File(root, INDEX_FILE_NAME);
        File autosavedFilesDirectory = new File(root, AUTOSAVED_DIRECTORY_NAME);
        if(!autosavedFilesDirectory.exists()){
            autosavedFilesDirectory.mkdirs();
        }
        nextTitle = 1;
        active = -1;
    }

    public int getCount(){
        return codeFiles.size();
    }

    public CodeFile getFileAt(int position){
        String location = locations.get(position);
        return codeFiles.get(location);
    }

    public int getActive(){
        return active;
    }

    public int addFile(CodeFile codeFile){
        String location = codeFile.getUri().toString();
        if(locations.contains(location)){
            return locations.indexOf(location);
        }
        else{
            locations.add(location);
            codeFiles.put(location, codeFile);
            writeIndex();
            return locations.size() - 1;
        }
    }

    public int addNew(Context context){
        String filename = "Untitled " + nextTitle++;
        CodeFile codeFile = new CodeFile(context, filename);
        return addFile(codeFile);
    }

    public int openUri(Context context, Uri uri){
        CodeFile codeFile = new CodeFile(context, uri);
        return addFile(codeFile);
    }

    public CodeFile removeFile(int position){
        String location = locations.remove(position);
        if(position == active){
            active--;
            if(active < 0 && getCount() > 1){
                active = 0;
            }
        }

        CodeFile codeFile = codeFiles.remove(location);
        writeIndex();
        return codeFile;
    }

    public CodeFile activateFile(int position){
        active = position;
        writeIndex();
        return getFileAt(position);
    }

    public void launch(Context context, Uri uri, Runnable callback){
        if(state == FileManagerState.LAUNCHING)
            return;

        Executor executor = context.getMainExecutor();
        if(state == FileManagerState.READY){
            if(callback != null){
                executor.execute(callback);
            }
            return;
        }

        state = FileManagerState.LAUNCHING;
        Thread thread = new Thread(() -> {
            try {
                InputStream inputStream = new FileInputStream(indexFile);
                InputStreamReader reader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(reader);
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    CodeFile codeFile = CodeFile.deserialize(context, line);
                    if(codeFile != null){
                        addFile(codeFile);
                        int sr = codeFile.getUntitled();
                        if(sr >= nextTitle){
                            nextTitle = sr + 1;
                        }
                    }
                }
                bufferedReader.close();
                reader.close();
                inputStream.close();
            } catch (Exception ignored) {

            }

            if(uri != null){
                active = openUri(context, uri);
            }

            state = FileManagerState.READY;
            if(callback != null){
                executor.execute(callback);
            }
            autoSave();
        });

        thread.start();
    }

    public void autoSave(){
        Thread thread = new Thread(()->{
            try {
                while(true){
                    FileWriter writer = new FileWriter(indexFile);
                    FileWriter codeFileWriter = null;
                    for(int i = 0; i < getCount(); i++){
                        CodeFile codeFile = getFileAt(i);
                        writer.append(CodeFile.serialize(codeFile));
                        if(active == i){
                            writer.append("|active");
                        }
                        writer.append("\n");

                        if(codeFile.isModified()){
                            codeFileWriter = new FileWriter(codeFile.getAutosavedFile());
                            codeFileWriter.write(codeFile.getContent());
                            codeFileWriter.close();
                        }
                    }
                    writer.close();
                    Thread.sleep(120000);
                }
            } catch (Exception ignored) {

            }
        });
        thread.start();
    }

    public void writeIndex(){
        Thread thread = new Thread(()->{
            try {

                FileWriter writer = new FileWriter(indexFile);
                for(int i = 0; i < getCount(); i++){
                    CodeFile codeFile = getFileAt(i);
                    writer.append(CodeFile.serialize(codeFile));
                    if(active == i){
                        writer.append("|active");
                    }
                    writer.append("\n");
                }
                writer.close();
            } catch (Exception ignored) {

            }
        });
        thread.start();
    }
}
