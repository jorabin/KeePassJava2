# KeePassJava2

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.linguafranca.pwdb/KeePassJava2-parent/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.linguafranca.pwdb/KeePassJava2-parent)
[![javadoc](https://javadoc.io/badge2/org.linguafranca.pwdb/KeePassJava2/javadoc.svg)](https://javadoc.io/doc/org.linguafranca.pwdb/KeePassJava2)

![alt text](https://badgen.net/badge/Branch/master/yellow?icon=github) ![alt text](https://badgen.net/badge/Build/2.2.2/blue?icon=github) [![CircleCI](https://dl.circleci.com/status-badge/img/gh/jorabin/KeePassJava2/tree/master.svg?style=shield)](https://dl.circleci.com/status-badge/redirect/gh/jorabin/KeePassJava2/tree/master)

![alt text](https://badgen.net/badge/Branch/develop/yellow?icon=github) ![alt text](https://badgen.net/badge/Build/2.2.3-SNAPSHOT/blue?icon=github)[![CircleCI](https://dl.circleci.com/status-badge/img/gh/jorabin/KeePassJava2/tree/develop.svg?style=shield)](https://dl.circleci.com/status-badge/redirect/gh/jorabin/KeePassJava2/tree/develop)

![alt text](https://badgen.net/badge/Branch/v3/yellow?icon=github) ![alt text](https://badgen.net/badge/Build/2.3-SNAPSHOT/blue?icon=github)[![CircleCI](https://dl.circleci.com/status-badge/img/gh/jorabin/KeePassJava2/tree/develop.svg?style=shield)](https://dl.circleci.com/status-badge/redirect/gh/jorabin/KeePassJava2/tree/v3)


A Java 8 API for databases compatible with the renowned [KeePass](http://keepass.info) password
safe for Windows. This is a "headless" implementation - if you want something with a UI
then [KeePassXC](https://keepassxc.org/) and [KeePassDX](https://www.keepassdx.com/) could
be just the things for you.

Features to date:

- Read and write KeePass 2.x format (KDBX file formats V3 and V4)
- Keepass 2.x Password and Keyfile Credentials
- Read KeePass 1.x format (KDB format, Rijndael only)
- *No* requirement for JCE Policy Files
- Android compatible
- Interfaces for Database, Group and Entry allow compatible addition of other formats
- Pluggable memory storage and protection strategy

It is licensed under the Apache 2 License and is currently usable.

    The work is provided on an "AS IS" BASIS, WITHOUT
    WARRANTIES OR CONDITIONS OF ANY KIND, either express or
    implied, including, without limitation, any warranties
    or conditions of TITLE, NON-INFRINGEMENT, MERCHANTABILITY,
    or FITNESS FOR A PARTICULAR PURPOSE.

    You are solely responsible for determining the appropriateness
    of using or redistributing the Work and assume any risks
    associated with Your exercise of permissions under this License.

 (see [license](#license))

## Current Status

This version is 2.3-SNAPSHOT

The current released code is version 2.2.2 - released to Maven September 2024. This is on the main branch. See [Build from Source](#build-from-source)

Key updates relative to 2.1:
- Java 8 (dependencies no longer support Java 7)
- File format version 4 support - with Argon2
- Inclusion of Jackson based KDBX support with a view to removing SimpleXML, JAXB and JAXB support
- Updated keyfile support
- Updated dependencies

See the [changelog](CHANGELOG.md) for more details.

## Maven Coordinates

### Release

The composite POM for the last release (2.2.2), Java 8 compatible, is

        <groupId>org.linguafranca.pwdb</groupId>
        <artifactId>KeePassJava2</artifactId>
        <version>2.2.2</version>

at Maven Central. Note that the artifactId has become Camel Case from release 2.1.x onwards.

### Snapshot

Snapshot builds are erratically available at [Sonatype](https://oss.sonatype.org/content/repositories/snapshots/org/linguafranca/pwdb/) e.g.:

        <groupId>org.linguafranca.pwdb</groupId>
        <artifactId>KeePassJava2</artifactId>
        <version>2.2.3-SNAPSHOT</version>
 
with appropriate `<repositories>` entry, like:

      <repositories>
         <repository>
           <id>oss.sonatype.org-snapshot</id>
           <url>https://oss.sonatype.org/content/repositories/snapshots</url>
           <releases>
             <enabled>false</enabled>
           </releases>
           <snapshots>
             <enabled>true</enabled>
           </snapshots>
         </repository>
       </repositories>
 
 There are also separate POMs for the various modules. The module structure is illustrated below
 under [Build from Source](#build-from-source).

## Java Version

From release 2.2 it requires Java 1.8. Earlier versions require Java 1.7.

## Quick Start

Create credentials and an input stream for the password vault in question:

      KdbxCreds creds = new KdbxCreds("123".getBytes());
      InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test1.kdbx");
      
then load the database:

      Database database = KdbxDatabase.load(credentials, inputStream)

> In the past there were a number of different database implementations, at present there
are two, one for KDBX (`KdbxDatabase` - previously called `JacksonDatabase`, because it uses Jackson for XML serialization)
and one to support the KeePass V2 KDB format (`KdbDatabase`).

### Storing Passwords

There are numerous well-understood problems
with storing passwords as Strings in Java. See [this discussion](./PropertyValueProtection.md) about the
KeePassJava2 approach to storing passwords, which is implemented in this release.

### Discussion

Password databases are modelled as a three layer abstraction. 

A *Database* is a collection of records whose physical representation needs only to be capable of rendering as a stream. *Entries* hold the information of value in the database and *Groups* allow the structuring of entries into collections, just like a folder structure. 

The Database has a root group and by following subgroups of the root group the tree structure of the database can be navigated. Entries belong to groups. Entries can be moved between groups and groups can also be moved between groups. However, entries and groups created in one database cannot be moved to another database without being converted: 

    database.newEntry(entryToCopy);
    database.newGroup(groupToCopy);

The class Javadoc on Interface classes
[Database](http://javadoc.io/page/org.linguafranca.pwdb/database/latest/org/linguafranca/pwdb/Database.html), 
[Group](http://javadoc.io/page/org.linguafranca.pwdb/database/latest/org/linguafranca/pwdb/Group.html) and 
[Entry](http://javadoc.io/page/org.linguafranca.pwdb/database/latest/org/linguafranca/pwdb/Entry.html) describe
how to use the methods of those classes to create and modify entries. These classes
provide the basis of all implementations of the various database formats,
initially KDB, KDBX 3.1 and KDBX 4 (KeePass 2) file formats, subsequently, potentially, others.

The class [QuickStart.java](example/src/main/java/org/linguafranca/pwdb/kdbx/QuickStart.java) provides some
illustrations of operations using the Database, Group and Entry interfaces.

### KeePassJava2 and KeePass

This project is so named by kind permission of Dominik Reichl the author of KeePass. There
is no formal connection with that project.

It has always been the intention to support other specific password database implementations.
Hence, the creation of abstract Database interfaces rather than following the KeePass model
exactly.

KeePass is in effect defined by the code that Dominik writes to create and maintain the project and
[KDBX File Format Specification](https://keepass.info/help/kb/kdbx.html) describes the file format. There 
is also a discussion of the [differences between KDBX version 3.1 and version 4](https://keepass.info/help/kb/kdbx_4.html).
Additionally, there is a discussion of the [enhancements in KDBX 4.1](https://keepass.info/help/kb/kdbx_4.1.html), as well
as a discussion of [Key Files](https://keepass.info/help/base/keys.html#keyfiles). 

Massive credit also to the folks over at [KeePassXC](https://keepassxc.org/) who wrote some 
[documentation](https://github.com/keepassxreboot/keepassxc-specs) about their understanding of various format things. Also, this is a 
useful [discussion/investigation](https://github.com/scubajorgen/KeepassDecrypt) of the KDBX format.

For the sake of
clarification and my own satisfaction I have written about my understanding of 
KeePass formats in the following locations:

1. The Javadoc header to [KdbxSerializer](http://javadoc.io/page/org.linguafranca.pwdb/KeePassJava2-kdbx/latest/org/linguafranca/pwdb/kdbx/stream_3_1/KdbxSerializer.html) describes KDBX stream formatting.
2. The XSD Schema [KDBX.4.xsd](KDBX.4.xsd) documents my understanding of the Keepass XML, and also my 
   lack of understanding, in parts. While preparing release 2.2.3 I found [this XSD](https://keepass.info/help/download/KDBX_XML.xsd) at the
   KeePass site. I have not (so far) attempted to reconcile my documentation with it.
3. The following graphic illustrates KDBX 3.1 and 4 file formats:


[![KDBX Formats](KdbxDiagram.svg "KDBX Formats")](KdbxDiagram.svg)

## Database Implementations

KeePass - or more specifically its file format KDBX - is an XML based format, so one of the main tasks
is serializing and deserializing XML. Over time (KeePassJava2 was originally released in 2014) approaches
to Java and XML have been a bit mysterious. However, Jackson has now been chosen as the 
underlying framework for implementation of KeePassJava2. From 2.2.3 a single KDBX implementation is available.

There are several other database implementations which will be maintained for bug-fix purposes
only, with a view to being withdrawn, since they perform badly and/or depend on obsolete technology.

## Dependencies

Aside from the JRE, at release 2.3, the API depends on:

- [Google Guava](https://github.com/google/guava/wiki) ([Apache 2 license](https://github.com/google/guava/blob/master/COPYING)).
- [Apache Commons Codec](https://commons.apache.org/proper/commons-codec/) ([Apache 2 license](http://www.apache.org/licenses/LICENSE-2.0)).
- [Bouncy Castle](https://github.com/bcgit/bc-java/blob/master/LICENSE.html) ([MIT License](https://github.com/bcgit/bc-java/blob/master/LICENSE.html)).
- [Faster XML Jackson](https://github.com/FasterXML/jackson)

It also depends on SLF4J and Junit 4 for tests.

## Build from Source

Included POM is for Maven 3.

### Module Structure

There are rather a lot of modules, this is in order to allow loading of minimal necessary functionality. The module dependencies are illustrated below.

[![Module Structure](ModuleStructure.svg "Module Structure")](./ModuleStructure.svg)

Each module corresponds to a Maven artifact. The GroupId is `org.linguafranca.pwdb`. The version id is as noted [above](#maven-coordinates).

<table>
<thead>
<tr><th>Module</th><th>ArtifactId</th><th>JavaDoc<th>Description</th></tr>
</thead>
<tbody>

<tr><td><a href="database">database</a></td><td>database</td>
<td>
<a href="http://www.javadoc.io/doc/org.linguafranca.pwdb/database"><img src="http://www.javadoc.io/badge/org.linguafranca.pwdb/database.svg" alt="Javadocs"></a>
</td>
<td>Base definition of the Database APIs.</td></tr>
<tr><td><a href="">example</a></td><td>example</td>
<td><a href="http://www.javadoc.io/doc/org.linguafranca.pwdb/example"><img src="http://www.javadoc.io/badge/org.linguafranca.pwdb/example.svg" alt="Javadocs"></a></td>
<td>Worked examples of loading, saving, splicing etc. using the APIs</td></tr>

<tr><td><a href="test">test</a></td><td>test</td>
<td><a href="http://www.javadoc.io/doc/org.linguafranca.pwdb/test"><img src="http://www.javadoc.io/badge/org.linguafranca.pwdb/test.svg" alt="Javadocs"></a></td>
<td>Shared tests to assess the viability of the implementation.</td></tr>

<tr><td><a href="all">all</a></td><td><strong>KeePassJava2</strong></td>
<td>(no JavaDoc)</td>
<td>This is the main KeePassJava2 Maven dependency. Provides a route to all artifacts (other than test and examples) via transitive dependency.</td></tr>

<tr><td><a href="kdb">kdb</a></td><td>KeePassJava2-kdb</td>
<td><a href="http://www.javadoc.io/doc/org.linguafranca.pwdb/KeePassJava2-kdb"><img src="http://www.javadoc.io/badge/org.linguafranca.pwdb/KeePassJava2-kdb.svg" alt="Javadocs"></a></td>
<td>An implementation of the Database APIs supporting KeePass KDB format.</td></tr>

<tr><td><a href="kdbx-io">kdbx-io</a></td><td>KeePassJava2-kdbx-io</td>
<td><a href="http://www.javadoc.io/doc/org.linguafranca.pwdb/KeePassJava2-kdbx"><img src="http://www.javadoc.io/badge/org.linguafranca.pwdb/KeePassJava2-kdbx-io.svg" alt="Javadocs"></a></td>
<td>Provides support for KDBX streaming and security.</td></tr>


<tr><td><a href="kdbx-database">kdbx-database</a></td><td>KeePassJava2-kdbx-database</td>
<td><a href="http://www.javadoc.io/doc/org.linguafranca.pwdb/KeePassJava2-jackson"><img src="http://www.javadoc.io/badge/org.linguafranca.pwdb/KeePassJava2-kdbx-database.svg" alt="Javadocs"></a></td>
<td>Provides support for KDBX data access and memory protection.</td></tr>

</tbody>
</table>

### Gradle

If you prefer Gradle the automatic conversion `gradle init` has been known to convert the POM successfully, however you will 
need to add something like [gradle-source-sets.txt](jaxb/gradle-source-sets.txt) to the `build.gradle` for the JAXB module, so that the generated sources
 get compiled correctly.

## Change Log

In [this file](./CHANGELOG.md).

## Acknowledgements

Many thanks to Pavel Ivanov [@ivanovpv](https://github.com/ivanovpv) for 
his help with Android and Gradle compatibility issues back in the very early days.

Thanks to Giuseppe Valente [@giusvale-dev](https://github.com/giusvale-dev) for 
his contribution of the Jackson module and enhancements to KeyFile support.

Thanks to other contributors and raisers of issues.

##  License

Copyright (c) 2024 Jo Rabin

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.