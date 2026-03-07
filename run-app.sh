set -a
source ./.env
set +a

./gradlew clean build
./gradlew bootRun