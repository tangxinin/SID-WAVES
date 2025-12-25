package com.example.hid_tangxin;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Path;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Pair;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.app.AlertDialog;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;


public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAPTURE_IMAGE_REQUEST = 2;
    private static final int REQUEST_PERMISSIONS = 100;
    private ImageView imageView;
    private Bitmap selectedImage;
    private TextView resultTextView1;
    private TextView resultTextView2;
    private TextView resultTextView3;
    private Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        resultTextView1 = findViewById(R.id.resultTextView1);
        resultTextView2 = findViewById(R.id.resultTextView2);
        resultTextView3 = findViewById(R.id.resultTextView3);
        Button uploadButton = findViewById(R.id.uploadButton);
        Button startDecodingButton = findViewById(R.id.startDecodingButton);

        // 请求权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, REQUEST_PERMISSIONS);
        }
        uploadButton.setOnClickListener(v -> openImagePicker());


        startDecodingButton.setOnClickListener(v -> {
            if (selectedImage != null) {
                new Thread(() -> {
                    try {
                        processImage(selectedImage);
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(this, "Error processing image.", Toast.LENGTH_SHORT).show());
                    }
                }).start();
            } else {
                Toast.makeText(this, "Please select an image first.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openImagePicker() {

        resultTextView1.setText("");
        resultTextView2.setText("");
        resultTextView3.setText("");


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image Source");
        builder.setItems(new CharSequence[]{"Choose from Gallery", "Open Camera"},
                (dialog, which) -> {
                    switch (which) {
                        case 0:
                            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
                            break;
                        case 1:
//
                            String filename = "test.png";
                            File outputImage = new File(getExternalCacheDir(),filename);

                            try {if (outputImage.exists()){
                                outputImage.delete();
                            }
                                outputImage.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (Build.VERSION.SDK_INT >= 24) {
                                //图片的url
                                imageUri = FileProvider.getUriForFile(MainActivity.this, "com.example.hid_tangxin.fileprovider", outputImage);
                            } else {
                                imageUri = Uri.fromFile(outputImage);
                            }

                            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                            startActivityForResult(intent, CAPTURE_IMAGE_REQUEST);
                            break;

                    }
                });
        builder.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
                Uri selectedImageUri = data.getData();
                handleImage(selectedImageUri);
            } else if (requestCode == CAPTURE_IMAGE_REQUEST) {
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                    handleImage(bitmap);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }


            }
        }
    }



    private void handleImage(Uri imageUri) {

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            imageView.setImageBitmap(bitmap);
            selectedImage = bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load image.", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleImage(Bitmap bitmap) {

        imageView.setImageBitmap(bitmap);
        selectedImage = bitmap;
    }

    private void processImage(Bitmap image) {
        Interpreter yoloInterpreter = null;
        Interpreter cnnInterpreter = null;

        try {

            yoloInterpreter = DetectTool.getYOLOInterpreter(this);


            final List<Box> detectedBoxes = DetectTool.detectImg(yoloInterpreter, image);
            if (detectedBoxes == null || detectedBoxes.isEmpty()) {
                Log.e("Detection", "No boxes detected.");
                runOnUiThread(() -> Toast.makeText(this, "No boxes detected.", Toast.LENGTH_SHORT).show());
                return;
            }
            Log.d("Detection", "Number of boxes detected: " + detectedBoxes.size());


            if (detectedBoxes.size() != 9) {
                runOnUiThread(() -> Toast.makeText(this, "Exactly 9 detection boxes are required.", Toast.LENGTH_SHORT).show());
                return;
            }


            float[] centerImg = {image.getWidth() / 2.0f, image.getHeight() / 2.0f};
            final List<Box> sortedBoxes = Box.processAndSortBoxes(detectedBoxes, centerImg);

            Pair<Bitmap, List<Bitmap>> result = processBoxes(image, sortedBoxes);
            Bitmap imageWithBoxes = result.first;



            imageView.setImageBitmap(imageWithBoxes);
            List<Bitmap> croppedImages = result.second;

            cnnInterpreter = DetectTool.getCnnInterpreter(this);


            int[] predictions = new int[9];
            for (int i = 0; i < croppedImages.size(); i++) {
                predictions[i] = DetectTool.predictImage(cnnInterpreter, croppedImages.get(i));
            }


            String[] results = interpretResults(predictions);


            String group1 = "Group 1 (#1 #2 #3):\n" + results[0];
            String group2 = "Group 2 (#4 #5 #6):\n" + results[1];
            String group3 = "Group 3 (#7 #8 #9):\n" + results[2];


            runOnUiThread(() -> {
                resultTextView1.setText(group1);
                resultTextView2.setText(group2);
                resultTextView3.setText(group3);


            });
        } catch (Exception e) {
            e.printStackTrace();

        } finally {

            if (yoloInterpreter != null) {
                yoloInterpreter.close();
            }
            if (cnnInterpreter != null) {
                cnnInterpreter.close();
            }


            if (selectedImage != null && !selectedImage.isRecycled()) {
                selectedImage.recycle();
                selectedImage = null;
            }
        }
    }


    private Pair<Bitmap, List<Bitmap>> processBoxes(Bitmap image, List<Box> boxes) {

        Bitmap mutableImage = image.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableImage);


        Paint boxPaint = new Paint();
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setColor(Color.BLUE);
        boxPaint.setStrokeWidth(20);


        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLUE);
        textPaint.setTextSize(150);


        List<Bitmap> croppedImages = new ArrayList<>();


        for (int idx = 0; idx < boxes.size(); idx++) {
            Box box = boxes.get(idx);


            float[] xyxyxyxy = box.getXyxyxyxy();
            Log.d("ProcessBoxes", "Box " + idx + " original vertices: " + Arrays.toString(xyxyxyxy));


            float widtho = box.getWidth();
            float heighto = box.getHeight();
            Log.d("ProcessBoxes", "Box " + idx + " original width: " + widtho + ", height: " + heighto);


            float shortEdge = Math.min(widtho, heighto);
            float longEdge = Math.max(widtho, heighto);


            if (widtho > heighto) {

                xyxyxyxy = new float[]{
                        xyxyxyxy[6], xyxyxyxy[7],
                        xyxyxyxy[0], xyxyxyxy[1],
                        xyxyxyxy[2], xyxyxyxy[3],
                        xyxyxyxy[4], xyxyxyxy[5]
                };
                Log.d("ProcessBoxes", "Box " + idx + " adjusted vertices: " + Arrays.toString(xyxyxyxy));
            }


            box.setWidth(shortEdge);
            box.setHeight(longEdge);
            Log.d("ProcessBoxes", "Box " + idx + " updated width: " + shortEdge + ", height: " + longEdge);


            Path path = new Path();
            path.moveTo(xyxyxyxy[0], xyxyxyxy[1]);
            path.lineTo(xyxyxyxy[2], xyxyxyxy[3]);
            path.lineTo(xyxyxyxy[4], xyxyxyxy[5]);
            path.lineTo(xyxyxyxy[6], xyxyxyxy[7]);
            path.close();
            canvas.drawPath(path, boxPaint);

            float imageCenterY = mutableImage.getHeight() / 2.0f;


            float centerY = box.getCenterY();

            float labelX, labelY;
            if (centerY > imageCenterY) {

                labelX = (xyxyxyxy[4] + xyxyxyxy[6]) / 2 -100;
                labelY = (xyxyxyxy[5] + xyxyxyxy[7]) / 2 + 200;
            } else {

                labelX = (xyxyxyxy[0] + xyxyxyxy[2]) / 2-100;
                labelY = (xyxyxyxy[1] + xyxyxyxy[3]) / 2 - 50;
            }

            String label = "#" + (idx + 1) ;

            canvas.drawText(label, labelX, labelY, textPaint);


            float cropWidth = shortEdge;
            float cropHeight = longEdge;


            float[][] dstPoints = {
                    {0, cropHeight},
                    {0, 0},
                    {cropWidth, 0},
                    {cropWidth, cropHeight}
            };


            float[][] srcPoints = {
                    {xyxyxyxy[0], xyxyxyxy[1]},
                    {xyxyxyxy[6], xyxyxyxy[7]},
                    {xyxyxyxy[4], xyxyxyxy[5]},
                    {xyxyxyxy[2], xyxyxyxy[3]}
            };

            Log.d("ProcessBoxes", "Box " + idx + " srcPoints: " + Arrays.deepToString(srcPoints));
            Log.d("ProcessBoxes", "Box " + idx + " dstPoints: " + Arrays.deepToString(dstPoints));


            Matrix transformMatrix = calculatePerspectiveTransform(srcPoints, dstPoints);


            Bitmap cropped = Bitmap.createBitmap((int) cropWidth, (int) cropHeight, Bitmap.Config.ARGB_8888);
            Canvas croppedCanvas = new Canvas(cropped);


            Paint paint = new Paint();
            croppedCanvas.setMatrix(transformMatrix);
            croppedCanvas.drawBitmap(image, 0, 0, paint);


            croppedImages.add(cropped);
            Log.d("ProcessBoxes", "Box " + idx + " cropped image added.");

        }


        return new Pair<>(mutableImage, croppedImages);
    }


    private Matrix calculatePerspectiveTransform(float[][] srcPoints, float[][] dstPoints) {
        Matrix matrix = new Matrix();


        float[] src = {
                srcPoints[0][0], srcPoints[0][1],
                srcPoints[1][0], srcPoints[1][1],
                srcPoints[2][0], srcPoints[2][1],
                srcPoints[3][0], srcPoints[3][1]
        };

        float[] dst = {
                dstPoints[0][0], dstPoints[0][1],
                dstPoints[1][0], dstPoints[1][1],
                dstPoints[2][0], dstPoints[2][1],
                dstPoints[3][0], dstPoints[3][1]
        };


        matrix.setPolyToPoly(src, 0, dst, 0, 4);
        return matrix;
    }


    private String[] interpretResults(int[] predictions) {

        int[] group1 = {predictions[0], predictions[1], predictions[2]};
        int[] group2 = {predictions[3], predictions[4], predictions[5]};
        int[] group3 = {predictions[6], predictions[7], predictions[8]};

        return new String[]{
                interpretGroupResult(group1),
                interpretGroupResult(group2),
                interpretGroupResult(group3)
        };
    }

    private String interpretGroupResult(int[] group) {
        if (group[0] == 1) return "Pollutional";
        String key = group[0] + " " + group[1] + " " + group[2];
        switch (key) {
            case "0 0 0":
                return "No Infection";
            case "0 1 0":
                return "HPV16";
            case "0 0 1":
                return "HPV18";
            case "0 1 1":
                return "HPV16&HPV18";
            default:
                return "Unknown";
        }
    }
}


