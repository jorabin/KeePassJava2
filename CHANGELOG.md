#Change Log

Trying to follow the suggestions at [Keep a Change Log](http://keepachangelog.com) and [Semantic Versioning](http://semver.org/spec/v2.0.0.html)

##[2.1.2] 2018-01-20

### Fixed

- [Issue #16] Fix for split package

##[2.1.1] 2017-01-27

### Fixed

- Simple implementation not reading DeletedObjects correctly per Nigel Rook
- Simple implementation CustomIcons optional per @Kin-k
- Simple implementation saving protected fields with Protected="true" instead of "True"

### Added

- Kdb Key File Support

##[2.1.0] 2016-10-29
### Added

- Added a module structure to allow selective building, for android etc.
- [Issue #5] Added a Jaxb implementation which is faster than the dom implementation
- [Issue #5] Added a Simple implementation since Jaxb not great for Android

### Changed

- artifactId became Camel Case KeePassJava2
- Documentation - beefed up the README

##[2.0.1] 2016-10-02
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


##[2.0.0] 2016-08-31

Starting at release 2.0.0 Since this is keepassjava2. Don't ask what happened to keepassjava1.

- Has readonly implementation for Keepass 1.x compatible files
- Has a DOM based implementation for Keepass 2.x KDBX files - being a DOM based implementation it's slow but means that saved entries are untouched by the program even if it doesn't understand them.



