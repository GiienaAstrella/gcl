# Changelog

All notable changes in GCL will be documented in this file.

The format is based on [Keep a Changelog].
This project adheres to [Semantic Versioning].

## [UNRELEASED]

### Added

- `Config#getComment()`.
- `Config#push()`.
- `Config#define()`.
- `Config.Value#getRaw()`.
- `Config.Value#set()`.
- `Config.Value#save()`.
- `Config.Value#clearCache()`.
- `Config.Builder#push()`.
- `Config.Builder#pop()`.
- `Config.Builder#define()`.

### Changed

- Version numbering now includes the Minecraft version as build metadata (e.g. `+26.2` for 
  Minecraft 26.2).
  Maven does not support `+`, so replace it with `-`.
  For example, `0.2.0+26.2` becomes `0.2.0-26.2`.
  Going forward, `HEAD` versions are numbered as `NEXT_SEMVER-COMMIT_HASH+MC_VERSION`, again
  swapping `+` with `-` when passing to Maven.
- `IPlatformHelper` and implementations, network code, and loader init are now under the `impl`
  package.
  Consumers of GCL should not be interacting with these interface and classes, they are considered
  API internals.
  Package `impl` is now also annotated with `@ApiStatus.Internal`.
- `Config#load()` will automatically save on first creation and when falling back to defaults
  values.
  Manual call to `Config#save()` upon first-load is redundant but safe.
- `Config#comment()` is now a `Config.Builder` method, not a comment retrieval.
  See `Config#getComment()` to retrieve comments.
- `Config.Value` is no longer a `final` class.
  Its constructors are now `protected`.
- `Config.Value` now stores a default `Supplier<T>` rather than a default value.
- `Config.Builder#comment()` now appends to the pending comment.
  It no longer accepts `null` `String`.
- Config key translation key format is now `modid.config_type.key`.
- Config tooltip translation key format is now `modid.config_type.key.tooltip`.
  If no tooltip translation exist for a given key, its comments will be used for the tooltip.

### Deprecated

### Removed

- `Config#get()`.
- `Config#getOrDefault()`.
- `Config#set()`.
- `Config#section()`.
- `Config.Builder#section()`.
- `Config.Builder#close()`.
- `Config.Builder#set()`.

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