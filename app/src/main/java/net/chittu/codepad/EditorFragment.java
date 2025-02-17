package net.chittu.codepad;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Adapter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import net.chittu.codepad.databinding.FragmentEditorBinding;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class EditorFragment extends Fragment {
    private final CodeFile codeFile;

    private FragmentEditorBinding binding;

    private CodeView mEditorView;
    private LineView mLineNumberView;

    public EditorFragment(CodeFile codeFile){
        this.codeFile = codeFile;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        codeFile.read(getContext(), null);
        binding = FragmentEditorBinding.inflate(inflater, container, false);

        mEditorView = binding.editor;
        mLineNumberView = binding.lineNumber;

        mEditorView.setLineView(mLineNumberView);

        final View editorContainer = binding.editorContainer;
        editorContainer.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mEditorView.setMinimumHeight(editorContainer.getHeight());
                        mLineNumberView.setMinimumHeight(editorContainer.getHeight());
                        editorContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
        );
        View root = binding.getRoot();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        codeFile.read(getContext(), () -> {
            if(codeFile.isRead()){
                mEditorView.setText(codeFile.getContent());
            }
            else{
                Toast.makeText(getContext(), "Unable to read file.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        codeFile.setContent(mEditorView.getText().toString());
    }

    public void saveFile(int id){
        String root = Environment.getExternalStorageDirectory().toString();
        String fname = "File" + id + ".txt";
        File file = new File (root, fname);
        if (file.exists ())
            file.delete ();
        try {

            FileOutputStream out = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(out);
            writer.write(mEditorView.getText().toString());
            writer.close();
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onClickTextButton(View view) {
        int start = Math.max(mEditorView.getSelectionStart(), 0);
        int end = Math.max(mEditorView.getSelectionEnd(), 0);
        mEditorView.getText().replace(Math.min(start, end), Math.max(start, end), ((TextView)view).getText(), 0, 1);
        mEditorView.requestFocus();
    }

    public void onClickTabButton(View view) {
        int start = Math.max(mEditorView.getSelectionStart(), 0);
        int end = Math.max(mEditorView.getSelectionEnd(), 0);
        mEditorView.getText().replace(Math.min(start, end), Math.max(start, end), "    ", 0, 4);
        mEditorView.requestFocus();
    }

    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
