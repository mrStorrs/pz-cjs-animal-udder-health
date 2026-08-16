# CJS Animal Udder Health

Project Zomboid B42.20 ZombieBuddy patch that prevents a full udder from
damaging an animal or blocking its ordinary health recovery. It leaves hunger,
thirst, age, combat, vehicle, predator, and genetic health consequences intact.

Enable `ZombieBuddy` followed by `cjsAnimalUdderHealth`. Existing animals and
saves are compatible: no saved data is changed.

Run `./build.sh` after a game or ZombieBuddy update. The build validates the
exact B42.20 `AnimalData.reduceHealthDueToMilk()` method before packaging.
