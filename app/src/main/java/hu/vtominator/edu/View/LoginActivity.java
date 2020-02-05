package hu.vtominator.edu.View;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import hu.vtominator.edu.Model.Constants;
import hu.vtominator.edu.Controller.SharedPrefManager;
import hu.vtominator.edu.R;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Context mContext = LoginActivity.this;

    private EditText etUsername, etPassword;
    private Button bLogin;
    private TextView bRegistration, bLoginguest;

    private LoginButton bFacebook;
    private CallbackManager callbackManager;

    private SignInButton bGoogle;
    private GoogleSignInClient mGoogleSignInClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fullScreen();
        setContentView(R.layout.activity_login);



        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);

        bLogin = findViewById(R.id.bLogin);
        bRegistration = findViewById(R.id.tvRegistration);
        bLoginguest = findViewById(R.id.tvLoginGuest);


        bFacebook = findViewById(R.id.bFacebook);
        callbackManager = CallbackManager.Factory.create();
        bFacebook.setReadPermissions(Arrays.asList("email", "public_profile"));
        bFacebook.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                facebookLogin();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

        bGoogle = findViewById(R.id.bGoogle);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        bGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleLogin();
            }
        });


        bLogin.setOnClickListener(this);
        bRegistration.setOnClickListener(this);
        bLoginguest.setOnClickListener(this);

        isLoggedInNormal();
        isLoggedInGoogle();


    }

    private void fullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void facebookLogin() {
        startActivity(new Intent(this, MainActivity.class));
    }

    private void googleLogin() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            startActivity(new Intent(mContext, MainActivity.class));
        } catch (ApiException e) {
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void isLoggedInNormal() {
        if (SharedPrefManager.getInstance(this).isLoggedIn()) {
            finish();
            startActivity(new Intent(mContext, MainActivity.class));
        }
    }

    private void isLoggedInGoogle() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            startActivity(new Intent(mContext, MainActivity.class));
        }
    }

    private void userLogin() {
        final String username = etUsername.getText().toString().trim();
        final String password = etPassword.getText().toString().trim();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.URL_LOGINUSER,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject obj = new JSONObject(response);
                            if (!obj.getBoolean("error")) {
                                SharedPrefManager.getInstance(mContext).login(
                                        obj.getString("user_id"),
                                        obj.getString("username"),
                                        obj.getString("email"),
                                        obj.getString("password"),
                                        1
                                );
                                startActivity(new Intent(mContext, MainActivity.class));
                                finish();

                            } else {
                                Toast.makeText(mContext, obj.getString("message"), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("password", password);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void guestLogin() {
        SharedPrefManager.getInstance(mContext).login(null, "Vendég", "", "", 0);
        startActivity(new Intent(mContext, MainActivity.class));
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bLogin:
                userLogin();
                break;
            case R.id.tvRegistration:
                startActivity(new Intent(this, RegistrationActivity.class));
                break;
            case R.id.tvLoginGuest:
                guestLogin();
                break;
        }
    }
}
