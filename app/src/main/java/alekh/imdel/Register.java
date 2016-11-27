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

public class Register extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // EditText fields
        final EditText eUser = (EditText) findViewById(R.id.register_username_field);
        final EditText ePass1 = (EditText) findViewById(R.id.register_password_field1);
        final EditText ePass2 = (EditText) findViewById(R.id.register_password_field2);

        // Create capture button with font
        Typeface font = Typeface.createFromAsset(getAssets(), "fontawesome-webfont.ttf");
        final Button registerButton = (Button) findViewById(R.id.register_button);
        registerButton.setTypeface(font);
        registerButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.setClickable(false);
                        String user = eUser.getText().toString();
                        String pass1 = ePass1.getText().toString();
                        String pass2 = ePass1.getText().toString();
                        if (pass1.equals(pass2)) {
                            register(user, pass1);
                        } else {
                            Toast.makeText(getApplicationContext(), "Re-typed password doesn't match.", Toast.LENGTH_LONG).show();
                        }
                        v.setClickable(true);
                    }
                }
        );
    }


    private void register(final String user, final String pass) {
        RequestParams params = new RequestParams();
        params.put("username", user);
        params.put("password", pass);
        ImdelBackendRestClient.post(getString(R.string.register_url), params, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("user", user);
                returnIntent.putExtra("pass", pass);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                try {
                    Toast.makeText(getApplicationContext(), (String) errorResponse.get("reason"), Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                System.out.println(errorResponse);
            }
        });
    }
}
