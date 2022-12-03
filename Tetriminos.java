package tetris;

import java.util.*;
import javafx.scene.paint.Paint;

public abstract class Tetriminos {
    public static Hashtable<String, Integer> indexes = new Hashtable() {{
        put("I", 0);
        put("O", 1);
        put("T", 2);
        put("S", 3);
        put("Z", 4);
        put("J", 5);
        put("L", 6);
    }};
    
    public static Hashtable<Integer, String> ids = new Hashtable() {{
        put(0, "I");
        put(1, "O");
        put(2, "T");
        put(3, "S");
        put(4, "Z");
        put(5, "J");
        put(6, "L");
    }};
    
    private static int[] rotationState = { 0, 0, 0, 0, 0, 0, 0 };
    private static ArrayList<int[][]> rotations = new ArrayList() {{            //  ArrayList holds the Tetriminos
        add(                                                                    //  Add I (0)
            new int[][] {                                                       //  Array lines hold the rotation states
                { 0, 1, 2, 3, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 1, 2, 3 },         //  Array columns hold each rotation's offsets 0-3: x-offsets, 4-7: y-offsets
                { 0, 1, 2, 3, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 1, 2, 3 }
            }
        );
        add(                                                                    //  Add O (1)
            new int[][] {                                                       //  Array lines hold the rotation states
                { 0, 1, 0, 1, 0, 0, 1, 1 }, { 0, 1, 0, 1, 0, 0, 1, 1 },         //  Array columns hold each rotation's offsets 0-3: x-offsets, 4-7: y-offsets
                { 0, 1, 0, 1, 0, 0, 1, 1 }, { 0, 1, 0, 1, 0, 0, 1, 1 }
            }
        );
        add(                                                                    //  Add T (2)
            new int[][] {                                                       //  Array lines hold the rotation states
                { 1, 0, 1, 2, 0, 1, 1, 1 }, { 1, 0, 1, 1, 0, 1, 1, 2 },         //  Array columns hold each rotation's offsets 0-3: x-offsets, 4-7: y-offsets
                { 0, 1, 2, 1, 0, 0, 0, 1 }, { 0, 0, 1, 0, 0, 1, 1, 2 }
            }
        );
        add(                                                                    //  Add S (3)
            new int[][] {                                                       //  Array lines hold the rotation states
                { 1, 2, 0, 1, 0, 0, 1, 1 }, { 0, 0, 1, 1, 0, 1, 1, 2 },         //  Array columns hold each rotation's offsets 0-3: x-offsets, 4-7: y-offsets
                { 1, 2, 0, 1, 0, 0, 1, 1 }, { 0, 0, 1, 1, 0, 1, 1, 2 }
            }
        );
        add(                                                                    //  Add Z (4)
            new int[][] {                                                       //  Array lines hold the rotation states
                { 0, 1, 1, 2, 0, 0, 1, 1 }, { 1, 0, 1, 0, 0, 1, 1, 2 },         //  Array columns hold each rotation's offsets 0-3: x-offsets, 4-7: y-offsets
                { 0, 1, 1, 2, 0, 0, 1, 1 }, { 1, 0, 1, 0, 0, 1, 1, 2 }
            }
        );
        add(                                                                    //  Add J (5)
            new int[][] {                                                       //  Array lines hold the rotation states
                { 0, 0, 1, 2, 0, 1, 1, 1 }, { 1, 1, 0, 1, 0, 1, 2, 2 },         //  Array columns hold each rotation's offsets 0-3: x-offsets, 4-7: y-offsets
                { 0, 1, 2, 2, 0, 0, 0, 1 }, { 0, 1, 0, 0, 0, 0, 1, 2 }
            }
        );
        add(                                                                    //  Add L (6)
            new int[][] {                                                       //  Array lines hold the rotation states
                { 2, 0, 1, 2, 0, 1, 1, 1 }, { 0, 1, 1, 1, 0, 0, 1, 2 },         //  Array columns hold each rotation's offsets 0-3: x-offsets, 4-7: y-offsets
                { 0, 1, 2, 0, 0, 0, 0, 1 }, { 0, 0, 0, 1, 0, 1, 2, 2 }
            }
        );
    }};
    
    private static int[][] rotationY = new int[4][4];
    
    public static int getWidth(String id) {
        return getWidth(indexes.get(id.toUpperCase()), null);
    }
    
    public static int getWidth(String id, String rDirection) {
        return getWidth(indexes.get(id.toUpperCase()), rDirection);
    }
    
