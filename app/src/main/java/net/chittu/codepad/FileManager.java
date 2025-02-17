package net.chittu.codepad;

import java.util.ArrayList;
import java.util.HashMap;

public class FileManager {
    private HashMap<String, CodeFile> codeFiles = new HashMap<>();
    private ArrayList<String> locations = new ArrayList<>();

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
