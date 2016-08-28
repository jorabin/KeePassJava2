# KeePassJava2

A Java 7 API for databases compatible with the renowned [KeePass](http://keepass.info) password
safe for Windows.

Features to date:

- Read and write KeePass 2.x format
- Keepass 2.x Password and Keyfile Credentials
- Read KeePass 1.x format (Rijndael only)
- *No* requirement for JCE Policy Files
- Interfaces for Database, Group and Entry allow compatible addition of other formats

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
        Credentials credentials = new KdbxCredentials.Password("123".getBytes());
        // open database
        Database database = DomDatabaseWrapper.load(credentials, inputStream);

        // visit all groups and entries and list them to console
        database.visit(new Vistor.Print());

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
        Credentials credentials = new KdbCredentials.Password("123".getBytes());
        // load KdbDatabase
        Database database = KdbDatabase.load(credentials, inputStream);

        // visit all groups and entries and list them to console
        database.visit(new Vistor.Print());


## Dependencies

Aside from the JRE the API depends on

- Google Guava [license](https://github.com/google/guava/blob/master/COPYING).
- Spongy Castle which is a repackaging for Android of Bouncy Castle [license](https://www.bouncycastle.org/licence.html)

It also depends on SLF4J and Junit for tests.

## Build

Maven 3.



##  <a name="license">License</a>

Copyright (c) 2016 Jo Rabin

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.