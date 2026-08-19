package com.cjstorrs.animaludderhealth;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class GameApiLinkageTest {
    private GameApiLinkageTest() {
    }

    public static void main(String[] args) throws Exception {
        Class<?> animalData = Class.forName("zombie.characters.animals.datas.AnimalData");
        Class<?> isoAnimal = Class.forName("zombie.characters.animals.IsoAnimal");
        Class<?> isoHutch = Class.forName("zombie.iso.objects.IsoHutch");
        Method updateHealth = animalData.getMethod("updateHealth");
        if (updateHealth.getReturnType() != void.class) {
            throw new AssertionError("AnimalData.updateHealth must return void");
        }

        if (animalData.getField("parent").getType() != isoAnimal) {
            throw new AssertionError("AnimalData.parent must remain a public IsoAnimal field");
        }

        Method getHealthLoss = animalData.getMethod("getHealthLoss", Float.class);
        if (getHealthLoss.getReturnType() != float.class) {
            throw new AssertionError("AnimalData.getHealthLoss must return float");
        }

        assertReturnType(isoAnimal, "isWild", boolean.class);
        assertReturnType(isoAnimal, "getHunger", float.class);
        assertReturnType(isoAnimal, "getThirst", float.class);
        assertReturnType(isoAnimal, "getHealth", float.class);
        if (isoAnimal.getMethod("setHealth", float.class).getReturnType() != void.class) {
            throw new AssertionError("IsoAnimal.setHealth must return void");
        }
        if (isoAnimal.getField("hutch").getType() != isoHutch) {
            throw new AssertionError("IsoAnimal.hutch must remain a public IsoHutch field");
        }
        assertReturnType(isoHutch, "getHutchDirt", float.class);

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

    private static void assertReturnType(Class<?> type, String methodName, Class<?> returnType) throws Exception {
        if (type.getMethod(methodName).getReturnType() != returnType) {
            throw new AssertionError(type.getName() + "." + methodName + " return type changed");
        }
    }
}
