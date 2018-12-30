package com.spently.spently;

import com.google.firebase.ml.vision.document.FirebaseVisionDocumentText;

public interface OCRFragment {
    void processFirebaseVisionText(FirebaseVisionDocumentText firebaseVisionDocumentText);
}
