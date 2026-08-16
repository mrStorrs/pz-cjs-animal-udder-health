package com.cjstorrs.animaludderhealth;

import me.zed_0xff.zombie_buddy.Patch;

public final class AnimalUdderHealthPatches {
    private AnimalUdderHealthPatches() {
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
}
