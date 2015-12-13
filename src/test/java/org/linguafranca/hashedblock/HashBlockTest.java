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

package org.linguafranca.hashedblock;

import org.junit.Test;

import java.io.*;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * @author jo
 */
public class HashBlockTest {

    @Test
    public void testSmallBuf () throws IOException {
        File test = File.createTempFile("test","hb");
        createFile(test, 1023, (byte) 0x7f);
        testFile(test, 1023, (byte) 0x7f);
    }

    @Test
    public void testUnderBuf () throws IOException {
        File test = File.createTempFile("test","hb");
        createFile(test, 1024*8-1, (byte) 0x5E);
        testFile(test, 1024*8-1, (byte) 0x5E);
    }

    @Test
    public void testEqualBuf () throws IOException {
        File test = File.createTempFile("test","hb");
        createFile(test, 1024*8, (byte) 0x71);
        testFile(test, 1024*8, (byte) 0x71);
    }
    @Test
    public void testOverBuf () throws IOException {
        File test = File.createTempFile("test","hb");
        createFile(test, 1024*8+1, (byte) 0x1D);
        testFile(test, 1024*8+1, (byte) 0x1D);
    }

    @Test
    public void testMultiBuf () throws IOException {
        File test = File.createTempFile("test","hb");
        createFile(test, 1024*8*3, (byte) 0x2d);
        testFile(test, 1024*8*3, (byte) 0x2d);
    }

    @Test
    public void testHugeBuf () throws IOException {
        File test = File.createTempFile("test","hb");
        createFile(test, 100001, (byte) 0x3A);
        testFile(test, 100001, (byte) 0x3A);
    }

    @Test
    public void testMultiWrite () throws IOException {
        File test = File.createTempFile("test","hb");
        DataOutputStream dos = new DataOutputStream(new FileOutputStream(test));
        HashedBlockOutputStream os = new HashedBlockOutputStream(dos);
        byte [] buf = new byte [1];
        buf[0] = 0x64;
        for (int i=0; i<100001; i++) {
            os.write(buf);
        }
        os.close();

        testFile(test, 100001, (byte) 0x64);
    }

    private void testFile(File test, int length, byte pattern) throws IOException {
        FileInputStream fis = new FileInputStream(test);
        HashedBlockInputStream is = new HashedBlockInputStream(fis);
        int bytesRead;
        int totalBytesRead=0;
        byte[] inbuf = new byte[31];
        byte[] expected = new byte[31];
        Arrays.fill(expected, pattern);
        while ((bytesRead = is.read(inbuf)) > 0 ){
            totalBytesRead += bytesRead;
            assertArrayEquals(expected, inbuf);
        }
        assertEquals(length, totalBytesRead);
    }

    private void createFile(File test, int length, byte pattern) throws IOException {
        FileOutputStream fos = new FileOutputStream(test);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        HashedBlockOutputStream os = new HashedBlockOutputStream(bos);
        byte [] buf = new byte [length];
        Arrays.fill(buf, pattern);
        os.write(buf);
        os.flush();
        os.close();
        System.out.println("File size is " + test.length() + " test buffer is " + length);
    }

}