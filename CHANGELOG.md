# Changelog

All notable changes in GCL will be documented in this file.

The format is based on [Keep a Changelog].
This project adheres to [Semantic Versioning].

## [UNRELEASED]

### Added

### Changed

- Version numbering now includes the Minecraft version as build metadata (e.g. `+26.2` for 
  Minecraft 26.2).
  Maven does not support `+`, so replace it with `-`.
  For example, `0.2.0+26.2` becomes `0.2.0-26.2`.
  Going forward, `HEAD` versions are numbered as `NEXT_SEMVER-COMMIT_HASH+MC_VERSION`, again
  swapping `+` with `-` when passing to Maven.

### Deprecated

### Removed

### Fixed

### Security

## [0.1.0] - 2026-07-21

### Added

- Config class.
- Config registry keyed by mods and Config type (`COMMON`, `SERVER`, `CLIENT`).
- Networked syncing for `COMMON` configurations.
- Mod configuration screen integrations.
  On NeoForge, this is done natively.
  On Fabric, this is done as an integration with [Mod Menu].

[0.1.0]: https://github.com/GiienaAstrella/gcl/releases/tag/v0.1.0
[Keep a Changelog]: https://keepachangelog.com/en/1.1.0/
[Mod Menu]: https://github.com/TerraformersMC/ModMenu
[Semantic Versioning]: https://semver.org/spec/v2.0.0.html
[UNRELEASED]: https://github.com/GiienaAstrella/gcl/compare/v0.1.0...HEAD