package com.example.ay22_23s2mdpgrp9.status;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.ay22_23s2mdpgrp9.R;
import com.example.ay22_23s2mdpgrp9.constant.Constant;

public class StatusFragment extends Fragment {
    private ImageView determinedImageIV;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       View view = inflater.inflate(R.layout.fragment_status, container, false);
        determinedImageIV = (ImageView) view.findViewById(R.id.determinedPhotoIv);
        determinedImageIV.setImageResource(Constant.idMapping.get(30));
       return view;
    }
}