package hu.vtominator.edu.View;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import hu.vtominator.edu.Model.Constants;
import hu.vtominator.edu.R;

public class RegistrationActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etUsername, etPassword, etPasswordAgain, etEmail;
    private Button bRegistration;

    private Context mContext = RegistrationActivity.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etPasswordAgain = findViewById(R.id.etPasswordAgain);
        etEmail = findViewById(R.id.etEmail);
        bRegistration = findViewById(R.id.bRegistration);

        bRegistration.setOnClickListener(this);
    }


    private void createUser() {
        final String username = etUsername.getText().toString().trim();
        final String email = etEmail.getText().toString().trim();
        final String password = etPassword.getText().toString().trim();
        final String password_again = etPasswordAgain.getText().toString().trim();


        Random random = new Random();
        final int hash = random.nextInt(999) + 1;
        final int defaultZero = 0;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.URL_CREATEUSER,
                new Response.Listener<String>() {
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            if (!jsonObject.getBoolean("error")) {
                                startActivity(new Intent(mContext, LoginActivity.class));
                            } else {

                                if (etUsername.getText().toString().trim().equals(""))
                                    etUsername.setError("A mező kitöltése kötelező");
                                if (etEmail.getText().toString().trim().equals(""))
                                    etEmail.setError("A mező kitöltése kötelező");
                                if (etPassword.getText().toString().trim().equals(""))
                                    etPassword.setError("A mező kitöltése kötelező");
                                if (etPasswordAgain.getText().toString().trim().equals(""))
                                    etPasswordAgain.setError("A mező kitöltése kötelező");


                                if (!etPassword.getText().toString().trim().matches(Constants.PASSWORD_PATTERN)) {
                                    etPassword.setError("A jelszónak legalább 6 karakter hosszúnak kell lennie, tartalmaznia kell kis- és nagybetűket, valamint legalább egy számot!");
                                }

                                if (!etPassword.getText().toString().trim().matches(etPasswordAgain.getText().toString().trim())) {
                                    etPasswordAgain.setError("A két jelszó nem egyezik!");
                                }

                                if (!etEmail.getText().toString().trim().matches(Constants.EMAIL_PATTERN)) {
                                    etEmail.setError("Helytelen e-mail cím!");
                                }
                                if (!etUsername.getText().toString().trim().equals("") && jsonObject.getString("code").equals("0"))
                                    etUsername.setError(jsonObject.getString("message"));

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },

                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override

            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("email", email);
                params.put("password", password);
                params.put("password_again", password_again);
                params.put("hash", Integer.toString(hash));
                params.put("active", Integer.toString(defaultZero));
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bRegistration:
                createUser();
                break;
        }
    }
}
