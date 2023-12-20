package com.arasholding.jetizzkuryeapp.helpers;

import java.util.Random;

public class Generator {
    public static int RandomGenerator(int min,int max){
        Random generator = new Random();

        int i = generator.nextInt(max-min+1)+min;
        return i;
    }
}
