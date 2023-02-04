package com.example.ay22_23s2mdpgrp9.Arena;

import static com.example.ay22_23s2mdpgrp9.Constants.A_ADD_OBSTACLE;
import static com.example.ay22_23s2mdpgrp9.Constants.A_REM_OBSTACLE;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class ObstacleInfo {
    public int x, y, dir, btnID, obstacleID;

    public ObstacleInfo(int obstacleID, int btnID, int x, int y, int dirSelected) {
        this.obstacleID = obstacleID;
        this.btnID = btnID;
        this.x = x;
        this.y = y;
        this.dir = dirSelected;
    }

    public JSONObject obToString(){
        try {
            JSONObject json = new JSONObject();
            json.put("obstacle_id", this.obstacleID);
            json.put("x", this.x);
            json.put("y", this.y);
            json.put("direction", this.dir);
            return json;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

}
