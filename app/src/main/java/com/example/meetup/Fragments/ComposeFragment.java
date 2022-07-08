package com.example.meetup.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.meetup.Models.Post;
import com.example.meetup.R;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ComposeFragment extends Fragment {
    private static final String TAG = "ComposeFragment";
    private static final int GET_FROM_GALLERY = 3;

    private EditText startupNameCompose;
    private EditText categoryCompose;
    // TODO get maps location private TextView location;
    private EditText captionCompose;
    private EditText descriptionCompose;
    private Button uploadImageCompose;
    private File photoFile;
    private ImageView logoCompose;
    private EditText rolesCompose;

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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        startupNameCompose = view.findViewById(R.id.startupNameCompose);
        categoryCompose = view.findViewById(R.id.categoryCompose);
        // TODO location = itemView.findViewById(R.id.location);
        captionCompose = view.findViewById(R.id.captionCompose);
        descriptionCompose = view.findViewById(R.id.descriptionCompose);
        // TODO list of roles = itemView.findViewById(R.id.roles);
        uploadImageCompose = view.findViewById(R.id.uploadImageCompose);
        submitCompose = view.findViewById(R.id.submit);
        constraintLayoutCompose = view.findViewById(R.id.constraintLayoutCompose);
        uploadImageCompose = view.findViewById(R.id.uploadImageCompose);
        logoCompose = view.findViewById(R.id.logoCompose);
        rolesCompose = view.findViewById(R.id.rolesCompose);

        uploadImageCompose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);

            }
        });
        submitCompose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String description = descriptionCompose.getText().toString();
                String caption = captionCompose.getText().toString();
                String category = categoryCompose.getText().toString();
                String startupName = startupNameCompose.getText().toString();
                if (description.isEmpty()) {
                    Toast.makeText(getContext(), "description can't be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (photoFile == null) {
                    Toast.makeText(getContext(), "there is no image", Toast.LENGTH_SHORT).show();
                    return;
                }
                ParseUser currentUser = ParseUser.getCurrentUser();
                savePost(startupName, description, caption, category, currentUser, photoFile);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Detects request codes
        if (requestCode == GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            Bitmap pictureBitmap = null;
            try {
                pictureBitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), selectedImage);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            logoCompose.setImageBitmap(pictureBitmap);
            photoFile = bitmapToFile(getContext(), pictureBitmap, "image.jpeg");
        }
    }

    public static File bitmapToFile(Context context, Bitmap bitmap, String fileNameToSave) { // File name like "image.png"
        //create a file to write bitmap data
        File file = null;
        try {
            // use cache to store image
            file = new File(context.getCacheDir(), fileNameToSave);

            file.createNewFile();

            //Convert bitmap to byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos); // YOU can also save it in JPEG
            byte[] bitmapdata = bos.toByteArray();

            //write the bytes in file
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return file;
        }
    }

    private void savePost(String startupName, String description, String caption, String category, ParseUser currentUser, File photoFile) {
        Post post = new Post();
        post.setStartupName(startupName);
        post.setCaption(caption);
        post.setDescription(description);
        post.setCategory(category);
        post.setUser(currentUser);
        ParseFile image = new ParseFile(photoFile);
        image.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "error while saving image", e);
                    Toast.makeText(getContext(), "error while saving image", Toast.LENGTH_SHORT).show();
                }
                Log.i(TAG, "image saved");
                post.setImage(image);

                // save post after saving image has finished
                post.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "error while saving", e);
                            Toast.makeText(getContext(), "error while saving", Toast.LENGTH_SHORT).show();
                        }
                        Log.i(TAG, "post save was successful");
                        startupNameCompose.setText("");
                        categoryCompose.setText("");
                        captionCompose.setText("");
                        descriptionCompose.setText("");
                        rolesCompose.setText("");
                        logoCompose.setImageResource(0);
                    }
                });
            }
        });


    }
}