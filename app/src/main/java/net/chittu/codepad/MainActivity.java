package net.chittu.codepad;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import net.chittu.codepad.databinding.ActivityMainBinding;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private CodeView mEditorView;
    private LineView mLineNumberView;
    private CodeFile activeCodeFile;

    private boolean idle = true;

    private FileManager fileManager;

    private ActivityResultLauncher<String[]> openLauncher;

    private ActivityResultLauncher<String> saveLauncher;
    private ActivityMainBinding binding;

    private boolean hidden = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Inflate activity layout
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        //Set content view of activity
        setContentView(binding.getRoot());

        //Set action bar
        setSupportActionBar(binding.toolbar);

        //Set up drawer
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, binding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //Set up editor
        mEditorView = binding.editor;
        mLineNumberView = binding.lineNumber;
        mEditorView.setLineView(mLineNumberView);

        //Set up file manager
        fileManager = new FileManager(this);

        //Check if activity was launched to view a file
        Intent intent = getIntent();
        Uri uri = intent.getData();

        //Start the file manager with uri if any
        fileManager.launch(this, uri, () -> setupTabs());

        //Respond to tab selection
        binding.tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateTab();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        mEditorView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!idle)
                    return;
                idle = false;
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        activeCodeFile.setContent(editable.toString());
                        idle = true;
                    }
                });
                thread.start();
            }
        });

        this.openLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        if(uri != null){
                            addTab(uri);
                        }
                    }
                });

        saveLauncher = registerForActivityResult(new ActivityResultContracts.CreateDocument("text/*"), saveUri -> {
            try {
                OutputStream outputStream = getContentResolver().openOutputStream(saveUri);
                OutputStreamWriter writer = new OutputStreamWriter(outputStream);
                BufferedWriter bufferedWriter = new BufferedWriter(writer);
                bufferedWriter.append(activeCodeFile.getContent());
                bufferedWriter.close();
                writer.close();
                outputStream.close();
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
            } catch (Exception ignored) {
                Toast.makeText(this, "Unable to save", Toast.LENGTH_SHORT).show();
            }
        });

        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.nav_new){
            addTab();
            binding.drawerLayout.close();
            return true;
        }

        if(item.getItemId() == R.id.nav_open){
            openLauncher.launch(new String[]{"*/*"});
            binding.drawerLayout.close();
            return true;
        }

        if(item.getItemId() == R.id.nav_save){
            saveLauncher.launch(activeCodeFile.getName());
            binding.drawerLayout.close();
            return true;
        }

        if(item.getItemId() == R.id.nav_close){
            removeTab();
            binding.drawerLayout.close();
            return true;
        }

        return false;
    }

    private void addTab(){
        int index = fileManager.addNew(this);
        setupEditor();
        activeCodeFile =  fileManager.activateFile(index);
        TabLayout.Tab tab = binding.tabs.newTab();
        tab.setText(activeCodeFile.getName());
        binding.tabs.addTab(tab);
        tab.select();
    }

    private void addTab(Uri uri){
        int index = fileManager.openUri(this, uri);
        setupEditor();
        activeCodeFile =  fileManager.activateFile(index);
        if(binding.tabs.getTabCount() != fileManager.getCount()){
            TabLayout.Tab tab = binding.tabs.newTab();
            tab.setText(activeCodeFile.getName());
            binding.tabs.addTab(tab);
            tab.select();
        }

        mEditorView.setEnabled(false);
        activeCodeFile.read(this, new Runnable() {
            @Override
            public void run() {
                if(activeCodeFile.isRead()){
                    mEditorView.setText(activeCodeFile.getContent());
                }
                else{
                    //Toast.makeText(MainActivity.this, "Unable to read file.", Toast.LENGTH_SHORT).show();
                }
                mEditorView.setEnabled(true);
            }
        });
    }

    private void removeTab(){
        if(fileManager.getCount() == 0)
            return;

        int selectedTabIndex = binding.tabs.getSelectedTabPosition();
        binding.tabs.removeTab(binding.tabs.getTabAt(selectedTabIndex));
        fileManager.removeFile(selectedTabIndex);

        int active = fileManager.getActive();
        if(active >= 0){
            activeCodeFile = fileManager.getFileAt(active);
            mEditorView.setEnabled(false);
            activeCodeFile.read(this, new Runnable() {
                @Override
                public void run() {
                    if(activeCodeFile.isRead()){
                        mEditorView.setText(activeCodeFile.getContent());
                    }
                    else{
                        //Toast.makeText(MainActivity.this, "Unable to read file.", Toast.LENGTH_SHORT).show();
                    }
                    mEditorView.setEnabled(true);
                }
            });
        }
        else{
            setupEditor();
        }
    }

    private void updateTab(){
        int selectedTabIndex = binding.tabs.getSelectedTabPosition();
        activeCodeFile = fileManager.activateFile(selectedTabIndex);

        mEditorView.setEnabled(false);
        activeCodeFile.read(this, new Runnable() {
            @Override
            public void run() {
                if(activeCodeFile.isRead()){
                    mEditorView.setText(activeCodeFile.getContent());
                }
                else{
                    //Toast.makeText(MainActivity.this, "Unable to read file.", Toast.LENGTH_SHORT).show();
                }
                mEditorView.setEnabled(true);
            }
        });
    }

    private void setupEditor(){
        if (fileManager.getCount() == 0){
            binding.editorContainer.setVisibility(View.GONE);
            binding.buttonWrapper.setVisibility(View.GONE);
        }
        else{
            binding.editorContainer.setVisibility(View.VISIBLE);
            binding.buttonWrapper.setVisibility(View.VISIBLE);

            //Adjust height of editor
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
        }
    }

    private void setupTabs(){
        setupEditor();
        int active = fileManager.getActive();

        for(int i = 0; i < fileManager.getCount(); i++){
            CodeFile codeFile = fileManager.getFileAt(i);
            TabLayout.Tab tab = binding.tabs.newTab();
            tab.setText(codeFile.getName());
            binding.tabs.addTab(tab);
            if(active == i){
                activeCodeFile = codeFile;
                binding.tabs.selectTab(tab);
            }
        }
    }

    public void onClickTextButton(View view) {
        if(activeCodeFile == null)
            return;

        int start = Math.max(mEditorView.getSelectionStart(), 0);
        int end = Math.max(mEditorView.getSelectionEnd(), 0);
        mEditorView.getText().replace(Math.min(start, end), Math.max(start, end), ((TextView)view).getText(), 0, 1);
        mEditorView.requestFocus();
    }

    public void onClickTabButton(View view) {
        if(activeCodeFile == null)
            return;
        int start = Math.max(mEditorView.getSelectionStart(), 0);
        int end = Math.max(mEditorView.getSelectionEnd(), 0);
        mEditorView.getText().replace(Math.min(start, end), Math.max(start, end), "    ", 0, 4);
        mEditorView.requestFocus();
    }
}