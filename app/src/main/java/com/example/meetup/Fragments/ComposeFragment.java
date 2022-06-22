package com.example.meetup.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.meetup.Models.Post;
import com.example.meetup.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ComposeFragment extends Fragment {
    private static final String TAG = "ComposeFragment";

    private EditText startupNameCompose;
    private EditText categoryCompose;
    // TODO get maps location private TextView location;
    private EditText captionCompose;
    private EditText descriptionCompose;
//    TODO upload logo image
//    private ImageView logo;
//    private File photoFile;
//    public String photoFileName = "photo.jpg";
//    TODO make it list of roles private TextView roles;

    private Button submitCompose;

    public ConstraintLayout constraintLayoutCompose;

    public ComposeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_compose, container, false);
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(@NonNull  View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        startupNameCompose = view.findViewById(R.id.startupNameCompose);
        categoryCompose = view.findViewById(R.id.categoryCompose);
        // TODO location = itemView.findViewById(R.id.location);
        captionCompose = view.findViewById(R.id.captionCompose);
        descriptionCompose = view.findViewById(R.id.descriptionCompose);
        // TODO list of roles = itemView.findViewById(R.id.roles);
        // TODO logo = view.findViewById(R.id.logoCompose);
        submitCompose = view.findViewById(R.id.submit);
        constraintLayoutCompose = view.findViewById(R.id.constraintLayoutCompose);

        submitCompose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String description = descriptionCompose.getText().toString();
                String caption = captionCompose.getText().toString();
                if (description.isEmpty()) {
                    Toast.makeText(getContext(), "description can't be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
//                TODO upload logo
//                if (photoFile == null || logoCompose.getDrawable() == null) {
//                    Toast.makeText(getContext(), "there is no image", Toast.LENGTH_SHORT).show();
//                    return;
//                }
                ParseUser currentUser = ParseUser.getCurrentUser();
                savePost(description, caption, currentUser);
            }
        });
    }

    // TODO set logo parameter
    private void savePost(String description, String caption, ParseUser currentUser) {
        Post post = new Post();
        post.setCaption(caption);
        post.setDescription(description);
        // post.setImage(new ParseFile(photoFile));
        post.setUser(currentUser);
        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "error while saving", e);
                    Toast.makeText(getContext(), "error while saving", Toast.LENGTH_SHORT).show();
                }
                Log.i(TAG, "post save was successful");
                descriptionCompose.setText("");
                // TODO set logo image
            }
        });
    }
}