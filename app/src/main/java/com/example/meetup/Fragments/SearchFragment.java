package com.example.meetup.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import com.example.meetup.Adapters.PostsAdapter;
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

    private AutoCompleteTextView searchName;
    private EditText searchCategory;
    private EditText searchKeyWord;
    private EditText searchRole;
    private Button searchFind;

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
        searchCategory = view.findViewById(R.id.searchCategory);
        searchKeyWord = view.findViewById(R.id.searchKeyWord);
        searchRole = view.findViewById(R.id.searchRole);
        searchFind = view.findViewById(R.id.searchFind);


        String[] startupNames = getColumnArray(Post.KEY_STARTUP_NAME).toArray(new String[0]);
        Log.i(TAG, startupNames.toString());
        String[] ss = {"aa", "bb"};
        ArrayAdapter<String> searchNameAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, ss);
        searchName.setAdapter(searchNameAdapter);
        searchName.setThreshold(1);

        posts = view.findViewById(R.id.posts);
        allPosts = new ArrayList<>();
        adapter = new PostsAdapter(getContext(), allPosts);
        posts.setAdapter(adapter);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        posts.setLayoutManager(llm);

        searchFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchNameQuery = searchName.getText().toString();
                String searchCategoryQuery = searchCategory.getText().toString();
                String searchKeyWordQuery = searchKeyWord.getText().toString();
                String searchRoleQuery = searchRole.getText().toString();
                queryPosts(searchNameQuery, searchCategoryQuery, searchKeyWordQuery, searchRoleQuery);

                searchName.setText("");
                searchCategory.setText("");
                searchKeyWord.setText("");
                searchRole.setText("");
            }
        });
    }

    private void queryPosts(String searchName, String searchCategory, String searchKeyWord, String searchRole) {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        // search for key work both in short caption and description
        // if the key word appears in any of them, add to query result
        if (searchKeyWord != null && searchKeyWord.length() > 0) {
            //query.whereContains(Post.KEY_DESCRIPTION, searchKeyWord);
            ParseQuery<Post> queryCaption = ParseQuery.getQuery(Post.class);
            queryCaption.whereContains(Post.KEY_CAPTION, searchKeyWord);
            ParseQuery<Post> queryDescription = ParseQuery.getQuery(Post.class);
            queryDescription.whereContains(Post.KEY_DESCRIPTION, searchKeyWord);

            List<ParseQuery<Post>> queries = new ArrayList<ParseQuery<Post>>();
            queries.add(queryCaption);
            queries.add(queryDescription);

             query = ParseQuery.or(queries);
             query.include(Post.KEY_USER);
        }
        if (searchName != null && searchName.length() > 0)
            query.whereEqualTo(Post.KEY_STARTUP_NAME, searchName);
        if (searchCategory != null && searchCategory.length() > 0)
            query.whereEqualTo(Post.KEY_CATEGORY, searchCategory);
        if (searchRole != null && searchRole.length() > 0)
            query.whereContains(Post.KEY_ROLES, searchRole);

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

    // TODO fix currently returned array is empty
    private ArrayList<String> getColumnArray(String columnName) {
        ArrayList columnArray = new ArrayList<String>();
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.addAscendingOrder(columnName);
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "issue getting " + columnName);
                }
                if (posts.size() > 0) {
                    for (Post post : posts)
                        // TODO replace getstartup name with function
                        columnArray.add(post.getStartupName());
                }
            }
        });

        return columnArray;
    }
}