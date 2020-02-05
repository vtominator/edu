package hu.vtominator.edu.View;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
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
import com.bumptech.glide.request.RequestOptions;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.support.v7.widget.SearchView;

import de.hdodenhof.circleimageview.CircleImageView;
import hu.vtominator.edu.Model.Constants;
import hu.vtominator.edu.Model.Event;
import hu.vtominator.edu.Controller.EventAdapter;
import hu.vtominator.edu.Controller.SharedPrefManager;
import hu.vtominator.edu.R;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Context mContext = MainActivity.this;

    private CircleImageView circleImageView;
    private TextView txtName, txtEmail, tvNo, tvYes;

    private DrawerLayout drawer;
    private GoogleSignInClient mGoogleSignInClient;

    private String getUsername;
    private Bitmap bitmap;
    private String image_url;

    private SwitchCompat switcher;
    private Button bFun, bKapcsolodj_ki, bJelentkezz;


    private RecyclerView mRecycleView;
    private EventAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private boolean toolbarClicked;

    private static ArrayList<Event> eventList = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);


        getUsername = SharedPrefManager.getInstance(this).getUsername();


        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);



        txtName = headerView.findViewById(R.id.profile_name);
        txtEmail = headerView.findViewById(R.id.profile_email);
        circleImageView = headerView.findViewById(R.id.profile_pic);

        txtName.setText(SharedPrefManager.getInstance(mContext).getUsername());
        txtEmail.setText(SharedPrefManager.getInstance(mContext).getEmail());

        if (getUsername == null ) {
            navigationView.inflateMenu(R.menu.drawer_menu_facebook);
        } else if (getUsername.equals("admin")){
            navigationView.inflateMenu(R.menu.drawer_menu_admin);

            Menu menu = navigationView.getMenu();
            MenuItem jelentkezzGombAktivalo = menu.findItem(R.id.nav_switch);
            View actionView = MenuItemCompat.getActionView(jelentkezzGombAktivalo);
            switcher = actionView.findViewById(R.id.switcher);
        } else {
            navigationView.inflateMenu(R.menu.drawer_menu_normal);
        }


        bJelentkezz = findViewById(R.id.bJelentkezz);
        bFun = findViewById(R.id.bFun);
        bKapcsolodj_ki = findViewById(R.id.bKapcsolodj_ki);



        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {


                switch (menuItem.getItemId()) {
                    case R.id.new_post:
                        startActivity(new Intent(mContext, AddEventActivity.class));
                        break;
                    case R.id.nav_switch:
                        switcher.setChecked(!switcher.isChecked());
                        if (switcher.isChecked()) {
                            bJelentkezz.setVisibility(View.VISIBLE);
                        } else {
                            bJelentkezz.setVisibility(View.GONE);
                        }
                        break;
                    case R.id.logout:
                        logoutProfile();
                        break;
                }
                return true;
            }
        });


        facebookSignIn();
        checkFacebookLogin();
        googleSignIn();
        topToolbar();
        searchBar();
        listToView();
        loadEvents();
        profilePicture();


    }


    private void makePinned(final Event currentEvent) {
        final int event_id = currentEvent.getEvent_id();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.URL_SETPINNED, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Toast.makeText(mContext, jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                    if (!jsonObject.getBoolean("error")) {


                        mAdapter.notifyDataSetChanged();


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
                params.put("event_id", Integer.toString(event_id));
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        requestQueue.add(stringRequest);
    }
    private void deleteEvent(Event currentEvent, PopupWindow popupWindow) {

        popupWindow.dismiss();

        final int event_id = currentEvent.getEvent_id();

        final Event tempEvent = currentEvent;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.URL_DELETEEVENT, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);

                    if (!jsonObject.getBoolean("error")) {

                        eventList.remove(tempEvent);
                        Intent refresh = new Intent(mContext, MainActivity.class);
                        refresh.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        mContext.startActivity(refresh);
                        finish();

                    } else {
                        Toast.makeText(mContext, jsonObject.getString("error"), Toast.LENGTH_SHORT).show();
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
                params.put("event_id", Integer.toString(event_id));
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        requestQueue.add(stringRequest);
    }
    private void popupWindow(final Event currentEvent){

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.delete_popup_window, null);

        tvNo = (TextView) popupView.findViewById(R.id.tvNo);
        tvYes = (TextView) popupView.findViewById(R.id.tvYes);

        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        tvNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        tvYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteEvent(currentEvent, popupWindow);
            }
        });
    }

    private void listToView() {
        mRecycleView = findViewById(R.id.recycleView);
        mRecycleView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);

        mAdapter = new EventAdapter(eventList);

        Collections.sort(eventList, Event.BY_PRIOR);


        mRecycleView.setLayoutManager(mLayoutManager);
        mRecycleView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new EventAdapter.OnItemClickListener() {
            @Override
            public void onPinnedClick(int position) {
                final Event currentEvent = eventList.get(position);
                makePinned(currentEvent);

                finish();
                overridePendingTransition(0, 0);
                startActivity(getIntent());
                overridePendingTransition(0, 0);

            }

            @Override
            public void onDeleteClick(int position) {
                final Event currentEvent = eventList.get(position);
                popupWindow(currentEvent);
            }

            @Override
            public void onCommentClick(int position) {
                final Event currentEvent = eventList.get(position);
                final String event_id = Integer.toString(currentEvent.getEvent_id());


                final String image_resource = image_url;

                Intent commentIntent = new Intent(mContext, CommentsActivity.class);
                commentIntent.putExtra("event_id", event_id);
                commentIntent.putExtra("profile_picture", image_resource);
                startActivity(commentIntent);
            }


        });
    }
    private void loadEvents() {
        eventList.clear();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Constants.URL_GETALLEVENTS,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        try {

                            JSONObject jsonObject = new JSONObject(response);

                            if (!jsonObject.getBoolean("error")) {

                                JSONArray events = jsonObject.getJSONArray("events");

                                for (int i = 0; i < events.length(); i++) {

                                    JSONObject eventObject = events.getJSONObject(i);

                                    int event_id = eventObject.getInt("event_id");
                                    String main_category = eventObject.getString("main_category");
                                    String side_category = eventObject.getString("side_category");
                                    String eventname = eventObject.getString("eventname");
                                    String organiser = eventObject.getString("organiser");
                                    String date = eventObject.getString("date");
                                    String time = eventObject.getString("time");
                                    String location = eventObject.getString("location");
                                    String description = eventObject.getString("description");
                                    String picture = eventObject.getString("picture");
                                    int pinned = eventObject.getInt("pinned");


                                    Event eventItem = new Event(event_id, main_category, side_category, eventname, organiser, date, time, location, description, picture, pinned);

                                    eventList.add(eventItem);


                                }
                            } else {
                                Toast.makeText(mContext, jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                            }

                            listToView(); // A lista beletöltése a viewba

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void facebookSignIn() {
        AccessTokenTracker tokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if (currentAccessToken == null) {
                    txtName.setText("");
                    txtEmail.setText("");
                    circleImageView.setImageResource(0);
                } else {
                    loadUserProfile(currentAccessToken);
                }
            }
        };
    }
    private void checkFacebookLogin() {
        if (AccessToken.getCurrentAccessToken() != null) {
            loadUserProfile(AccessToken.getCurrentAccessToken());
        }
    }
    private void loadUserProfile(AccessToken newAccessToken) {

        GraphRequest request = GraphRequest.newMeRequest(newAccessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try {
                    String first_name = object.getString("first_name");
                    String last_name = object.getString("last_name");
                    String email = object.getString("email");
                    String id = object.getString("id");

                    image_url = "https://graph.facebook.com/" + id + "/picture?type=normal";

                    SharedPrefManager.getInstance(mContext).facebook_login(
                            object.getString("id"),
                            object.getString("first_name"),
                            object.getString("last_name"),
                            object.getString("email")
                    );


                    RequestOptions requestOptions = new RequestOptions();
                    requestOptions.dontAnimate();

                    txtName.setText(last_name + " " + first_name);
                    txtEmail.setText(email);
                    Glide.with(getApplicationContext()).load(image_url).into(circleImageView);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "first_name, last_name, email, id");
        request.setParameters(parameters);
        request.executeAsync();

    }
    private void googleSignIn() {
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(mContext);
        if (account != null) {

            SharedPrefManager.getInstance(mContext).google_login(
                    account.getId(),
                    account.getDisplayName(),
                    account.getEmail()
            );


            txtEmail.setText(account.getEmail());
            txtName.setText(account.getDisplayName());
//            image_url = account.getPhotoUrl().toString();

            Glide.with(this).load(account.getPhotoUrl()).into(circleImageView);
            //Glide.with(this).load(image_url).into(circleImageView);
        }
    }


    private void topToolbar() {
        final Toolbar topToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(topToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        final LinearLayout layoutToolbar = topToolbar.findViewById(R.id.toolbar_container);

        bFun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                layoutToolbar.removeAllViews();
                toolbarClicked = true;
                mAdapter.getFilter().filter(getResources().getString(R.string.fun));


                Button bSzorakozas = new Button(mContext);
                Button bEsemeny = new Button(mContext);
                Button bLehetoseg = new Button(mContext);

                bSzorakozas.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
                bEsemeny.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
                bLehetoseg.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f));

                bSzorakozas.setText(getResources().getString(R.string.szorazkozas));
                bEsemeny.setText(getResources().getString(R.string.esemeny));
                bLehetoseg.setText(getResources().getString(R.string.lehetoseg));

                Typeface font = Typeface.createFromAsset(getAssets(), "scpbold.ttf");
                bSzorakozas.setTypeface(font);
                bEsemeny.setTypeface(font);
                bLehetoseg.setTypeface(font);

                bSzorakozas.setTextSize(18);
                bEsemeny.setTextSize(18);
                bLehetoseg.setTextSize(18);

                bSzorakozas.setBackgroundColor(getResources().getColor(R.color.yellow));
                bEsemeny.setBackgroundColor(getResources().getColor(R.color.yellow));
                bLehetoseg.setBackgroundColor(getResources().getColor(R.color.yellow));

                layoutToolbar.addView(bSzorakozas);
                layoutToolbar.addView(bEsemeny);
                layoutToolbar.addView(bLehetoseg);

                bSzorakozas.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAdapter.getFilter().filter("SZÓRAKOZÁS");
                    }
                });

                bEsemeny.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAdapter.getFilter().filter("ESEMÉNY");
                    }
                });

                bLehetoseg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAdapter.getFilter().filter("LEHETŐSÉG");
                    }
                });

            }
        });
        bKapcsolodj_ki.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                layoutToolbar.removeAllViews();
                toolbarClicked = true;
                mAdapter.getFilter().filter(getResources().getString(R.string.kapcsolodj_ki));

                Button bSzulo = new Button(mContext);
                Button bFiatalok = new Button(mContext);
                Button bSzakemberek = new Button(mContext);

                bSzulo.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
                bFiatalok.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
                bSzakemberek.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f));

                bSzulo.setText(getResources().getString(R.string.szulo));
                bFiatalok.setText(getResources().getString(R.string.fiatalok));
                bSzakemberek.setText(getResources().getString(R.string.szakemberek));

                Typeface font = Typeface.createFromAsset(getAssets(), "scpbold.ttf");
                bSzulo.setTypeface(font);
                bFiatalok.setTypeface(font);
                bSzakemberek.setTypeface(font);

                bSzulo.setTextSize(18);
                bFiatalok.setTextSize(18);
                bSzakemberek.setTextSize(18);

                bSzulo.setBackgroundColor(getResources().getColor(R.color.yellow));
                bFiatalok.setBackgroundColor(getResources().getColor(R.color.yellow));
                bSzakemberek.setBackgroundColor(getResources().getColor(R.color.yellow));

                layoutToolbar.addView(bSzulo);
                layoutToolbar.addView(bFiatalok);
                layoutToolbar.addView(bSzakemberek);

                bSzulo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAdapter.getFilter().filter("SZÜLŐ");
                    }
                });

                bFiatalok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAdapter.getFilter().filter("FIATALOK");
                    }
                });

                bSzakemberek.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAdapter.getFilter().filter("SZAKEMBEREK");
                    }
                });

            }
        });
    }
    private void searchBar() {
        Toolbar searchBar = findViewById(R.id.searchBar);
        setSupportActionBar(searchBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.searchbar, menu);


        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setIconifiedByDefault(false);

        searchView.setQueryHint("Keresés");
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setBackground(getDrawable(R.drawable.roundedbutton));
        searchView.setPadding(-16, 0, 0, 0);


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                mAdapter.getFilter().filter(s);
                return false;
            }
        });
        return true;
    }


    private void profilePicture() {
        if (SharedPrefManager.getInstance(this).getLoginType() == 1) {

            circleImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chooseFile();
                }
            });
        }
    }
    private void chooseFile() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Válassz profilképet"), 1);
    }
    private void uploadPicture(final String username, final String photo) {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Feltöltés... ");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.URL_UPLOADPICTURE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String success = jsonObject.getString("success");

                    if (success.equals("1")) {
                        Toast.makeText(mContext, "Sikeres", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                    Toast.makeText(mContext, e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(mContext, error.toString(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("photo", photo);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
    private String getStringImage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

        byte[] imageByteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imageByteArray, Base64.DEFAULT); //String encodedimage;


    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                circleImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

            uploadPicture(getUsername, getStringImage(bitmap));

        }
    }



    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
           drawer.closeDrawer(GravityCompat.START);

        }else if(toolbarClicked) {
            toolbarClicked = false;

            mAdapter.getFilter().filter("");
            final Toolbar topToolbar = findViewById(R.id.toolbar);
            final LinearLayout layoutToolbar = topToolbar.findViewById(R.id.toolbar_container);
            layoutToolbar.removeAllViews();

            layoutToolbar.addView(bFun);
            layoutToolbar.addView(bKapcsolodj_ki);
            layoutToolbar.addView(bJelentkezz);


        }else{
            super.onBackPressed();
        }
    }

    private void logoutProfile() {
        if (AccessToken.getCurrentAccessToken() != null) {
            LoginManager.getInstance().logOut();
            SharedPrefManager.getInstance(this).logout();
            Intent intent = new Intent(mContext, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            finish();
            startActivity(intent);
        } else {
            googlesignOut();
            SharedPrefManager.getInstance(this).logout();
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
    }
    private void googlesignOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        finish();
                        startActivity(new Intent(mContext, LoginActivity.class));
                    }
                });
    }

}
