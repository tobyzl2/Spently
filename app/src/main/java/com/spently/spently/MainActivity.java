package com.spently.spently;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    return true;
                case R.id.navigation_camera:
                    getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new CameraFragment()).addToBackStack(null).commit();
                    return true;
                case R.id.navigation_entries:
                    return true;
            }
            return false;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

//    private void detectText() {
//        FirebaseVisionImage img = FirebaseVisionImage.fromBitmap(rotatedBitmap);
//        FirebaseVisionTextRecognizer recognizer = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
//        recognizer.processImage(img).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
//            @Override
//            public void onSuccess(FirebaseVisionText firebaseVisionText) {
//                processText(firebaseVisionText);
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(getApplicationContext(), "Failed To Detect Receipt", Toast.LENGTH_LONG).show();
//                e.printStackTrace();
//            }
//        });
//    }
//
//    private void processText(FirebaseVisionText text) {
//        List<FirebaseVisionText.TextBlock> blocks = text.getTextBlocks();
//        if (blocks.size() == 0) {
//            Toast.makeText(this, "No Receipt Detected", Toast.LENGTH_LONG).show();
//            return;
//        }
//        String textVal = "";
//        for (FirebaseVisionText.TextBlock block : blocks) {
//            textVal += block.getText();
//            textView.setText(textVal);
//        }
//    }
}