package com.example.meetup.Fragments;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.meetup.AccessTokenLoader;
import com.example.meetup.Adapters.PostsAdapter;
import com.example.meetup.Models.MapMarker;
import com.example.meetup.Models.Post;
import com.example.meetup.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.slider.Slider;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.language.v1.CloudNaturalLanguage;
import com.google.api.services.language.v1.CloudNaturalLanguageRequest;
import com.google.api.services.language.v1.CloudNaturalLanguageScopes;
import com.google.api.services.language.v1.model.AnalyzeEntitiesResponse;
import com.google.api.services.language.v1.model.AnnotateTextRequest;
import com.google.api.services.language.v1.model.Document;
import com.google.api.services.language.v1.model.Features;
import com.google.api.services.language.v1.model.Token;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.gson.JsonObject;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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
    private TextView distanceText;
    private Slider searchDistance;
    private float maxDistance;
    private MapMarker currentLocation;

    private ArrayList<String> startupNames;
    private ArrayList<String> categories;
    private ArrayList<String> roles;
    private ArrayList<String> keywords;

    private ArrayAdapter<String> searchNameAdapter;
    private ArrayAdapter<String> searchCategoriesAdapter;
    private ArrayAdapter<String> searchRolesAdapter;
    private ArrayAdapter<String> searchKeywordsAdapter;

    private static final int LOADER_ACCESS_TOKEN = 1; // Token used to initiate the request loader
    private GoogleCredential credential = null; //GoogleCredential object so that the requests for NLP Api could be made

    // A  Thread on which the Api request will be made and results will be delivered. As network calls cannot be made on the amin thread, so we are creating a separate thread for the network calls
    private Thread thread;

    // Google Request for the NLP Api. This actually is acting like a Http client queue that will process each request and response from the Google Cloud server
    private final BlockingQueue<CloudNaturalLanguageRequest<? extends GenericJson>> requests
            = new ArrayBlockingQueue<>(100);

    // Api for CloudNaturalLanguage from the Google Client library, this is the instance of our request that we will make to analyze the text.
    private CloudNaturalLanguage api = new CloudNaturalLanguage.Builder(
            new NetHttpTransport(),
            JacksonFactory.getDefaultInstance(),
            new HttpRequestInitializer() {
                @Override
                public void initialize(HttpRequest request) throws IOException {
                    credential.initialize(request);
                }
            }).build();


    public SearchFragment() {
        // Required empty public constructor
    }

    interface QueryResponseCallback {
        public void onPostsReturned(List<Post> posts) throws IOException;
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
        searchDistance = view.findViewById(R.id.searchDistance);
        distanceText = view.findViewById(R.id.distanceText);
        searchDistance.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                maxDistance = value;
            }
        });

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

                allPosts.clear();
                adapter.clear();
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

        getColumnArray(new QueryResponseCallback() {
            @Override
            public void onPostsReturned(List<Post> postsList) throws IOException {
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
                    String textDescription = post.getDescription();
                    analyzeTextUsingCloudNLPApi(textDescription);

                    String textCaption = post.getCaption();
                    analyzeTextUsingCloudNLPApi(textCaption);

                }
                searchNameAdapter.notifyDataSetChanged();
                searchCategoriesAdapter.notifyDataSetChanged();
                searchRolesAdapter.notifyDataSetChanged();
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

        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(requireContext());
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
            // return;
        }
        MapMarker mapMarker = new MapMarker();
        ParseQuery<Post> finalQuery = query;
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
                                currentLocation = mapMarker ;
                                Log.i(TAG, "maxdist " + maxDistance);
                                Log.i(TAG, "current loc " + currentLocation.getLocation().toString());
                                finalQuery.whereWithinKilometers(Post.KEY_GEOPOINT, currentLocation.getLocation(), maxDistance);

                                finalQuery.findInBackground(new FindCallback<Post>() {
                                    @Override
                                    public void done(List<Post> posts, ParseException e) {
                                        if (e != null) {
                                            Log.e(TAG, "issue with getting posts");
                                        }
                                        Log.i(TAG, "getting posts now!" + posts.size());
                                        for (Post post: posts) {
                                            Log.i(TAG, "Post " + post.getDescription() + " username " + post.getUser().getUsername() + " location " + post.getGeoPoint());
                                        }

                                        // save received posts to list and notify adapter of new data
                                        allPosts.addAll(posts);
                                        adapter.notifyDataSetChanged();
                                    }
                                });
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

    private void getColumnArray(QueryResponseCallback callback) {
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
                        callback.onPostsReturned(posts);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    // send text to Cloud Api for analysis
    public void analyzeTextUsingCloudNLPApi(String text) {
        try {
            requests.add(api
                    .documents()
                    .annotateText(new AnnotateTextRequest()
                            .setDocument(new Document()
                                    .setContent(text)
                                    .setType("PLAIN_TEXT"))
                            .setFeatures(new Features()
                                    .setExtractSyntax(true)
                                    .setExtractEntities(true)
                            )));
            prepareApi();
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
        credential = new GoogleCredential()
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
        if (thread != null) {
            return;
        }
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (thread == null) {
                        break;
                    }
                    try {
                        // API calls are executed here in this worker thread
                        Log.i(TAG, requests.toString());
                        deliverResponse(requests.take().execute());
                    } catch (InterruptedException e) {
                        Log.e("TAG", "Interrupted.", e);
                        break;
                    } catch (IOException e) {
                        Log.e("TAG", "Failed to execute a request.", e);
                    }
                }
            }
        });
        thread.start();
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
                for (Token token:((ArrayList<Token>) response.get("tokens"))) {
                    String tag = token.getPartOfSpeech().getTag();
                    Log.i(TAG, tag);
                    if (tag.equals("NOUN")) {
                        Log.i(TAG, token.getLemma());
                        Log.i(TAG, keywords.toString());
                        if (!keywords.contains(token.getLemma())) {
                            keywords.add(token.getLemma());
                            Log.i(TAG, token.getLemma());
                        }
                    }
                }
                searchKeywordsAdapter.notifyDataSetChanged();
            }
       });

    }
}