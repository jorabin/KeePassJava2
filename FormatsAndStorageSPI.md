# Formats and Storage SPI (draft)

Design for [issue 110](https://github.com/jorabin/KeePassJava2/issues/110), planned for release 3.2.0.
The aim, from the issue:

> **Load any format, store it any way, and make everything available to users through the standard interfaces.**

The SPI is **experimental** in 3.2.0: it will change as more formats are added (LastPass and Bitwarden
exports, for example) and show what it needs. The user API (`Database`, `Group` and `Entry`) stays as it is,
apart from the additions and fixes described under [Unsupported features](#unsupported-features),
[Presence and absence](#presence-and-absence) and [Additions to the API](#additions-to-the-api). A separate,
new family of interfaces was considered and set aside for now (see [Considered](#considered-a-new-family-of-interfaces)).

This is a draft for discussion. Comments are welcome anywhere in the text, as a quoted line starting with
your initials, for example:

> **JR:** I'm not sure about this.

## Facts from the code that shape the design

1. **The KDBX model already is the API.** `KdbxEntry` and `KdbxGroup` are the Jackson model classes, and also
   implement `Entry` and `Group`. They already hold every KDBX field: AutoType, colours, tags, CustomData,
   history and so on. So the Jackson model is already a complete KDBX storage. What is missing is a way to get
   the data in and out in a format-neutral form.
2. **Entries are already extensible.** `Entry` properties are name and value pairs, which may be protected,
   and KDBX `<String>` elements are exactly that. So every ad hoc KDBX property is already part of the API,
   and needs no extension.
3. **KDB is small.** Beyond what `Entry` and `Group` expose, it has group `flags`, the group `level` (which
   is structure, expressed by nesting), an `ExtData` block that is currently ignored, and one attachment per
   entry (a description and data), which the API can't reach at present.

## How the data divides

Every KDBX field falls into one of three groups, by who it matters to.

### 1. Content any password manager could have: core

These belong in the API, so they are core in the SPI. A database that can't hold one says so through
`supports(Feature)` (see [Unsupported features](#unsupported-features)).

| Concept | KDBX source | Feature |
|---|---|---|
| UUID, title or name, standard icon, the five times, children in order | as now | always |
| Entry properties, in order, protected or not | Entry `String` | always (ad hoc names: `AD_HOC_PROPERTIES`) |
| Attachments, in order | Entry `Binary` | `BINARY_PROPERTIES`, `MULTIPLE_BINARY_PROPERTIES` |
| Database name, description, recycle bin | Meta | recycle bin: `RECYCLE_BIN` |
| Tags | Entry and Group `Tags` | `TAGS` |
| Group notes | Group `Notes` | `GROUP_NOTES` |
| Custom icons (images) | `CustomIconUUID` and `Meta/CustomIcons` | `CUSTOM_ICONS` |
| History | Entry `History` | `HISTORY` |
| Deleted objects | `Root/DeletedObjects` | `DELETED_OBJECTS` |
| Protected by default | `Meta/MemoryProtection` | through [property descriptors](#properties-and-descriptors) |

Times are `java.time.Instant`, as in the API from 3.1.0.

**History** is a list of entry snapshots inside the entry (core and extensions), each with the parent's UUID
and no history of its own. **Deleted objects** are a UUID and a deletion time; they are useful to any storage
that synchronises.

### 2. KeePass application state: one transparent extension per element

State used by the KeePass desktop applications (KeePass for Windows, and partly KeePassXC). Nothing else
needs to understand it. It is read and written unchanged, and has no API methods beyond
`getExtension(...)`.

- **`KeePassEntryState`:** AutoType (enabled, data transfer obfuscation, default sequence, associations),
  OverrideURL, QualityCheck, foreground and background colour, UsageCount, LocationChanged,
  PreviousParentGroup.
- **`KeePassGroupState`:** IsExpanded, DefaultAutoTypeSequence, EnableAutoType, EnableSearching,
  LastTopVisibleEntry, PreviousParentGroup, UsageCount, LocationChanged.
- **`KeePassDatabaseState`:** Generator, SettingsChanged and the other `…Changed` times, DefaultUserName,
  Color, MaintenanceHistoryDays, HistoryMaxItems, HistoryMaxSize, MasterKeyChanged, MasterKeyChangeRec,
  MasterKeyChangeForce, MasterKeyChangeForceOnce, EntryTemplatesGroup, LastSelectedGroup,
  LastTopVisibleGroup.

Because nothing outside the KDBX format interprets this state, the format can also offer a codec that turns
it into an opaque value (an XML fragment), for storage that can't keep Java objects, such as SQL.

The KDB equivalent is **`KdbGroupState`**, holding the group flags.

### 3. Plugin data: generic and transparent

**`CustomData`** on Meta, Group and Entry: key and value items, with an optional modification time, owned
by plugins (for example KeePassXC's browser integration). It is kept as an ordered list, read and written
unchanged, and neither interpreted nor merged.

## Unsupported features

Different databases support different parts of the API, as KDB and KDBX already show. At present the code
signals this in several ways: some getters throw `UnsupportedOperationException` (KDB's
`getBinaryPropertyNames`), others return a neutral value (KDB's `isRecycleBinEnabled` returns `false`), and
`AbstractDatabase.newEntry(Entry)` catches exceptions around every copy. The standard:

1. **One way to ask.** `enum Feature` and `boolean Database.supports(Feature)`. The four existing
   `supports…()` methods stay, and delegate to it.
2. **Getters don't throw for "not supported".** They return the value that means "none": an empty list,
   `null` where the API already uses `null` for absent, `false`, or `Optional.empty()` in new methods.
   So code that reads, such as copying, visiting or `writeTo(sink)`, works with any database without
   catching exceptions, and "not supported" reads the same as "not present". The exception is a getter with
   no truthful neutral value, such as `getPropertyValueStrategy()`, which keeps throwing and is covered by
   `supports(PROPERTY_VALUE_STRATEGY)`.
3. **Setters throw `UnsupportedOperationException`** with a message, except when setting the neutral value
   (as `enableRecycleBin(false)` does now). Each such setter documents
   `@throws UnsupportedOperationException if !supports(X)`.
4. **The SPI never throws for unsupported core data.** A storage or format writer that can't hold something
   (history in KDB, say) drops it and reports it, so a conversion can't fail part way. Extensions are always
   kept.

A consequence for KDB: it supports exactly one attachment per entry, named by its description. So
`supports(BINARY_PROPERTIES)` is true and `supports(MULTIPLE_BINARY_PROPERTIES)` is false, and the
attachment becomes reachable through the API.

## Presence and absence

Four different things can look alike: a database that can't hold something (*not supported*), an item that
has no value (*not present*), a value that is *empty*, and Java's `null`. Each is answered in one place only:

| Question | Answered by | What the caller sees |
|---|---|---|
| Not supported | `supports(Feature)` only | getters answer as for "not present"; setters throw `UnsupportedOperationException` |
| Not present | the getter | lookups: `null`; attributes: `""`, an empty list, or `Optional.empty()` in new methods |
| Present and empty | the getter | `""`, a zero-length `byte[]`: only where the format really distinguishes it from "not present" |
| `null` | | only the existing lookups' way of saying "not found"; never returned by attributes or new methods |

**The rule:**

- **Lookups** (by key, UUID or position) may return `null`, meaning "not found", like `Map.get`:
  `findEntry`, `findGroup`, `getProperty`, `getPropertyValue`, `getBinaryProperty`, `getParent`,
  `getRecycleBin`. They are annotated `@Nullable`.
- **Attributes** (getters without an argument) never return `null`: text with no value is `""`, collections
  are empty, a single optional value in a new method is an `Optional`, and a three-state value is an enum.
  They are annotated `@NotNull`.

**Where "not present" and "empty" differ:** properties (an entry can have `Foo` with an empty value, or no
`Foo`; required properties are never "not present", at worst `""`), and attachments (a zero-length attachment
is still an attachment). For tags, group notes and other free text and collections KeePass makes no
difference, so they are normalised to `""` or an empty list. Fidelity means the same meaning, not the same
bytes.

**When `null` is a value, it gets a type.** In KDBX a group's `EnableSearching` and `EnableAutoType` are true,
false or absent, and absent means "inherit from the parent". That becomes
`enum Inherit { INHERIT, TRUE, FALSE }`, not a nullable `Boolean`.

**Setters:** setting an optional value to "none" makes it not present; a required value can't be set to
`null` (`IllegalArgumentException`, as `setExpiryTime(null)` already does); `setProperty(name, null)` throws
`IllegalArgumentException`, and `removeProperty` makes a property not present.

**In the SPI records:** collections are never `null`; a single optional field is `null` only for "not
present", documented on the record, with an `Optional` accessor; "not supported" never appears, since a
source just has nothing to send.

**Changes to the existing interfaces.** No signatures change; a few return values do, and are listed in the
CHANGELOG:

| Method | Now | After |
|---|---|---|
| `Database.getName()`, `getDescription()` | `null` if not supported (KDB), or `null` if the KDBX `Meta` element is missing | `""` when there is none |
| `Entry` and `Group` `getExpiryTime()` | `null` in Basic until set | never `null`; Basic starts with the creation time, as KDBX does |
| `getProperty`, `getPropertyValue`, `getBinaryProperty` | javadoc mixes "not known" and "not supported" | `null` means not present; "not supported" is `supports(...)` |
| `getRecycleBin()` | `null` if none or not supported | unchanged, javadoc in these terms |
| interface methods returning objects | 6 annotated | all annotated `@Nullable` or `@NotNull` |

## Properties and descriptors

What exists now:

- **Required** properties are implicit: the five `STANDARD_PROPERTY_NAMES`, which every implementation
  supports and `removeProperty` refuses to remove.
- **Ad hoc** properties are a single switch, `supportsNonStandardPropertyNames()`.
- **Protection** by default is separate: `shouldProtect(name)` and `listShouldProtect()`.
- **Discovery** is `entry.getPropertyNames()`, for one entry, and the static list.

That can't describe formats whose required properties differ, such as a card entry that needs `Number` and
`Expiry`, or what a database expects before any entry exists. So, property descriptors:

```java
public record PropertyDescriptor(String name, boolean required, boolean protectedByDefault) {}

// Database: the properties this database knows about;
// supports(Feature.AD_HOC_PROPERTIES) says whether other names are allowed
List<PropertyDescriptor> getPropertyDescriptors();

// Entry: by default the database's, so that a format with entry types can describe each entry
default List<PropertyDescriptor> getPropertyDescriptors() { return getDatabase().getPropertyDescriptors(); }
```

- **KDBX:** the five standard properties are required, `Password` is protected by default (from
  `MemoryProtection`), and ad hoc properties are allowed.
- **KDB:** the same five are required, and ad hoc properties are not allowed.
- **A typed format:** descriptors depend on the entry's type. KDBX's `EntryTemplatesGroup` could later be
  presented this way.

`STANDARD_PROPERTY_NAMES`, `shouldProtect` and `supportsNonStandardPropertyNames` keep working, defined
from the descriptors, and `removeProperty` refuses any required property.

In the SPI, a sink that receives an entry without a required property adds it, empty. A storage or format
that can't take ad hoc properties drops and reports them. (KeePass 1 put them in the notes; that could be
an option for KDB.)

## KDBX 3.1 and 4

**One set of KDBX extensions covers both.** Later versions only add optional elements. Following KeePass's
descriptions of [KDBX 4](https://keepass.info/help/kb/kdbx_4.html) and
[KDBX 4.1](https://keepass.info/help/kb/kdbx_4.1.html):

- 4.0 adds CustomData on groups and entries, and `SettingsChanged` in Meta;
- 4.1 adds `Tags` and `PreviousParentGroup` on groups, `QualityCheck` and `PreviousParentGroup` on entries,
  `Name` and `LastModificationTime` on custom icons, and `LastModificationTime` on CustomData items.

(Which version added `MasterKeyChangeForceOnce` isn't documented, so it is always written.) As KeePass does,
a KDBX 4 database is written as 4.1 only if it uses 4.1 features, unless a version is set, and writing a
version that can't hold some of the content leaves it out of the file, keeps it in the database, and reports
it. `Meta/Binaries`, `HeaderHash` and the KDBX 4 inner header are format details and don't appear in the SPI.

## Jackson

**The Jackson model stays, and `KdbxDatabase` becomes a storage that is both a source and a sink.**

- `KdbxDatabase.read` and `write` keep their direct path: no conversion, no change in speed.
- `KdbxDatabase.writeTo(Sink)` maps its fields to core records and extensions, and a `KdbxDatabase` sink
  does the reverse, building the model from events.
- The KDBX format reader and writer for *other* storages go through the Jackson model: decrypt, parse into
  `KdbxDatabase`, `writeTo(sink)`; and the reverse for writing. That gives full fidelity at once with no new
  XML code. A streaming (StAX) reader could replace it later without changing the SPI.

## SPI sketch

The SPI goes in package `org.linguafranca.pwdb.spi` in the `database` module. The KDBX extensions go in the
KDBX format module (`kdbx-io`), so that storage such as Basic can use them without depending on Jackson.
The KDB extension goes in `kdb`.

```java
// org.linguafranca.pwdb.spi
public interface Sink {
    void database(DatabaseData database);          // always first
    void startGroup(GroupData group);
    void entry(EntryData entry);
    void endGroup();
    void deletedObject(UUID uuid, Instant deletionTime);
    void end();
}

public interface Source {
    void writeTo(Sink sink) throws IOException;
}

/** Marker for format-specific data; storage keeps extensions it doesn't understand */
public interface Extension {}

public record Extensions(Map<Class<? extends Extension>, Extension> map) {
    public <T extends Extension> Optional<T> get(Class<T> type) { ... }
    public Extensions with(Extension extension) { ... }   // immutable
    public static Extensions none() { ... }
}

public record Times(Instant creation, Instant lastModification, Instant lastAccess,
                    Instant expiry, boolean expires) {}

public record Property(String name, PropertyValue value) {}
public record BinaryProperty(String name, byte[] value) {}
public record CustomIcon(UUID uuid, byte[] image, String name, Instant lastModification) {}

public record EntryData(UUID uuid, int icon, UUID customIcon, List<Property> properties,
                        List<BinaryProperty> binaries, List<String> tags, Times times,
                        List<EntryData> history, Extensions extensions) { /* + builder */ }

public record GroupData(UUID uuid, String name, String notes, int icon, UUID customIcon,
                        List<String> tags, Times times, Extensions extensions) { /* + builder */ }

public record DatabaseData(String name, String description, boolean recycleBinEnabled,
                           UUID recycleBinUuid, List<PropertyDescriptor> propertyDescriptors,
                           List<CustomIcon> customIcons, Extensions extensions) { /* + builder */ }
```

```java
// in kdbx-io
public record KeePassEntryState(AutoType autoType, String overrideUrl, Boolean qualityCheck,
        String foregroundColor, String backgroundColor, long usageCount, Instant locationChanged,
        UUID previousParentGroup) implements Extension {}
// and KeePassGroupState, KeePassDatabaseState, with value records AutoType and Association

public record CustomData(List<Item> items) implements Extension {
    public record Item(String key, String value, Instant lastModification) {}
}

// in kdb
public record KdbGroupState(int flags) implements Extension {}
```

### Reaching extensions through the API

`Entry`, `Group` and `Database` get default methods, so nothing KeePass-specific is added to the API:

```java
default <T extends Extension> Optional<T> getExtension(Class<T> type) { return Optional.empty(); }
default void setExtension(Extension extension) { throw new UnsupportedOperationException(); }
```

`KdbxEntry` and the others implement them by mapping to and from their fields.

### Additions to the API

Group 1 above means these additions, each covered by a `Feature`:

- `Entry` and `Group`: `getTags()` and `setTags(List<String>)`; custom icon get and set.
- `Group`: `getNotes()` and `setNotes(String)`.
- `Entry`: `getHistory()`, read only.
- `Database`: `supports(Feature)`, `getPropertyDescriptors()`, custom icons, and `getDeletedObjects()`.

### Invalid input

The library's readers guarantee the schema's rules. A `CheckingSink` decorator checks structure for anyone
who wants it: `startGroup` and `endGroup` balance, `database` comes first, custom icon references resolve,
and UUIDs are unique except in history.

### Basic as the second storage

`BasicDatabase` becomes the in-memory storage that doesn't use Jackson: filled through a sink, replayed as a
source, keeping extensions unchanged. That gives the issue's main example: read KDBX 4.1 into Basic, use it
through `Database`, `Group` and `Entry`, and write it back with nothing lost. Basic's own `<database>` XML
stays as it is.

## Considered: a new family of interfaces

Keeping `Database`, `Group` and `Entry` exactly as in 3.0 (with `Date`), and adding a new family of
interfaces built on the rules above, would make 3.1 purely additive and give the new rules a clean start. It
was set aside for now because one class can't implement both families (`Date getCreationTime()` and
`Instant getCreationTime()` clash), so the new API would need separate view or adapter objects, and two APIs
to document and test until 4.0. The priority is the SPI; the existing interfaces get the smaller fixes above.

## Testing

As in the issue:

- A purpose-built KDBX 4.1 XML file using every element and attribute, validated against
  `XSD/KDBX.4.1.reichl.xsd`.
- A `RecordingSink`, to check the reader field by field.
- Round trips: Jackson to Basic to Jackson; KDBX 4 to 3.1 to 4 (with the 4.x-only fields expected to drop);
  KDB to KDBX.
- Tests that each database's `supports(Feature)` answers match what its getters and setters do, and that no
  attribute getter returns `null`.

## Decisions

Agreed on 2026-10-03:

1. **Colours** are KeePass application state (group 2).
2. **CustomData** stays an extension.
3. **Property descriptors** have `name`, `required` and `protectedByDefault`, until a format needs a kind
   (typed items in LastPass or Bitwarden exports may be the first).
4. **Dropped data** is reported through an optional `Consumer<String>` callback on writers and sinks.
5. **`Instant`** replaces `Date` in `Entry` and `Group` (released in 3.1.0).
6. **Order:** first `supports(Feature)` and the presence and absence fixes to the existing interfaces; then the
   experimental SPI, property descriptors, the group 1 API additions, the KDBX and KDB extensions,
   `KdbxDatabase` as source and sink, Basic as storage, the KDB reader as a source, and the tests. SQL
   storage, the opaque codec and a streaming reader later.
7. **The opaque codec** for keeping extensions in non-memory storage is deferred.
8. **Ad hoc properties in KDB** are dropped and reported; appending them to the notes could be an option later.
9. **`setExtension`** replaces the extension of that type; it throws `UnsupportedOperationException` where
   extensions aren't kept.
10. **History in the API** is read only; storage adds history when an entry changes, if the database's policy
    says so.

**Releases** (decided 2026-10-04): 3.1.0 is Java 17, `read`/`write` and `Instant`. `supports(Feature)`, the
presence and absence fixes, the KDBX version handling and the SPI are in 3.2.0, possibly with prototype releases
for comment.
