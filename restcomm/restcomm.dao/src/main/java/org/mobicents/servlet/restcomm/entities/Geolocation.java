
/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2016, Telestax Inc and individual contributors
 * by the @authors tag. 
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
        
package org.mobicents.servlet.restcomm.entities;

import java.math.BigInteger;
import java.net.URI;

import org.joda.time.DateTime;
import org.mobicents.servlet.restcomm.annotations.concurrency.NotThreadSafe;

/**
 * @author Fernando
 *
 */
public class Geolocation {
    
    private final Sid sid;
    private final DateTime dateCreated;
    private final DateTime dateUpdated;
    private final DateTime dateExecuted;
    private final Sid accountSid;
    private final String source;
    private final String globalCellId;
    private final String locationAreaId;
    private final Integer ageOfLocationInfo;
    private final Integer mobileCountryCode;
    private final Integer mobileNetworkCode;
    private final BigInteger networkEntityAddress;
    private final String deviceLatitude;
    private final String deviceLongitude;
    private final String physicalAddress;
    private final String internetAddress;
    private final BigInteger radius;
    private final BigInteger interval;
    private final String occurrence;
    private final String geoLocationType;
    private final BigInteger geoLocationResponseTime;
    private final String status;
    private final String apiVersion;
    private final URI uri;
    
    public Geolocation(Sid sid, DateTime dateCreated, DateTime dateUpdated, DateTime dateExecuted, Sid accountSid,
            String source, String globalCellId, String locationAreaId, Integer ageOfLocationInfo, Integer mobileCountryCode,
            Integer mobileNetworkCode, BigInteger networkEntityAddress, String deviceLatitude, String deviceLongitude,
            String physicalAddress, String internetAddress, BigInteger radius, BigInteger interval, String occurrence,
            String geoLocationType, BigInteger geoLocationResponseTime, String status, String apiVersion, URI uri) {
        super();
        this.sid = sid;
        this.dateCreated = dateCreated;
        this.dateUpdated = dateUpdated;
        this.dateExecuted = dateExecuted;
        this.accountSid = accountSid;
        this.source = source;
        this.globalCellId = globalCellId;
        this.locationAreaId = locationAreaId;
        this.ageOfLocationInfo = ageOfLocationInfo;
        this.mobileCountryCode = mobileCountryCode;
        this.mobileNetworkCode = mobileNetworkCode;
        this.networkEntityAddress = networkEntityAddress;
        this.deviceLatitude = deviceLatitude;
        this.deviceLongitude = deviceLongitude;
        this.physicalAddress = physicalAddress;
        this.internetAddress = internetAddress;
        this.radius = radius;
        this.interval = interval;
        this.occurrence = occurrence;
        this.geoLocationType = geoLocationType;
        this.geoLocationResponseTime = geoLocationResponseTime;
        this.status = status;
        this.apiVersion = apiVersion;
        this.uri = uri;
    }

    public Sid getSid() {
        return sid;
    }

    public DateTime getDateCreated() {
        return dateCreated;
    }

    public DateTime getDateUpdated() {
        return dateUpdated;
    }

    public DateTime getDateExecuted() {
        return dateExecuted;
    }

    public Sid getAccountSid() {
        return accountSid;
    }

    public String getSource() {
        return source;
    }

    public String getGlobalCellId() {
        return globalCellId;
    }

    public String getLocationAreaId() {
        return locationAreaId;
    }

    public Integer getAgeOfLocationInfo() {
        return ageOfLocationInfo;
    }

    public Integer getMobileCountryCode() {
        return mobileCountryCode;
    }

    public Integer getMobileNetworkCode() {
        return mobileNetworkCode;
    }

    public BigInteger getNetworkEntityAddress() {
        return networkEntityAddress;
    }

    public String getDeviceLatitude() {
        return deviceLatitude;
    }

    public String getDeviceLongitude() {
        return deviceLongitude;
    }

    public String getPhysicalAddress() {
        return physicalAddress;
    }

    public String getInternetAddress() {
        return internetAddress;
    }

    public BigInteger getRadius() {
        return radius;
    }

    public BigInteger getInterval() {
        return interval;
    }

    public String getOccurrence() {
        return occurrence;
    }

    public String getGeoLocationType() {
        return geoLocationType;
    }

