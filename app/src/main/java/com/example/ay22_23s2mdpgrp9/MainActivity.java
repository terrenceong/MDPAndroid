package com.example.ay22_23s2mdpgrp9;

import static android.view.DragEvent.ACTION_DRAG_ENTERED;
import static android.view.DragEvent.ACTION_DRAG_EXITED;
import static android.view.DragEvent.ACTION_DROP;
import static com.example.ay22_23s2mdpgrp9.Constants.ADD_OBSTACLE;
import static com.example.ay22_23s2mdpgrp9.Constants.LOG;
import static com.example.ay22_23s2mdpgrp9.Constants.MOVE_BACKWARD;
import static com.example.ay22_23s2mdpgrp9.Constants.MOVE_FORWARD;
import static com.example.ay22_23s2mdpgrp9.Constants.REMOVE_OBSTACLE;
import static com.example.ay22_23s2mdpgrp9.Constants.TURN_LEFT;
import static com.example.ay22_23s2mdpgrp9.Constants.TURN_RIGHT;
import static com.example.ay22_23s2mdpgrp9.Constants.UPDATE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.example.ay22_23s2mdpgrp9.Arena.ArenaButton;
import com.example.ay22_23s2mdpgrp9.Arena.ObstacleImages;
import com.example.ay22_23s2mdpgrp9.Arena.ObstacleInfo;
import com.example.ay22_23s2mdpgrp9.bluetooth.BluetoothConnectionService;
import com.example.ay22_23s2mdpgrp9.status.StatusFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountedCompleter;

public class MainActivity extends AppCompatActivity {

    public static boolean hasBtConnectedDevice = false;
    public static BluetoothConnectionService globalBluetoothService = null;

    public static BluetoothDevice connectedDevice = null;

    public static StringBuilder serialChat = new StringBuilder();

    StatusFragment fragmentMap;

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

    private void rpiMessageHandler(JSONObject json) throws JSONException {
        JSONObject val = (json.has("value")) ? json.getJSONObject("value") : null;
        int x, y, imageID, direction;

        switch (json.getInt("type")) {
            case MOVE_FORWARD:
                fragmentMap.setRoboStatus("MOVE FORWARD");
                int t = val.getInt("time");
                for(int i =0;i<t;i++)
                {
                    fragmentMap.moveRobot(true);
                    fragmentMap.setRobotPosition(fragmentMap.getPositionString());
                }
                //fragmentMap.moveRobot(true);
                //fragmentLeftCol.setRobotPosition(fragmentMap.getPositionString());
                break;

            case MOVE_BACKWARD:
                fragmentMap.setRoboStatus("MOVE BACKWARD");
                fragmentMap.moveRobot(false);
                fragmentMap.setRobotPosition(fragmentMap.getPositionString());
                break;

            case TURN_LEFT:
                fragmentMap.setRoboStatus("TURN LEFT");
                fragmentMap.rotateRobot(null, -90);
                fragmentMap.setRoboDirection(fragmentMap.getDirectionString());
                break;

            case TURN_RIGHT:
                fragmentMap.setRoboStatus("TURN RIGHT");
                fragmentMap.rotateRobot(null, 90);
                fragmentMap.setRoboDirection(fragmentMap.getDirectionString());
                break;

            case ADD_OBSTACLE:
                fragmentMap.setRoboStatus("ADD OBSTACLE");
                x = val.getInt("x");
                y = val.getInt("y");
                if(x < 0) x= 0;
                if(x > 19) x =19;
                if(y < 0) y = 0;
                if(y > 19) y = 19;
                imageID = val.getInt("image_id");
                // direction = val.getInt("DIRECTION");
                drawObstacleImg(x, y, imageID);
                break;

            case REMOVE_OBSTACLE:
                fragmentMap.setRoboStatus("REMOVE OBSTACLE");
                imageID = val.getInt("image_id");
                fragmentMap.emptyCellObsID(imageID);
                break;

            case UPDATE:
                fragmentMap.setRoboStatus("Updating Robot");
                x = val.getInt("x");
                y = val.getInt("y");
                if(x < 0) x= 0;
                if(x > 19) x =19;
                if(y < 0) y = 0;
                if(y > 19) y = 19;
                direction = val.getInt("direction");
                fragmentMap.setRobotDirection(direction);
                fragmentMap.setRobotXY(x, y);
                fragmentMap.setRoboDirection(fragmentMap.getDirectionString());
                fragmentMap.setRobotPosition(fragmentMap.getPositionString());
                break;

//            case LOG:
//                String msg = val.getString("MESSAGE");
//                fragmentMap.addConsoleMessage(msg);
//                break;

            //(FIX) Not sure if needed
        }
    }
    private void drawObstacleImg(int x, int y, int imageID) {
        // gets image to be drawn
        Drawable imgDrawable = getImgDrawable(imageID);

        int obstacleID = fragmentMap.getObstacleIDByCoord(x, y);

        if (obstacleID == -1)
            return;

        // gets corresponding obstacle
        ObstacleInfo obsInfo = fragmentMap.getObstacles().get(obstacleID);

        // draws recognised image onto obstacle block
        ArenaButton btn = findViewById(obsInfo.btnID);
        imgDrawable.setBounds(2,2,btn.getWidth() - 2,btn.getHeight() - 2);
        btn.getOverlay().add(imgDrawable);
        btn.setText("");
        btn.setTextColor(Color.parseColor("#FFFFFFFF"));
    }

    private Drawable getImgDrawable(int imageID) {
        int drawableID = ObstacleImages.getDrawableID(imageID);
        return AppCompatResources.getDrawable(this, drawableID);
    }

    // for drag events to anything outside the grid
    // remove the dragged item
    private class OutOfBoundsDragListener implements View.OnDragListener {
        @Override
        public boolean onDrag(View view, DragEvent e) {
            switch (e.getAction()) {
                case ACTION_DRAG_ENTERED:
                case ACTION_DRAG_EXITED:
                    return true;

                case ACTION_DROP:
                    ArenaButton originalBtn = (ArenaButton) e.getLocalState();
                    fragmentMap.emptyCell(originalBtn);
                    return true;
            }
            return true;
        }
    }

}