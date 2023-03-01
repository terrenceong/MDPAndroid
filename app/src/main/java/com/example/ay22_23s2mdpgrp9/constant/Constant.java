package com.example.ay22_23s2mdpgrp9.constant;

import com.example.ay22_23s2mdpgrp9.R;

import java.util.HashMap;
import java.util.Map;

public class Constant {
    public static final Map<Integer,Integer> imageMapping = new HashMap<Integer,Integer>() {{
        put(0,R.drawable.image_id_0);
        put(11,R.drawable.image_id_11);
        put(12,R.drawable.image_id_12);
        put(13,R.drawable.image_id_13);
        put(14,R.drawable.image_id_14);
        put(15,R.drawable.image_id_15);
        put(16,R.drawable.image_id_16);
        put(17,R.drawable.image_id_17);
        put(18,R.drawable.image_id_18);
        put(19,R.drawable.image_id_19);
        put(20,R.drawable.image_id_20);
        put(21,R.drawable.image_id_21);
        put(22,R.drawable.image_id_22);
        put(23,R.drawable.image_id_23);
        put(24,R.drawable.image_id_24);
        put(25,R.drawable.image_id_25);
        put(26,R.drawable.image_id_26);
        put(27,R.drawable.image_id_27);
        put(28,R.drawable.image_id_28);
        put(29,R.drawable.image_id_29);
        put(30,R.drawable.image_id_30);
        put(31,R.drawable.image_id_31);
        put(32,R.drawable.image_id_32);
        put(33,R.drawable.image_id_33);
        put(34,R.drawable.image_id_34);
        put(35,R.drawable.image_id_35);
        put(36,R.drawable.image_id_36);
        put(37,R.drawable.image_id_37);
        put(38,R.drawable.image_id_38);
        put(39,R.drawable.image_id_39);
        put(40,R.drawable.image_id_40);



    }};
//    public static final Map<Integer,Integer> idMapping = new HashMap<Integer,Integer>() {{
//        put(11,R.drawable.id_11);
//        put(12,R.drawable.id_12);
//        put(13,R.drawable.id_13);
//        put(14,R.drawable.id_14);
//        put(15,R.drawable.id_15);
//        put(16,R.drawable.id_16);
//        put(17,R.drawable.id_17);
//        put(18,R.drawable.id_18);
//        put(19,R.drawable.id_19);
//        put(20,R.drawable.id_20);
//        put(21,R.drawable.id_21);
//        put(22,R.drawable.id_22);
//        put(23,R.drawable.id_23);
//        put(24,R.drawable.id_24);
//        put(25,R.drawable.id_25);
//        put(26,R.drawable.id_26);
//        put(27,R.drawable.id_27);
//        put(28,R.drawable.id_28);
//        put(29,R.drawable.id_29);
//        put(30,R.drawable.id_30);
//        put(31,R.drawable.id_31);
//        put(32,R.drawable.id_32);
//        put(33,R.drawable.id_33);
//        put(34,R.drawable.id_34);
//        put(35,R.drawable.id_35);
//        put(36,R.drawable.id_36);
//        put(37,R.drawable.id_37);
//        put(38,R.drawable.id_38);
//        put(39,R.drawable.id_39);
//        put(40,R.drawable.id_40);
//
//
//
//    }};
    public static final int NORTH = 0;
    public static final int EAST = 1;
    public static final int SOUTH = 2;
    public static final int WEST = 3;

    public static final String ROBOT = "ROBOT";

    public static final String TARGET = "TARGET";

    public static final String READY="READYA2RPI";

    public static final String HANDSHAKE = "READYRPI2A";

    public static final String FORWARD = "0";

    public static final String REVERSE = "1";

    public static final String TURN_LEFT_GEAR_FORWARD = "2";

    public static final String TURN_LEFT_GEAR_BACKWARD = "3";

    public static final String TURN_RIGHT_GEAR_FORWARD = "4";

    public static final String TURN_RIGHT_GEAR_BACKWARD = "5";

    public static final String IMAGE_SCAN = "IMAGE_SCAN";

    public static final Map<String,String> statusMapping = new HashMap<String,String>(){
        {
            put("ready","ready to start");
            put("search","looking for target");
            put("image","target detected");
            put("f","moving forward");
            put("trgf","right turn & gear forward");
            put("trgb","right turn & gear backward");
            put("tlgf","left turn & gear forward");
            put("tlgb","left turn & gear backward");
            put("r","reversing");
        }};
}
