package com.example.test.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.test.HomeActivity;
import com.example.test.MainActivity;
import com.example.test.R;
import com.example.test.StrangerChatActivity;
import com.example.test.databinding.FragmentStatusBinding;


public class StatusFragment extends Fragment {

    FragmentStatusBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding=FragmentStatusBinding.inflate(inflater,container,false);
        binding.begin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i= new Intent(getContext(), StrangerChatActivity.class);
                startActivity(i);
            }
        });
        return binding.getRoot();

    }

    public StatusFragment() {
        // Required empty public constructor
    }
}