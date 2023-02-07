package com.example.ay22_23s2mdpgrp9;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.ay22_23s2mdpgrp9.bluetooth.BluetoothConnectionService;
import com.example.ay22_23s2mdpgrp9.status.StatusFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;


public class MainActivity extends AppCompatActivity{

    public static boolean hasBtConnectedDevice = false;
    public static BluetoothConnectionService globalBluetoothService = null;

    public static BluetoothDevice connectedDevice = null;

    public static StringBuilder serialChat = new StringBuilder();

    public static Context statusFragmentContext = null;

    public static ProgressDialog mProgressDialog;

    StatusFragment fragmentMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
         AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(R.id.bluetoothFragment, R.id.statusFragment,
                R.id.controllerFragment).build();
        NavController navController = Navigation.findNavController(this,  R.id.fragmentContainerView);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
        NavigationUI.setupActionBarWithNavController(this, navController,appBarConfiguration);
//        bottomNavigationView.setOnNavigationItemSelectedListener(this);
//        mProgressDialog = new ProgressDialog(this);
//        mProgressDialog.setMessage("Loading...");
//        mProgressDialog.setCancelable(false);
        getSupportActionBar().hide();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

    }

//    @Override
//    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//        switch(item.getItemId()){
//            case R.id.bluetoothFragment: Navigation.findNavController(
//                    this, R.id.fragmentContainerView).navigate(R.id.bluetoothFragment);
//            break;
//            case R.id.statusFragment:
//                mProgressDialog.show();
//                    Navigation.findNavController(
//                    this, R.id.fragmentContainerView).navigate(R.id.statusFragment);
//            break;
//            case R.id.controllerFragment: Navigation.findNavController(
//                    this, R.id.fragmentContainerView).navigate(R.id.controllerFragment);
//            break;
//
//        }
//        return true;
//
//    }


//    private void rpiMessageHandler(JSONObject json) throws JSONException {
//        JSONObject val = (json.has("value")) ? json.getJSONObject("value") : null;
//        int x, y, imageID, direction;
//
//        switch (json.getInt("type")) {
//            case MOVE_FORWARD:
//                fragmentMap.setRoboStatus("MOVE FORWARD");
//                int t = val.getInt("time");
//                for(int i =0;i<t;i++)
//                {
//                    fragmentMap.moveRobot(true);
//                    fragmentMap.setRobotPosition(fragmentMap.getPositionString());
//                }
//                //fragmentMap.moveRobot(true);
//                //fragmentLeftCol.setRobotPosition(fragmentMap.getPositionString());
//                break;
//
//            case MOVE_BACKWARD:
//                fragmentMap.setRoboStatus("MOVE BACKWARD");
//                fragmentMap.moveRobot(false);
//                fragmentMap.setRobotPosition(fragmentMap.getPositionString());
//                break;
//
//            case TURN_LEFT:
//                fragmentMap.setRoboStatus("TURN LEFT");
//                fragmentMap.rotateRobot(null, -90);
//                fragmentMap.setRoboDirection(fragmentMap.getDirectionString());
//                break;
//
//            case TURN_RIGHT:
//                fragmentMap.setRoboStatus("TURN RIGHT");
//                fragmentMap.rotateRobot(null, 90);
//                fragmentMap.setRoboDirection(fragmentMap.getDirectionString());
//                break;
//
//            case ADD_OBSTACLE:
//                fragmentMap.setRoboStatus("ADD OBSTACLE");
//                x = val.getInt("x");
//                y = val.getInt("y");
//                if(x < 0) x= 0;
//                if(x > 19) x =19;
//                if(y < 0) y = 0;
//                if(y > 19) y = 19;
//                imageID = val.getInt("image_id");
//                // direction = val.getInt("DIRECTION");
//                drawObstacleImg(x, y, imageID);
//                break;
//
//            case REMOVE_OBSTACLE:
//                fragmentMap.setRoboStatus("REMOVE OBSTACLE");
//                imageID = val.getInt("image_id");
//                fragmentMap.emptyCellObsID(imageID);
//                break;
//
//            case UPDATE:
//                fragmentMap.setRoboStatus("Updating Robot");
//                x = val.getInt("x");
//                y = val.getInt("y");
//                if(x < 0) x= 0;
//                if(x > 19) x =19;
//                if(y < 0) y = 0;
//                if(y > 19) y = 19;
//                direction = val.getInt("direction");
//                fragmentMap.setRobotDirection(direction);
//                fragmentMap.setRobotXY(x, y);
//                fragmentMap.setRoboDirection(fragmentMap.getDirectionString());
//                fragmentMap.setRobotPosition(fragmentMap.getPositionString());
//                break;
//
////            case LOG:
////                String msg = val.getString("MESSAGE");
////                fragmentMap.addConsoleMessage(msg);
////                break;
//
//            //(FIX) Not sure if needed
//        }
//    }

}