/*
 * Copyright 2015 Jo Rabin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.linguafranca.pwdb.kdb;

import com.google.common.io.LittleEndianDataInputStream;
import org.linguafranca.pwdb.Credentials;
import org.linguafranca.pwdb.Group;
import org.linguafranca.pwdb.security.Encryption;

import java.io.DataInput;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.*;

/**
 * This class provides support for reading a KDB stream and constructing an in memory database.
 * <p/>
 * A KDB stream consists of:
 * <ol>
 * <li>16 bits of file signature</li>
 * <li>An unencrypted header containing encryption and other information such as the count of groups and entries</li>
 * <li>In an Encrypted form:</li>
 * <ol>
 * <li>Repeated serialised form of all Groups</li>
 * <li>Repeated serialised form of all Entries</li>
 * </ol>
 * </ol>
 *
 * @author jo
 */
public class KdbSerializer {

    /**
     * A purely static class
     */
    private KdbSerializer() {
    }

    /**
     * Construct a KDB database from the supplied inputstream.
     *
     * @param credentials the credentials
     * @param kdbHeader a header to be populated with values read from the stream
     * @param inputStream an inputStream to read from
     * @return a constructed KdbDatabase
     * @throws IOException if reading of the inputStream fails
     * @throws IllegalStateException if decoding of KDB format fails
     */
    public static KdbDatabase createKdbDatabase(Credentials credentials, KdbHeader kdbHeader, InputStream inputStream) throws IOException {
        // everything is little endian
        DataInput dataInput = new LittleEndianDataInputStream(inputStream);
        // check the magic values to verify file type
        checkSignature(dataInput);
        // load the header
        deserializeHeader(kdbHeader, dataInput);

        // Create a decrypted stream from where we have read to
        InputStream decryptedInputStream = kdbHeader.createDecryptedInputStream(credentials.getKey(), inputStream);

        // Wrap the decrypted stream in a digest stream
        MessageDigest digest = Encryption.getSha256MessageDigestInstance();
        DigestInputStream digestInputStream = new DigestInputStream(decryptedInputStream, digest);

        // Start the dataInput at wherever we have got to in the stream
        dataInput = new LittleEndianDataInputStream(digestInputStream);

        // read the decrypted serialized form of all groups
        KdbDatabase kdbDatabase = new KdbDatabase();
        KdbGroup lastGroup = (KdbGroup) kdbDatabase.getRootGroup();
        for (long group = 0; group < kdbHeader.getGroupCount(); group++) {
            lastGroup = deserializeGroup(lastGroup, dataInput);
        }

        // read the decrypted serialized form of all entries
        for (long entry = 0; entry < kdbHeader.getEntryCount(); entry++) {
            deserializeEntry(kdbDatabase, dataInput);
        }

        // check that the digest is correct (one would imagine that it would all have failed horribly by now if not)
        if (!Arrays.equals(digest.digest(), kdbHeader.getContentHash())) {
            throw new IllegalStateException("Hash values did not match");
        }

        // close digest and all underlying streams
        digestInputStream.close();

        return kdbDatabase;
    }

    private static void setDatabase(KdbDatabase kdbDatabase, KdbGroup group) {
        for (KdbGroup child: group.getGroups()){
            ((KdbGroup) child).database = kdbDatabase;
            setDatabase(kdbDatabase, child);
        }
    }

    // these are the signatures of a KDB "V3" file
    private static final int SIGNATURE1 = 0x9AA2D903;
    private static final int SIGNATURE2 = 0xB54BFB65;

    /**
     * Check the signature of a data source
     *
     * @param dataInput the source to check
     * @throws IOException
     * @throws IllegalStateException if the signature does not match
     */
    public static void checkSignature(DataInput dataInput) throws IOException {
        if (dataInput.readInt() != SIGNATURE1 || dataInput.readInt() != SIGNATURE2) {
            throw new IllegalStateException("Signature bytes do not match");
        }
    }

    /**
     * Deserialize a header from a source into the supplied kdbHeader
     *
     * @param kdbHeader a header to populate with relevant values
     * @param dataInput a source of data
     * @throws IOException
     */
    private static void deserializeHeader(KdbHeader kdbHeader, DataInput dataInput) throws IOException {
        kdbHeader.setFlags(dataInput.readInt());
        kdbHeader.setVersion(dataInput.readInt());

        byte[] buffer = new byte[16];
        dataInput.readFully(buffer);
        kdbHeader.setMasterSeed(buffer);

        buffer = new byte[16];
        dataInput.readFully(buffer);
        kdbHeader.setEncryptionIv(buffer);

        kdbHeader.setGroupCount(dataInput.readInt());
        kdbHeader.setEntryCount(dataInput.readInt());

        byte[] buffer32 = new byte[32];
        dataInput.readFully(buffer32);
        kdbHeader.setContentHash(buffer32);

        buffer32 = new byte[32];
        dataInput.readFully(buffer32);
        kdbHeader.setTransformSeed(buffer32);

        kdbHeader.setTransformRounds(dataInput.readInt());
    }

