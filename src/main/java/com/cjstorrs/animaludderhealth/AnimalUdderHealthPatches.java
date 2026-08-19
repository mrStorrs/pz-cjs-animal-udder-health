package com.cjstorrs.animaludderhealth;

import me.zed_0xff.zombie_buddy.Patch;

public final class AnimalUdderHealthPatches {
    private AnimalUdderHealthPatches() {
    }

    @Patch(
        className = "zombie.characters.animals.datas.AnimalData",
        methodName = "updateHealth",
        strictMatch = true
    )
    public static final class PassiveHealthLoss {
        @Patch.OnEnter(skipOn = true)
        public static boolean enter(@Patch.This Object data) {
            PassiveHealthRuntime.apply(data);
            return true;
        }
    }

    @Patch(
        className = "zombie.characters.animals.datas.AnimalData",
        methodName = "reduceHealthDueToMilk",
        strictMatch = true
    )
    public static final class FullUdderHealthLoss {
        @Patch.OnExit
        public static void exit(@Patch.Return(readOnly = false) boolean losesHealth) {
            losesHealth = false;
        }
    }

    @Patch(
        className = "zombie.characters.animals.datas.AnimalData",
        methodName = "checkOld",
        strictMatch = true
    )
    public static final class GeriatricHealthLoss {
        @Patch.OnEnter(skipOn = true)
        public static boolean enter() {
            return true;
        }
    }
}
