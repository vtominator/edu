package hu.vtominator.edu.View;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import hu.vtominator.edu.Model.Comment;
import hu.vtominator.edu.Controller.CommentAdapter;
import hu.vtominator.edu.Model.Constants;
import hu.vtominator.edu.Controller.SharedPrefManager;
import hu.vtominator.edu.R;

public class CommentsActivity extends AppCompatActivity {
    private static final String TAG = "CommentsActivity";
    private Context mContext;

    private EditText etComment;
    private ImageView ivImage_profile;
    private TextView bPost;


    private RecyclerView mRecycleView;
    private CommentAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private static ArrayList<Comment> commentList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Kommentek");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        etComment = findViewById(R.id.add_comment);
        ivImage_profile = findViewById(R.id.image_profile);

        Intent commentIntent = getIntent();
        String image_url = commentIntent.getStringExtra("profile_picture");
        Glide.with(this).load(image_url).into(ivImage_profile);
        Log.d(TAG, "asd: "+ivImage_profile);

        bPost = findViewById(R.id.post);

        bPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etComment.getText().toString().equals("")){
                    Toast.makeText(CommentsActivity.this, "Nem küldhetsz üres kommentet!", Toast.LENGTH_SHORT).show();
                } else {
                    addComment();
                }
            }
        });

        loadComments();
    }


    private void addComment() {
        Intent commentIntent = getIntent();

        final String event_id = commentIntent.getStringExtra("event_id");
        final String user_id = SharedPrefManager.getInstance(this).getUserId();
        final String comment = etComment.getText().toString().trim();


        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.URL_ADDCOMMENT, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);

                    if (!jsonObject.getBoolean("error")) {
                        finish();
                        overridePendingTransition(0, 0);
                        startActivity(getIntent());
                        overridePendingTransition(0, 0);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("event_id", event_id);
                params.put("user_id", user_id);
                params.put("comment", comment);

                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

    private void listToView() {
        mRecycleView = findViewById(R.id.comments_view);
        mRecycleView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);

        mAdapter = new CommentAdapter(commentList);

        mRecycleView.setLayoutManager(mLayoutManager);
        mRecycleView.setAdapter(mAdapter);


    }
    private void loadComments() {
        commentList.clear();

        Intent commentIntent = getIntent();
        final String saved_eventID = commentIntent.getStringExtra("event_id");


        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.URL_GETALLCOMMENTS,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        try {

                            JSONObject jsonObject = new JSONObject(response);

                            if (!jsonObject.getBoolean("error")) {

                                JSONArray comments = jsonObject.getJSONArray("comments");

                                for (int i = 0; i < comments.length(); i++) {

                                    JSONObject commentObject = comments.getJSONObject(i);


                                    String event_id = commentObject.getString("event_id");
                                    String username = commentObject.getString("username");
                                    String comment = commentObject.getString("comment");

                                    Comment commentItem = new Comment(username, comment);

                                    if (saved_eventID.equals(event_id)){
                                        commentList.add(commentItem);
                                    }


                                }
                            } else {
                                Toast.makeText(getApplicationContext(), jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                            }

                            listToView(); // A lista beletöltése a viewba

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        })
        {
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("event_id", saved_eventID);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }



}
