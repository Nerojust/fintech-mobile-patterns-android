# Contributing

Thanks for taking a look! This started as a portfolio/sample project, but issues and pull requests are genuinely welcome — whether that's a bug fix, a pattern you think is missing, or just a question.

## Before you start

For anything more than a small fix, please open an issue first to talk through the change — it saves everyone rework.

## Making a change

1. Fork the repo and create a branch off `main`.
2. Make your change, following the existing module structure (see the "How it's organized" section in the README) and code style.
3. Add or update tests for anything you change — see the "Running the tests" section in the README.
4. Before opening a PR, make sure everything passes locally:
   ```bash
   ./gradlew ktlintCheck detekt
   ./gradlew assembleDebug
   ./gradlew test
   ```
5. Open a pull request describing what changed and why.

## Code style

This project uses [ktlint](https://github.com/pinterest/ktlint) and [detekt](https://detekt.dev/) to keep things consistent — both run in CI, and `./gradlew ktlintCheck detekt` will catch anything before you push. `ktlintFormat` will auto-fix most formatting issues for you.

## Reporting bugs

Open an issue with steps to reproduce, what you expected, and what actually happened. A screenshot or logcat snippet helps a lot for anything UI- or crash-related.

## Code of Conduct

This project follows a [Code of Conduct](CODE_OF_CONDUCT.md) — please be kind.
