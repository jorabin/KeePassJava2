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

package org.linguafranca.pwdb.kdbx.jackson.model;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.linguafranca.pwdb.kdbx.jackson.converter.InstantToStringConverter;
import org.linguafranca.pwdb.kdbx.jackson.converter.StringToInstantConverter;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Times {
    @JacksonXmlProperty(localName = "LastModificationTime")
    @JsonDeserialize(converter = StringToInstantConverter.class)
    @JsonSerialize(converter = InstantToStringConverter.class)
    protected Instant lastModificationTime;

    @JacksonXmlProperty(localName = "CreationTime")
    @JsonDeserialize(converter = StringToInstantConverter.class)
    @JsonSerialize(converter = InstantToStringConverter.class)
    protected Instant creationTime;

    @JacksonXmlProperty(localName = "LastAccessTime")
    @JsonDeserialize(converter = StringToInstantConverter.class)
    @JsonSerialize(converter = InstantToStringConverter.class)
    protected Instant lastAccessTime;

    @JacksonXmlProperty(localName = "ExpiryTime")
    @JsonSerialize(converter = InstantToStringConverter.class)
    @JsonDeserialize(converter = StringToInstantConverter.class)
    protected Instant expiryTime;

    @JacksonXmlProperty(localName = "Expires")
    protected Boolean expires;

    @JacksonXmlProperty(localName = "UsageCount")
    protected int usageCount;

    @JacksonXmlProperty(localName = "LocationChanged")
    @JsonDeserialize(converter = StringToInstantConverter.class)
    @JsonSerialize(converter = InstantToStringConverter.class)
    protected Instant locationChanged;

    public Instant getLastModificationTime() {
        return lastModificationTime;
    }

    public void setLastModificationTime(Instant lastModificationTime) {
        this.lastModificationTime = lastModificationTime;
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Instant creationTime) {
        this.creationTime = creationTime;
    }

    public Instant getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(Instant lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    public Instant getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(Instant expiryTime) {
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

    public Instant getLocationChanged() {
        return locationChanged;
    }

    public void setLocationChanged(Instant locationChanged) {
        this.locationChanged = locationChanged;
    }

    public Times() {
        // KDBX times are whole seconds
        this(Instant.now().truncatedTo(ChronoUnit.SECONDS));
    }

    public Times(Instant instant) {
        lastModificationTime = instant;
        lastAccessTime = instant;
        locationChanged = instant;
        creationTime = instant;
        expiryTime = instant;
        expires = false;
        usageCount = 0;
    }
}
