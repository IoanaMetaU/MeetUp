package com.example.meetup.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.example.meetup.AccessTokenLoader;
import com.example.meetup.Adapters.PostsAdapter;
import com.example.meetup.Models.Post;
import com.example.meetup.R;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.language.v1.CloudNaturalLanguage;
import com.google.api.services.language.v1.CloudNaturalLanguageRequest;
import com.google.api.services.language.v1.CloudNaturalLanguageScopes;
import com.google.api.services.language.v1.model.AnnotateTextRequest;
import com.google.api.services.language.v1.model.Document;
import com.google.api.services.language.v1.model.Features;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class SearchFragment extends Fragment {

    private static final String TAG = "SearchFragment";

    private RecyclerView posts;

    private PostsAdapter adapter;
    private List<Post> allPosts;

    private AutoCompleteTextView searchName;
    private AutoCompleteTextView searchCategory;
    private AutoCompleteTextView searchRole;
    private AutoCompleteTextView searchKeyWord;
    private Button searchFind;

    private ArrayList<String> startupNames;
    private ArrayList<String> categories;
    private ArrayList<String> roles;
    private ArrayList<String> keywords;

    private ArrayAdapter<String> searchNameAdapter;
    private ArrayAdapter<String> searchCategoriesAdapter;
    private ArrayAdapter<String> searchRolesAdapter;
    private ArrayAdapter<String> searchKeywordsAdapter;

    private static final int LOADER_ACCESS_TOKEN = 1; // Token used to initiate the request loader
    private GoogleCredential mCredential = null; //GoogleCredential object so that the requests for NLP Api could be made

    // A  Thread on which the Api request will be made and results will be delivered. As network calls cannot be made on the amin thread, so we are creating a separate thread for the network calls
    private Thread mThread;

    // Google Request for the NLP Api. This actually is acting like a Http client queue that will process each request and response from the Google Cloud server
    private final BlockingQueue<CloudNaturalLanguageRequest<? extends GenericJson>> mRequests
            = new ArrayBlockingQueue<>(100);

    // Api for CloudNaturalLanguage from the Google Client library, this is the instance of our request that we will make to analyze the text.
    private CloudNaturalLanguage mApi = new CloudNaturalLanguage.Builder(
            new NetHttpTransport(),
            JacksonFactory.getDefaultInstance(),
            new HttpRequestInitializer() {
                @Override
                public void initialize(HttpRequest request) throws IOException {
                    mCredential.initialize(request);
                }
            }).build();


    public SearchFragment() {
        // Required empty public constructor
    }

    interface Function {
        public void onCalled(List<Post> posts) throws IOException;
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

        prepareApi();

        setupAutocomplete();

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

//                allPosts.clear();
//                adapter.clear();
            }
        });
    }

    public void setupAdapters() {
        startupNames = new ArrayList<String>();
        searchNameAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, startupNames);
        categories = new ArrayList<String>();
        searchCategoriesAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, categories);
        roles = new ArrayList<String>();
        searchRolesAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, roles);
        keywords = new ArrayList<String>();
        searchKeywordsAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, keywords);
    }

    public void setupAutocomplete() {
        setupAdapters();

        getColumnArray(new Function() {
            @Override
            public void onCalled(List<Post> postsList) throws IOException {
                startupNames.clear();
                categories.clear();
                keywords.clear();
                roles.clear();
                for (Post post : postsList) {
                    if (!startupNames.contains(post.getStartupName()))
                        startupNames.add(post.getStartupName());
                    if (!categories.contains(post.getCategory()))
                        categories.add(post.getCategory());
                    ArrayList<String> rolesInPost = new ArrayList<>(Arrays.asList(post.getRoles().split(",[ ]*")));
                    for (String role : rolesInPost) {
                        if (!roles.contains(role))
                            roles.add(role);
                    }
                    String text = post.getDescription();
                    analyzeTextUsingCloudNLPApi(text);
                   // StringProcessing.getNouns(post.getDescription(), getContext());
//                    TODO update this code to use tokenizer and nouns as keywords
//                    ArrayList<String> keywordsInPostCaption = new ArrayList<>(Arrays.asList(post.getCaption().split("[ .,]+")));
//                    ArrayList<String> keywordsInPostDescription = new ArrayList<>(Arrays.asList(post.getDescription().split("[ .,]+")));
//
//                    for (String word : keywordsInPostCaption) {
//                        if (!keywords.contains(word));
//                            keywords.add(word);
//                    }
//                    for (String word : keywordsInPostDescription) {
//                        if (!keywords.contains(word));
//                        keywords.add(word);
//                    }
                }
                searchNameAdapter.notifyDataSetChanged();
                searchCategoriesAdapter.notifyDataSetChanged();
                searchRolesAdapter.notifyDataSetChanged();
                searchKeywordsAdapter.notifyDataSetChanged();
            }
        });

        setAdaptersToViews();
    }

    public void setAdaptersToViews() {
        searchName.setAdapter(searchNameAdapter);
        searchName.setThreshold(1);
        searchCategory.setAdapter(searchCategoriesAdapter);
        searchCategory.setThreshold(1);
        searchRole.setAdapter(searchRolesAdapter);
        searchRole.setThreshold(1);
        searchKeyWord.setAdapter(searchKeywordsAdapter);
        searchKeyWord.setThreshold(1);
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

    private void getColumnArray(Function callback) {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "issue getting posts");
                }
                if (posts.size() > 0) {
                    try {
                        callback.onCalled(posts);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

//    /**
//     * Method called on the click of the Button
//     * @param view -> the view which is clicked
//     */
//    public void startAnalysis(View view) {
//        String textToAnalyze = editTextView.getText().toString();
//        if (TextUtils.isEmpty(textToAnalyze)) {
//            editTextView.setError(getString(R.string.empty_text_error_msg));
//        } else {
//            editTextView.setError(null);
//            analyzeTextUsingCloudNLPApi(textToAnalyze);
//        }
//    }

    // send text to Cloud Api for analysis
    public void analyzeTextUsingCloudNLPApi(String text) {
        try {
            mRequests.add(mApi
                    .documents()
                    .annotateText(new AnnotateTextRequest()
                            .setDocument(new Document()
                                    .setContent(text)
                                    .setType("PLAIN_TEXT"))
                            .setFeatures(new Features()
                                    .setExtractSyntax(true)
                                    .setExtractEntities(true)
                            )));
        } catch (IOException e) {
            Log.e("TAG", "Failed to create analyze request.", e);
        }
    }


    /**
     * Preparing the Cloud Api before making the actual request.
     * This method will actually initiate the AccessTokenLoader async task on completion
     * of which we will recieve the token that should be set in our request for Cloud NLP Api.
     */
    private void prepareApi() {
        // Initiate token refresh
        getLoaderManager().initLoader(LOADER_ACCESS_TOKEN, null,
                new LoaderManager.LoaderCallbacks<String>() {
                    @Override
                    public Loader<String> onCreateLoader(int id, Bundle args) {
                        return new AccessTokenLoader(getContext());
                    }

                    @Override
                    public void onLoadFinished(Loader<String> loader, String token) {
                        setAccessToken(token);
                    }

                    @Override
                    public void onLoaderReset(Loader<String> loader) {
                    }
                });
    }


    /**
     * This method will set the token from the Credentials.json file to the Google credential object.
     * @param token -> token recieved from the Credentials.json file.
     */
    public void setAccessToken(String token) {
        mCredential = new GoogleCredential()
                .setAccessToken(token)
                .createScoped(CloudNaturalLanguageScopes.all());
        startWorkerThread();
    }


    /**
     * This method will actually initiate a Thread and on this thread we will execute our Api request
     * and responses.
     *
     * Responses recieved will be delivered from here.
     */
    private void startWorkerThread() {
        if (mThread != null) {
            return;
        }
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (mThread == null) {
                        break;
                    }
                    try {
                        // API calls are executed here in this worker thread
                        deliverResponse(mRequests.take().execute());
                    } catch (InterruptedException e) {
                        Log.e("TAG", "Interrupted.", e);
                        break;
                    } catch (IOException e) {
                        Log.e("TAG", "Failed to execute a request.", e);
                    }
                }
            }
        });
        mThread.start();
    }


    /**
     * this method will handle the response recieved from the Cloud NLP request.
     * The response is a JSON object only.
     * This has been casted to GenericJson from Google Cloud so that the developers can easily parse through the same and can understand the response.
     *
     *
     * @param response --> the JSON object recieved as a response for the cloud NLP Api request
     */
    private void deliverResponse(final GenericJson response) {
        Log.d("TAG", "Generic Response --> " + response);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Response Recieved from Cloud NLP API");
                try {
                    Log.i(TAG, response.toPrettyString());
//                    resultTextView.setText(response.toPrettyString());
//                    nestedScrollView.setVisibility(View.VISIBLE);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });


    }
}