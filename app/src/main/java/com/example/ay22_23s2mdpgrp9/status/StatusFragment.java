package com.example.ay22_23s2mdpgrp9.status;

import static android.view.DragEvent.ACTION_DRAG_ENTERED;
import static android.view.DragEvent.ACTION_DRAG_EXITED;
import static android.view.DragEvent.ACTION_DROP;
import static com.example.ay22_23s2mdpgrp9.Constants.A_ROBOT_POS;
import static com.example.ay22_23s2mdpgrp9.Constants.EAST;
import static com.example.ay22_23s2mdpgrp9.Constants.NORTH;
import static com.example.ay22_23s2mdpgrp9.Constants.SOUTH;
import static com.example.ay22_23s2mdpgrp9.Constants.WEST;
//import static com.example.ay22_23s2mdpgrp9.bluetooth.BluetoothService.STATE_CONNECTED;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
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
import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.ay22_23s2mdpgrp9.Arena.ArenaButton;
import com.example.ay22_23s2mdpgrp9.Arena.MyDragShadowBuilder;
import com.example.ay22_23s2mdpgrp9.Arena.ObstacleInfo;

import java.nio.charset.StandardCharsets;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


import com.example.ay22_23s2mdpgrp9.R;
import com.example.ay22_23s2mdpgrp9.bluetooth.Packet;
import com.example.ay22_23s2mdpgrp9.constant.Constant;

public class StatusFragment extends Fragment {

    int x, y, btnH, btnW, drawn = 0;
    int robotRotation = 0;

    TextView txtRoboStatus, txtRoboDirection, txtRoboPosition;

    //ObstacleList
    ArrayList <JSONObject> ObstacleList = new ArrayList<JSONObject>();
    // robot defaults to facing north
    int robotDirection = NORTH;

    //Status, Direction and position updates for leftcol fragment
    protected int robotX, robotY;
    //public LeftColFragment fragmentLeftCol;

    // keeps buttonIDs by xy coordinates
    int[][] coord = new int[20][20];

    // obstacleID: obstacleInfo obj
    HashMap<Integer, ObstacleInfo> obstacles = new HashMap<>();
    ArrayList<String> dirs = new ArrayList<>(Arrays.asList("Top", "Right", "Bottom", "Left"));
    Drawable btnBG = null;

    TableLayout mapTable;
    ImageView imgRobot;
    RadioGroup spawnGroup;
    private ImageView determinedImageIV;
    private ImageButton resetbtn;

    Button resetButton;

