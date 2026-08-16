package com.cjstorrs.animaludderhealth;

import java.lang.reflect.Method;
import java.util.List;
import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.PatchEngine;

public final class AnimalUdderHealthPatchTest {
    private AnimalUdderHealthPatchTest() {
    }

    public static void main(String[] args) throws ReflectiveOperationException {
        Patch patch = AnimalUdderHealthPatches.FullUdderHealthLoss.class.getAnnotation(Patch.class);
        check(patch != null, "full-udder patch annotation is required");
        check(
            "zombie.characters.animals.datas.AnimalData".equals(patch.className()),
            "patch must target AnimalData"
        );
        check("reduceHealthDueToMilk".equals(patch.methodName()), "patch must target milk health loss");
        check(patch.strictMatch(), "patch must use strict overload matching");

        Method exit = AnimalUdderHealthPatches.FullUdderHealthLoss.class.getDeclaredMethod(
            "exit",
            boolean.class
        );
        Patch.Return result = exit.getParameters()[0].getAnnotation(Patch.Return.class);
        check(result != null && !result.readOnly(), "patch must replace the vanilla result");

        List<Class<?>> patches = PatchEngine.collectPatches(
            "com.cjstorrs.animaludderhealth",
            AnimalUdderHealthPatchTest.class.getClassLoader()
        );
        check(patches.equals(List.of(AnimalUdderHealthPatches.FullUdderHealthLoss.class)), "patch discovery changed");
        System.out.println("AnimalUdderHealthPatchTest: PASS");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
