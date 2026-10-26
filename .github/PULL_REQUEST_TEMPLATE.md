## Summary

<!-- What does this PR change, and why? -->

Closes #

## Type of change

- [ ] Feature
- [ ] Bug fix
- [ ] Refactor (no behavior change)
- [ ] Docs / CI / build

## Screenshots / recordings

<!-- Required for UI changes. Phone portrait; add a ≥600dp screenshot if the layout changes. -->

| Before | After |
|---|---|
|  |  |

## How to test

1.
2.

## Checklist

- [ ] `./gradlew ktlintCheck lintDebug testDebugUnitTest assembleDebug` passes locally
- [ ] Every user-facing text is in `strings*.xml` (no hardcoded strings, including content descriptions)
- [ ] Uses design tokens (`MaterialTheme.colorScheme` / `typography`, `Spacing`, `Dimens`), no raw colors or `sp`
- [ ] New screens/components have `@Preview` (incl. Loading/Error/Empty states where relevant)
- [ ] Checked with large font size and on a ≥600dp window if layout changed
- [ ] Docs updated if architecture, conventions, or API contracts changed
