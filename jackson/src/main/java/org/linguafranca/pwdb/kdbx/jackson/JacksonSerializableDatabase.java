package org.linguafranca.pwdb.kdbx.jackson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.linguafranca.pwdb.SerializableDatabase;
import org.linguafranca.pwdb.security.StreamEncryptor;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class JacksonSerializableDatabase implements SerializableDatabase{

    public KeePassFile keePassFile;
    private StreamEncryptor encryption;


    public static KeePassFile createEmptyDatabase() throws StreamReadException, DatabindException, IOException {

        InputStream inputStream = JacksonSerializableDatabase.class.getClassLoader().getResourceAsStream("base.kdbx.xml");
        XmlMapper mapper = new XmlMapper();
        KeePassFile res = mapper.readValue(inputStream, KeePassFile.class);
        return res;

    }

    public  JacksonSerializableDatabase(){
    }
    public JacksonSerializableDatabase(KeePassFile keePassFile) {
        this.keePassFile = keePassFile;
    }

    @Override
    public JacksonSerializableDatabase load(InputStream inputStream) throws IOException {
        XmlMapper mapper = new XmlMapper();
        keePassFile = mapper.readValue(inputStream, KeePassFile.class);
        return this;
    }

    @Override
    public void save(OutputStream outputStream) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    public StreamEncryptor getEncryption() {
        return this.encryption;
    }

    @Override
    public void setEncryption(StreamEncryptor encryption) {
        this.encryption = encryption;
    }

    @Override
    public byte[] getHeaderHash() {
        return keePassFile.meta.headerHash;    }

    @Override
    public void setHeaderHash(byte[] hash) {
        this.keePassFile.meta.headerHash = hash;
    }

    @Override
    public void addBinary(int index, byte[] payload) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addBinary'");
    }

    @Override
    public byte[] getBinary(int index) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBinary'");
    }

    @Override
    public int getBinaryCount() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBinaryCount'");
    }

    /**
     * On load add parents
     * @param parent a parent to recurse
     */
    static void fixUp(JacksonGroup parent){
        
        for (JacksonGroup group: parent.group) {
            group.parent = parent;
            group.database = parent.database;
            fixUp(group);
        }

        for (JacksonEntry entry: parent.entry) {
            entry.database = parent.database;
            entry.parent = parent;
        }
    }
    
}
