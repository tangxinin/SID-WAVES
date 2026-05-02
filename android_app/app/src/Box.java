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

    public float getCenterX() {
        return centerX;
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
    // 设置顶点坐标
    public void setXyxyxyxy(float[] xyxyxyxy) {
        this.xyxyxyxy = xyxyxyxy;
    }


//    public static List<Box> processAndSortBoxes(List<Box> boxes, float[] centerImg) {
//        for (Box box : boxes) {
//            float width = box.getWidth();
//            float height = box.getHeight();
//            float angle = box.getAngle();
//            float[] xyxyxyxy = box.getXyxyxyxy();
//
//
//            if (height > width) {
//                angle += Math.PI / 2;
//            }
//
//            float[][] points = new float[4][2];
//            for (int i = 0; i < 4; i++) {
//                points[i][0] = xyxyxyxy[i * 2];
//                points[i][1] = xyxyxyxy[i * 2 + 1];
//            }
//
//            float[] distances = new float[4];
//            for (int i = 0; i < 4; i++) {
//                distances[i] = (float) Math.sqrt(
//                        Math.pow(centerImg[0] - points[i][0], 2) +
//                                Math.pow(centerImg[1] - points[i][1], 2)
//                );
//            }
//
//            int[] sortedIndices = sortIndicesByDistance(distances);
//            if (sortedIndices[0] == 0 || sortedIndices[1] == 0) {
//                angle += Math.PI;
//            }
//
//            box.setAngle(angle);
//        }
//
//
//        boxes.sort((box1, box2) -> Float.compare(box1.getAngle(), box2.getAngle()));
//
//
//        if (boxes.size() >= 3) {
//            float angleDiff31 = boxes.get(2).getAngle() - boxes.get(0).getAngle();
//
//            if (angleDiff31 >= 1) {
//                float angleDiff21 = boxes.get(1).getAngle() - boxes.get(0).getAngle();
//
//                if (angleDiff21 < 1) {
//                    List<Box> temp = new ArrayList<>(boxes.subList(2, boxes.size()));
//                    temp.addAll(boxes.subList(0, 2));
//                    boxes = temp;
//                } else {
//                    List<Box> temp = new ArrayList<>(boxes.subList(1, boxes.size()));
//                    temp.addAll(boxes.subList(0, 1));
//                    boxes = temp;
//                }
//            }
//        }
//
//        return boxes;
//    }

    public static List<Box> processAndSortBoxes(List<Box> boxes, float[] centerImg) {
        for (Box box : boxes) {
            float width = box.getWidth();
            float height = box.getHeight();
            float angle = box.getAngle();
            float[] xyxyxyxy = box.getXyxyxyxy();

            // Step 1: 提取四个顶点
            float[][] points = new float[4][2];
            for (int i = 0; i < 4; i++) {
                points[i][0] = xyxyxyxy[i * 2];
                points[i][1] = xyxyxyxy[i * 2 + 1];
            }

            // Step 2: 如果高度大于宽度，调整角度并交换点 [1, 3] 和 [3, 1]
            if (height > width) {
                angle += Math.PI / 2;

                // 交换点 [1, 3] 和 [3, 1]
                float[] temp = points[1];
                points[1] = points[3];
                points[3] = temp;

                // 更新 xyxyxyxy 数组
                xyxyxyxy = new float[]{
                        points[0][0], points[0][1],
                        points[1][0], points[1][1],
                        points[2][0], points[2][1],
                        points[3][0], points[3][1]
                };
            }

            // Step 3: 计算每个顶点到图像中心的距离
            float[] distances = new float[4];
            for (int i = 0; i < 4; i++) {
                distances[i] = (float) Math.sqrt(
                        Math.pow(centerImg[0] - points[i][0], 2) +
                                Math.pow(centerImg[1] - points[i][1], 2)
                );
            }

            // Step 4: 排序距离，找到最近的两个点
            Integer[] sortedIndices = {0, 1, 2, 3};
            Arrays.sort(sortedIndices, (a, b) -> Float.compare(distances[a], distances[b]));

            // Step 5: 如果第一个顶点是前两个靠近中心点的点，调整角度并交换点
            if (sortedIndices[0] == 0 || sortedIndices[1] == 0) {
                angle += Math.PI;

                // 交换点 [0, 2] 和 [2, 0]
                float[] temp = points[0];
                points[0] = points[2];
                points[2] = temp;

                // 交换点 [1, 3] 和 [3, 1]
                temp = points[1];
                points[1] = points[3];
                points[3] = temp;

                // 更新 xyxyxyxy 数组
                xyxyxyxy = new float[]{
                        points[0][0], points[0][1],
                        points[1][0], points[1][1],
                        points[2][0], points[2][1],
                        points[3][0], points[3][1]
                };
            }

            // Step 6: 更新 Box 的角度和顶点信息
            box.setAngle(angle);
            box.setXyxyxyxy(xyxyxyxy);
        }

        // Step 7: 按角度对框进行排序
        boxes.sort((box1, box2) -> Float.compare(box1.getAngle(), box2.getAngle()));

        // Step 8: 调整框的顺序（如果框数量大于等于 3）
        if (boxes.size() >= 3) {
            float angleDiff31 = boxes.get(2).getAngle() - boxes.get(0).getAngle();

            if (angleDiff31 >= 1) {
                float angleDiff21 = boxes.get(1).getAngle() - boxes.get(0).getAngle();

                if (angleDiff21 < 1) {
                    // 如果 #2 和 #1 的角度差值 < 1，将 #3 变为新的 #1，后续序号依次减 2
                    List<Box> temp = new ArrayList<>(boxes.subList(2, boxes.size()));
                    temp.addAll(boxes.subList(0, 2));
                    boxes = temp;
                } else {
                    // 如果 #2 和 #1 的角度差值 >= 1，将 #2 变为新的 #1，后续序号依次减 2
                    List<Box> temp = new ArrayList<>(boxes.subList(1, boxes.size()));
                    temp.addAll(boxes.subList(0, 1));
                    boxes = temp;
                }
            }
        }

        return boxes;
    }


//    private static int[] sortIndicesByDistance(float[] distances) {
//        Integer[] indices = new Integer[distances.length];
//        for (int i = 0; i < distances.length; i++) {
//            indices[i] = i;
//        }
//        Arrays.sort(indices, (i1, i2) -> Float.compare(distances[i1], distances[i2]));
//        return Arrays.stream(indices).mapToInt(Integer::intValue).toArray();
//    }
}