    public StatusFragment() {
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       View view = inflater.inflate(R.layout.map_page, container, false);
        //determinedImageIV = (ImageView) view.findViewById(R.id.determinedPhotoIv);
        //determinedImageIV.setImageResource(Constant.imageMapping.get(40));

        //(FIX) Not sure what the above 2 lines are for

        resetButton = view.findViewById(R.id.btnReset);
        //resetButton.setOnClickListener((View.OnClickListener) this);

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                reset();
            }
        });
       return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        spawnGroup = view.findViewById(R.id.spawnGroup);
        mapTable = view.findViewById(R.id.mapTable);
        imgRobot = view.findViewById(R.id.imgRobot);

        txtRoboStatus = view.findViewById(R.id.txtRoboStatus);
        txtRoboDirection = view.findViewById(R.id.txtRoboDirection);
        txtRoboPosition = view.findViewById(R.id.txtRoboPosition);

        // draws a 20x20 map for robot traversal when first rendered
        mapTable.getViewTreeObserver().addOnPreDrawListener(() -> {
            Log.d("DRAW", "Map Drawn");
            if (drawn < 1) {
                initMap(mapTable);

                drawn++;
            }
            return true;
        });
    }

    private void initMap(TableLayout mapTable) {
        // set cell height and width
        btnH = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 22, getResources().getDisplayMetrics());
        btnW = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 22, getResources().getDisplayMetrics());

        // default background for cell
        btnBG = AppCompatResources.getDrawable(this.requireContext(), R.drawable.btn_background);

        // 20x20 map
        for (y = 19; y >= 0; y--) {
            TableRow row = new TableRow(this.getContext());
            //row.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);

            for (x = 0; x < 20; x++) {
                ArenaButton btn = new ArenaButton(this.getContext(), x, y);
                btn.setId(View.generateViewId());
                btn.setPadding(1, 1, 1, 1);
                btn.setBackground(btnBG);
                btn.setLayoutParams(new TableRow.LayoutParams(btnW, btnH));
                btn.setTextColor(Color.rgb(255, 255, 255));

                coord[x][y] = btn.getId();

                btn.setOnClickListener(new MapBtnClickListener(x, y, btn.getId()));
                btn.setOnDragListener(new BtnDragListener(x, y, btn.getId()));
                row.addView(btn);
            }
            mapTable.addView(row);
        }
    }

    public void emptyCellObsID(int obstacleID) {
        ObstacleInfo obstacleInfo = obstacles.get(obstacleID);
        assert obstacleInfo != null;

        View v = getView();
        assert v != null;

        ArenaButton btn = v.findViewById(obstacleInfo.btnID);
        sendRemoveObstacle(obstacleInfo);
        obstacles.remove(obstacleID);
        btn.setText("");
        btn.obstacleID = -1;
        btn.setBackground(btnBG);
    }

    // returns specified cell to regular state
    public void emptyCell(ArenaButton btn) {
        int obstacleID = btn.obstacleID;

        // updates robot on obstacle removal
        ObstacleInfo obstacleInfo = obstacles.get(obstacleID);
        sendRemoveObstacle(obstacleInfo);

        obstacles.remove(obstacleID);
        btn.setText("");
        btn.obstacleID = -1;
        btn.setBackground(btnBG);
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
            // stops listener if not connected via bluetooth yet
//            if (bluetoothService.state != STATE_CONNECTED) {
//                Toast.makeText(view.getContext(), btAlert, Toast.LENGTH_SHORT).show();
//                return;
//            }

            ArenaButton btn = view.findViewById(this.id);
            // removes item if it's on cell
            if (!btn.getText().equals("")) {
                emptyCell(btn);
                return;
            }

            // get checked radiobutton value
            String spawn = getSpawn();

            // adds robot onto map
            if (spawn.equals("Robot")) {
                spawnRobot(btn);
                //sendSpawnRobot();
            }
            // (FIX) sendSpawnRobot() sends position of robot via bluetooth,
            // can uncomment once method is completed below

            // adds obstacle otherwise
            if (spawn.equals("Obstacle")) {
                for (int obsID = 1; obsID <= 400; obsID++) {
                    // finds next obstacle id
                    if (!obstacles.containsKey(obsID)) {
                        queryObstacleDirection(obsID,this.id, this.x,this.y);
                        break;
                    }
                }
            }
        }
    }

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

                        // moves obstacle over to new cell
                        int obstacleID = json.getInt("obstacleID");
                        int dirSelected = json.getInt("dirSelected");
                        addObstacle(obstacleID, newCell.getId(), dirSelected);
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }
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

        // get drawable ID for image direction
        switch (dirSelected) {
            case EAST:
                borderID = R.drawable.right_border;
                break;
            case SOUTH:
                borderID = R.drawable.bottom_border;
                break;
            case WEST:
                borderID = R.drawable.left_border;
                break;
        }

        // add obstacle id to cell
        ArenaButton btn = mapTable.findViewById(btnID);
        btn.setText(String.valueOf(obstacleID));
        btn.obstacleID = obstacleID;

        // keeps track of obstacle in memory
        ObstacleInfo obstacleInfo = new ObstacleInfo(obstacleID, btnID, btn.x, btn.y, dirSelected);
       obstacles.put(obstacleID, obstacleInfo);

        // sends addition of obstacle over to robot
        sendAddObstacleData(obstacleInfo);

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
                    // deprecated method, for my old ass tablet
                else
                    view.startDrag(dragData, shadow, btn, 0);

            } catch (JSONException ex) {
                ex.printStackTrace();
            }

            return true;
        });
    }

//    private void sendAddObstacleData(ObstacleInfo obstacleInfo) {
//        if (bluetoothService.state == STATE_CONNECTED) {
//            bluetoothService.write(obstacleInfo.toAddBytes());
//            ObstacleList.add(obstacleInfo.obToString());
//        }
//        else
//            Toast.makeText(this.getContext(), btAlert, Toast.LENGTH_LONG).show();
//    }

    private void sendAddObstacleData(ObstacleInfo obstacleInfo) {
            ObstacleList.add(obstacleInfo.obToString());
    }
    // (FIX) not sure how to check for bluetooth connection, but once that is sorted out can try to
    // change the code to something similar above


