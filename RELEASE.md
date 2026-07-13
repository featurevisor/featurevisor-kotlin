# Releasing

To cut a new release:

1. Decide on the next version number (plain numbering, no `v` prefix, e.g. `0.0.23`).
2. Create and publish a GitHub Release with a tag matching that version, e.g. `gh release create 0.0.23 --title 0.0.23 --generate-notes`.
3. Done — no manual build/publish step needed.

## How it works

Publishing the release triggers [`.github/workflows/publish.yml`](./.github/workflows/publish.yml), which runs `./gradlew -Pversion=<tag> publish` and pushes the artifact to GitHub Packages.

Separately, and independently of that workflow, [JitPack](https://jitpack.io/#featurevisor/featurevisor-kotlin) builds the same tag on demand the first time someone resolves that version — it only needs the tag to exist, not any CI to run.

## Distribution channels

- **JitPack** — what the [README's Installation section](./README.md#installation) tells consumers to use (`com.github.featurevisor:featurevisor-kotlin:<version>` via `https://jitpack.io`). This is also what the only known consumer (`android-dazn-app`) actually uses.
- **GitHub Packages** — published to `https://maven.pkg.github.com/featurevisor/featurevisor-kotlin` by CI on every release. Requires authenticating to GitHub Packages to consume.

## Notes

- The "Latest" release marked on GitHub is currently `0.0.11`, while `0.0.12`–`0.0.22` are marked as pre-releases.
