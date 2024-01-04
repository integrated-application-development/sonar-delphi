#!/bin/bash

set -e

if [ "$#" -eq 0 ]; then
  echo "usage: update-version.sh <version>"
  exit 1
fi

if ! [[ "$1" =~ ^[0-9]+\.[0-9]+\.[0-9]+(\-.+)?$ ]]; then
  echo 'invalid SemVer version: expected format "major.minor.patch[-label]"'
  exit 1
fi

pushd "$(dirname -- $0)" > /dev/null

if [ -n "$(git status --porcelain)" ]; then
  echo "dirty working tree: commit changes and try again"
  popd > /dev/null
  exit 1
fi

TAG="v$1"
if [ $(git tag -l "$TAG") ]; then
  echo "tag $TAG already exists"
  popd > /dev/null
  exit 1
fi

mvn versions:set -DnewVersion=$1 -DgenerateBackupPoms=false --non-recursive
mvn versions:set-property \
  -Dproperty=sonar.delphi.version -DnewVersion=$1 -DgenerateBackupPoms=false \
  -f docs/delphi-custom-rules-example/
mvn keepachangelog:release --non-recursive

git add .
git commit -m "Release v$1"
git tag $TAG HEAD

echo $TAG

popd > /dev/null