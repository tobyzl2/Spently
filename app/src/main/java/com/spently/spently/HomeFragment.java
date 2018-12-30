package com.spently.spently;

import android.app.AlertDialog;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.ml.vision.document.FirebaseVisionDocumentText;

import java.io.File;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class HomeFragment extends Fragment implements OCRFragment{
    static final int REQUEST_TAKE_PHOTO = 1;
    TextView totalSpent;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View homeLayout = inflater.inflate(R.layout.fragment_home, container, false);
        totalSpent = homeLayout.findViewById(R.id.total_spent);
        try {
            totalSpent.setText("$" + FileHelper.readFromFile(getContext(),"total").get(0));
        } catch (Exception e) {
            //No File Exists
        }
        homeLayout.findViewById(R.id.picture_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        try {
            double total = Double.parseDouble(FileHelper.readFromFile(getContext(),"total").get(0));
            double goal = Double.parseDouble(FileHelper.readFromFile(getContext(), "goal").get(0));
            ((TextView) homeLayout.findViewById(R.id.home_goal)).setText("$" + goal);
            ((Button) homeLayout.findViewById(R.id.home_set_goal)).setText("Edit Goal");
            ((ProgressBar) homeLayout.findViewById(R.id.home_goal_progress)).setProgress((int)(total / goal * 100));
        } catch (Exception e) {
            //No Goal
        }
        homeLayout.findViewById(R.id.home_set_goal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setGoal(inflater, container);
            }
        });
        return homeLayout;
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
            FirebaseOCR.detectText(ImageHelper.createBitmap(photoFile.getAbsolutePath()), getContext(), this);
        }
    }

    public void processFirebaseVisionText(FirebaseVisionDocumentText firebaseVisionDocumentText) {
        String textVal = FirebaseOCR.processText(firebaseVisionDocumentText, getContext());
        if (textVal == null) {
            Toast.makeText(getContext(), "Could not find a price!", Toast.LENGTH_LONG).show();
            return;
        }
        Double total = TotalFinder.totalFinder(textVal);
        if (total == -1.0) {
            Toast.makeText(getContext(), "Could not find a price!", Toast.LENGTH_LONG).show();
        } else {
            confirmInfo(total);
        }
    }

    public void confirmInfo(final double total) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Confirmation")
                .setMessage("Price $" + String.format( "%.2f", total) + " was detected. Continue?")
                .setCancelable(false)
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent toInputForm = new Intent(getActivity(), InputFormActivity.class);
                        toInputForm.putExtra("total_spent", total);
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

    public void setGoal(LayoutInflater inflater, ViewGroup container) {
        final View field = inflater.inflate(R.layout.alert_set_goal, container, false);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Set Goal")
                .setMessage("Enter Your Spending Goal.");
        builder.setView(field);
        builder.setIcon(R.drawable.ic_goal);
        builder.setPositiveButton("Save",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String dollars = ((EditText)field.findViewById(R.id.set_goal_dollars_field)).getText().toString();
                        String cents = ((EditText)field.findViewById(R.id.set_goal_cents_field)).getText().toString();
                        FileHelper.writeToFile(getContext(), "goal", String.format( "%.2f", Double.parseDouble(dollars + "." + cents)), MODE_PRIVATE);
                        getFragmentManager().beginTransaction().replace(R.id.content_frame, new HomeFragment()).addToBackStack(null).commit();
                    }
                });
        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        builder.show();
    }
}
