# Change Log

Trying to follow the suggestions at [Keep a Change Log](http://keepachangelog.com) and [Semantic Versioning](http://semver.org/spec/v2.0.0.html)

## [3.2.0] Unreleased

### Changed

- `Database.supports(Feature)` says which optional features a database has (`DATABASE_NAME`, `AD_HOC_PROPERTIES`, `BINARY_PROPERTIES`, `MULTIPLE_BINARY_PROPERTIES`, `RECYCLE_BIN`, `PROPERTY_VALUE_STRATEGY`); the existing `supports…()` methods still work
- Consistent handling of unsupported features and missing values: getters no longer throw for an unsupported feature but answer as if nothing were there, setters throw `UnsupportedOperationException` except when setting "nothing", attribute getters never return null (database name and description are `""` when there is none, expiry times are never null), and the interfaces are annotated `@NotNull` or `@Nullable`
- KDB entries' attachment is available as a binary property (KDB allows one per entry); KDB property values can be read and set as `PropertyValue`s (unprotected)
- Removing a property or binary property that isn't there returns false rather than throwing, whether or not the database supports it
- KDBX minor versions: `KdbxHeader` has a minor version, so a database keeps its version when it is written (a 4.1 file was written back as 4.0). New KDBX 4 databases are written as 4.1 only if they use 4.1 features and otherwise as 4.0, as KeePass does (`KdbxHeader.isMinorVersionAutomatic()`); `KdbxHeaderOpts.V4_1_AES_ARGON_CHA_CHA` is added to choose 4.1
- When writing a KDBX version that can't hold some of the content (4.1 features in 4.0, 4.x features in 3.1), that content is left out of the file, kept in the database, and reported as a warning through `System.Logger`. `SerializableDatabase` has new default methods `getMinimumMinorVersion` and `setFormatVersion` for this
- `getStreamFormat()` is never null: a new KDBX or Basic database has the KDBX 4 format, and a KDB database has the new `KdbStreamFormat`, holding the `KdbHeader` it was read with. The new `setStreamFormat` sets the format `write(Credentials, OutputStream)` uses
- `getDatabase()` on KDB groups and entries is never null (the root group and new groups and entries had none)
- The `all` module's Javadoc shows the module structure diagram (it linked to a 2.x GitHub page rather than the image) and links to the v3 readme; the stray empty paragraph is gone

## [3.1.0] 2026-10-05

### Changed

- **Breaking:** requires Java 17 (3.0.0 required Java 11)
- [Issue 111] **Breaking:** `Entry` and `Group` times are `java.time.Instant` instead of `java.util.Date`; to upgrade, convert where a `Date` is needed, e.g. `Date.from(entry.getCreationTime())` and `entry.setExpiryTime(date.toInstant())`. `Helpers.toDate`/`fromDate` are now `toInstant`/`fromInstant`, and the KDBX model uses `Instant` throughout
- [Issue 109] `write` and `read` leave the caller's stream open: `Database.write`, `KdbxDatabase.read`, `KdbxDatabase.readXml`, `KdbDatabase.read`, and `write`/`read` on `StreamFormat`, `SerializableDatabase` and `BasicDatabaseSerializer`. Existing implementations of those interfaces get them as default methods
- [Issue 109] Deprecated `save` and `load`, which close the stream they are given, and the `saveNx`/`loadNx` methods. `readXml` throws `IOException` where `loadXml` threw `Exception`
- [Issue 109] `Util.listDatabase` no longer closes the output stream
- [Issue 109] Examples, tests and readme use `read`/`write` with try-with-resources
- Replace Jackson's deprecated `setSerializationInclusion` with `setDefaultPropertyInclusion`, which does the same
- Implementations of the deprecated `Entry.getPassword`/`setPassword` are marked deprecated too, the KDB reader no longer uses them, and they are tested
- The readme has upgrade notes, from 2.x to 3.0 (as in the 3.0.0 release note) and from 3.0 to 3.1

## [3.0.0] 2026-10-03

### Changed

- [Issue 81, 83] Remove junit dependency from main code and resolve test failure from UTF-8 encoding in test resources
- [Issue 87] Problem with incorrect serialization of CustomIcons in Jackson implementation
- [Issue 89] Incompatibility to KeePass due to missing empty element in autotype field in Jackson implementation
- [Issue 88, 90] Update dependencies to resolve security vulnerabilities, and to current versions of Jackson, Woodstox, Guava, Bouncy Castle and Commons Codec (as 2.2.5)
- [Issue 96] Load XML into the KDBX database: new `KdbxDatabase.load(StreamFormat, Credentials, InputStream)`, KeePass XML export `ProtectInMemory` values kept protected, and `loadXml()` no longer fails on `Protected` values
- [Issue 97] KDBX 4 attachments were written twice, in the inner header and in Meta/Binaries, and the inner header gained another copy of every attachment on each save of a loaded database
- [Issue 98] Meta/Binaries was written as `<Binaries><Binaries>` instead of `<Binaries><Binary>`, so KeePassXC dropped KDBX 3.1 attachments
- [Issue 104] With a default encoding other than UTF-8, the database XML was written in that encoding, so non-ASCII content could not be loaded back
- [Issue 99] Add creation, modification, access and expiry times to the `Group` interface, as on `Entry`
- [Issue 93] Automatic-Module-Names are set explicitly, following the v3 modules, and differ from 2.x: `org.linguafranca.pwdb.database`, `org.linguafranca.pwdb.kdb`, `org.linguafranca.pwdb.kdbx.io`, `org.linguafranca.pwdb.kdbx.database` and `org.linguafranca.pwdb.all`
- [Issue 68] Update Maven plugins; publish with central-publishing-maven-plugin, as OSSRH is shut down
- update to Java 11
- refactor API
  - remove complicated generics
  - fluent element builder
  - additional convenience methods
- refactor examples
- refactor modules
  - rename Jackson database to KDBX database
  - add experimental `basic` database support
  - remove Simple database
  - remove JAXB database
  - remove DOM database
  - remove `util` module; `test` module now holds the test code shared by the database implementations, and is not published
- refactor tests
  - restructure
  - shared test code is in the `test` module rather than a `database` test-jar, so it is not published with `database`
  - "upgrade" to JUnit 5


## [2.2.4] 2025-03-05

- [Issue 76, 78] Resolve incompatibility with KeePassXC (empty elements)
- [Issue 73] Trying to resolve dependency clashes for Woodstox etc

## [2.2.3] 2025-01-05

### Added

- implementation of property value storage interface PropertyValue to allow memory protection of sensitive values
- Default implementations of protected and unprotected storage

### Changed

- Jackson implementation supports this interface
- Other implementations throw exceptions appropriately as unsupported
- [Issue #70] Improved support for KDBX 4.1 format (Jackson only)
- [ISSUE #71] Module restructure to avoid test files, junit being pulled in unnecessarily

### Fixed

KDBX file format version 4.1 now supported (Issue-70) (in Jackson, not Simple)

## [2.2.2] 2024-09-06

### Added

- implementation of database using Jackson via @giusvale-dev
- enhancement of KeyFile support via @giusvale-dev

## Changed

- Updated dependencies (leave jaxb and guava as is)

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



