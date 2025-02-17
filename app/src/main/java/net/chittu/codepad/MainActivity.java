package net.chittu.codepad;

import android.app.ComponentCaller;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import net.chittu.codepad.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private int stock = 0;
    private FilesAdapter filesAdapter;
    private CodeView mEditorView;
    private LineView mLineNumberView;
    private CodeFile activeCodeFile;

    private boolean idle = true;

    private FileManager fileManager;

    private ActivityResultLauncher<String[]> openLauncher;
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_home)
                .setOpenableLayout(drawer)
                .build();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, binding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        mEditorView = binding.editor;
        mLineNumberView = binding.lineNumber;

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

        //this.filesAdapter = new FilesAdapter(getSupportFragmentManager(), getLifecycle());

        this.fileManager = new FileManager();
        Intent intent = getIntent();
        Uri uri = intent.getData();
        if(uri != null){
            CodeFile codeFile = new CodeFile(this, uri);
            int tabIndex = fileManager.open(codeFile);
            TabLayout.Tab tab = binding.tabs.getTabAt(tabIndex);
            if(tab != null){
                tab.select();
            }
        }

        binding.tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                changeCodeFile();
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

        /*
        binding.pager.setAdapter(filesAdapter);
        new TabLayoutMediator(binding.tabs, binding.pager, filesAdapter).attach();
        Intent intent = getIntent();
        Uri uri = intent.getData();
        if(uri != null){
            CodeFile codeFile = new CodeFile(this, uri);
            int tabIndex = filesAdapter.open(codeFile);
            TabLayout.Tab tab = binding.tabs.getTabAt(tabIndex);
            if(tab != null){
                tab.select();
            }
        }*/

        this.openLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        if(uri != null){
                            CodeFile codeFile = new CodeFile(MainActivity.this, uri);
                            addCodeFile(codeFile);
                            /*
                            int tabIndex = filesAdapter.open(codeFile);
                            TabLayout.Tab tab = binding.tabs.getTabAt(tabIndex);
                            if(tab != null){
                                tab.select();
                            }*/
                        }
                    }
                });

        navigationView.setNavigationItemSelectedListener(this);
    }

    public void onClickTextButton(View view) {
        /*
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        Fragment fragment = supportFragmentManager.findFragmentByTag("f" + binding.pager.getCurrentItem());

        if(fragment instanceof EditorFragment){
            EditorFragment homeFragment = (EditorFragment) fragment;
            homeFragment.onClickTextButton(view);
        }*/
    }

    public void onClickTabButton(View view) {
        /*
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        Fragment fragment = supportFragmentManager.findFragmentByTag("f" + binding.pager.getCurrentItem());

        if(fragment instanceof EditorFragment){
            EditorFragment homeFragment = (EditorFragment) fragment;
            homeFragment.onClickTabButton(view);
        }*/
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    public boolean askForFilePermissions() {
        boolean hasPermission = this.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        if (!hasPermission) {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                /*
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    FragmentManager supportFragmentManager = getSupportFragmentManager();
                    Fragment fragment = supportFragmentManager.findFragmentByTag("f" + binding.pager.getCurrentItem());

                    if(fragment instanceof EditorFragment){
                        EditorFragment homeFragment = (EditorFragment) fragment;
                        homeFragment.saveFile(binding.pager.getCurrentItem());
                    }

                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }*/

                return;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data, @NonNull ComponentCaller caller) {
        super.onActivityResult(requestCode, resultCode, data, caller);

        if(resultCode == RESULT_OK){

        }
        else{
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }

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
            newFile();
            binding.drawerLayout.close();
            return true;
        }

        if(item.getItemId() == R.id.nav_open){
            openFile();
            binding.drawerLayout.close();
            return true;
        }

        if(item.getItemId() == R.id.nav_close){
            closeFile();
            binding.drawerLayout.close();
            return true;
        }

        return false;
    }

    private void newFile(){
        String filename = "Untitled " + (++stock);
        Uri uri = Uri.parse("codepad://" + filename);
        CodeFile codeFile = new CodeFile(uri, filename, filename, "");
        addCodeFile(codeFile);
        /*
        int tabIndex = filesAdapter.open(codeFile);
        TabLayout.Tab tab = binding.tabs.getTabAt(tabIndex);
        if(tab != null){
            tab.select();
        }*/
    }

    private void openFile(){
        openLauncher.launch(new String[]{"*/*"});
    }

    private void closeFile(){
        //filesAdapter.close(binding.tabs.getSelectedTabPosition());
        removeCodeFile();
    }

    private void addCodeFile(CodeFile codeFile){
        int index = fileManager.open(codeFile);
        activeCodeFile = codeFile;
        mEditorView.setEnabled(false);
        activeCodeFile.read(this, new Runnable() {
            @Override
            public void run() {
                if(activeCodeFile.isRead()){
                    mEditorView.setText(activeCodeFile.getContent());
                }
                else{
                    Toast.makeText(MainActivity.this, "Unable to read file.", Toast.LENGTH_SHORT).show();
                }
                mEditorView.setEnabled(true);
            }
        });

        if(index == binding.tabs.getTabCount()){
            TabLayout.Tab tab = binding.tabs.newTab();
            tab.setText(codeFile.getName());
            binding.tabs.addTab(tab, true);
        }
        else{
            binding.tabs.selectTab(binding.tabs.getTabAt(index));
        }

    }

    private void removeCodeFile(){
        int tabIndex = binding.tabs.getSelectedTabPosition();
        binding.tabs.removeTab(binding.tabs.getTabAt(tabIndex));
        fileManager.close(tabIndex);
        tabIndex = binding.tabs.getSelectedTabPosition();
        activeCodeFile = fileManager.getCodeFile(tabIndex);
        mEditorView.setEnabled(false);
        activeCodeFile.read(this, new Runnable() {
            @Override
            public void run() {
                if(activeCodeFile.isRead()){
                    mEditorView.setText(activeCodeFile.getContent());
                }
                else{
                    Toast.makeText(MainActivity.this, "Unable to read file.", Toast.LENGTH_SHORT).show();
                }
                mEditorView.setEnabled(true);
            }
        });
    }

    private void changeCodeFile(){
        int tabIndex = binding.tabs.getSelectedTabPosition();
        activeCodeFile = fileManager.getCodeFile(tabIndex);
        mEditorView.setEnabled(false);
        activeCodeFile.read(this, new Runnable() {
            @Override
            public void run() {
                if(activeCodeFile.isRead()){
                    mEditorView.setText(activeCodeFile.getContent());
                }
                else{
                    Toast.makeText(MainActivity.this, "Unable to read file.", Toast.LENGTH_SHORT).show();
                }
                mEditorView.setEnabled(true);
            }
        });
    }
}