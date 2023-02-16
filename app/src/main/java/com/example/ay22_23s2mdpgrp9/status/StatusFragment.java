package com.example.ay22_23s2mdpgrp9.status;

import static android.view.DragEvent.ACTION_DRAG_ENTERED;
import static android.view.DragEvent.ACTION_DRAG_EXITED;
import static android.view.DragEvent.ACTION_DROP;
import static com.example.ay22_23s2mdpgrp9.constant.Constant.EAST;
import static com.example.ay22_23s2mdpgrp9.constant.Constant.FORWARD;
import static com.example.ay22_23s2mdpgrp9.constant.Constant.HANDSHAKE;
import static com.example.ay22_23s2mdpgrp9.constant.Constant.LEFT_TURN;
import static com.example.ay22_23s2mdpgrp9.constant.Constant.NORTH;
import static com.example.ay22_23s2mdpgrp9.constant.Constant.READY;
import static com.example.ay22_23s2mdpgrp9.constant.Constant.REVERSE;
import static com.example.ay22_23s2mdpgrp9.constant.Constant.RIGHT_TURN;
import static com.example.ay22_23s2mdpgrp9.constant.Constant.ROBOT;
import static com.example.ay22_23s2mdpgrp9.constant.Constant.SOUTH;
import static com.example.ay22_23s2mdpgrp9.constant.Constant.TARGET;
import static com.example.ay22_23s2mdpgrp9.constant.Constant.WEST;
import static com.example.ay22_23s2mdpgrp9.constant.Constant.statusMapping;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.ay22_23s2mdpgrp9.Arena.ArenaButton;
import com.example.ay22_23s2mdpgrp9.Arena.MyDragShadowBuilder;
import com.example.ay22_23s2mdpgrp9.Arena.ObstacleInfo;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;


import com.example.ay22_23s2mdpgrp9.MainActivity;
import com.example.ay22_23s2mdpgrp9.R;
import com.example.ay22_23s2mdpgrp9.constant.Constant;

public class StatusFragment extends Fragment {

    //drawn is the variable that keep tracks how many grid the table layout have drawn
    //btnH,bthW is the grid height and grid width
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    int x, y, btnH, btnW, drawn = 0;
    // grid drawable background
    Drawable btnBG = null;
    int robotRotation = 0;

    private String TAG ="StatusFragment";

    TextView txtRoboStatus, txtRoboDirection, txtRoboPosition;

    //ObstacleList
    // robot defaults to facing north
    int robotDirection = NORTH;
    protected int robotX=-1;
    protected int robotY =-1;
    //public LeftColFragment fragmentLeftCol;

    // stores buttonIDs by xy coordinates
    int[][] coord = new int[20][20];

    // obstacleID: obstacleInfo obj
    HashMap<Integer, ObstacleInfo> obstacles = new HashMap<>();
    ArrayList<String> dirs = new ArrayList<>(Arrays.asList("Top", "Right", "Bottom", "Left"));

    TableLayout mapTable;
    ImageView imgRobot;
    RadioGroup spawnGroup;
    private ImageView determinedImageIV;

    private ImageButton forwardBtn;

    private ImageButton reverseBtn;

    private ImageButton rightTurnBtn;

    private ImageButton leftTurnBtn;

    private Button imageRecBtn;

    private Button fastestCarBtn;
    Button resetButton;

