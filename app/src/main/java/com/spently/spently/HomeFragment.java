package com.spently.spently;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.ml.vision.document.FirebaseVisionDocumentText;

import java.io.File;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;

public class HomeFragment extends Fragment implements OCRFragment {
    static final int REQUEST_TAKE_PHOTO = 1;
    View homeLayout;
    ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        homeLayout = inflater.inflate(R.layout.fragment_home, container, false);
        FileHelper.logFile(getContext(), "entries");
        MonthTracker.checkMonth(getContext());
        initiateTotal();
        homeLayout.findViewById(R.id.picture_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
        return homeLayout;
    }

    public void initiateTotal() {
        TextView totalSpent = homeLayout.findViewById(R.id.total_spent);
        try {
            totalSpent.setText("$" + FileHelper.readFromFile(getContext(), "total").get(0));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    File photoFile = null;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            try {
                photoFile = ImageHelper.createImageFile(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getContext(), "com.spently.spently.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage("Analyzing Image...");
            progressDialog.show();
            FirebaseOCR.detectText(ImageHelper.createBitmap(photoFile.getAbsolutePath()), getContext(), this);
        }
    }

    public void processFirebaseVisionText(FirebaseVisionDocumentText firebaseVisionDocumentText) {
        String textVal = FirebaseOCR.processText(firebaseVisionDocumentText);
        if (textVal == null) {
            Toast.makeText(getContext(), "Could not find a price!", Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
            return;
        }
        Double total = TextFinder.findTotal(textVal);
        String name = TextFinder.findName(textVal);
        if (total == -1.0) {
            Toast.makeText(getContext(), "Could not find a price!", Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
        } else {
            confirmInfo(total, name);
        }
    }

    public void confirmInfo(final double total, final String name) {
        progressDialog.dismiss();
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Confirmation")
                .setMessage("Price $" + String.format("%.2f", total) + " was detected. Continue?")
                .setCancelable(false)
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent toInputForm = new Intent(getActivity(), InputFormActivity.class);
                        toInputForm.putExtra("total_spent", total);
                        toInputForm.putExtra("name", name);
                        startActivity(toInputForm);
                    }
                })
                .setNegativeButton("Retake", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dispatchTakePictureIntent();
                    }
                })
                .show();
    }
}
