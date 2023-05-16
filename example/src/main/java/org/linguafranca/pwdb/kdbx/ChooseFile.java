package org.linguafranca.pwdb.kdbx;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import static org.linguafranca.util.TestUtil.getTestPrintStream;

public class ChooseFile {

    OutputStream outputStream = getTestPrintStream();

    public static void main(String[] args) throws URISyntaxException, IOException {
        ChooseFile cf = new ChooseFile();
        cf.choose();
    }

    Logger logger = LoggerFactory.getLogger(this.getClass());

    public ChooseFile(){

    }
    public void choose() throws IOException {
        final JFileChooser fc = new JFileChooser();
        URL resources = this.getClass().getClassLoader().getResource("kdb.key");
        try {
            fc.setCurrentDirectory(Paths.get(resources.toURI()).toFile());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
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
            Util.listXml(fc.getSelectedFile().getName(),
                    new KdbxCreds(s.getBytes()),
                    new PrintWriter(outputStream));
        }
    }
}
