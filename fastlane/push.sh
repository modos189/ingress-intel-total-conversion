#!/bin/bash

if [[ "$TRAVIS_COMMIT_MESSAGE" != *"[Release]"* || "$BUILD_TYPE" == "release" ]]
then
  echo "bundle exec fastlane";
  bundle exec fastlane deploy_$BUILD_TYPE;
else
  echo "bundle else";
fi