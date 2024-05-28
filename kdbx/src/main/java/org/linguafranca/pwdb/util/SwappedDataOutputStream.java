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

package org.linguafranca.pwdb.util;

import org.apache.commons.io.EndianUtils;

import java.io.*;

/**
 * DataOutput for systems relying on little-endian data formats. When written, values will be changed from big-endian to little-endian
 * formats for external usage.
 */
public final class SwappedDataOutputStream extends FilterOutputStream implements DataOutput {

    /**
     * Creates a {@code SwappedDataOutputStream} that wraps the given stream.
     *
     * @param out the stream to delegate to
     */
    public SwappedDataOutputStream(OutputStream out) {
        super(new DataOutputStream(out));
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }

    @Override
    public void writeBoolean(boolean v) throws IOException {
        ((DataOutputStream) out).writeBoolean(v);
    }

    @Override
    public void writeByte(int v) throws IOException {
        ((DataOutputStream) out).writeByte(v);
    }

    @Deprecated
    @Override
    public void writeBytes(String s) throws IOException {
        ((DataOutputStream) out).writeBytes(s);
    }

    /**
     * Writes a char as specified by {@link DataOutputStream#writeChar(int)}, except using
     * little-endian byte order.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void writeChar(int v) throws IOException {
        writeShort(v);
    }

    /**
     * Writes a {@code String} as specified by {@link DataOutputStream#writeChars(String)}, except
     * each character is written using little-endian byte order.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void writeChars(String s) throws IOException {
        for (int i = 0; i < s.length(); i++) {
            writeChar(s.charAt(i));
        }
    }

    /**
     * Writes a {@code double} as specified by {@link DataOutputStream#writeDouble(double)}, except
     * using little-endian byte order.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void writeDouble(double v) throws IOException {
        EndianUtils.writeSwappedDouble(out, v);
    }

    /**
     * Writes a {@code float} as specified by {@link DataOutputStream#writeFloat(float)}, except using
     * little-endian byte order.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void writeFloat(float v) throws IOException {
        EndianUtils.writeSwappedFloat(out, v);
    }

    /**
     * Writes an {@code int} as specified by {@link DataOutputStream#writeInt(int)}, except using
     * little-endian byte order.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void writeInt(int v) throws IOException {
        EndianUtils.writeSwappedInteger(out, v);
    }

    /**
     * Writes a {@code long} as specified by {@link DataOutputStream#writeLong(long)}, except using
     * little-endian byte order.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void writeLong(long v) throws IOException {
        EndianUtils.writeSwappedLong(out, v);
    }

    /**
     * Writes a {@code short} as specified by {@link DataOutputStream#writeShort(int)}, except using
     * little-endian byte order.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void writeShort(int v) throws IOException {
        EndianUtils.writeSwappedShort(out, (short) v);
    }

    @Override
    public void writeUTF(String str) throws IOException {
        ((DataOutputStream) out).writeUTF(str);
    }

    @Override
    public void close() throws IOException {
        out.close();
    }
}
