package com.example.meetup.Adapters;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

import static java.lang.Math.sqrt;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.meetup.Models.Post;
import com.example.meetup.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;

import java.util.Date;
import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {
    private static final String TAG = "PostsAdapter";
    private Context context;
    private List<Post> posts;

    public PostsAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView startupName;
        private TextView founderUsername;
        private TextView category;
        private TextView location;
        private TextView caption;
        private TextView description;
        private ImageView logo;
        private TextView roles;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            startupName = itemView.findViewById(R.id.startupName);
            founderUsername = itemView.findViewById(R.id.founderUsername);
            category = itemView.findViewById(R.id.category);
            location = itemView.findViewById(R.id.location);
            caption = itemView.findViewById(R.id.caption);
            description = itemView.findViewById(R.id.description);
            logo = itemView.findViewById(R.id.logo);
            roles = itemView.findViewById(R.id.roles);
        }

        public void bind(Post post) {
            startupName.setText(post.getStartupName());
            founderUsername.setText(post.getUser().getUsername());
            category.setText(post.getCategory());
            caption.setText(post.getCaption());
            description.setText(post.getDescription());
            roles.setText(post.getRoles());
            ParseFile image = post.getImage();
            if (image != null) {
                Log.i(TAG, "image not null");
                Glide.with(context).load(image.getUrl()).into(logo);
            }
            FusedLocationProviderClient locationClient = getFusedLocationProviderClient(caption.getContext());
            if (ActivityCompat.checkSelfPermission(caption.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(caption.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO request permissions
            }
            locationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location currentLocation) {
                            if (currentLocation != null) {
                                ParseGeoPoint parseGeoPoint = new ParseGeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
                                int dist = (int) parseGeoPoint.distanceInKilometersTo(post.getGeoPoint());
                                location.setText(String.valueOf(dist) + " km away");
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
    }

    public void clear() {
        posts.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Post> list) {
        posts.addAll(list);
        notifyDataSetChanged();
    }
}