package com.cjstorrs.animaludderhealth;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

final class PassiveHealthRuntime {
    private static final float MAX_HEALTH = 1.0F;
    private static final float MAX_HUNGER_OR_THIRST_FOR_RECOVERY = 0.8F;
    private static final float MAX_HUTCH_DIRT_FOR_RECOVERY = 40.0F;
    private static final Float RECOVERY_DIVISOR = 3.0F;

    private static final ClassValue<DataAccess> DATA_ACCESS = new ClassValue<>() {
        @Override
        protected DataAccess computeValue(Class<?> type) {
            return new DataAccess(type);
        }
    };

    private static final ClassValue<AnimalAccess> ANIMAL_ACCESS = new ClassValue<>() {
        @Override
        protected AnimalAccess computeValue(Class<?> type) {
            return new AnimalAccess(type);
        }
    };

    private static final ClassValue<Method> HUTCH_DIRT_ACCESS = new ClassValue<>() {
        @Override
        protected Method computeValue(Class<?> type) {
            try {
                return type.getMethod("getHutchDirt");
            } catch (ReflectiveOperationException exception) {
                throw incompatible(type, exception);
            }
        }
    };

    private PassiveHealthRuntime() {
    }

    static void apply(Object data) {
        try {
            DataAccess dataAccess = DATA_ACCESS.get(data.getClass());
            Object animal = dataAccess.parent.get(data);
            AnimalAccess animalAccess = ANIMAL_ACCESS.get(animal.getClass());
            if ((Boolean) animalAccess.isWild.invoke(animal)) {
                return;
            }

            Object hutch = animalAccess.hutch.get(animal);
            if (hutch != null
                && number(HUTCH_DIRT_ACCESS.get(hutch.getClass()).invoke(hutch)) > MAX_HUTCH_DIRT_FOR_RECOVERY) {
                return;
            }

            if (number(animalAccess.getHunger.invoke(animal)) > MAX_HUNGER_OR_THIRST_FOR_RECOVERY
                || number(animalAccess.getThirst.invoke(animal)) > MAX_HUNGER_OR_THIRST_FOR_RECOVERY) {
                return;
            }

            float health = number(animalAccess.getHealth.invoke(animal));
            if (health < MAX_HEALTH) {
                float recovery = number(dataAccess.getHealthLoss.invoke(data, RECOVERY_DIVISOR));
                animalAccess.setHealth.invoke(animal, Math.min(MAX_HEALTH, health + recovery));
            }
        } catch (ReflectiveOperationException exception) {
            throw incompatible(data.getClass(), exception);
        }
    }

    private static float number(Object value) {
        return ((Number) value).floatValue();
    }

    private static IllegalStateException incompatible(Class<?> type, ReflectiveOperationException exception) {
        return new IllegalStateException(
            "[cjsAnimalUdderHealth] Incompatible B42.20 animal API on " + type.getName(),
            exception
        );
    }

    private static final class DataAccess {
        private final Field parent;
        private final Method getHealthLoss;

        private DataAccess(Class<?> type) {
            try {
                this.parent = type.getField("parent");
                this.getHealthLoss = type.getMethod("getHealthLoss", Float.class);
            } catch (ReflectiveOperationException exception) {
                throw incompatible(type, exception);
            }
        }
    }

    private static final class AnimalAccess {
        private final Field hutch;
        private final Method isWild;
        private final Method getHunger;
        private final Method getThirst;
        private final Method getHealth;
        private final Method setHealth;

        private AnimalAccess(Class<?> type) {
            try {
                this.hutch = type.getField("hutch");
                this.isWild = type.getMethod("isWild");
                this.getHunger = type.getMethod("getHunger");
                this.getThirst = type.getMethod("getThirst");
                this.getHealth = type.getMethod("getHealth");
                this.setHealth = type.getMethod("setHealth", float.class);
            } catch (ReflectiveOperationException exception) {
                throw incompatible(type, exception);
            }
        }
    }
}
