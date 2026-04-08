# Twitter Counter

An Android app for writing and posting tweets. It counts characters the way Twitter actually does — URLs are always 23 characters, emojis are 2 — so you know exactly where you stand before hitting post.

## Stack

Kotlin, Jetpack Compose, Hilt, Retrofit, Coroutines + StateFlow. Clean architecture with UI / Domain / Data layers.

## Getting started

You'll need API credentials from the [X Developer Portal](https://developer.twitter.com/). Add them to `local.properties`:

```properties
TWITTER_API_KEY=
TWITTER_API_SECRET=
TWITTER_ACCESS_TOKEN=
TWITTER_ACCESS_TOKEN_SECRET=
```

Then just build and run. Requires API 28+.

## Running tests

```bash
./gradlew test                    # unit tests
./gradlew connectedAndroidTest    # UI tests
```
