package com.example.ay22_23s2mdpgrp9;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.WindowManager;

import com.example.ay22_23s2mdpgrp9.bluetooth.BluetoothConnectionService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.concurrent.CountedCompleter;

public class MainActivity extends AppCompatActivity {

    public static boolean hasBtConnectedDevice = false;
    public static BluetoothConnectionService globalBluetoothService = null;

    public static BluetoothDevice connectedDevice = null;

    public static StringBuilder serialChat = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
         AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(R.id.bluetoothFragment, R.id.statusFragment, R.id.mapConfigFragment,
                R.id.controllerFragment).build();
        NavController navController = Navigation.findNavController(this,  R.id.fragmentContainerView);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        getSupportActionBar().hide();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);


    }
}