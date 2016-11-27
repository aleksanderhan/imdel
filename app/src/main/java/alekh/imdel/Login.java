package alekh.imdel;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


public class Login extends AppCompatActivity {

    private static final int REGISTER_REQUEST_CODE = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Create capture button with font
        Typeface font = Typeface.createFromAsset(getAssets(), "fontawesome-webfont.ttf");
        final Button loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setTypeface(font);
        loginButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.setClickable(false);
                        EditText user = (EditText) findViewById(R.id.login_username_field);
                        EditText pass = (EditText) findViewById(R.id.login_password_field);
                        login(user.getText().toString(), pass.getText().toString());
                        v.setClickable(true);
                    }
                }
        );

        // Create capture button with font
        final Button registerButton = (Button) findViewById(R.id.to_register_button);
        registerButton.setTypeface(font);
        registerButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.setClickable(false);
                        Intent registerIntent = new Intent(getApplicationContext(), Register.class);
                        startActivityForResult(registerIntent, REGISTER_REQUEST_CODE);
                        v.setClickable(true);
                    }
                }
        );
    }


    private void login(String username, String password) {
        RequestParams params = new RequestParams();
        params.put("username", username);
        params.put("password", password);
        ImdelBackendRestClient.post(getString(R.string.login_url), params, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    String token = (String) response.get("token");
                    int userID = (int) response.get("user_id");
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("token", token);
                    returnIntent.putExtra("user_id", userID);
                    setResult(Activity.RESULT_OK, returnIntent);
                    Toast.makeText(getApplicationContext(), "Login successful.", Toast.LENGTH_LONG).show();
                    finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == REGISTER_REQUEST_CODE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                String username = data.getStringExtra("user");
                String password = data.getStringExtra("pass");
                login(username, password);
            } else {

            }
        }
    }

}
