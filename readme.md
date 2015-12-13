# KeePassJava2

A Java 7 API for databases compatible with the renowned KeePass password
safe for Windows. It can read and write KeePass 2.x and can read KeePass 1.x formats.

It is licensed under the Apache 2 License and is currently in a pre-release
stage of development, **you are especially warned to take care when using it**.

    The work is provided on an "AS IS" BASIS, WITHOUT
    WARRANTIES OR CONDITIONS OF ANY KIND, either express or
    implied, including, without limitation, any warranties
    or conditions of TITLE, NON-INFRINGEMENT, MERCHANTABILITY,
    or FITNESS FOR A PARTICULAR PURPOSE.

    You are solely responsible for determining the appropriateness
    of using or redistributing the Work and assume any risks
    associated with Your exercise of permissions under this License.

 (see [license](#license))

## Maven Coordinates
_not yet_
## Status
Alpha. Somewhat tested but awaiting contributions ...
## Java Version

It is written for Java 1.7.

## Quick Start

The class Javadoc on Interface classes Database, Group and Entry describe
how to use the methods of those classes to create and modifty entries.

### Load KDBX Database

        // get an input stream
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test123.kdbx");
        // password credentials
        Credentials credentials = new Credentials.Password("123");
        // open database
        Database database = new DomDatabaseWrapper(credentials, inputStream);

        // visit all groups and entries and list them to console
        database.visit(new Database.PrintVisitor());

### Save KDBX Database
        // create an empty database
        DomDatabaseWrapper database = new DomDatabaseWrapper();

        // add some groups and entries
        for (Integer g = 0; g < 5; g++){
            Group group = database.getRootGroup().addGroup(database.newGroup(g.toString()));
            for (int e = 0; e <= g; e++) {
                // entry factory is a local helper to populate an entry
                group.addEntry(entryFactory(database, g.toString(), e));
            }
        }

        // save to a file with password "123"
        FileOutputStream outputStream = new FileOutputStream("test.kdbx");
        database.save(new KdbxCredentials.Password("123".getBytes()), outputStream);


### Load KDB Database

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test.kdb");
        // password credentials
        Credentials credentials = new Credentials.Password("123");
        // load KdbDatabase
        Database database = KdbDatabase.load(credentials, inputStream);

        // visit all groups and entries and list them to console
        database.visit(new Database.PrintVisitor());


## Java Cryptography Extensions

Java Cryptography Extensions are required, as 256 bit encryption is used.

Download the approriate version:
- [Java 7](http://www.oracle.com/technetwork/java/javase/downloads/jce-7-download-432124.html)
- [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html)

and open the Zip file which contains a README describing where the jar files
need to go.

## Dependencies

Aside from the JRE the API depends on

- Bouncy Castle [license](https://www.bouncycastle.org/licence.html)
- Google Guava [license](https://github.com/google/guava/blob/master/COPYING).

It also depends on SLF4J and Junit for tests.

## Build

Maven 3.



##  <a name="license">License</a>

Copyright (c) 2015 Jo Rabin

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.