    /**
     * Deserialize a KdbGroup from a data source and attach it to the group structure of a database
     *
     * @param lastGroup the last group loaded from this source, or the root group if none
     * @param dataInput a source of data
     * @return a new KdbxGroup
     * @throws IOException
     */
    private static KdbGroup deserializeGroup(KdbGroup lastGroup, DataInput dataInput) throws IOException {
        int fieldType;
        KdbGroup group = new KdbGroup();
        while ((fieldType = dataInput.readUnsignedShort()) != 0xFFFF) {
            switch (fieldType) {
                case 0x0000:
                    // not doing anything with this for now
                    readExtData(dataInput);
                    break;
                case 0x0001:
                    UUID uuid = new UUID(0, readInt(dataInput));
                    group.setUuid(uuid);
                    break;
                case 0x0002:
                    group.setName(readString(dataInput));
                    break;
                case 0x0003:
                    group.setCreationTime(readDate(dataInput));
                    break;
                case 0x0004:
                    group.setLastModificationTime(readDate(dataInput));
                    break;
                case 0x0005:
                    group.setLastAccessTime(readDate(dataInput));
                    break;
                case 0x0006:
                    group.setExpiryTime(readDate(dataInput));
                    break;
                case 0x0007:
                    group.setIcon(new KdbIcon(readInt(dataInput)));
                    break;
                case 0x0008:
                    int level = readShort(dataInput);
                    group.setParent(computeParentGroup(lastGroup, level));
                    break;
                case 0x0009:
                    group.setFlags(readInt(dataInput));
                    break;
                default:
                    throw new IllegalStateException("Unknown field type " + String.valueOf(fieldType));
            }
        }
        dataInput.readInt();
        return group;
    }

    /**
     * Deserialize a KdbEntry from a data source
     *
     * @param database  a database to insert the entry into
     * @param dataInput a source of data
     * @throws IOException
     */
    private static void deserializeEntry(KdbDatabase database, DataInput dataInput) throws IOException {
        int fieldType;
        KdbEntry entry = new KdbEntry();
        while ((fieldType = dataInput.readUnsignedShort()) != 0xFFFF) {
            switch (fieldType) {
                case 0x0000:
                    // we are not doing anything with ExtData for now. Contains header hash ...
                    readExtData(dataInput);
                    break;
                case 0x0001:
                    entry.setUuid(readUuid(dataInput));
                    break;
                case 0x0002:
                    int groupId = readInt(dataInput);
                    // group UUIDs are just the index of the group converted to a UUID
                    Group group = database.findGroup(new UUID(0, groupId));
                    if (group == null) {
                        throw new IllegalStateException("Entry belongs to group that does not exist");
                    }
                    group.addEntry(entry);
                    break;
                case 0x0003:
                    entry.setIcon(new KdbIcon(readInt(dataInput)));
                    break;
                case 0x0004:
                    entry.setTitle(readString(dataInput));
                    break;
                case 0x0005:
                    entry.setUrl(readString(dataInput));
                    break;
                case 0x0006:
                    entry.setUsername(readString(dataInput));
                    break;
                case 0x0007:
                    entry.setPassword(readString(dataInput));
                    break;
                case 0x0008:
                    // these are not really notes, they are things like properties from KDBX databases
                    // that don't have anywhere else to live and that we would like to expose
                    entry.setNotes(readString(dataInput));
                    break;
                case 0x0009:
                    entry.setCreationTime(readDate(dataInput));
                    break;
                case 0x000A:
                    entry.setLastModificationTime(readDate(dataInput));
                    break;
                case 0x000B:
                    entry.setLastAccessTime(readDate(dataInput));
                    break;
                case 0x000C:
                    entry.setExpiryTime(readDate(dataInput));
                    break;
                case 0x000D:
                    entry.setBinaryDescription(readString(dataInput));
                    break;
                case 0x000E:
                    entry.setBinaryData(readBuffer(dataInput));
                    break;
                default:
                    throw new IllegalStateException("Unknown field type");
            }
        }
        // consume the length indicator on the final block
        dataInput.readInt();
    }

