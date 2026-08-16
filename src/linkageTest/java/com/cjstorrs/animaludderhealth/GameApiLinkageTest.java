package com.cjstorrs.animaludderhealth;

import java.lang.reflect.Method;

public final class GameApiLinkageTest {
    private GameApiLinkageTest() {
    }

    public static void main(String[] args) throws Exception {
        Class<?> animalData = Class.forName("zombie.characters.animals.datas.AnimalData");
        Method reduceHealthDueToMilk = animalData.getMethod("reduceHealthDueToMilk");
        if (reduceHealthDueToMilk.getReturnType() != boolean.class) {
            throw new AssertionError("AnimalData.reduceHealthDueToMilk must return boolean");
        }
        System.out.println("GameApiLinkageTest: PASS");
    }
}
