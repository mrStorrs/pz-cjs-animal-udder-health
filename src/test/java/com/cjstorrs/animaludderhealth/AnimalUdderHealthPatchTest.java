package com.cjstorrs.animaludderhealth;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import me.zed_0xff.zombie_buddy.Patch;
import me.zed_0xff.zombie_buddy.PatchEngine;

public final class AnimalUdderHealthPatchTest {
    private AnimalUdderHealthPatchTest() {
    }

    public static void main(String[] args) throws ReflectiveOperationException {
        verifyRuntimeVisibility();
        verifyRecoveryBehavior();
        assertTarget(AnimalUdderHealthPatches.PassiveHealthLoss.class, "updateHealth");
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

        Method passiveHealthEnter = AnimalUdderHealthPatches.PassiveHealthLoss.class.getDeclaredMethod(
            "enter",
            Object.class
        );
        Patch.OnEnter passiveHealthAdvice = passiveHealthEnter.getAnnotation(Patch.OnEnter.class);
        check(
            passiveHealthAdvice != null && passiveHealthAdvice.skipOn(),
            "passive-health patch must replace the vanilla health update"
        );
        Patch.This receiver = passiveHealthEnter.getParameters()[0].getAnnotation(Patch.This.class);
        check(receiver != null, "passive-health patch must receive AnimalData");

        List<Class<?>> patches = PatchEngine.collectPatches(
            "com.cjstorrs.animaludderhealth",
            AnimalUdderHealthPatchTest.class.getClassLoader()
        );
        check(
            patches.equals(
                List.of(
                    AnimalUdderHealthPatches.FullUdderHealthLoss.class,
                    AnimalUdderHealthPatches.GeriatricHealthLoss.class,
                    AnimalUdderHealthPatches.PassiveHealthLoss.class
                )
            ),
            "patch discovery changed"
        );
        System.out.println("AnimalUdderHealthPatchTest: PASS");
    }

    private static void verifyRecoveryBehavior() {
        FakeAnimal recoverable = new FakeAnimal(false, null, 0.2F, 0.2F, 0.5F);
        PassiveHealthRuntime.apply(new FakeData(recoverable));
        checkClose(0.6F, recoverable.health, "recoverable animal must heal at the vanilla rate");

        FakeAnimal starving = new FakeAnimal(false, null, 0.81F, 0.2F, 0.5F);
        PassiveHealthRuntime.apply(new FakeData(starving));
        checkClose(0.4F, starving.health, "starving animal must lose health at the vanilla rate");

        FakeAnimal dehydrated = new FakeAnimal(false, null, 0.2F, 0.81F, 0.5F);
        PassiveHealthRuntime.apply(new FakeData(dehydrated));
        checkClose(0.4F, dehydrated.health, "dehydrated animal must lose health at the vanilla rate");

        FakeAnimal starvingInDirtyHutch = new FakeAnimal(false, new FakeHutch(40.1F), 0.81F, 0.2F, 0.5F);
        PassiveHealthRuntime.apply(new FakeData(starvingInDirtyHutch));
        checkClose(0.4F, starvingInDirtyHutch.health, "dirty hutch must not prevent starvation damage");

        FakeAnimal dirtyHutch = new FakeAnimal(false, new FakeHutch(40.1F), 0.2F, 0.2F, 0.5F);
        PassiveHealthRuntime.apply(new FakeData(dirtyHutch));
        checkClose(0.5F, dirtyHutch.health, "dirty hutch must still prevent recovery");

        FakeAnimal wild = new FakeAnimal(true, null, 0.2F, 0.2F, 0.5F);
        PassiveHealthRuntime.apply(new FakeData(wild));
        checkClose(0.5F, wild.health, "wild animals must remain outside the patch scope");

        FakeAnimal nearlyHealthy = new FakeAnimal(false, null, 0.2F, 0.2F, 0.95F);
        PassiveHealthRuntime.apply(new FakeData(nearlyHealthy));
        checkClose(1.0F, nearlyHealthy.health, "recovery must not exceed full health");
    }

    private static void verifyRuntimeVisibility() throws ReflectiveOperationException {
        check(Modifier.isPublic(PassiveHealthRuntime.class.getModifiers()), "runtime class must be public for injected game code");
        Method apply = PassiveHealthRuntime.class.getMethod("apply", Object.class);
        check(Modifier.isPublic(apply.getModifiers()), "runtime apply method must be public for injected game code");
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

    private static void checkClose(float expected, float actual, String message) {
        check(Math.abs(expected - actual) < 0.0001F, message + ": expected " + expected + ", got " + actual);
    }

    public static final class FakeData {
        public final FakeAnimal parent;

        public FakeData(FakeAnimal parent) {
            this.parent = parent;
        }

        public float getHealthLoss(Float ignored) {
            return 0.1F;
        }
    }

    public static final class FakeAnimal {
        public final boolean wild;
        public final FakeHutch hutch;
        public final float hunger;
        public final float thirst;
        public float health;

        public FakeAnimal(boolean wild, FakeHutch hutch, float hunger, float thirst, float health) {
            this.wild = wild;
            this.hutch = hutch;
            this.hunger = hunger;
            this.thirst = thirst;
            this.health = health;
        }

        public boolean isWild() {
            return this.wild;
        }

        public float getHunger() {
            return this.hunger;
        }

        public float getThirst() {
            return this.thirst;
        }

        public float getHealth() {
            return this.health;
        }

        public void setHealth(float health) {
            this.health = health;
        }
    }

    public static final class FakeHutch {
        public final float dirt;

        public FakeHutch(float dirt) {
            this.dirt = dirt;
        }

        public float getHutchDirt() {
            return this.dirt;
        }
    }
}
