package net.chittu.codepad;

import java.util.ArrayList;
import java.util.HashMap;

public class FileManager {
    public static final String AUTOSAVED_DIRECTORY_NAME = "autosaved";
    private File autosavedFilesDirectory;
    private HashMap<String, CodeFile> codeFiles;
    private ArrayList<String> locations;
    private int untitledCount;

    public FileManager(Context context){
        File root = context.getFilesDir();
        File autosavedFilesDirectory = new File(root, AUTOSAVED_DIRECTORY_NAME);
        if(!autosavedFilesDirectory.exists()){
            autosavedFilesDirectory.mkdirs();
        }



        codeFiles = new HashMap<>();
        locations = new ArrayList<>();
        untitledCount = 0;
    }

    public int getCount(){
        return codeFiles.size();
    }

    public CodeFile getFile(){
        String location = locations.get(position);
        return codeFiles.get(location);
    }

    public CodeFile addFile(){
        String filename = "Untitled " + (++untitledCount);
        Uri uri = Uri.parse("codepad://" + filename);

    }

    public int openFile(CodeFile codeFile){
        String location = codeFile.getUri().toString();
        if(locations.contains(location)){
            return locations.indexOf(location);
        }
        else{
            locations.add(location);
            codeFiles.put(location, codeFile);
            int index = locations.size() - 1;
            return index;
        }
    }

    public void closeFile(int position){
        if(position < 0 || position >= getItemCount())
            return;

        String location = locations.remove(position);
        codeFiles.remove(location);
    }











    public int getItemCount() {
        return codeFiles.size();
    }

    public CodeFile getCodeFile(int position){
        String location = locations.get(position);
        return codeFiles.get(location);
    }

    public int open(CodeFile codeFile){
        String location = codeFile.getUri().toString();
        if(locations.contains(location)){
            return locations.indexOf(location);
        }
        else{
            locations.add(location);
            codeFiles.put(location, codeFile);
            int index = locations.size() - 1;
            return index;
        }
    }

    public void close(int position){
        if(position < 0 || position >= getItemCount())
            return;

        String location = locations.remove(position);
        codeFiles.remove(location);
    }
}
