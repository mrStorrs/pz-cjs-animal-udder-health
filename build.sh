#!/usr/bin/env bash
set -euo pipefail

root_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
zombie_buddy_jar=${ZOMBIE_BUDDY_JAR:-/home/cjstorrs/games/Project Zomboid Linux 42.20.0/game/projectzomboid/ZombieBuddy.jar}
project_zomboid_jar=${PROJECT_ZOMBOID_JAR:-/home/cjstorrs/games/Project Zomboid Linux 42.20.0/game/projectzomboid/projectzomboid.jar}
project_zomboid_java=${PROJECT_ZOMBOID_JAVA:-$(dirname -- "$project_zomboid_jar")/jre64/bin/java}
build_dir="$root_dir/.build"
main_classes="$build_dir/classes/main"
test_classes="$build_dir/classes/test"
linkage_test_classes="$build_dir/classes/linkage-test"
output_jar="$root_dir/42.20/media/java/CJSAnimalUdderHealth.jar"

for path in "$zombie_buddy_jar" "$project_zomboid_jar"; do
    if [ ! -f "$path" ]; then
        echo "Required JAR not found: $path" >&2
        exit 1
    fi
done

if [ ! -x "$project_zomboid_java" ]; then
    echo "Project Zomboid Java runtime not found: $project_zomboid_java" >&2
    exit 1
fi

rm -rf "$build_dir"
mkdir -p "$main_classes" "$test_classes" "$linkage_test_classes" "$(dirname -- "$output_jar")"

mapfile -d '' main_sources < <(find "$root_dir/src/main/java" -type f -name '*.java' -print0 | sort -z)
javac --release 17 -cp "$zombie_buddy_jar" -d "$main_classes" "${main_sources[@]}"
jar --create --file "$output_jar" --date=2000-01-01T00:00:00Z -C "$main_classes" .

if jar --list --file "$output_jar" | grep -Eq '^(zombie|org/joml)/'; then
    echo "Game API classes leaked into $output_jar" >&2
    exit 1
fi

mapfile -d '' test_sources < <(find "$root_dir/src/test/java" -type f -name '*.java' -print0 | sort -z)
javac --release 17 -cp "$main_classes:$zombie_buddy_jar" -d "$test_classes" "${test_sources[@]}"
java -ea -cp "$test_classes:$main_classes:$zombie_buddy_jar" com.cjstorrs.animaludderhealth.AnimalUdderHealthPatchTest

mapfile -d '' linkage_test_sources < <(find "$root_dir/src/linkageTest/java" -type f -name '*.java' -print0 | sort -z)
javac --release 17 -d "$linkage_test_classes" "${linkage_test_sources[@]}"
"$project_zomboid_java" -ea -cp "$linkage_test_classes:$main_classes:$zombie_buddy_jar:$project_zomboid_jar" com.cjstorrs.animaludderhealth.GameApiLinkageTest

echo "Built: $output_jar"
