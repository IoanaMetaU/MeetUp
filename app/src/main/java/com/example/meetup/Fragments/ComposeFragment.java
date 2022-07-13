package com.example.meetup.Fragments;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
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

import com.example.meetup.Models.MapMarker;
import com.example.meetup.Models.Post;
import com.example.meetup.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
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
    private EditText captionCompose;
    private EditText descriptionCompose;
    private Button uploadImageCompose;
    private File photoFile;
    private ImageView logoCompose;
    private EditText rolesCompose;
    private MapMarker locationCompose;

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
        captionCompose = view.findViewById(R.id.captionCompose);
        descriptionCompose = view.findViewById(R.id.descriptionCompose);
        rolesCompose = view.findViewById(R.id.rolesCompose);
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
                String roles = rolesCompose.getText().toString();
                if (description.isEmpty()) {
                    Toast.makeText(getContext(), "description can't be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (photoFile == null) {
                    Toast.makeText(getContext(), "there is no image", Toast.LENGTH_SHORT).show();
                    return;
                }
                ParseUser currentUser = ParseUser.getCurrentUser();
                getLocation(startupName, caption, description, category, roles, currentUser);
            }
        });
    }

    public void getLocation(String startupName, String caption, String description, String category, String roles, ParseUser currentUser) {
        MapMarker mapMarker = new MapMarker();
        mapMarker.setName(startupName);

        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(requireContext());
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
            // return;
        }
        locationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            ParseGeoPoint parseGeoPoint = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
                            mapMarker.setLocation(parseGeoPoint);
                            mapMarker.saveInBackground(e -> {
                                if (e != null) {
                                    Log.e(TAG, "Error while saving new map marker!", e);
                                    Toast.makeText(getActivity(), "Error while saving!", Toast.LENGTH_SHORT).show();
                                }
                                Log.i(TAG, "MapMarker save was successful!");
                                locationCompose = mapMarker ;
                                savePost(startupName, description, caption, category, roles, currentUser, photoFile, locationCompose, locationCompose.getLocation());
                            });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error trying to get last GPS location");
                        e.printStackTrace();
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

    public void createMarker(String title, LatLng latLng) {

    }

    private void savePost(String startupName, String description, String caption, String category, String roles, ParseUser currentUser, File photoFile, MapMarker mapMarker, ParseGeoPoint geoPoint) {
        Post post = new Post();
        post.setStartupName(startupName);
        post.setCaption(caption);
        post.setDescription(description);
        post.setCategory(category);
        post.setUser(currentUser);
        post.setRoles(roles);
        post.setMapMarker(mapMarker);
        post.setGeoPoint(geoPoint);
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