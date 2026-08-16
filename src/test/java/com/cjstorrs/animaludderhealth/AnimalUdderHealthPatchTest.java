package com.cjstorrs.animaludderhealth;

import java.lang.reflect.Method;
import java.util.List;
import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.PatchEngine;

public final class AnimalUdderHealthPatchTest {
    private AnimalUdderHealthPatchTest() {
    }

    public static void main(String[] args) throws ReflectiveOperationException {
        assertTarget(
            AnimalUdderHealthPatches.FullUdderHealthLoss.class,
            "reduceHealthDueToMilk"
        );
        assertTarget(AnimalUdderHealthPatches.GeriatricHealthLoss.class, "checkOld");

        Method exit = AnimalUdderHealthPatches.FullUdderHealthLoss.class.getDeclaredMethod(
            "exit",
            boolean.class
        );
        Patch.Return result = exit.getParameters()[0].getAnnotation(Patch.Return.class);
        check(result != null && !result.readOnly(), "patch must replace the vanilla result");

        Method enter = AnimalUdderHealthPatches.GeriatricHealthLoss.class.getDeclaredMethod("enter");
        Patch.OnEnter onEnter = enter.getAnnotation(Patch.OnEnter.class);
        check(onEnter != null && onEnter.skipOn(), "geriatric patch must skip vanilla health loss");

        List<Class<?>> patches = PatchEngine.collectPatches(
            "com.cjstorrs.animaludderhealth",
            AnimalUdderHealthPatchTest.class.getClassLoader()
        );
        check(
            patches.equals(
                List.of(
                    AnimalUdderHealthPatches.FullUdderHealthLoss.class,
                    AnimalUdderHealthPatches.GeriatricHealthLoss.class
                )
            ),
            "patch discovery changed"
        );
        System.out.println("AnimalUdderHealthPatchTest: PASS");
    }

    private static void assertTarget(Class<?> patchClass, String methodName) {
        Patch patch = patchClass.getAnnotation(Patch.class);
        check(patch != null, patchClass.getName() + " patch annotation is required");
        check(
            "zombie.characters.animals.datas.AnimalData".equals(patch.className()),
            patchClass.getName() + " must target AnimalData"
        );
        check(methodName.equals(patch.methodName()), patchClass.getName() + " target changed");
        check(patch.strictMatch(), patchClass.getName() + " must use strict overload matching");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