    public BigInteger getGeoLocationResponseTime() {
        return geoLocationResponseTime;
    }

    public String getStatus() {
        return status;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public URI getUri() {
        return uri;
    }
    
    public Geolocation setSid(Sid sid) {
        return new Geolocation(sid,  dateCreated,  dateUpdated,  dateExecuted,  accountSid,
                source,  globalCellId,  locationAreaId,  ageOfLocationInfo,  mobileCountryCode,
                mobileNetworkCode,  networkEntityAddress,  deviceLatitude,  deviceLongitude,
                physicalAddress,  internetAddress,  radius,  interval,  occurrence,
                geoLocationType,  geoLocationResponseTime,  status,  apiVersion,  uri);
    }

    public Geolocation setDateCreated(DateTime dateCreated) {
        return new Geolocation(sid,  dateCreated,  dateUpdated,  dateExecuted,  accountSid,
                source,  globalCellId,  locationAreaId,  ageOfLocationInfo,  mobileCountryCode,
                mobileNetworkCode,  networkEntityAddress,  deviceLatitude,  deviceLongitude,
                physicalAddress,  internetAddress,  radius,  interval,  occurrence,
                geoLocationType,  geoLocationResponseTime,  status,  apiVersion,  uri);
    }

    public Geolocation setDateUpdated(DateTime dateUpdated) {
        return new Geolocation(sid,  dateCreated,  dateUpdated,  dateExecuted,  accountSid,
                source,  globalCellId,  locationAreaId,  ageOfLocationInfo,  mobileCountryCode,
                mobileNetworkCode,  networkEntityAddress,  deviceLatitude,  deviceLongitude,
                physicalAddress,  internetAddress,  radius,  interval,  occurrence,
                geoLocationType,  geoLocationResponseTime,  status,  apiVersion,  uri);
    }

    public Geolocation setDateExecuted(DateTime dateExecuted) {
        return new Geolocation(sid,  dateCreated,  dateUpdated,  dateExecuted,  accountSid,
                source,  globalCellId,  locationAreaId,  ageOfLocationInfo,  mobileCountryCode,
                mobileNetworkCode,  networkEntityAddress,  deviceLatitude,  deviceLongitude,
                physicalAddress,  internetAddress,  radius,  interval,  occurrence,
                geoLocationType,  geoLocationResponseTime,  status,  apiVersion,  uri);
    }

    public Geolocation setAccountSid(Sid accountSid) {
        return new Geolocation(sid,  dateCreated,  dateUpdated,  dateExecuted,  accountSid,
                source,  globalCellId,  locationAreaId,  ageOfLocationInfo,  mobileCountryCode,
                mobileNetworkCode,  networkEntityAddress,  deviceLatitude,  deviceLongitude,
                physicalAddress,  internetAddress,  radius,  interval,  occurrence,
                geoLocationType,  geoLocationResponseTime,  status,  apiVersion,  uri);
    }

    public Geolocation setSource(String source) {
        return new Geolocation(sid,  dateCreated,  dateUpdated,  dateExecuted,  accountSid,
                source,  globalCellId,  locationAreaId,  ageOfLocationInfo,  mobileCountryCode,
                mobileNetworkCode,  networkEntityAddress,  deviceLatitude,  deviceLongitude,
                physicalAddress,  internetAddress,  radius,  interval,  occurrence,
                geoLocationType,  geoLocationResponseTime,  status,  apiVersion,  uri);
    }

    public Geolocation setGlobalCellId(String globalCellId) {
        return new Geolocation(sid,  dateCreated,  dateUpdated,  dateExecuted,  accountSid,
                source,  globalCellId,  locationAreaId,  ageOfLocationInfo,  mobileCountryCode,
                mobileNetworkCode,  networkEntityAddress,  deviceLatitude,  deviceLongitude,
                physicalAddress,  internetAddress,  radius,  interval,  occurrence,
                geoLocationType,  geoLocationResponseTime,  status,  apiVersion,  uri);
    }

    public Geolocation setLocationAreaId(String locationAreaId) {
        return new Geolocation(sid,  dateCreated,  dateUpdated,  dateExecuted,  accountSid,
                source,  globalCellId,  locationAreaId,  ageOfLocationInfo,  mobileCountryCode,
                mobileNetworkCode,  networkEntityAddress,  deviceLatitude,  deviceLongitude,
                physicalAddress,  internetAddress,  radius,  interval,  occurrence,
                geoLocationType,  geoLocationResponseTime,  status,  apiVersion,  uri);
    }

    public Geolocation setAgeOfLocationInfo(Integer ageOfLocationInfo) {
        return new Geolocation(sid,  dateCreated,  dateUpdated,  dateExecuted,  accountSid,
                source,  globalCellId,  locationAreaId,  ageOfLocationInfo,  mobileCountryCode,
                mobileNetworkCode,  networkEntityAddress,  deviceLatitude,  deviceLongitude,
                physicalAddress,  internetAddress,  radius,  interval,  occurrence,
                geoLocationType,  geoLocationResponseTime,  status,  apiVersion,  uri);
    }

    public Geolocation setMobileCountryCode(Integer mobileCountryCode) {
        return new Geolocation(sid,  dateCreated,  dateUpdated,  dateExecuted,  accountSid,
                source,  globalCellId,  locationAreaId,  ageOfLocationInfo,  mobileCountryCode,
                mobileNetworkCode,  networkEntityAddress,  deviceLatitude,  deviceLongitude,
                physicalAddress,  internetAddress,  radius,  interval,  occurrence,
                geoLocationType,  geoLocationResponseTime,  status,  apiVersion,  uri);
    }

    public Geolocation setMobileNetworkCode(Integer mobileNetworkCode) {
        return new Geolocation(sid,  dateCreated,  dateUpdated,  dateExecuted,  accountSid,
                source,  globalCellId,  locationAreaId,  ageOfLocationInfo,  mobileCountryCode,
                mobileNetworkCode,  networkEntityAddress,  deviceLatitude,  deviceLongitude,
                physicalAddress,  internetAddress,  radius,  interval,  occurrence,
                geoLocationType,  geoLocationResponseTime,  status,  apiVersion,  uri);
    }

    public Geolocation setNetworkEntityAddress(BigInteger networkEntityAddress) {
        return new Geolocation(sid,  dateCreated,  dateUpdated,  dateExecuted,  accountSid,
                source,  globalCellId,  locationAreaId,  ageOfLocationInfo,  mobileCountryCode,
                mobileNetworkCode,  networkEntityAddress,  deviceLatitude,  deviceLongitude,
                physicalAddress,  internetAddress,  radius,  interval,  occurrence,
                geoLocationType,  geoLocationResponseTime,  status,  apiVersion,  uri);
    }

    public Geolocation setDeviceLatitude(String deviceLatitude) {
        return new Geolocation(sid,  dateCreated,  dateUpdated,  dateExecuted,  accountSid,
                source,  globalCellId,  locationAreaId,  ageOfLocationInfo,  mobileCountryCode,
                mobileNetworkCode,  networkEntityAddress,  deviceLatitude,  deviceLongitude,
                physicalAddress,  internetAddress,  radius,  interval,  occurrence,
                geoLocationType,  geoLocationResponseTime,  status,  apiVersion,  uri);
    }

    public Geolocation setDeviceLongitude(String deviceLongitude) {
        return new Geolocation(sid,  dateCreated,  dateUpdated,  dateExecuted,  accountSid,
                source,  globalCellId,  locationAreaId,  ageOfLocationInfo,  mobileCountryCode,
                mobileNetworkCode,  networkEntityAddress,  deviceLatitude,  deviceLongitude,
                physicalAddress,  internetAddress,  radius,  interval,  occurrence,
                geoLocationType,  geoLocationResponseTime,  status,  apiVersion,  uri);
    }

    public Geolocation setPhysicalAddress(String physicalAddress) {
        return new Geolocation(sid,  dateCreated,  dateUpdated,  dateExecuted,  accountSid,
                source,  globalCellId,  locationAreaId,  ageOfLocationInfo,  mobileCountryCode,
                mobileNetworkCode,  networkEntityAddress,  deviceLatitude,  deviceLongitude,
                physicalAddress,  internetAddress,  radius,  interval,  occurrence,
                geoLocationType,  geoLocationResponseTime,  status,  apiVersion,  uri);
    }

    public Geolocation setInternetAddress(String internetAddress) {
        return new Geolocation(sid,  dateCreated,  dateUpdated,  dateExecuted,  accountSid,
                source,  globalCellId,  locationAreaId,  ageOfLocationInfo,  mobileCountryCode,
                mobileNetworkCode,  networkEntityAddress,  deviceLatitude,  deviceLongitude,
                physicalAddress,  internetAddress,  radius,  interval,  occurrence,
                geoLocationType,  geoLocationResponseTime,  status,  apiVersion,  uri);
    }

    public Geolocation setRadius(BigInteger radius) {
        return new Geolocation(sid,  dateCreated,  dateUpdated,  dateExecuted,  accountSid,
                source,  globalCellId,  locationAreaId,  ageOfLocationInfo,  mobileCountryCode,
                mobileNetworkCode,  networkEntityAddress,  deviceLatitude,  deviceLongitude,
                physicalAddress,  internetAddress,  radius,  interval,  occurrence,
                geoLocationType,  geoLocationResponseTime,  status,  apiVersion,  uri);
    }

    public Geolocation setInterval(BigInteger interval) {
        return new Geolocation(sid,  dateCreated,  dateUpdated,  dateExecuted,  accountSid,
                source,  globalCellId,  locationAreaId,  ageOfLocationInfo,  mobileCountryCode,
                mobileNetworkCode,  networkEntityAddress,  deviceLatitude,  deviceLongitude,
                physicalAddress,  internetAddress,  radius,  interval,  occurrence,
                geoLocationType,  geoLocationResponseTime,  status,  apiVersion,  uri);
    }

    public Geolocation setOccurrence(String occurrence) {
        return new Geolocation(sid,  dateCreated,  dateUpdated,  dateExecuted,  accountSid,
                source,  globalCellId,  locationAreaId,  ageOfLocationInfo,  mobileCountryCode,
                mobileNetworkCode,  networkEntityAddress,  deviceLatitude,  deviceLongitude,
                physicalAddress,  internetAddress,  radius,  interval,  occurrence,
                geoLocationType,  geoLocationResponseTime,  status,  apiVersion,  uri);
    }

    public Geolocation setGeoLocationType(String geoLocationType) {
        return new Geolocation(sid,  dateCreated,  dateUpdated,  dateExecuted,  accountSid,
                source,  globalCellId,  locationAreaId,  ageOfLocationInfo,  mobileCountryCode,
                mobileNetworkCode,  networkEntityAddress,  deviceLatitude,  deviceLongitude,
                physicalAddress,  internetAddress,  radius,  interval,  occurrence,
                geoLocationType,  geoLocationResponseTime,  status,  apiVersion,  uri);
    }

    public Geolocation setGeoLocationResponseTime(BigInteger geoLocationResponseTime) {
        return new Geolocation(sid,  dateCreated,  dateUpdated,  dateExecuted,  accountSid,
                source,  globalCellId,  locationAreaId,  ageOfLocationInfo,  mobileCountryCode,
                mobileNetworkCode,  networkEntityAddress,  deviceLatitude,  deviceLongitude,
                physicalAddress,  internetAddress,  radius,  interval,  occurrence,
                geoLocationType,  geoLocationResponseTime,  status,  apiVersion,  uri);
    }

    public Geolocation setStatus(String status) {
        return new Geolocation(sid,  dateCreated,  dateUpdated,  dateExecuted,  accountSid,
                source,  globalCellId,  locationAreaId,  ageOfLocationInfo,  mobileCountryCode,
                mobileNetworkCode,  networkEntityAddress,  deviceLatitude,  deviceLongitude,
                physicalAddress,  internetAddress,  radius,  interval,  occurrence,
                geoLocationType,  geoLocationResponseTime,  status,  apiVersion,  uri);
    }

    public Geolocation setApiVersion(String apiVersion) {
        return new Geolocation(sid,  dateCreated,  dateUpdated,  dateExecuted,  accountSid,
                source,  globalCellId,  locationAreaId,  ageOfLocationInfo,  mobileCountryCode,
                mobileNetworkCode,  networkEntityAddress,  deviceLatitude,  deviceLongitude,
                physicalAddress,  internetAddress,  radius,  interval,  occurrence,
                geoLocationType,  geoLocationResponseTime,  status,  apiVersion,  uri);
    }

    public Geolocation setUri(URI uri) {
        return new Geolocation(sid,  dateCreated,  dateUpdated,  dateExecuted,  accountSid,
                source,  globalCellId,  locationAreaId,  ageOfLocationInfo,  mobileCountryCode,
                mobileNetworkCode,  networkEntityAddress,  deviceLatitude,  deviceLongitude,
                physicalAddress,  internetAddress,  radius,  interval,  occurrence,
                geoLocationType,  geoLocationResponseTime,  status,  apiVersion,  uri);
    }


    @NotThreadSafe
    public static final class Builder {
        
        private Sid sid;
        private DateTime dateCreated;
        private DateTime dateUpdated;
        private DateTime dateExecuted;
        private Sid accountSid;
        private String source;
        private String globalCellId;
        private String locationAreaId;
        private Integer ageOfLocationInfo;
        private Integer mobileCountryCode;
        private Integer mobileNetworkCode;
        private BigInteger networkEntityAddress;
        private String deviceLatitude;
        private String deviceLongitude;
        private String physicalAddress;
        private String internetAddress;
        private BigInteger radius;
        private BigInteger interval;
        private String occurrence;
        private String geoLocationType;
        private BigInteger geoLocationResponseTime;
        private String status;
        private String apiVersion;
        private URI uri;
        
        private Builder() {
            super();
        }
        
        public Geolocation build() {
            final DateTime now = DateTime.now();
            return new Geolocation(sid,  dateCreated,  dateUpdated,  dateExecuted,  accountSid,
                    source,  globalCellId,  locationAreaId,  ageOfLocationInfo,  mobileCountryCode,
                    mobileNetworkCode,  networkEntityAddress,  deviceLatitude,  deviceLongitude,
                    physicalAddress,  internetAddress,  radius,  interval,  occurrence,
                    geoLocationType,  geoLocationResponseTime,  status,  apiVersion,  uri);
        }

        public void setSid(Sid sid) {
            this.sid = sid;
        }

        public void setDateCreated(DateTime dateCreated) {
            this.dateCreated = dateCreated;
        }

        public void setDateUpdated(DateTime dateUpdated) {
            this.dateUpdated = dateUpdated;
        }

        public void setDateExecuted(DateTime dateExecuted) {
            this.dateExecuted = dateExecuted;
        }

        public void setAccountSid(Sid accountSid) {
            this.accountSid = accountSid;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public void setGlobalCellId(String globalCellId) {
            this.globalCellId = globalCellId;
        }

        public void setLocationAreaId(String locationAreaId) {
            this.locationAreaId = locationAreaId;
        }

        public void setAgeOfLocationInfo(Integer ageOfLocationInfo) {
            this.ageOfLocationInfo = ageOfLocationInfo;
        }

        public void setMobileCountryCode(Integer mobileCountryCode) {
            this.mobileCountryCode = mobileCountryCode;
        }

        public void setMobileNetworkCode(Integer mobileNetworkCode) {
            this.mobileNetworkCode = mobileNetworkCode;
        }

        public void setNetworkEntityAddress(BigInteger networkEntityAddress) {
            this.networkEntityAddress = networkEntityAddress;
        }

        public void setDeviceLatitude(String deviceLatitude) {
            this.deviceLatitude = deviceLatitude;
        }

        public void setDeviceLongitude(String deviceLongitude) {
            this.deviceLongitude = deviceLongitude;
        }

        public void setPhysicalAddress(String physicalAddress) {
            this.physicalAddress = physicalAddress;
        }

        public void setInternetAddress(String internetAddress) {
            this.internetAddress = internetAddress;
        }

        public void setRadius(BigInteger radius) {
            this.radius = radius;
        }

        public void setInterval(BigInteger interval) {
            this.interval = interval;
        }

        public void setOccurrence(String occurrence) {
            this.occurrence = occurrence;
        }

        public void setGeoLocationType(String geoLocationType) {
            this.geoLocationType = geoLocationType;
        }

        public void setGeoLocationResponseTime(BigInteger geoLocationResponseTime) {
            this.geoLocationResponseTime = geoLocationResponseTime;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public void setApiVersion(String apiVersion) {
            this.apiVersion = apiVersion;
        }

        public void setUri(URI uri) {
            this.uri = uri;
        }
        
        
    }
}

