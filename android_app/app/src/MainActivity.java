package com.example.hid_tangxin;

import android.app.Dialog;
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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.app.AlertDialog;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;

import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;


public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAPTURE_IMAGE_REQUEST = 2;
    private static final int REQUEST_PERMISSIONS = 100;
    private ImageView imageView;
    private TextView selectedTextView;
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
        selectedTextView = findViewById(R.id.selectedTextView);
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
            if (selectedImage == null) {
                Toast.makeText(this, "Please select an image first.", Toast.LENGTH_SHORT).show();
                return;
            }

            showModeMenu(v);  // 新增：先弹出模式选择
        });
    }

    private void showModeMenu(View anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);

        popupMenu.getMenu().add("MT");
        popupMenu.getMenu().add("IA");
        popupMenu.getMenu().add("MDA");

        popupMenu.setOnMenuItemClickListener(item -> {
            String mode = item.getTitle().toString();
            showPickerDialog(mode); // 进入第二步
            return true;
        });

        popupMenu.show();
    }

    private void showPickerDialog(String mode) {

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_picker);

        NumberPicker picker1 = dialog.findViewById(R.id.picker1);
        NumberPicker picker2 = dialog.findViewById(R.id.picker2);
        NumberPicker picker3 = dialog.findViewById(R.id.picker3);

        TextView title1 = dialog.findViewById(R.id.title1);
        TextView title2 = dialog.findViewById(R.id.title2);
        TextView title3 = dialog.findViewById(R.id.title3);

        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);

        // 设置标题
        title1.setText("Group 1");
        title2.setText("Group 2");
        title3.setText("Group 3");

        // 根据模式设置数据
        String[] options;

        switch (mode) {
            case "MT":
                options = new String[]{"HPV", "RV", "HIV"};
                break;
            case "IA":
                options = new String[]{"RV", "HIV", "HBV"};
                break;
            case "MDA":
                options = new String[]{"RV", "IgM/IgG", "CRP"};
                break;
            default:
                options = new String[]{"N/A"};
        }

        setupPicker(picker1, options);
        setupPicker(picker2, options);
        setupPicker(picker3, options);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {

            String g1 = options[picker1.getValue()];
            String g2 = options[picker2.getValue()];
            String g3 = options[picker3.getValue()];

            dialog.dismiss();

               startDecodingWithSelection(g1, g2, g3);
        });

        dialog.show();
    }



    private void setupPicker(NumberPicker picker, String[] data) {
        picker.setMinValue(0);
        picker.setMaxValue(data.length - 1);
        picker.setDisplayedValues(data);
        picker.setWrapSelectorWheel(true);
    }

    private void startDecodingWithSelection(String g1, String g2, String g3) {

        // 👉 更新UI显示（后面会加TextView）
        runOnUiThread(() -> {
            selectedTextView.setText("Selected: " + g1 + ", " + g2 + ", " + g3);
        });

        new Thread(() -> {
            try {
                processImage(selectedImage, g1, g2, g3);

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Error processing image.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void openImagePicker() {

        resultTextView1.setText("");
        resultTextView2.setText("");
        resultTextView3.setText("");
        selectedTextView.setText("");

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

//

//private void processImage(Bitmap image) {
private void processImage(Bitmap image, String g1, String g2, String g3) {
    Interpreter yoloInterpreter = null;
    Interpreter cnnInterpreter = null;
    Log.d("Selection", "Group1=" + g1 + ", Group2=" + g2 + ", Group3=" + g3);
    try {
        // 初始化 YOLO 模型
        yoloInterpreter = DetectTool.getYOLOInterpreter(this);

        // 使用 YOLO 模型检测目标框
        final List<Box> detectedBoxes = DetectTool.detectImg(yoloInterpreter, image);
        if (detectedBoxes == null || detectedBoxes.isEmpty()) {
            Log.e("Detection", "No boxes detected.");
            runOnUiThread(() -> Toast.makeText(this, "No boxes detected.", Toast.LENGTH_SHORT).show());
            return;
        }
        Log.d("Detection", "Number of boxes detected: " + detectedBoxes.size());

        // 确保检测到的框数量为 9
        if (detectedBoxes.size() != 9) {
            runOnUiThread(() -> Toast.makeText(this, "Exactly 9 detection boxes are required.", Toast.LENGTH_SHORT).show());
            return;
        }

        // 对检测框进行排序
        float[] centerImg = {image.getWidth() / 2.0f, image.getHeight() / 2.0f};
        final List<Box> sortedBoxes = Box.processAndSortBoxes(detectedBoxes, centerImg);

        // 绘制检测框并裁剪
        Pair<Bitmap, List<Map<String, Bitmap>>> result = processBoxes(image, sortedBoxes);
        Bitmap imageWithBoxes = result.first;

        // 显示带框的图片
        imageView.setImageBitmap(imageWithBoxes);
        List<Map<String, Bitmap>> croppedImages = result.second;

        // 初始化分类模型
        cnnInterpreter = DetectTool.getCnnInterpreter(this);

        // 存储预测结果
        float[] ncResults = new float[3];
        float[] h16Results = new float[3];
        float[] h18Results = new float[3];

        // 遍历裁剪的图片并进行预测
        for (int i = 0; i < croppedImages.size(); i++) {
            Map<String, Bitmap> cropPair = croppedImages.get(i);
            Bitmap topCrop = cropPair.get("top");
            Bitmap bottomCrop = cropPair.get("bottom");

            // 对 top 和 bottom 进行预测
            float predTop = DetectTool.predictImage(cnnInterpreter, topCrop);
            float predBottom = DetectTool.predictImage(cnnInterpreter, bottomCrop);

            // 计算差值
            float diff = predTop - predBottom;

            if (i == 0 || i == 3 || i == 6) { // NC
                int groupIndex = 2 - (i / 3); // 调整 groupIndex 的计算逻辑
                ncResults[groupIndex] = diff;
                Log.d("NC", "Group " + (groupIndex + 1) + " NC diff: " + diff);
            } else if (i == 1 || i == 4 || i == 7) { // H16
                int groupIndex = 2 - (i / 3); // 调整 groupIndex 的计算逻辑
                h16Results[groupIndex] = diff;
                Log.d("H16", "Group " + (groupIndex + 1) + " H16 diff: " + diff);
            } else if (i == 2 || i == 5 || i == 8) { // H18
                int groupIndex = 2 - (i / 3); // 调整 groupIndex 的计算逻辑
                h18Results[groupIndex] = diff;
                Log.d("H18", "Group " + (groupIndex + 1) + " H18 diff: " + diff);
            }
        }


        String[] results = interpretResults(ncResults, h16Results, h18Results, g1, g2, g3);


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
        // 释放资源
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

    private Pair<Bitmap, List<Map<String, Bitmap>>> processBoxes(Bitmap image, List<Box> boxes) {

        Bitmap mutableImage = image.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableImage);

        Paint boxPaint = new Paint();
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setColor(Color.BLUE);
        boxPaint.setStrokeWidth(20);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLUE);
        textPaint.setTextSize(150);

        List<Map<String, Bitmap>> croppedImages = new ArrayList<>();

        for (int idx = 0; idx < boxes.size(); idx++) {
            Box box = boxes.get(idx);

            float[] xyxyxyxy = box.getXyxyxyxy();
            float centerX = box.getCenterX();
            float centerY = box.getCenterY();
            float width = box.getWidth();
            float height = box.getHeight();
            float angle = box.getAngle();

            // Step 1: Adjust width and height if necessary
            if (width < height) {
                float temp = width;
                width = height;
                height = temp;
            }

            // Step 2: Calculate destination points for perspective transform
            float[][] dstPoints = {
                    {0, height},      // Bottom-left
                    {0, 0},           // Top-left
                    {width, 0},       // Top-right
                    {width, height}   // Bottom-right
            };

            // Step 3: Perform perspective transform
            float[][] srcPoints = {
                    {xyxyxyxy[0], xyxyxyxy[1]},
                    {xyxyxyxy[6], xyxyxyxy[7]},
                    {xyxyxyxy[4], xyxyxyxy[5]},
                    {xyxyxyxy[2], xyxyxyxy[3]}
            };

            Matrix transformMatrix = calculatePerspectiveTransform(srcPoints, dstPoints);

            Bitmap cropped = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);
            Canvas croppedCanvas = new Canvas(cropped);

            Paint paint = new Paint();
            croppedCanvas.setMatrix(transformMatrix);
            croppedCanvas.drawBitmap(image, 0, 0, paint);

            // Step 4: Rotate the cropped image 90 degrees clockwise
            Matrix rotationMatrix = new Matrix();
            rotationMatrix.postRotate(90);
            Bitmap croppedRotated = Bitmap.createBitmap(cropped, 0, 0, cropped.getWidth(), cropped.getHeight(), rotationMatrix, true);

            // Step 5: Split the rotated image into top and bottom halves
            int h = croppedRotated.getHeight();
            int w = croppedRotated.getWidth();

            Bitmap croppedTopHalf = Bitmap.createBitmap(croppedRotated, 0, 0, w, (int) (h * 0.5));
            Bitmap croppedBottomHalf = Bitmap.createBitmap(croppedRotated, 0, (int) (h * 0.5), w, (int) (h * 0.4));

            // Step 6: Save the cropped images to the list
            Map<String, Bitmap> croppedParts = new HashMap<>();
            croppedParts.put("top", croppedTopHalf);
            croppedParts.put("bottom", croppedBottomHalf);
            croppedImages.add(croppedParts);

            // Step 7: Draw the bounding box and label on the original image
            Path path = new Path();
            path.moveTo(xyxyxyxy[0], xyxyxyxy[1]);
            path.lineTo(xyxyxyxy[2], xyxyxyxy[3]);
            path.lineTo(xyxyxyxy[4], xyxyxyxy[5]);
            path.lineTo(xyxyxyxy[6], xyxyxyxy[7]);
            path.close();
            canvas.drawPath(path, boxPaint);

            // Adjust the index for labeling
            int adjustedIdx;
            if (idx >= 6) { // #7, #8, #9 -> #1, #2, #3
                adjustedIdx = idx - 5;
            } else if (idx <= 2) { // #1, #2, #3 -> #7, #8, #9
                adjustedIdx = idx + 7;
            } else { // #4, #5, #6 remain unchanged
                adjustedIdx = idx + 1;
            }

            // Step 2: Determine label position
            float labelY, labelX;
            if (adjustedIdx <= 3) { // #1, #2, #3
                labelY = xyxyxyxy[1] - 30;
                labelX = xyxyxyxy[0] - 200;
            } else if (adjustedIdx >= 7) { // Other groups
                labelY = xyxyxyxy[1] - 30;
                labelX = xyxyxyxy[0];
            }else {
                labelY = xyxyxyxy[1] + 120;
                labelX = xyxyxyxy[0] +30;
            }


            String label = "#" + adjustedIdx;
            canvas.drawText(label, labelX, labelY, textPaint);
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



    private String[] interpretResults(float[] ncResults, float[] ch1Results, float[] ch2Results,
                                      String g1, String g2, String g3) {

        String[] groupResults = new String[3];

        for (int i = 0; i < 3; i++) {

            int nc = ncResults[i] < -0.5 ? 1 : 0;
            int c1 = ch1Results[i] < -0.5 ? 1 : 0;
            int c2 = ch2Results[i] < -0.5 ? 1 : 0;

            String key = nc + "" + c1 + "" + c2;


            groupResults[i] = decodeByMarker(key, getMarkerByIndex(i, g1, g2, g3));
        }

        return groupResults;
    }

    private String getMarkerByIndex(int index, String g1, String g2, String g3) {
        if (index == 0) return g1;
        if (index == 1) return g2;
        return g3;
    }

    private String decodeByMarker(String key, String marker) {

        switch (marker) {

            // ================= HPV =================
            case "HPV":
                return decodeHPV(key);

            // ================= RV（呼吸道病毒） =================
            case "RV":
                return decodeRV(key);

            // ================= 其他（占位，可扩展） =================
            case "HIV":
            case "HBV":
            case "IgM/IgG":
            case "CRP":
                return "Pending";  // 后续扩展

            default:
                return "Unknown";
        }
    }

    private String decodeHPV(String key) {

        switch (key) {
            case "000":
            case "100":
                return "No Infection";

            case "010":
            case "110":
                return "HPV16";

            case "001":
            case "101":
                return "HPV18";

            case "011":
            case "111":
                return "HPV16 & HPV18";

            default:
                return "Unknown";
        }
    }

    private String decodeRV(String key) {

        switch (key) {
            case "000":
                return "No Infection";
            case "100":
                return "SARS-CoV-2";
            case "010":
                return "IAV";
            case "001":
                return "IBV";
            case "110":
                return "SARS-CoV-2 & IAV";
            case "101":
                return "SARS-CoV-2 & IBV";
            case "011":
                return "IAV & IBV";
            case "111":
                return "SARS-CoV-2 & IAV & IBV";
            default:
                return "Unknown";
        }
    }
}


