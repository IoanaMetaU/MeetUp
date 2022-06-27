package com.example.meetup.Fragments;

import static com.example.meetup.Models.Post.KEY_STARTUP_NAME;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.meetup.Adapters.PostsAdapter;
import com.example.meetup.EndlessRecyclerViewScrollListener;
import com.example.meetup.Models.Post;
import com.example.meetup.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private static final String TAG = "SearchFragment";

    private RecyclerView posts;

    private PostsAdapter adapter;
    private List<Post> allPosts;

    private SearchView searchName;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        searchName = view.findViewById(R.id.searchName);

        posts = view.findViewById(R.id.posts);
        allPosts = new ArrayList<>();
        adapter = new PostsAdapter(getContext(), allPosts);
        posts.setAdapter(adapter);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        posts.setLayoutManager(llm);

        searchName.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String searchNameQuery = searchName.getQuery().toString();
                Log.i(TAG, searchNameQuery);
                queryPosts(searchNameQuery);
                return false;
            }
        });
    }

    private void queryPosts(String searchName) {
        // specify which class to query
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.whereEqualTo(Post.KEY_STARTUP_NAME, searchName);
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "issue with getting posts");
                }
                for (Post post: posts) {
                    Log.i(TAG, "Post " + post.getDescription() + " username " + post.getUser().getUsername());
                }

                // save received posts to list and notify adapter of new data
                allPosts.addAll(posts);
                adapter.notifyDataSetChanged();
            }
        });
    }

}