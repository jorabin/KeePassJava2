package org.linguafranca.pwdb.kdbx;

import com.google.common.base.Strings;
import org.linguafranca.util.HexViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.linguafranca.util.TestUtil.getTestPrintStream;

/**
 * Utility to allow browsing of database files and listing content to console
 */
public class ChooseFile {

    OutputStream outputStream = getTestPrintStream();

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
/*
            Util.listXml(fc.getSelectedFile().getName(),
                    new KdbxCreds(s.getBytes()),
                    new PrintWriter(outputStream));
*/
            try (InputStream is = Files.newInputStream(Paths.get(fc.getSelectedFile().getPath()))) {
                HexViewer.list(is);
            }
        }
    }
}
