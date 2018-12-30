package com.spently.spently;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentText;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentTextRecognizer;

import java.util.List;
public class FirebaseOCR {
    public static void detectText(Bitmap bitmap, final Context context, final OCRFragment ocrFragment) {
        FirebaseVisionImage img = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionDocumentTextRecognizer recognizer = FirebaseVision.getInstance().getCloudDocumentTextRecognizer();
        recognizer.processImage(img).addOnSuccessListener(new OnSuccessListener<FirebaseVisionDocumentText>() {
            @Override
            public void onSuccess(FirebaseVisionDocumentText firebaseVisionDocumentText) {
                ocrFragment.processFirebaseVisionText(firebaseVisionDocumentText);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "API Failure.", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        });
    }

    public static String processText(FirebaseVisionDocumentText text, Context context) {
        if (text == null) {
            return null;
        }
        List<FirebaseVisionDocumentText.Block> blocks = text.getBlocks();
        StringBuilder textVal = new StringBuilder();
        for (FirebaseVisionDocumentText.Block block : blocks) {
            textVal.append(block.getText());
        }
        return textVal.toString();
    }
}
