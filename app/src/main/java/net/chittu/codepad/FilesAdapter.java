package net.chittu.codepad;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.HashMap;


public class FilesAdapter extends FragmentStateAdapter implements TabLayoutMediator.TabConfigurationStrategy {
    private HashMap<String, CodeFile> codeFiles = new HashMap<>();
    private ArrayList<String> locations = new ArrayList<>();

    public FilesAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @Override
    public int getItemCount() {
        return codeFiles.size();
    }


    private CodeFile getCodeFile(int position){
        String location = locations.get(position);
        return codeFiles.get(location);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        CodeFile codeFile = getCodeFile(position);
        return new EditorFragment(codeFile);
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
            notifyItemInserted(index);
            return index;
        }
    }

    public void close(int position){
        if(position < 0 || position >= getItemCount())
            return;

        String location = locations.remove(position);
        codeFiles.remove(location);

        notifyItemRemoved(position);
    }

    @Override
    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
        CodeFile codeFile = getCodeFile(position);
        tab.setText(codeFile.getName());
    }
}
