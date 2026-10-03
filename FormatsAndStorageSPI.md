# Formats and Storage SPI (draft)

Design for [issue 110](https://github.com/jorabin/KeePassJava2/issues/110), planned for 3.1.0.
The aim, from the issue:

> **Load any format, store it any way, and make everything available to users through the standard interfaces.**

This is a draft for discussion. Comments are welcome anywhere in the text, as a quoted line starting with
your initials, for example:

> **JR:** I'm not sure about this.

## Two facts from the code that shape the design

1. **The KDBX model already is the API.** `KdbxEntry` and `KdbxGroup` are the Jackson model classes, and also
   implement `Entry` and `Group`. They already hold every KDBX field: AutoType, colours, tags, CustomData,
   history and so on. So the Jackson model is already a complete KDBX storage. What is missing is a way to get
   the data in and out in a format-neutral form.
2. **KDB is small.** Beyond what `Entry` and `Group` expose, it has group `flags`, the group `level` (which is
   structure, expressed by nesting), an `ExtData` block that is currently ignored, and one attachment per
   entry (a description and data), which maps onto a binary property.

## Decisions proposed for the open questions

### Core and extensions

**Rule: the core is what `Database`, `Group` and `Entry` expose. Everything else is an extension.**

This gives a simple test for every field, and adding a field to the API later moves it into the core as a
deliberate step.

| | Core | KDBX extension | KDB extension |
|---|---|---|---|
| **Entry** | UUID, standard icon index, properties in order (as `PropertyValue`, so protection is kept), binary properties in order, the five times, history | custom icon UUID, colours, OverrideURL, QualityCheck, Tags, PreviousParentGroup, UsageCount, LocationChanged, AutoType, CustomData | none |
| **Group** | UUID, name, standard icon index, the five times, children in order | Notes, custom icon UUID, IsExpanded, DefaultAutoTypeSequence, EnableAutoType, EnableSearching, LastTopVisibleEntry, PreviousParentGroup, Tags, CustomData, UsageCount, LocationChanged | flags |
| **Database** | name, description, recycle bin enabled, recycle bin group UUID, protected property names | the rest of `Meta`: Generator, the `…Changed` times, DefaultUserName, Color, master key fields, CustomIcons, EntryTemplatesGroup, history limits, LastSelectedGroup, LastTopVisibleGroup, CustomData, MemoryProtection details | none |

Times are core: KDBX, KDB and Basic all have creation, last modification, last access, expiry and the
expires flag. They are `java.time.Instant`, as in the API from 3.1.0.

Group notes are an extension under this rule, because `Group` has no notes. They are a candidate for the API.

### History

**History is core: a list of `EntryData` inside `EntryData`.** Each item is a full snapshot (core and
extensions), with the parent entry's UUID and no history of its own. Keeping versions of an entry isn't
specific to KeePass: a SQL storage would want it, and so might an API method such as `entry.getHistory()`.
Formats without history, such as KDB, drop it when writing.

**Deleted objects** are treated the same way: a core `deletedObject(uuid, time)` event at the end of the
document, since tracking deletions is useful to any storage that synchronises. Formats without them drop
them.

### KDBX 3.1 and 4

**One KDBX extension covers both.** Comparing the XSDs, 4.x only adds optional elements: `SettingsChanged`
and `MasterKeyChangeForceOnce` in Meta, `PreviousParentGroup` and `QualityCheck` on entries, `Name` and
`LastModificationTime` on custom icons, `LastModificationTime` on CustomData items, and group `Tags`. The 3.1
writer leaves them out, as the Jackson serializer does now. `Meta/Binaries`, `HeaderHash` and the KDBX 4
inner header are format details and don't appear in the SPI.

### Jackson

**The Jackson model stays, and `KdbxDatabase` becomes a storage that is both a source and a sink.**

- `KdbxDatabase.read` and `write` keep their direct path: no conversion, no change in speed.
- `KdbxDatabase.writeTo(Sink)` maps its fields to core records and KDBX extensions, and a `KdbxDatabase`
  sink does the reverse, building the model from events.
- The KDBX format reader and writer for *other* storages go through the Jackson model: decrypt, parse into
  `KdbxDatabase`, `writeTo(sink)`; and the reverse for writing. That gives full fidelity at once with no new
  XML code. A streaming (StAX) reader could replace it later without changing the SPI.

## SPI sketch

The SPI goes in package `org.linguafranca.pwdb.spi` in the `database` module. The KDBX extension records
go in the KDBX format module (`kdbx-io`), so that storage such as Basic can use them without depending on
Jackson. The KDB extension goes in `kdb`.

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

public record EntryData(UUID uuid, int icon, List<Property> properties,
                        List<BinaryProperty> binaries, Times times,
                        List<EntryData> history, Extensions extensions) { /* + builder */ }

public record GroupData(UUID uuid, String name, int icon, Times times,
                        Extensions extensions) { /* + builder */ }

public record DatabaseData(String name, String description, boolean recycleBinEnabled,
                           UUID recycleBinUuid, List<String> protectedProperties,
                           Extensions extensions) { /* + builder */ }
```

```java
// in kdbx-io
public record KdbxEntryExtension(UUID customIcon, String foregroundColor, String backgroundColor,
        String overrideUrl, Boolean qualityCheck, String tags, UUID previousParentGroup,
        long usageCount, Instant locationChanged, AutoType autoType, CustomData customData)
        implements Extension { ... }
// and KdbxGroupExtension, KdbxDatabaseExtension (Meta, CustomIcons ...), with value records
// AutoType, Association, CustomData (items with an optional LastModificationTime) and CustomIcon
```

### Reaching extensions through the API

`Entry`, `Group` and `Database` get default methods, so nothing KeePass-specific is added to the API:

```java
default <T extends Extension> Optional<T> getExtension(Class<T> type) { return Optional.empty(); }
default void setExtension(Extension extension) { throw new UnsupportedOperationException(); }
```

`KdbxEntry` and the others implement them by mapping to and from their fields.

### Invalid input

The library's readers guarantee the schema's rules. A `CheckingSink` decorator checks structure for anyone
who wants it: `startGroup` and `endGroup` balance, `database` comes first, custom icon references resolve,
and UUIDs are unique except in history.

### Basic as the second storage

`BasicDatabase` becomes the in-memory storage that doesn't use Jackson: filled through a sink, replayed as a
source, keeping extensions unchanged. That gives the issue's main example: read KDBX 4.1 into Basic, use it
through `Database`, `Group` and `Entry`, and write it back with nothing lost. Basic's own `<database>` XML
stays as it is.

## Testing

As in the issue:

- A purpose-built KDBX 4.1 XML file using every element and attribute, validated against
  `XSD/KDBX.4.1.reichl.xsd`.
- A `RecordingSink`, to check the reader field by field.
- Round trips: Jackson to Basic to Jackson; KDBX 4 to 3.1 to 4 (with the 4.x-only fields expected to drop);
  KDB to KDBX.

## Open points

1. **The core rule.** Is "core = what the API exposes" right, given that it puts group notes in the extension?
2. **History and deleted objects in the core,** rather than as KDBX extensions?
3. **Persisting extensions.** A SQL storage can't keep Java objects. Should each format provide a codec for
   its extensions (KDBX extensions as XML fragments, say), or is that the storage's problem? Proposed:
   defer, since in-memory storage doesn't need it.
4. **Reporting what is dropped** when writing to a format that can't hold an extension or history: silently,
   logged, or through a listener? Proposed: an optional `Consumer<String>` callback on writers.
5. **Scope for 3.1.0.** Proposed: the SPI, the KDBX extensions, `KdbxDatabase` as source and sink, Basic as
   storage, the KDB reader as a source, and the tests above. SQL storage and a streaming reader later.