    public StatusFragment() {
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public void onPause() {
        super.onPause();
        reset(true);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       View view = inflater.inflate(R.layout.fragment_status, container, false);
        ConstraintLayout statusFragmentLayout = view.findViewById(R.id.statusFragmentLayout);
        statusFragmentLayout.setOnDragListener(new OutOfBoundsDragListener());
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(broadcastReceiverIncomingMsg, new IntentFilter("IncomingMsg"));
        determinedImageIV = (ImageView) view.findViewById(R.id.detectedIV);
        spawnGroup = view.findViewById(R.id.spawnGroup);
        mapTable = view.findViewById(R.id.mapTable);
        imgRobot = view.findViewById(R.id.imgRobot);

        txtRoboStatus = view.findViewById(R.id.txtRoboStatus);
        txtRoboDirection = view.findViewById(R.id.txtRoboDirection);
        txtRoboPosition = view.findViewById(R.id.txtRoboPosition);
        forwardBtn = view.findViewById(R.id.forwardButton);
        reverseBtn = view.findViewById(R.id.reverseButton);
        leftTurnBtn = view.findViewById(R.id.turnLeftButton);
        rightTurnBtn =  view.findViewById(R.id.turnRightButton);
        imageRecBtn = view.findViewById(R.id.btnImageRec);
        fastestCarBtn = view.findViewById(R.id.btnFastestCar);
        MainActivity.statusFragmentContext = getContext();
        if(MainActivity.hasBtConnectedDevice && MainActivity.globalBluetoothService!=null){
            this.txtRoboStatus.setText("Connected");
            MainActivity.serialChat = new StringBuilder(MainActivity.serialChat.append("This Device:"+READY+'\n'));
            byte[] bytes = READY.getBytes(Charset.defaultCharset());
            MainActivity.globalBluetoothService.write(bytes);
        }else{
            this.txtRoboStatus.setText("Not Connected");
        }
        // draws a 20x20 map for robot traversal when first rendered
        mapTable.getViewTreeObserver().addOnPreDrawListener(() -> {
            Log.d("DRAW", "Map Drawn");
            if (drawn < 1) {
                initMap(mapTable);

                drawn++;
            }
            return true;
        });


        //(FIX) Not sure what the above 2 lines are for

        resetButton = view.findViewById(R.id.btnReset);
        //resetButton.setOnClickListener((View.OnClickListener) this);

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                reset(false);
            }
        });
        imageRecBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( !bluetoothAdapter.isEnabled()|| !MainActivity.hasBtConnectedDevice || MainActivity.globalBluetoothService ==null){
                    Toast.makeText(getContext(), "Connection not establish", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(robotX == -1 || robotY == -1 || obstacles.size() < 1){
                    Toast.makeText(getContext(),"Arena is not ready",Toast.LENGTH_SHORT).show();
                    return;
                }
                sendMessageToRPI("START_IMAGE_TASK");
                setRoboStatus(statusMapping.get("search"));
            }
        });
        fastestCarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!bluetoothAdapter.isEnabled() || !MainActivity.hasBtConnectedDevice || MainActivity.globalBluetoothService ==null){
                    Toast.makeText(getContext(), "Connection not establish", Toast.LENGTH_SHORT).show();
                    return;
                }
                sendMessageToRPI("START_FASTEST_TASK");
            }
        });
        implementMovementListeners();
       return view;
    }

    private final BroadcastReceiver broadcastReceiverIncomingMsg = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Receiver Incoming message");
            String msg = intent.getStringExtra("receivingMsg");
            String [] msgParts = msg.split(",");
            switch(msgParts[0].toUpperCase()){
                case HANDSHAKE: setRoboStatus(statusMapping.get("ready")); break;
                case TARGET:
                        if(obstacles.containsKey(Integer.parseInt(msgParts[1]))){
                            updateDetectedImage(obstacles.get(Integer.parseInt(msgParts[1]))
                                    ,Integer.parseInt(msgParts[2]));
                            setRoboStatus(statusMapping.get("image"));
                        }
                        break;
                case ROBOT: setRobotXY(Integer.parseInt(msgParts[1]),Integer.parseInt(msgParts[2])
                        ,msgParts[3]);break;
                case FORWARD: setRoboStatus(statusMapping.get("f"));moveRobot(true);break;
                case REVERSE: setRoboStatus(statusMapping.get("r"));moveRobot(false);break;
                case LEFT_TURN: setRoboStatus(statusMapping.get("tl"));rotateRobot(imgRobot,-90);break;
                case RIGHT_TURN: setRoboStatus(statusMapping.get("tr"));rotateRobot(imgRobot,90);break;

            }
        }
    };

    private void initMap(TableLayout mapTable) {
        // set cell height and width
        btnH = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, getResources().getDisplayMetrics());
        btnW = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, getResources().getDisplayMetrics());

        // default background for cell
        btnBG = AppCompatResources.getDrawable(this.requireContext(), R.drawable.btn_background);

        // 20x20 map
        for (y = 19; y >= 0; y--) {
            TableRow row = new TableRow(this.getContext());
            row.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);

            for (x = 0; x < 20; x++) {
                ArenaButton btn = new ArenaButton(this.getContext(), x, y);
                btn.setId(View.generateViewId());
                btn.setPadding(1, 1, 1, 1);
                btn.setBackground(btnBG);
                btn.setLayoutParams(new TableRow.LayoutParams(btnW, btnH));
                //set it to white
                btn.setTextColor(Color.rgb(255, 255, 255));

                coord[x][y] = btn.getId();

                btn.setOnClickListener(new MapBtnClickListener(x, y, btn.getId()));
                btn.setOnDragListener(new BtnDragListener(x, y, btn.getId()));
                row.addView(btn);
            }
            mapTable.addView(row);
        }
    }
    private void implementMovementListeners(){
            forwardBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!bluetoothAdapter.isEnabled() || !MainActivity.hasBtConnectedDevice || MainActivity.globalBluetoothService ==null){
                        Toast.makeText(getContext(), "Connection not establish", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    moveRobot(true);
                    sendMessageToRPI(FORWARD);
//                    Toast.makeText(getContext(), "Forward", Toast.LENGTH_SHORT).show();
                }
            });
            reverseBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!bluetoothAdapter.isEnabled() || !MainActivity.hasBtConnectedDevice || MainActivity.globalBluetoothService ==null){
                        Toast.makeText(getContext(), "Connection not establish", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    moveRobot(false);
                    sendMessageToRPI(REVERSE);
//                    Toast.makeText(getContext(), "Reverse", Toast.LENGTH_SHORT).show();
                }
            });
            leftTurnBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!bluetoothAdapter.isEnabled() || !MainActivity.hasBtConnectedDevice || MainActivity.globalBluetoothService ==null){
                        Toast.makeText(getContext(), "Connection not establish", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    rotateRobot(imgRobot,-90);
                    sendMessageToRPI(LEFT_TURN);
//                    Toast.makeText(getContext(), "Turning left", Toast.LENGTH_SHORT).show();
                }
            });
            rightTurnBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!bluetoothAdapter.isEnabled() || !MainActivity.hasBtConnectedDevice || MainActivity.globalBluetoothService ==null){
                        Toast.makeText(getContext(), "Connection not establish", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    rotateRobot(imgRobot,90);
                    sendMessageToRPI(RIGHT_TURN);
//                    Toast.makeText(getContext(), "Turning right", Toast.LENGTH_SHORT).show();
                }
            });


    }

    public void emptyCellObsID(int obstacleID) {
        ObstacleInfo obstacleInfo = obstacles.get(obstacleID);
        assert obstacleInfo != null;

        View v = getView();
        assert v != null;

        ArenaButton btn = v.findViewById(obstacleInfo.btnID);
        obstacles.remove(obstacleID);
        btn.setText("");
        btn.obstacleID = -1;
        btn.setBackground(btnBG);
    }

    // returns specified cell to regular state
    public void emptyCell(ArenaButton btn) {
        //need to find out where this btn.obstacleId is initialize
        int obstacleID = btn.obstacleID;
        //remove obstacle from map
        obstacles.remove(obstacleID);
        //set text to empty
        btn.setText("");
        //-1 means not storing any obstacle in this arena button
        btn.obstacleID = -1;
        btn.setBackground(btnBG);
        //"Remove,id,x,y"
        removeObstacleToRPI(obstacleID);

    }

    private class MapBtnClickListener implements View.OnClickListener {
        int x, y, id;

        public MapBtnClickListener(int x, int y, int id) {
            this.x = x;
            this.y = y;
            this.id = id;
        }

        @Override
        public void onClick(View view) {
//             stops listener if not connected via bluetooth yet
            if (!bluetoothAdapter.isEnabled() || !MainActivity.hasBtConnectedDevice || MainActivity.globalBluetoothService==null) {
                Toast.makeText(getContext(),"Connection not established",Toast.LENGTH_LONG).show();
                return;
            }
            ArenaButton btn = view.findViewById(this.id);
            // do nothing on a btn that already contained the obstacle
            if (!btn.getText().equals("")) {
                //remove obstacle
//                emptyCell(btn);
                return;
            }

            // get checked radiobutton value
            String spawn = getSpawn();

            // adds robot onto map
            if (spawn.equals("Robot")) {
                spawnRobot(btn);
                sendSpawnRobot();
            }
            // adds obstacle otherwise
            if (spawn.equals("Obstacle")) {
                for (int obsID = 1; obsID <= 8; obsID++) {
                    // finds next obstacle id
                    if (!obstacles.containsKey(obsID)) {
                        queryObstacleDirection(obsID,this.id, this.x,this.y);
                        break;
                    }
                }
            }
        }
    }
    // drag listener within the map
    private class BtnDragListener implements View.OnDragListener {
        int x, y, id;

        public BtnDragListener(int x, int y, int id) {
            this.x = x;
            this.y = y;
            this.id = id;
        }

        @Override
        public boolean onDrag(View newCell, DragEvent e) {
            switch (e.getAction()) {
                case ACTION_DRAG_ENTERED:
                case ACTION_DRAG_EXITED:
                    return true;

                case ACTION_DROP:
                    try {
                        // get drag data
                        ClipData dragData = e.getClipData();
                        JSONObject json = new JSONObject(dragData.getItemAt(0).getText().toString());

                        ArenaButton originalBtn = (ArenaButton) e.getLocalState();

                        // stops if dropped cell is same as original cell
                        if (originalBtn.getId() == newCell.getId())
                            return true;

                        // removes original cell
                        emptyCell(originalBtn);
                        Thread.sleep(500);

                        // moves obstacle over to new cell
                        int obstacleID = json.getInt("obstacleID");
                        int dirSelected = json.getInt("dirSelected");
                        addObstacle(obstacleID, newCell.getId(), dirSelected);
                    } catch (JSONException | InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    return true;
            }
            return true;
        }
    }
    // for drag events to anything outside the map
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
                    emptyCell(originalBtn);
                    return true;
            }
            return true;
        }
    }

    @SuppressLint("DefaultLocale")
    private void queryObstacleDirection(int obstacleID, int btnID, int x, int y) {
        // 4 choices of directions
        final String[] directions = {"Top", "Left", "Bottom", "Right"};

        // direction default to Top
        final String[] dirSelected = {"Top"};

        // retrieves direction of image on obstacle through radiobuttons
        AlertDialog.Builder builder = new AlertDialog.Builder(this.requireContext());
        builder.setTitle("Choose direction of image"+"("+x+","+y+")");
        builder.setSingleChoiceItems(directions, 0, (dialogInterface, i) -> dirSelected[0] = directions[i]);

        // confirm to add obstacle
        builder.setPositiveButton("Confirm", (dialogInterface, i) -> {
            int dir = dirs.indexOf(dirSelected[0]);
            addObstacle(obstacleID, btnID, dir);
            dialogInterface.dismiss();
        });

        // exit process
        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());
        builder.show();
    }

    private void addObstacle(int obstacleID, int btnID, int dirSelected) {
        // prevent duplicate obstacles by id
        if (obstacles.containsKey(obstacleID))
            return;

        // direction default to north
        int borderID = R.drawable.top_border;
        char dirChar = 'N';

        // get drawable ID for image direction
        switch (dirSelected) {
            case EAST:
                borderID = R.drawable.right_border;
                dirChar = 'E';
                break;
            case SOUTH:
                borderID = R.drawable.bottom_border;
                dirChar = 'S';
                break;
            case WEST:
                borderID = R.drawable.left_border;
                dirChar = 'W';
                break;
        }

        // add obstacle id to cell
        ArenaButton btn = mapTable.findViewById(btnID);
        btn.setText(String.valueOf(obstacleID));
        btn.obstacleID = obstacleID;

        // keep track of which obstacle is already created
        // a map that stores id -> obstacle info
        ObstacleInfo obstacleInfo = new ObstacleInfo(obstacleID, btnID, btn.x, btn.y, dirSelected);
        obstacles.put(obstacleID, obstacleInfo);

        // draws direction of image onto cell
        Drawable border = AppCompatResources.getDrawable(this.requireContext(), borderID);
        btn.setBackground(border);

        // set drag listener to move cell item
        btn.setOnLongClickListener(view -> {
            try {
                // we can to keep track of:
                // original button id, obstacle id and direction of image
                JSONObject json = new JSONObject();
                json.put("obstacleID", obstacleID);
                json.put("dirSelected", dirSelected);

                // store data in ClipItem which stays until drag is stopped
                ClipData.Item item = new ClipData.Item(json.toString());
                ClipData dragData = new ClipData(
                        "dragObstacle",
                        new String[]{},
                        item
                );

                // shadow will just be the button itself
                View.DragShadowBuilder shadow = new MyDragShadowBuilder(btn);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    view.startDragAndDrop(dragData, shadow, btn, 0);
                else
                    view.startDrag(dragData, shadow, btn, 0);

            } catch (JSONException ex) {
                ex.printStackTrace();
            }

            return true;
        });
        addObstacleToRPI(obstacleID,btn.x,btn.y,dirChar);
    }

   private void sendSpawnRobot() {
        String text = "ROBOT," + robotX + "," + robotY + ",";
        switch(robotDirection){
            case NORTH: text +='N';break;
            case SOUTH: text+='S';break;
            case EAST: text+='E';break;
            case WEST: text+='W';break;
        }
        byte[] bytes = text.getBytes(Charset.defaultCharset());
       MainActivity.serialChat = new StringBuilder(MainActivity.serialChat.append("This Device:" + text + '\n'));
        MainActivity.globalBluetoothService.write(bytes);
    }


    // gets text value of radiobutton selected
    private String getSpawn() {
        int btnID = spawnGroup.getCheckedRadioButtonId();
        RadioButton btn = spawnGroup.findViewById(btnID);
        return btn.getText().toString();
    }

    // spawns robot image on button position
    private void spawnRobot(ArenaButton btn) {
        robotX = btn.x;
        robotY = btn.y;

        // Display current position of robot on the textview
        // (FIX) Create textview in layout xml
        // create method setRobotPosition and setRoboDirection in this java file to display position

        // gets button x and y coordinates
        int[] pt = new int[2];
        btn.getLocationInWindow(pt);

        // makes robot visible
        imgRobot.setVisibility(View.VISIBLE);
        // set robot drawing position to bottom left instead of top left
        imgRobot.setX(btn.getX());
        // 24 for status bar and 50 for placing it 2 buttons up
        imgRobot.setY(pt[1] - dpToPixels(24) - dpToPixels(25));

        //set robot position in textview (x,y)
        setRobotPosition(getPositionString());
        //set robot direction in textview N,S,E,W
        setRoboDirection(getDirectionString());

        imgRobot.setRotation(((robotDirection) * 90) % 360);
        // rotates 90 degrees clockwise on click
        imgRobot.setOnClickListener(robot -> {
            rotateRobot(robot, 90);
            sendSpawnRobot();
        });
    }


    // Clears all cells back to default state
    public void reset(boolean isPause)  {
        this.determinedImageIV.setImageDrawable(null);
        Set<Integer> keys = obstacles.keySet();

        for (int key : keys) {
            ObstacleInfo obstacleInfo = obstacles.get(key);
            assert obstacleInfo != null;
            int btnID = obstacleInfo.btnID;
            ArenaButton btn = mapTable.findViewById(btnID);

            btn.setText("");
            btn.setTextColor(getActivity().getResources().getColor(android.R.color.white,getActivity().getTheme()));
            btn.obstacleID = -1;
            btn.setBackground(btnBG);
            btn.setOnLongClickListener(null);
        }
        obstacles.clear();
        imgRobot.setVisibility(View.GONE);
        robotX = -1;
        robotY = -1;
        setRoboStatus("Not Connected");
        setRoboDirection("Direction");
        setRobotPosition("(x,y)");
        if(bluetoothAdapter.isEnabled() && MainActivity.hasBtConnectedDevice && MainActivity.globalBluetoothService!=null){
            sendMessageToRPI("RESET");
            setRoboStatus("Connected");
            if(!isPause){
                try{
                    Thread.sleep(500L);
                    sendMessageToRPI(READY);

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }


        // (FIX) Same as above -> create methods and textview then display position
    }


    private int dpToPixels(int dp) {
        return (int) (dp * MainActivity.statusFragmentContext.getResources().getDisplayMetrics().density);
    }


    public HashMap<Integer, ObstacleInfo> getObstacles() {
        return obstacles;
    }

    // to rotate the robot
    public void rotateRobot(View v, int rotation) {
        if(robotX==-1 || robotY==-1){
            return;
        }
        if (v == null)
            v = imgRobot;

        if (rotation > 0)
            robotDirection = (robotDirection + 1) % 4;

        else if (rotation < 0) {
            if (robotDirection == NORTH)
                robotDirection = WEST;
            else
                robotDirection--;
        }

        v.setPivotX(v.getWidth() / 2);
        v.setPivotY(v.getHeight() / 2);

        robotRotation = (robotRotation + rotation) % 360;
        v.setRotation(robotRotation);
        //set textview
        setRoboDirection(getDirectionString());

        //(FIX) Same as above -> create methods and textview then display position

        Log.d("Check Direction", String.valueOf(robotDirection));
    }

// need to check called from main act
    public void moveRobot(boolean forward) {
        if(robotY==-1 || robotX==-1){
//            Toast.makeText(getContext(),"Please place the robot on the map first",Toast.LENGTH_SHORT).show();
            return;
        }
        int multiplier;
        if(forward){
            multiplier = 1;
        }
        else{
            multiplier = -1;
        }
        switch (robotDirection) {
            case NORTH:
                if(forward && robotY < 18)
                {
                    robotY += 1;
                    imgRobot.setY(imgRobot.getY() - dpToPixels(25) * multiplier);
                }
                else if(!forward && robotY > 0){
                    robotY -= 1;
                    imgRobot.setY(imgRobot.getY() - dpToPixels(25) * multiplier);
                }
                break;
            case SOUTH:
                if(forward && robotY > 0){
                    robotY -= 1;
                    imgRobot.setY(imgRobot.getY() + dpToPixels(25) * multiplier);
                }
                else if(!forward && robotY < 18){
                    robotY += 1;
                    imgRobot.setY(imgRobot.getY() + dpToPixels(25) * multiplier);
                }
                break;
            case WEST:
                if(forward && robotX > 0){
                    robotX -= 1;
                    imgRobot.setX(imgRobot.getX() - dpToPixels(25) * multiplier);
                }
                else if(!forward && robotX < 18){
                    robotX += 1;
                    imgRobot.setX(imgRobot.getX() - dpToPixels(25) * multiplier);
                }
                break;
            case EAST:
                if(forward && robotX < 18){
                    robotX += 1;
                    imgRobot.setX(imgRobot.getX() + dpToPixels(25) * multiplier);
                }
                else if(!forward && robotX > 0){
                    robotX -= 1;
                    imgRobot.setX(imgRobot.getX() + dpToPixels(25) * multiplier);
                }
                break;

        }
        setRobotPosition(getPositionString());
    }

    // need to check called from main act
    public void setRobotXY(int x, int y,String dir) {
        x = x < 0 ? 0 : x;
        x = x > 19 ? 19 : x;
        y = y < 0 ? 0 : y;
        y = y > 19 ? 19:y;
        int btnID = coord[x][y];
        switch(dir.toLowerCase()){
            case "n":robotDirection = NORTH; robotRotation = 0;break;
            case "e":robotDirection = EAST;robotRotation = 90;break;
            case "s":robotDirection = SOUTH;robotRotation=180;break;
            case "w":robotDirection = WEST;robotRotation=270;
        }

        ArenaButton btn = mapTable.findViewById(btnID);
        spawnRobot(btn);
    }
    private void updateDetectedImage(ObstacleInfo obstacleInfo,int targetID){
        if(targetID < 11 || targetID > 40){
            return;
        }
        Log.d(TAG,"Updating obstacle id:" +  obstacleInfo.obstacleID + " to target id:" + targetID + " at coordinate "
        + "(" + obstacleInfo.x+ ","+obstacleInfo.y+")");
        ArenaButton btn = mapTable.findViewById(coord[obstacleInfo.x][obstacleInfo.y]);
        switch(obstacleInfo.dir){
            case NORTH:  btn.setBackground(AppCompatResources.getDrawable(
                    MainActivity.statusFragmentContext, R.drawable.top_border_detected));
            break;
            case EAST: btn.setBackground(AppCompatResources.getDrawable(
                    MainActivity.statusFragmentContext, R.drawable.right_border_detected));
            break;
            case WEST:
                    btn.setBackground(AppCompatResources.getDrawable(
                            MainActivity.statusFragmentContext, R.drawable.left_border_detected));
            break;
            case  SOUTH: btn.setBackground(AppCompatResources.getDrawable(
                    MainActivity.statusFragmentContext, R.drawable.bottom_border_detected));
            break;
        }
        btn.setText(String.valueOf(targetID));
        btn.setTextColor(getActivity().getResources().getColor(android.R.color.black,getActivity().getTheme()));
        this.determinedImageIV.setImageResource(Constant.imageMapping.get(targetID));
    }

    public void setRoboStatus(String status) {
        txtRoboStatus.setText(status);
    }

    public void setRoboDirection(String direction) {
        txtRoboDirection.setText(direction);
    }

    public void setRobotPosition(String position) {
        txtRoboPosition.setText(position);
    }

    private void addObstacleToRPI(int obstacleId,int x,int y,char dir){
        String text = "ADD," + obstacleId + "," + x + "," + y + "," + dir;
        MainActivity.serialChat = new StringBuilder(MainActivity.serialChat.append("This Device:" + text + '\n'));
        byte[] bytes = text.getBytes(Charset.defaultCharset());
        MainActivity.globalBluetoothService.write(bytes);
    }
    private void removeObstacleToRPI(int obstacleId){
        String text = "REMOVE," + obstacleId;
        MainActivity.serialChat = new StringBuilder(MainActivity.serialChat.append("This Device:" + text + '\n'));
        byte[] bytes = text.getBytes(Charset.defaultCharset());
        MainActivity.globalBluetoothService.write(bytes);
    }
    private void sendMessageToRPI(String text){
        byte[] bytes = text.getBytes(Charset.defaultCharset());
        MainActivity.serialChat = new StringBuilder(MainActivity.serialChat.append("This Device:" + text + '\n'));
        MainActivity.globalBluetoothService.write(bytes);
    }

    public String getDirectionString() {
        String[] dirArray = new String[]{"NORTH", "EAST", "SOUTH", "WEST"};
        return dirArray[robotDirection];
    }


    public String getPositionString() {
        return "(" + robotX + "," + robotY + ")";
    }


    public int getObstacleIDByCoord(int x, int y) {
        int btnID = coord[x][y];
        Set<Integer> keys = obstacles.keySet();

        for (int key : keys) {
            ObstacleInfo obstacleInfo = obstacles.get(key);
            assert obstacleInfo != null;

            if (obstacleInfo.btnID == btnID)
                return key;
        }

        return -1;
    }
}