    public static int getWidth(int index) {
        return getWidth(index, null);
    }
    
    public static int getWidth(int index, String rDirection) {
        int first, last;
        int state = getRotationIndex(index, rDirection);
        
        first = rotations.get(index)[state][0];
        last = rotations.get(index)[state][0];
        
        for (int i = 1; i < rotations.get(index)[state].length / 2; i++) {
            if (rotations.get(index)[state][i] < first)
                first = rotations.get(index)[state][i];
            
            if (rotations.get(index)[state][i] > last)
                last = rotations.get(index)[state][i];
        }
        
        System.out.println("\"" + ids.get(index) + "\" width = " + (last - first + 1));
        return last - first + 1;
    }
    
    public static int getHeight(String id) {
        return getWidth(indexes.get(id.toUpperCase()), null);
    }
    
    public static int getHeight(String id, String rDirection) {
        return getWidth(indexes.get(id.toUpperCase()), rDirection);
    }
    
    public static int getHeight(int index) {
        return getWidth(index, null);
    }
    
    public static int getHeight(int index, String rDirection) {
        int first, last;
        int state = getRotationIndex(index, rDirection);
        
        first = rotations.get(index)[state][4];
        last = rotations.get(index)[state][4];
        
        for (int i = 5; i < rotations.get(index)[state].length; i++) {
            if (rotations.get(index)[state][i] < first)
                first = rotations.get(index)[state][i];
            
            if (rotations.get(index)[state][i] > last)
                last = rotations.get(index)[state][i];
        }
        System.out.println("\"" + ids.get(index) + "\" height = " + (last - first + 1));
        return last - first + 1;
    }
    
    public static int[] getRotationX(String id){
        return getRotationX(indexes.get(id.toUpperCase()), null);
    }
    
    public static int[] getRotationX(String id, String rDirection){
        return getRotationX(indexes.get(id.toUpperCase()), rDirection);
    }
    
    public static int[] getRotationX(int index) {
        return getRotationX(index, null);
    }
    
    public static int[] getRotationX(int index, String rDirection) {
        int[] rotation = new int[4];
        int state = getRotationIndex(index, rDirection);
        
        for (int i = 0; i < rotation.length; i++)
            rotation[i] = rotations.get(index)[state][i];
        
        return rotation;
    }
    
    public static int[] getRotationY(String id){
        return getRotationY(indexes.get(id.toUpperCase()), null);
    }
    
    public static int[] getRotationY(String id, String rDirection){
        return getRotationY(indexes.get(id.toUpperCase()), rDirection);
    }
    
    public static int[] getRotationY(int index) {
        return getRotationY(index, null);
    }
    
    public static int[] getRotationY(int index, String rDirection) {
        int[] rotation = new int[4];
        int state = getRotationIndex(index, rDirection);
        
        for (int i = 0; i < rotation.length; i++)
            rotation[i] = rotations.get(index)[state][i + rotation.length];
        
        return rotation;
    }
    
    public static void setRotation(String id, String rDirection) {
        setRotation(indexes.get(id.toUpperCase()), rDirection);
    }
    
    public static void setRotation(int index, String rDirection) {
        if (rDirection == null)
            return;
        
        rotationState[index] = getRotationIndex(index, rDirection);
    }
    
    public static Paint getColor(String id) {
        return getColor(indexes.get(id.toUpperCase()));
    }
    
    public static Paint getColor(int tetrimino) {
        switch (tetrimino) {
            case 0:
                return Paint.valueOf("cyan");
            case 1:
                return Paint.valueOf("yellow");
            case 2:
                return Paint.valueOf("purple");
            case 3:
                return Paint.valueOf("green");
            case 4:
                return Paint.valueOf("red");
            case 5:
                return Paint.valueOf("blue");
            case 6:
                return Paint.valueOf("orange");
            default:
                return Paint.valueOf("black");
        }
    }
    
    private static int getRotationIndex(int index, String rDirection) {
        int state = rotationState[index];
        
        if (rDirection != null) {
            if (rDirection.toLowerCase().equals("left")) {
                if (state > 0)
                    state--;
                else
                    state = 3;
            } else if (rDirection.toLowerCase().equals("right")) {
                if (state < 3)
                    state++;
                else
                    state = 0;
            } else if (rDirection.toLowerCase().equals("origin")) {
                state = 0;
            }
        }

        return state;
    }
    
}
