# CJS Animal Passive Health

Project Zomboid B42.20 ZombieBuddy patch that stops domestic animals from
losing health to old age or overfull udders. Fed and watered animals recover at
the vanilla health rate when they are not in a dirty hutch. Hunger and thirst
still cause vanilla health loss and can kill an animal.

Animal/player attacks, vehicle impacts, trailer crashes, wild-animal wound
consequences, and genetic death at birth are unchanged. The mod therefore
prevents passive farm-health deaths without making animals invulnerable.

Enable `ZombieBuddy` followed by `cjsAnimalUdderHealth`. Existing animals and
saves are compatible: no saved data is changed.

Run `./build.sh` after a game or ZombieBuddy update. The build validates the
exact B42.20 `AnimalData` methods and fields used by the patch before
packaging. ZombieBuddy approvals are JAR-hash-specific, so approve the rebuilt
JAR at the next game startup.
