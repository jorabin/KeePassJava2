# Change Log

Trying to follow the suggestions at [Keep a Change Log](http://keepachangelog.com) and [Semantic Versioning](http://semver.org/spec/v2.0.0.html)

## [2.2.3-SNAPSHOT]

### Changed

- Deprecated method Entry.getPassword(): This means that the method getPassword() in the Entry class has been marked as deprecated. Deprecated methods are no longer recommended for use and might be removed in future versions of the code.

### Added

- byte[] getPasswordAsBytes() to AbstractEntry class: This means that a new method getPasswordAsBytes() has been added to the AbstractEntry class. This method likely returns the password in the form of a byte array instead of a plain string.

- byte[] getPropertyAsBytes(String name) to the Entry interface: This indicates that a new method getPropertyAsBytes(String name) has been added to the Entry interface. This method likely returns a specific property as a byte array.

- Added test cases to ensure the byte password is used correctly: This means that new test cases have been added to the codebase to ensure that the handling of byte passwords is functioning correctly. These tests will help verify that the newly introduced methods are working as expected.

## [2.2.2-SNAPSHOT]

### Added

- implementation of database using Jackson

## [2.2.1] 2023-08-21

### Added

- support for V4 files
  - numerous updates to accommodate this

### Changed

- Minimum version supported is Java 8
- updated documentation of various sorts
- kdbx multithreaded fix
- fixes for a number of issues
- tidy up in various places
- update dependencies
  - spongy castle replaced by bouncy castle
  - simplexml replaced by simple-xml-safe
  - update versions throughout
  - list JAXB as external dependency for Java 11
- remove http module
- don't deploy examples to Maven

## [2.1.4] 2018-02-03

### Added

- removeProperty for custom property via @AugustNagro
- AutomaticModuleNames for Java 9 via @AugustNagro
- expires functionality on Entry via @AugustNagro
- database reports support for optional features

## [2.1.3] 2018-01-21

### Fixed

- Travis test failures relating to update to Openjdk 7 and a bug in Simple serialization

### Added

- Various functionality for searching databases
- Recycle bin functionality
- An experimental implementation of [keepasshttp](https://github.com/pfn/keepasshttp/) see
[the readme](./readme.md) for warnings, limitations, etc. about this.

## [2.1.2] 2018-01-20

### Fixed

- [Issue #16] Fix for split package

## [2.1.1] 2017-01-27

### Fixed

- Simple implementation not reading DeletedObjects correctly per Nigel Rook
- Simple implementation CustomIcons optional per @Kin-k
- Simple implementation saving protected fields with Protected="true" instead of "True"

### Added

- Kdb Key File Support

## [2.1.0] 2016-10-29
### Added

- Added a module structure to allow selective building, for android etc.
- [Issue #5] Added a Jaxb implementation which is faster than the dom implementation
- [Issue #5] Added a Simple implementation since Jaxb not great for Android

### Changed

- artifactId became Camel Case KeePassJava2
- Documentation - beefed up the README

## [2.0.1] 2016-10-02
### Added

- this changelog file
- documentation about file formats
    - a diagram explaining format 3.1 vs 4
    - an xsd for 3.1
- [Issue #4] support binary properties

### Fixed

- [Issue #6] did not understand difference between no password and empty password

### Changed

- Kdbx credentials simplified, old version deprecated
    - KdbxCredentials Deprecated
    - KdbxCreds Introduced


## [2.0.0] 2016-08-31

Starting at release 2.0.0 Since this is keepassjava2. Don't ask what happened to keepassjava1.

- Has readonly implementation for Keepass 1.x compatible files
- Has a DOM based implementation for Keepass 2.x KDBX files - being a DOM based implementation it's slow but means that saved entries are untouched by the program even if it doesn't understand them.



