package org.linguafranca.keepass.kdb;

import org.linguafranca.keepass.db.Group;
import org.linguafranca.keepass.db.Icon;
import org.linguafranca.keepass.db.base.AbstractEntry;

import java.util.Date;
import java.util.UUID;

/**
 * @author jo
 */
public class KdbEntry extends AbstractEntry {
    KdbGroup parent;
    private UUID uuid;
    private String title;
    private String url;
    private String notes;
    private KdbIcon icon;
    private String username;
    private String password;
    private Date creationTime;
    private Date lastModificationTime;
    private Date lastAccessTime;
    private Date expiryTime;
    private String binaryDescription;
    private byte[] binaryData;

    @Override
    public String getProperty(String name) {
        throw new UnsupportedOperationException("Cannot get or set properties from KDB format");
    }

    @Override
    public void setProperty(String name, String value) {
        throw new UnsupportedOperationException("Cannot get or set properties from KDB format");
    }

    @Override
    public Group getParent() {
        return parent;
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String pass) {
        this.password = pass;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getNotes() {
        return notes;
    }

    @Override
    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public Icon getIcon() {
        return icon;
    }

    @Override
    public void setIcon(Icon icon) {
        this.icon = (KdbIcon) icon;
    }
    
    void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    void setLastModificationTime(Date lastModificationTime) {
        this.lastModificationTime = lastModificationTime;
    }

    public Date getLastModificationTime() {
        return lastModificationTime;
    }

    void setLastAccessTime(Date lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    public Date getLastAccessTime() {
        return lastAccessTime;
    }

    void setExpiryTime(Date expiryTime) {
        this.expiryTime = expiryTime;
    }

    public Date getExpiryTime() {
        return expiryTime;
    }

    public String getBinaryDescription() {
        return binaryDescription;
    }

    void setBinaryDescription(String binaryDescription) {
        this.binaryDescription = binaryDescription;
    }

    public byte[] getBinaryData() {
        return binaryData;
    }

    void setBinaryData(byte[] binaryData) {
        this.binaryData = binaryData;
    }

    public String toString() {
        String time = KdbDatabase.isoDateFormat.format(creationTime);
        return String.format("\"%s\" (%s, %s, %s) %s [%s]", title, url, username, notes.substring(0,Math.min(notes.length(), 24)), time, binaryDescription);
    }
}
