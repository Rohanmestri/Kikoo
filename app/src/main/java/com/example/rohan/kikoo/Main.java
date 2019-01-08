package com.example.rohan.kikoo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Main extends Activity implements View.OnClickListener {

    Button button1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout);

        button1 = (Button) findViewById(R.id.question);
        button1.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.question:
                Intent intent1 = new Intent(this, DetectLabel.class);
                startActivity(intent1);
                break;
            default:
                break;
        }
    }
}
