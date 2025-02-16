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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import net.chittu.codepad.databinding.FragmentEditorBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class EditorFragment extends Fragment {
    FragmentEditorBinding binding;
    private CodeView mEditorView;
    private LineView mLineNumberView;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

    public void openFile(){
        mEditorView.setVisibility(View.GONE);
        Intent intent = getActivity().getIntent();

        if(intent != null){
            final Uri uri = intent.getData();
            if(uri != null){
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Manager.getInstance(getActivity()).readUri(uri, mEditorView.getText());
                    }
                }, 500);

            }
            mEditorView.setVisibility(View.VISIBLE);
        }
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
