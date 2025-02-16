package net.chittu.codepad;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayoutMediator;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import net.chittu.codepad.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    private EditorsAdapter adapter;

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

        EditorsAdapter adapter = new EditorsAdapter(getSupportFragmentManager(), getLifecycle());
        binding.pager.setAdapter(adapter);
        new TabLayoutMediator(binding.tabs, binding.pager, (tab, position) -> tab.setText("File " + (position + 1))
        ).attach();
    }

    public void onClickTextButton(View view) {
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        Fragment fragment = supportFragmentManager.findFragmentByTag("f" + binding.pager.getCurrentItem());

        if(fragment instanceof EditorFragment){
            EditorFragment homeFragment = (EditorFragment) fragment;
            homeFragment.onClickTextButton(view);
        }
    }

    public void onClickTabButton(View view) {
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        Fragment fragment = supportFragmentManager.findFragmentByTag("f" + binding.pager.getCurrentItem());

        if(fragment instanceof EditorFragment){
            EditorFragment homeFragment = (EditorFragment) fragment;
            homeFragment.onClickTabButton(view);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public boolean askForFilePermissions() {
        boolean hasPermission = this.checkSelfPermission(android.Manifest.permission.MANAGE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        if (!hasPermission) {
            requestPermissions(new String[]{android.Manifest.permission.MANAGE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    FragmentManager supportFragmentManager = getSupportFragmentManager();
                    Fragment fragment = supportFragmentManager.findFragmentByTag("f" + binding.pager.getCurrentItem());

                    if(fragment instanceof EditorFragment){
                        EditorFragment homeFragment = (EditorFragment) fragment;
                        homeFragment.saveFile(binding.pager.getCurrentItem());
                    }

                } else {
                    // permission denied, boo!
                }

                return;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.save){
            askForFilePermissions();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return super.onSupportNavigateUp();
    }
}