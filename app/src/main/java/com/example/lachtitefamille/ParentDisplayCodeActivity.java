package com.example.lachtitefamille;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ParentDisplayCodeActivity extends AppCompatActivity {

    private ParentDisplayCodeActivity myInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        myInstance = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_display_code);

        String code = getIntent().getExtras().getString("code");
        ((TextView) findViewById(R.id.textView8)).setText(code);


        Button clickButton = (Button) findViewById(R.id.button3);

        clickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mIntent = new Intent(myInstance, ParentActivity.class);
                startActivity(mIntent);
            }
        });
    }
}