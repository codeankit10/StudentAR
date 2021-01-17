package com.example.studentar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class MainActivity extends AppCompatActivity {
    final Context context=this;
    public static final Integer RecordAudio=1;
    private SpeechRecognizer speechRecognizer;
    //private EditText editText;
    private Button button;
    //Intent speechRecog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Snackbar.make(findViewById(R.id.frame),"Search For Plain Surface And Tap Screen",
                10000)
            .show();
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){
            checkPermission();
        }
        ArFragment arFragment=(ArFragment)getSupportFragmentManager().findFragmentById(R.id.ar_frag);
        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent)->
                {
                    showEdittext(arFragment,hitResult);
                }
                );


    }

    private void showEdittext(ArFragment arFragment, HitResult hitResult) {

        final Dialog dialog= new Dialog(context);
        dialog.setContentView(R.layout.dialog);
        dialog.setTitle("Title");

        final EditText text =(EditText)dialog.findViewById(R.id.text);
        final Button image=(Button)dialog.findViewById(R.id.image);
        image.setOnClickListener(new View.OnClickListener() {
                                     @Override
                                     public void onClick(View view) {
                                         speechRecognizer = SpeechRecognizer.createSpeechRecognizer(MainActivity.this);
                                         final Intent speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                                         speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                                         speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

                                         speechRecognizer.setRecognitionListener(new RecognitionListener() {
                                             @Override
                                             public void onReadyForSpeech(Bundle bundle) {

                                             }

                                             @Override
                                             public void onBeginningOfSpeech() {
                                                 text.setText("");
                                                 text.setHint("Listening....");
                                             }

                                             @Override
                                             public void onRmsChanged(float v) {

                                             }

                                             @Override
                                             public void onBufferReceived(byte[] bytes) {

                                             }

                                             @Override
                                             public void onEndOfSpeech() {

                                             }

                                             @Override
                                             public void onError(int i) {

                                             }

                                             @Override
                                             public void onResults(Bundle bundle) {
                                                 ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                                                 text.setText(data.get(0));
                                             }

                                             @Override
                                             public void onPartialResults(Bundle bundle) {

                                             }

                                             @Override
                                             public void onEvent(int i, Bundle bundle) {

                                             }

                                         });

                                         image.setOnTouchListener(new View.OnTouchListener() {
                                             @Override
                                             public boolean onTouch(View view, MotionEvent motionEvent) {
                                                 if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                                                     speechRecognizer.stopListening();
                                                 }
                                                 if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                                                     speechRecognizer.startListening(speechIntent);
                                                 }
                                                 return false;
                                             }
                                         });
                                     }
                                 });
                Button dialogButton = (Button) dialog.findViewById(R.id.ok);
                dialogButton.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onClick(View view) {
                        placeTextView(arFragment, hitResult, text.getText().toString());
                        dialog.dismiss();
                    }
                   });
                dialog.show();
            }


            @RequiresApi(api = Build.VERSION_CODES.N)
            private void placeTextView(ArFragment arFragment, HitResult hitResult, String toString) {
                CompletableFuture<Void> renerable =
                        ViewRenderable.builder()
                                .setView(arFragment.getContext(), R.layout.textboard)
                                .build()
                                .thenAccept(renderable -> {
                                    TextView textView = (TextView) renderable.getView();
                                    textView.setText(toString);
                                    Anchor anchor = hitResult.createAnchor();
                                    AnchorNode anchorNode = new AnchorNode(anchor);
                                    anchorNode.setParent(arFragment.getArSceneView().getScene());
                                    TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
                                    transformableNode.setParent(anchorNode);
                                    transformableNode.setRenderable(renderable);
                                    transformableNode.select();
                                });


            }

            @Override
            protected void onDestroy() {
                super.onDestroy();
                speechRecognizer.destroy();
            }

            private void checkPermission() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RecordAudio);

                }
            }

            @Override
            public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                if (requestCode == RecordAudio && grantResults.length > 0) ;
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this, "Permisssion Granted", Toast.LENGTH_SHORT).show();
            }
        }
