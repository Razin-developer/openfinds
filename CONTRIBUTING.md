# Contributing to OpenFind

Thanks for considering a contribution. OpenFind is a small, focused, privacy-first project — contributions that keep it that way are especially welcome.

## Ground rules

- **No servers, no accounts, no analytics.** Any contribution that introduces a network call to anything other than a peer device on the local network will be rejected.
- **No new permissions without justification.** If a feature needs a new Android permission, explain why in the PR description and add an onboarding rationale card for it.
- Follow the existing architecture: MVVM + Clean Architecture, repository pattern, Hilt for DI, one feature package per screen group under `feature/`.

## Getting set up

1. Install Android Studio (or just the command-line SDK) with `compileSdk 35` and `minSdk 26` available.
2. Clone the repo and open it in Android Studio, or build from the CLI:
   ```bash
   ./gradlew :app:assembleDebug
   ```
3. Run tests and static analysis before opening a PR:
   ```bash
   ./gradlew test lint ktlintCheck detekt
   ```
   Auto-fix formatting issues with `./gradlew ktlintFormat`.

## Commit style

This project uses [Conventional Commits](https://www.conventionalcommits.org/):

```
feat(pairing): add PIN pairing retry on handshake timeout
fix(network): handle NSD registration failure on routers without multicast
docs(readme): clarify release signing steps
```

Common types: `feat`, `fix`, `docs`, `refactor`, `test`, `chore`, `ci`.

## Branching

Branch from `main` using `feat/<short-name>`, `fix/<short-name>`, or `chore/<short-name>`. Keep PRs scoped to one change.

## Pull requests

- Fill in the PR template.
- Include screenshots/screen recordings for UI changes.
- Add or update tests for behavior changes.
- Make sure `./gradlew test lint` passes; CI will also run it.

## Reporting bugs / requesting features

Use the issue templates under `.github/ISSUE_TEMPLATE/`. For security issues, do **not** open a public issue — see [SECURITY.md](SECURITY.md).
