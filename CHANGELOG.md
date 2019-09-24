# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/).

## [Unreleased]

### [0.4.0] 2019-09-24
### Added
- This Changelog

### Changed
- Bumped mininum Leiningen version to 2.9.0
- Updated Amazonica dependencies to 0.3.143 and AWS Java SDK to 1.11.586
- Bumped development dependency for Cider to 0.21.0
- Moved spec instrumentation to test namespace(s)
- Changed the values returned by library functions. This INCOMPATIBLE CHANGE was driven by these goals:
  - Define specs for the generic protocol methods, not the particular S3 implementation details. And change the implementation to conform to those specifications.
  - Catch Amazonica exceptions and return error values instead. Those error values shoudl comply with the protocol specification, instead of returning implementation specific results.

## [0.3.0] - 2019-01-29
### Changed
- Updated Clojure version to 1.10.0

## [0.2.0] - 2019-01-28
- Initial commit (previous versions were not publicly released)

[UNRELEASED]:  https://github.com/magnetcoop/buddy-auth.jwt-oidc/compare/v0.4.0...HEAD
[0.4.0]: https://github.com/magnetcoop/buddy-auth.jwt-oidc/releases/tag/v0.4.0
[0.3.0]: https://github.com/magnetcoop/buddy-auth.jwt-oidc/releases/tag/v0.3.0
[0.2.0]: https://github.com/magnetcoop/buddy-auth.jwt-oidc/releases/tag/v0.2.0

