/*
 * Copyright 2023 Giuseppe Valente
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

package org.linguafranca.pwdb.kdbx.jackson.model;

import java.util.Date;

import org.linguafranca.pwdb.kdbx.jackson.converter.DateToStringConverter;
import org.linguafranca.pwdb.kdbx.jackson.converter.StringToDateConverter;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

// @JacksonXmlRootElement(localName = "Times")
public class Times {
    @JacksonXmlProperty(localName = "LastModificationTime")
    @JsonDeserialize(converter = StringToDateConverter.class)
    @JsonSerialize(converter = DateToStringConverter.class)
    protected Date lastModificationTime;

    @JacksonXmlProperty(localName = "CreationTime")
    @JsonDeserialize(converter = StringToDateConverter.class)
    @JsonSerialize(converter = DateToStringConverter.class)
    protected Date creationTime;

    @JacksonXmlProperty(localName = "LastAccessTime")
    @JsonDeserialize(converter = StringToDateConverter.class)
    @JsonSerialize(converter = DateToStringConverter.class)
    protected Date lastAccessTime;

    @JacksonXmlProperty(localName = "ExpiryTime")
    @JsonSerialize(converter = DateToStringConverter.class)
    @JsonDeserialize(converter = StringToDateConverter.class)
    protected Date expiryTime;

    @JacksonXmlProperty(localName = "Expires")
    protected Boolean expires;

    @JacksonXmlProperty(localName = "UsageCount")
    protected int usageCount;

    @JacksonXmlProperty(localName = "LocationChanged")
    @JsonDeserialize(converter = StringToDateConverter.class)
    @JsonSerialize(converter = DateToStringConverter.class)
    protected Date locationChanged;

    public Date getLastModificationTime() {
        return lastModificationTime;
    }

    public void setLastModificationTime(Date lastModificationTime) {
        this.lastModificationTime = lastModificationTime;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(Date lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    public Date getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(Date expiryTime) {
        this.expiryTime = expiryTime;
    }

    public Boolean getExpires() {
        return expires;
    }

    public void setExpires(Boolean expires) {
        this.expires = expires;
    }

    public int getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(int usageCount) {
        this.usageCount = usageCount;
    }

    public Date getLocationChanged() {
        return locationChanged;
    }

    public void setLocationChanged(Date locationChanged) {
        this.locationChanged = locationChanged;
    }

    public Times() {
        this(new Date(System.currentTimeMillis() / 1000 * 1000));
    }

    public Times(Date date) {
        lastModificationTime = date;
        lastAccessTime = date;
        locationChanged = date;
        creationTime = date;
        expiryTime = date;
        expires = false;
        usageCount = 0;
    }
}