//    private void sendRemoveObstacle(ObstacleInfo obstacleInfo) {
//        if (bluetoothService.state == STATE_CONNECTED) {
//            bluetoothService.write(obstacleInfo.toRemoveBytes());
//            ObstacleList = removeJson(ObstacleList,obstacleInfo.obToString());
//
//        }
//        else
//            Toast.makeText(this.getContext(), btAlert, Toast.LENGTH_LONG).show();
//    }

    private void sendRemoveObstacle(ObstacleInfo obstacleInfo) {
            ObstacleList = removeJson(ObstacleList,obstacleInfo.obToString());
    }
    // (FIX) same issue as the method above

    private ArrayList<JSONObject> removeJson(ArrayList<JSONObject> list,JSONObject obj)
    {
        for(int i = 0;i <list.size();i++)
        {
            try {
                if(list.get(i).getInt("obstacle_id") == obj.getInt("obstacle_id"))
                {
                    list.remove(i);
                    break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    // (FIX) Sends robot position via bluetooth, check for bluetooth connection
   private void sendSpawnRobot() {
//        if (bluetoothService.state == STATE_CONNECTED) {
           Packet packet = new Packet(A_ROBOT_POS);
           packet.setX(robotX);
           packet.setY(robotY);
           packet.setDirection(robotDirection);

//            bluetoothService.write(packet.getJSONBytes());
//        } else {
//            Toast.makeText(this.getContext(), btAlert, Toast.LENGTH_LONG).show();
//        }
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

//        fragmentLeftCol.setRobotPosition(getPositionString());
//        fragmentLeftCol.setRoboDirection(getDirectionString());

        // gets button x and y coordinates
        int[] pt = new int[2];
        btn.getLocationInWindow(pt);

        // makes robot visible
        imgRobot.setVisibility(View.VISIBLE);
        // set robot drawing position to bottom left instead of top left
        imgRobot.setX(btn.getX());
        // 24 for status bar and 50 for placing it 2 buttons up
        imgRobot.setY(pt[1] - dpToPixels(21) - dpToPixels(22));

        //String pos = "(" + String.valueOf(robotX) + "," + String.valueOf(robotY) + ")";
        setRobotPosition(getPositionString());
        setRoboDirection(getDirectionString());

        imgRobot.setRotation(((robotDirection) * 90) % 360);
        // rotates 90 degrees clockwise on click
        imgRobot.setOnClickListener(robot -> {
            rotateRobot(robot, 90);
            sendSpawnRobot();

            // (FIX) Uncomment when sendSpawnRobot() method is completed
        });
    }


    // Clears all cells back to default state
    public void reset() {
        Set<Integer> keys = obstacles.keySet();

        for (int key : keys) {
            ObstacleInfo obstacleInfo = obstacles.get(key);
            assert obstacleInfo != null;
            int btnID = obstacleInfo.btnID;
            ArenaButton btn = mapTable.findViewById(btnID);

            btn.setText("");
            btn.obstacleID = -1;
            btn.setBackground(btnBG);
            btn.setOnLongClickListener(null);

            // updates robot on obstacle removal
            sendRemoveObstacle(obstacleInfo);
        }

        obstacles.clear();
        imgRobot.setVisibility(View.GONE);

        setRoboStatus("Status");
        setRoboDirection("Direction");
        setRobotPosition("(x,y)");

        // (FIX) Same as above -> create methods and textview then display position
    }


    private int dpToPixels(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }


    public HashMap<Integer, ObstacleInfo> getObstacles() {
        return obstacles;
    }

    public void rotateRobot(View v, int rotation) {
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
        setRoboDirection(getDirectionString());

        //fragmentLeftCol.setRoboDirection(getDirectionString());

        //(FIX) Same as above -> create methods and textview then display position

        Log.d("Check Direction", String.valueOf(robotDirection));
    }


    public void moveRobot(boolean forward) {
        int multiplier = forward ? 1 : -1;
        switch (robotDirection) {
            case NORTH:
                imgRobot.setY(imgRobot.getY() - dpToPixels(22) * multiplier);
                robotY += (forward) ? 1 : -1;
                break;
            case SOUTH:
                imgRobot.setY(imgRobot.getY() + dpToPixels(22) * multiplier);
                robotY += (forward) ? -1 : 1;
                break;
            case WEST:
                imgRobot.setX(imgRobot.getX() - dpToPixels(22) * multiplier);
                robotX += (forward) ? -1 : 1;
                break;
            case EAST:
                imgRobot.setX(imgRobot.getX() + dpToPixels(22) * multiplier);
                robotX += (forward) ? 1 : -1;
                break;

        }
    }


    public void setRobotXY(int x, int y) {
        robotX = x;
        robotY = y;
        int btnID = coord[x][y];

        View v = getView();
        assert v != null;

        ArenaButton btn = v.findViewById(btnID);
        spawnRobot(btn);
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


//    public void setBluetoothService(BluetoothService bluetoothService) {
//        this.bluetoothService = bluetoothService;
//    }

    //(FIX) Not sure if needed


    public void setSpawnGroup(RadioGroup spawnGroup) {
        this.spawnGroup = spawnGroup;
    }


//    public void setLeftColFragment(LeftColFragment fragmentLeftCol) {
//        this.fragmentLeftCol = fragmentLeftCol;
//    }

    // (FIX) Don't think we need this since we only have 1 fragment for the map

    public void setRobotDirection(int direction) {
        this.robotDirection = direction;
    }
    public ArrayList<JSONObject> getObstacleList()
    {
        return ObstacleList;
    }

    public RadioGroup getSpawnGroup() {
        return spawnGroup;
    }







}