    /**
     * Figure out who the parent of this group is.
     * <p/>
     * Groups are serialised in a depth first traversal
     * so any group's parent is the nearest parent group
     * with a level of one less in the hierarchy.
     * <p/>
     * Since the database tree is built progressively the group passed in the "lastGroup" parameter has
     * already been knitted into the hierarchy and so either this group is a sub group of that group or is a subgroup
     * of the nearest ancestor to the last group that has a level less than the group we are reading.
     *
     * @param lastGroup the last group we saw in the stream
     * @param level     the level of this group
     * @return a parent
     * @throws IllegalStateException if a parent could not be figured out
     */
    private static KdbGroup computeParentGroup(KdbGroup lastGroup, int level) {
        // the level of the last group
        int lastLevel = lastGroup.computedLevel();
        // if we are one greater then we are its child
        if (level == lastLevel + 1) {
            return lastGroup;
        }
        // there is a missing group
        if (level > lastLevel) {
            throw new IllegalStateException("Could not determine parent group from level supplied");
        }
        // working variable holding current candidate parent
        KdbGroup candidateParent = (KdbGroup) lastGroup.getParent();
        // work our way up through the parents till we find one
        while (level <= candidateParent.computedLevel()) {
            candidateParent = ((KdbGroup) candidateParent.getParent());
        }
        return candidateParent;
    }

    /**
     * Stucture is in this format: 00YYYYYY YYYYYYMM MMDDDDDH HHHHMMMM MMSSSSSS
     *
     * @param buffer 5 bytes containing a packed date
     * @return a date constructed from the buffer
     */
    public static Date unpackDate(byte[] buffer) {
        // copy passed buffer into the less significant bytes of 8 bytes
        byte[] buffer8 = new byte[8];
        System.arraycopy(buffer, 0, buffer8, 3, 5);

        // construct a long from the buffer
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer8);
        long longValue = byteBuffer.getLong();

        // now mask and shift progressively to get what we need
        int second = (int) longValue & 0x3f;
        longValue = longValue >> 6;

        int minute = (int) longValue & 0x3f;
        longValue = longValue >> 6;

        int hour = (int) longValue & 0x1f;
        longValue = longValue >> 5;

        int day = (int) longValue & 0x1f;
        longValue = longValue >> 5;

        int month = (int) longValue & 0xF;
        longValue = longValue >> 4;

        int year = (int) longValue & 0xFFF;

        // just to work around the deprecation on the similar Date constructor
        GregorianCalendar cal = new GregorianCalendar();
        // I think the time is stored in local time but anyway, let's say it's UTC for the sake of argument
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        //noinspection MagicConstant
        cal.set(year, month - 1, day, hour, minute, second);
        // otherwise we seems to end up with arbitrary millis
        cal.set(GregorianCalendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /****
     * Utility methods to read and consume from the stream according to the data type we need.
     ****/
    private static Date readDate(DataInput dataInput) throws IOException {
        if (dataInput.readInt() != 5) {
            throw new IllegalStateException("Date must be 5 bytes");
        }
        byte[] buffer = new byte[5];
        dataInput.readFully(buffer);
        return unpackDate(buffer);
    }

    private static int readInt(DataInput dataInput) throws IOException {
        if (dataInput.readInt() != 4) {
            throw new IllegalStateException("Integer must be 4 bytes");
        }
        return dataInput.readInt();
    }

    private static short readShort(DataInput dataInput) throws IOException {
        if (dataInput.readInt() != 2) {
            throw new IllegalStateException("Short must be 2 bytes");
        }
        return dataInput.readShort();
    }

    private static String readString(DataInput dataInput) throws IOException {
        int length = dataInput.readInt();
        byte[] buffer = new byte[length];
        dataInput.readFully(buffer);
        // don't copy the trailing C-Style null
        // ideally we'd be sure what the encoding is
        return new String(buffer, 0, length - 1);
    }

    private static UUID readUuid(DataInput dataInput) throws IOException {
        if (dataInput.readInt() != 16) {
            throw new IllegalStateException("Uuid must be 16 bytes");
        }
        long lesserBits = dataInput.readLong();
        long greaterBits = dataInput.readLong();
        return new UUID(greaterBits, lesserBits);
    }

    private static byte[] readBuffer(DataInput dataInput) throws IOException {
        int size = dataInput.readInt();
        byte[] buffer = new byte[size];
        dataInput.readFully(buffer);
        return buffer;
    }

    private static byte[] readExtData(DataInput dataInput) throws IOException {
        int size = dataInput.readInt();
        byte[] buffer = new byte[size];
        dataInput.readFully(buffer);
        return buffer;
    }
}
