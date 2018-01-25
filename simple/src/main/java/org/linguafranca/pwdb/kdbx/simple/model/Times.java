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

package org.linguafranca.pwdb.kdbx.simple.model;

import org.linguafranca.pwdb.kdbx.simple.converter.KeePassBooleanConverter;
import org.linguafranca.pwdb.kdbx.simple.converter.TimeConverter;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.convert.Convert;

import java.util.Date;

/**
 * @author jo
 */
@SuppressWarnings({"unused", "WeakerAccess"})
@Root
public class Times {
    @Element(name = "LastModificationTime", type = Date.class)
    @Convert(TimeConverter.class)
    protected Date lastModificationTime;
    @Element(name = "CreationTime", type = Date.class)
    @Convert(TimeConverter.class)
    protected Date creationTime;
    @Element(name = "LastAccessTime", type = Date.class)
    @Convert(TimeConverter.class)
    protected Date lastAccessTime;
    @Element(name = "ExpiryTime", type = Date.class)
    @Convert(TimeConverter.class)
    protected Date expiryTime;
    @Element(name = "Expires", type = Boolean.class)
    @Convert(KeePassBooleanConverter.class)
    protected Boolean expires;
    @Element(name = "UsageCount")
    protected int usageCount;
    @Element(name = "LocationChanged", type = Date.class)
    @Convert(TimeConverter.class)
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
        this(new Date(System.currentTimeMillis()/1000*1000));
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
