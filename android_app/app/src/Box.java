package com.example.hid_tangxin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
public class Box {
    private float centerX;
    private float centerY;
    private float width;
    private float height;
    private float angle;
    private float confidence;
    private String name;
    private float[] xyxyxyxy;

    public Box(float centerX, float centerY, float width, float height, float angle, float confidence, String name, float[] xyxyxyxy) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.width = width;
        this.height = height;
        this.angle = angle;
        this.confidence = confidence;
        this.name = name;
        this.xyxyxyxy = xyxyxyxy;
    }

    public float[] getXyxyxyxy() {
        return xyxyxyxy;
    }

    public String getName() {
        return name;
    }


    public float getCenterY() {
        return centerY;
    }

    public float getWidth(){
        return width;
    }

    public float getHeight(){
        return height;
    }

    public float getAngle(){
        return angle;
    }
    public void setAngle(float angle) {
        this.angle = angle;
    }

    public void setWidth(float width) {
        this.angle = width;
    }

    public void setHeight(float height) {
        this.angle = height;
    }


    public static List<Box> processAndSortBoxes(List<Box> boxes, float[] centerImg) {
        for (Box box : boxes) {
            float width = box.getWidth();
            float height = box.getHeight();
            float angle = box.getAngle();
            float[] xyxyxyxy = box.getXyxyxyxy();


            if (height > width) {
                angle += Math.PI / 2;
            }

            float[][] points = new float[4][2];
            for (int i = 0; i < 4; i++) {
                points[i][0] = xyxyxyxy[i * 2];
                points[i][1] = xyxyxyxy[i * 2 + 1];
            }

            float[] distances = new float[4];
            for (int i = 0; i < 4; i++) {
                distances[i] = (float) Math.sqrt(
                        Math.pow(centerImg[0] - points[i][0], 2) +
                                Math.pow(centerImg[1] - points[i][1], 2)
                );
            }

            int[] sortedIndices = sortIndicesByDistance(distances);
            if (sortedIndices[0] == 0 || sortedIndices[1] == 0) {
                angle += Math.PI;
            }

            box.setAngle(angle);
        }


        boxes.sort((box1, box2) -> Float.compare(box1.getAngle(), box2.getAngle()));


        if (boxes.size() >= 3) {
            float angleDiff31 = boxes.get(2).getAngle() - boxes.get(0).getAngle();

            if (angleDiff31 >= 1) {
                float angleDiff21 = boxes.get(1).getAngle() - boxes.get(0).getAngle();

                if (angleDiff21 < 1) {
                    List<Box> temp = new ArrayList<>(boxes.subList(2, boxes.size()));
                    temp.addAll(boxes.subList(0, 2));
                    boxes = temp;
                } else {
                    List<Box> temp = new ArrayList<>(boxes.subList(1, boxes.size()));
                    temp.addAll(boxes.subList(0, 1));
                    boxes = temp;
                }
            }
        }

        return boxes;
    }


    private static int[] sortIndicesByDistance(float[] distances) {
        Integer[] indices = new Integer[distances.length];
        for (int i = 0; i < distances.length; i++) {
            indices[i] = i;
        }
        Arrays.sort(indices, (i1, i2) -> Float.compare(distances[i1], distances[i2]));
        return Arrays.stream(indices).mapToInt(Integer::intValue).toArray();
    }
}
