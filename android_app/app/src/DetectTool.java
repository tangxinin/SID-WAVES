package com.example.hid_tangxin;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.util.Log;
import org.tensorflow.lite.Interpreter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



public class DetectTool {
    static String TAG = "DetectTool";

    public static List<Box> detectImg(Interpreter yoloInterpreter, Bitmap bitmap) {
        long startTime = System.currentTimeMillis();

        Bitmap resizedBitmap = resizeBitmap(bitmap, 640);
        float[][][][] inputArr = bitmapToFloatArray(resizedBitmap);


        int[] outputShape = yoloInterpreter.getOutputTensor(0).shape();
        Log.d("OutputShape", "Output shape: " + Arrays.toString(outputShape));


        float[][][] outArray = new float[1][300][7];

        try {
            yoloInterpreter.run(inputArr, outArray);
            Log.d("ModelStatus", "Inference completed successfully.");
        } catch (Exception e) {
            Log.e("ModelStatus", "Inference failed: " + e.getMessage());
            return new ArrayList<>();
        }
        Log.d("ModelOutput", Arrays.deepToString(outArray[0]));

        float confidenceThreshold = 0.3f;
        List<Box> boxes = new ArrayList<>();
        float imageWidth = bitmap.getWidth();
        float imageHeight = bitmap.getHeight();

        float[][] detections = outArray[0];
        for (int i = 0; i < detections.length; i++) {
            float[] detection = detections[i];
            float confidence = detection[4];
            if (confidence >= confidenceThreshold) {
                float centerX = detection[0] * imageWidth;
                float centerY = detection[1] * imageHeight;
                float width = detection[2] * imageWidth;
                float height = detection[3] * imageHeight;
                float angle = detection[6];
                int classId = (int) detection[5];


                float[] xyxyxyxy = calculateOBBVertices(centerX, centerY, width, height, angle);


                boxes.add(new Box(centerX, centerY, width, height, angle, confidence, "class_" + classId, xyxyxyxy));
            }
        }

        return boxes;
    }

    private static float[] calculateOBBVertices(float centerX, float centerY, float width, float height, float angle) {
        float halfWidth = width / 2;
        float halfHeight = height / 2;


        float cosTheta = (float) Math.cos(angle);
        float sinTheta = (float) Math.sin(angle);


        float[] vertices = new float[8];
        vertices[0] = centerX + (-halfWidth * cosTheta - (-halfHeight) * sinTheta); // x1
        vertices[1] = centerY + (-halfWidth * sinTheta + (-halfHeight) * cosTheta); // y1
        vertices[2] = centerX + (halfWidth * cosTheta - (-halfHeight) * sinTheta);  // x2
        vertices[3] = centerY + (halfWidth * sinTheta + (-halfHeight) * cosTheta);  // y2
        vertices[4] = centerX + (halfWidth * cosTheta - halfHeight * sinTheta);     // x3
        vertices[5] = centerY + (halfWidth * sinTheta + halfHeight * cosTheta);     // y3
        vertices[6] = centerX + (-halfWidth * cosTheta - halfHeight * sinTheta);    // x4
        vertices[7] = centerY + (-halfWidth * sinTheta + halfHeight * cosTheta);    // y4

        return vertices;
    }

    public static int predictImage(Interpreter cnnInterpreter, Bitmap image) {

        Bitmap preprocessedImage = resizeBitmap(image, 224);


        float[][][][] input = bitmapToFloatArray(preprocessedImage);
        float[][] output = new float[1][1];


        cnnInterpreter.run(input, output);
        Log.d("PredictImage", "Input image size: " + preprocessedImage.getWidth() + "x" + preprocessedImage.getHeight());
        Log.d("PredictImage", "Model output: " + output[0][0]);


        return Math.round(output[0][0]);
    }


    public static Bitmap resizeBitmap(Bitmap source, int maxSize) {

        return Bitmap.createScaledBitmap(source, maxSize, maxSize, true);
    }
    public static float[][][][] bitmapToFloatArray(Bitmap bitmap) {

        int height = bitmap.getHeight();
        int width = bitmap.getWidth();


        float[][][][] result = new float[1][height][width][3];

        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {

                int pixel = bitmap.getPixel(j, i);

                result[0][i][j][0] = ((pixel >> 16) & 0xFF) / 255.0f;
                result[0][i][j][1] = ((pixel >> 8) & 0xFF) / 255.0f;
                result[0][i][j][2] = (pixel & 0xFF) / 255.0f;
            }
        }
        return result;
    }

    private static MappedByteBuffer loadModelFile(Context context, String fileName) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(fileName);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public static Interpreter getYOLOInterpreter(Context context){
        Interpreter.Options options = new Interpreter.Options();
        options.setNumThreads(4);
        Interpreter interpreter = null;
        try {
            interpreter = new Interpreter(loadModelFile(context, "best_float32.tflite"), options);
        } catch (IOException e) {
            throw new RuntimeException("Error loading model file.", e);
        }
        return interpreter;
    }

    public static Interpreter getCnnInterpreter(Context context) {
        Interpreter.Options options = new Interpreter.Options();
        options.setNumThreads(2);
        Interpreter interpreter = null;
        try {
            interpreter = new Interpreter(loadModelFile(context, "model_cnn.tflite"), options);
        } catch (IOException e) {
            throw new RuntimeException("Error loading CNN model file.", e);
        }
        return interpreter;
    }

}


