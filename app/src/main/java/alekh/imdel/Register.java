package alekh.imdel;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Register extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Create capture button with font
        Typeface font = Typeface.createFromAsset(getAssets(), "fontawesome-webfont.ttf");
        final Button registerButton = (Button) findViewById(R.id.login_button);
        registerButton.setTypeface(font);
        registerButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.setClickable(false);
                        EditText user = (EditText) findViewById(R.id.register_username_field);
                        EditText pass1 = (EditText) findViewById(R.id.register_password_field1);
                        EditText pass2 = (EditText) findViewById(R.id.register_password_field2);
                        if (pass1.getText().toString() == pass2.getText().toString()) {
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("user", user.getText().toString());
                            returnIntent.putExtra("pass", pass1.getText().toString());
                            setResult(Activity.RESULT_OK, returnIntent);
                        } else {
                            // TOAST
                        }
                        v.setClickable(true);
                    }
                }
        );
    }
}
