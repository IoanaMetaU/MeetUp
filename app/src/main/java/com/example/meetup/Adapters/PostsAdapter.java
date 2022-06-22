package com.example.meetup.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meetup.Models.Post;
import com.example.meetup.R;
import com.parse.ParseFile;

import java.util.Date;
import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {
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
        // TODO get maps location private TextView location;
        private TextView caption;
        private TextView description;
        // TODO make it list of roles private TextView roles;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            startupName = itemView.findViewById(R.id.startupName);
            founderUsername = itemView.findViewById(R.id.founderUsername);
            category = itemView.findViewById(R.id.category);
            // TODO location = itemView.findViewById(R.id.location);
            caption = itemView.findViewById(R.id.caption);
            description = itemView.findViewById(R.id.description);
            // TODO list of roles = itemView.findViewById(R.id.roles);
        }

        public void bind(Post post) {
            startupName.setText(post.getStartupName());
            founderUsername.setText(post.getUser().getUsername());
            category.setText(post.getCategory());
            // TODO location.setText(post.getLocation());
            caption.setText(post.getCaption());
            description.setText(post.getDescription());
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