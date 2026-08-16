package com.cjstorrs.animaludderhealth;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class GameApiLinkageTest {
    private GameApiLinkageTest() {
    }

    public static void main(String[] args) throws Exception {
        Class<?> animalData = Class.forName("zombie.characters.animals.datas.AnimalData");
        Method reduceHealthDueToMilk = animalData.getMethod("reduceHealthDueToMilk");
        if (reduceHealthDueToMilk.getReturnType() != boolean.class) {
            throw new AssertionError("AnimalData.reduceHealthDueToMilk must return boolean");
        }

        Method checkOld = animalData.getDeclaredMethod("checkOld");
        if (checkOld.getReturnType() != void.class || !Modifier.isPrivate(checkOld.getModifiers())) {
            throw new AssertionError("AnimalData.checkOld must remain a private void method");
        }
        System.out.println("GameApiLinkageTest: PASS");
    }
}
