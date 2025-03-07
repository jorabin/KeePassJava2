/*
 * Copyright (c) 2025. Jo Rabin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.linguafranca.pwdb.kdbx;

import com.google.common.base.Strings;
import org.linguafranca.pwdb.format.KdbxCreds;
import org.linguafranca.pwdb.format.KdbxHeader;
import org.linguafranca.pwdb.format.KdbxSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.linguafranca.pwdb.kdbx.Util.streamToString;
import static org.linguafranca.util.TestUtil.getTestPrintStream;

/**
 * Utility to allow browsing of database files and listing content to console
 */
public class ChooseFile {

    public static void main(String[] args) throws IOException {
        ChooseFile cf = new ChooseFile();
        cf.choose();
    }

    Logger logger = LoggerFactory.getLogger(this.getClass());

    public ChooseFile(){

    }
    public void choose() throws IOException {
        final JFileChooser fc = new JFileChooser();
        // get a file in the test resource directory
        URL resources = this.getClass().getClassLoader().getResource("kdb.key");
        try {
            fc.setCurrentDirectory(Paths.get(resources.toURI()).toFile());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().endsWith(".kdbx");
            }

            @Override
            public String getDescription() {
                return "KDBX Database";
            }
        });

        int returnVal = fc.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String s = (String)JOptionPane.showInputDialog(
                    null,
                    "Enter the password for " + fc.getSelectedFile().getName(),
                    "Password",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    "123");
            if (Strings.isNullOrEmpty(s)) {
                return;
            }
            logger.info("Opening {}", fc.getSelectedFile().getPath());
            try (InputStream is = Files.newInputStream(Paths.get(fc.getSelectedFile().getPath()))) {
                InputStream ss = KdbxSerializer.createUnencryptedInputStream(new KdbxCreds(s.getBytes()), new KdbxHeader(), is);
                System.out.println(streamToString(ss));
                System.out.println();
                System.out.flush();
            }
/*            try (InputStream is = Files.newInputStream(Paths.get(fc.getSelectedFile().getPath()))) {
                HexViewer.list(is);
            }*/
        }
    }
}
