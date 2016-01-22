
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

package org.mobicents.servlet.restcomm.http.converter;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.apache.commons.configuration.Configuration;
import org.joda.time.DateTime;
import org.mobicents.servlet.restcomm.entities.Geolocation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * @author fernando.mendioroz@telestax.com (Fernando Mendioroz)
 *
 */
public class GeolocationConverter extends AbstractConverter implements JsonSerializer<Geolocation> {

    public GeolocationConverter(final Configuration configuration) {
        super(configuration);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean canConvert(final Class klass) {
        return Geolocation.class.equals(klass);
    }

    @Override
    public void marshal(final Object object, final HierarchicalStreamWriter writer, final MarshallingContext context) {
        final Geolocation geolocation = (Geolocation) object;
        writer.startNode("GeoLocation");
        writeSid(geolocation.getSid(), writer);
        writeDateCreated(geolocation.getDateCreated(), writer);
        writeDateUpdated(geolocation.getDateUpdated(), writer);
        writeDateExecuted(geolocation.getDateExecuted(), writer);
        writeAccountSid(geolocation.getAccountSid(), writer);
        writeSource(geolocation.getSource(), writer);
        writeDeviceIdentifier(geolocation.getDeviceIdentifier(), writer);
        writeGlobalCellId(geolocation.getGlobalCellId(), writer);
        writeLocationAreaId(geolocation.getLocationAreaId(), writer);
        writeAgeOfLocationInfo(geolocation.getAgeOfLocationInfo(), writer);
        writeMobileCountryCode(geolocation.getMobileCountryCode(), writer);
        writeMobileNetworkCode(geolocation.getMobileNetworkCode(), writer);
        writeNetworkEntityAddress(geolocation.getNetworkEntityAddress(), writer);
        writeDeviceLatitude(geolocation.getDeviceLatitude(), writer);
        writeDeviceLongitude(geolocation.getDeviceLongitude(), writer);
        writePhysicalAddress(geolocation.getPhysicalAddress(), writer);
        writeInternetAddress(geolocation.getInternetAddress(), writer);
        writeRadius(geolocation.getRadius(), writer);
        writeInterval(geolocation.getInterval(), writer);
        writeOccurrence(geolocation.getOccurrence(), writer);
        writeGeoLocationType(geolocation.getGeoLocationType(), writer);
        writeGeoLocationResponseTime(geolocation.getGeoLocationResponseTime(), writer);
        writeStatus(geolocation.getStatus(), writer);
        writeApiVersion(geolocation.getApiVersion(), writer);
        writeUri(geolocation.getUri(), writer);
        writer.endNode();

    }

    @Override
    public JsonElement serialize(final Geolocation geolocation, final Type type, final JsonSerializationContext context) {
        final JsonObject object = new JsonObject();
        writeSid(geolocation.getSid(), object);
        writeDateCreated(geolocation.getDateCreated(), object);
        writeDateUpdated(geolocation.getDateUpdated(), object);
        writeDateExecuted(geolocation.getDateExecuted(), object);
        writeAccountSid(geolocation.getAccountSid(), object);
        writeSource(geolocation.getSource(), object);
        writeDeviceIdentifier(geolocation.getDeviceIdentifier(), object);
        writeGlobalCellId(geolocation.getGlobalCellId(), object);
        writeLocationAreaId(geolocation.getLocationAreaId(), object);
        writeAgeOfLocationInfo(geolocation.getAgeOfLocationInfo(), object);
        writeMobileCountryCode(geolocation.getMobileCountryCode(), object);
        writeMobileNetworkCode(geolocation.getMobileNetworkCode(), object);
        writeNetworkEntityAddress(geolocation.getNetworkEntityAddress(), object);
        writeDeviceLatitude(geolocation.getDeviceLatitude(), object);
        writeDeviceLongitude(geolocation.getDeviceLongitude(), object);
        writePhysicalAddress(geolocation.getPhysicalAddress(), object);
        writeInternetAddress(geolocation.getInternetAddress(), object);
        writeRadius(geolocation.getRadius(), object);
        writeInterval(geolocation.getInterval(), object);
        writeOccurrence(geolocation.getOccurrence(), object);
        writeGeoLocationType(geolocation.getGeoLocationType(), object);
        writeGeoLocationResponseTime(geolocation.getGeoLocationResponseTime(), object);
        writeStatus(geolocation.getStatus(), object);
        writeApiVersion(geolocation.getApiVersion(), object);
        writeUri(geolocation.getUri(), object);
        return object;
    }

    protected void writeDateExecuted(final DateTime dateExecuted, final HierarchicalStreamWriter writer) {
        writer.startNode("DateExecuted");
        writer.setValue(new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US).format(dateExecuted.toDate()));
        writer.endNode();
    }

    protected void writeDateExecuted(final DateTime dateExecuted, final JsonObject object) {
        object.addProperty("DateExecuted",
                new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US).format(dateExecuted.toDate()));
    }

    protected void writeSource(final String source, final HierarchicalStreamWriter writer) {
        writer.startNode("Source");
        writer.setValue(source);
        writer.endNode();
    }

    protected void writeSource(final String source, final JsonObject object) {
        object.addProperty("login", source);
    }

    protected void writeDeviceIdentifier(final String deviceIdentifier, final HierarchicalStreamWriter writer) {
        writer.startNode("DeviceIdentifier");
        writer.setValue(deviceIdentifier);
        writer.endNode();
    }

    protected void writeDeviceIdentifier(final String deviceIdentifier, final JsonObject object) {
        object.addProperty("DeviceIdentifier", deviceIdentifier);
    }

    protected void writeGlobalCellId(final String globalCellId, final HierarchicalStreamWriter writer) {
        writer.startNode("GlobalCellId");
        writer.setValue(globalCellId);
        writer.endNode();
    }

    protected void writeGlobalCellId(final String globalCellId, final JsonObject object) {
        object.addProperty("GlobalCellId", globalCellId);
    }

    protected void writeLocationAreaId(final String locationAreaId, final HierarchicalStreamWriter writer) {
        writer.startNode("LocationAreaId");
        writer.setValue(locationAreaId);
        writer.endNode();
    }

    protected void writeLocationAreaId(final String locationAreaId, final JsonObject object) {
        object.addProperty("LocationAreaId", locationAreaId);
    }

    protected void writeAgeOfLocationInfo(final Integer ageOfLocationInfo, final HierarchicalStreamWriter writer) {
        writer.startNode("AgeOfLocationInfo");
        writer.setValue(ageOfLocationInfo.toString());
        writer.endNode();
    }

    protected void writeAgeOfLocationInfo(final Integer ageOfLocationInfo, final JsonObject object) {
        object.addProperty("AgeOfLocationInfo", ageOfLocationInfo);
    }

    protected void writeMobileCountryCode(final Integer mobileCountryCode, final HierarchicalStreamWriter writer) {
        writer.startNode("MobileCountryCode");
        writer.setValue(mobileCountryCode.toString());
        writer.endNode();
    }

    protected void writeMobileCountryCode(final Integer mobileCountryCode, final JsonObject object) {
        object.addProperty("MobileCountryCode", mobileCountryCode);
    }

    protected void writeMobileNetworkCode(final Integer mobileNetworkCode, final HierarchicalStreamWriter writer) {
        writer.startNode("MobileNetworkCode");
        writer.setValue(mobileNetworkCode.toString());
        writer.endNode();
    }

    protected void writeMobileNetworkCode(final Integer mobileNetworkCode, final JsonObject object) {
        object.addProperty("MobileNetworkCode", mobileNetworkCode);
    }

    protected void writeNetworkEntityAddress(final BigInteger networkEntityAddress, final HierarchicalStreamWriter writer) {
        writer.startNode("NetworkEntityAddress");
        writer.setValue(networkEntityAddress.toString());
        writer.endNode();
    }

    protected void writeNetworkEntityAddress(final BigInteger networkEntityAddress, final JsonObject object) {
        object.addProperty("NetworkEntityAddress", networkEntityAddress);
    }

    protected void writeDeviceLatitude(final String deviceLatitude, final HierarchicalStreamWriter writer) {
        writer.startNode("DeviceLatitude");
        writer.setValue(deviceLatitude);
        writer.endNode();
    }

    protected void writeDeviceLatitude(final String deviceLatitude, final JsonObject object) {
        object.addProperty("DeviceLatitude", deviceLatitude);
    }

    protected void writeDeviceLongitude(final String deviceLongitude, final HierarchicalStreamWriter writer) {
        writer.startNode("DeviceLongitude");
        writer.setValue(deviceLongitude);
        writer.endNode();
    }

    protected void writeDeviceLongitude(final String deviceLongitude, final JsonObject object) {
        object.addProperty("DeviceLongitude", deviceLongitude);
    }

    protected void writePhysicalAddress(final String physicalAddress, final HierarchicalStreamWriter writer) {
        writer.startNode("PhysicalAddress");
        writer.setValue(physicalAddress);
        writer.endNode();
    }

    protected void writePhysicalAddress(final String physicalAddress, final JsonObject object) {
        object.addProperty("PhysicalAddress", physicalAddress);
    }

    protected void writeInternetAddress(final String internetAddress, final HierarchicalStreamWriter writer) {
        writer.startNode("InternetAddress");
        writer.setValue(internetAddress);
        writer.endNode();
    }

    protected void writeInternetAddress(final String internetAddress, final JsonObject object) {
        object.addProperty("InternetAddress", internetAddress);
    }

    protected void writeRadius(final BigInteger radius, final HierarchicalStreamWriter writer) {
        writer.startNode("Radius");
        writer.setValue(radius.toString());
        writer.endNode();
    }

    protected void writeRadius(final BigInteger radius, final JsonObject object) {
        object.addProperty("Radius", radius);
    }

    protected void writeInterval(final BigInteger interval, final HierarchicalStreamWriter writer) {
        writer.startNode("Interval");
        writer.setValue(interval.toString());
        writer.endNode();
    }

    protected void writeInterval(final BigInteger interval, final JsonObject object) {
        object.addProperty("Interval", interval);
    }

    protected void writeOccurrence(final String occurrence, final HierarchicalStreamWriter writer) {
        writer.startNode("Occurrence");
        writer.setValue(occurrence);
        writer.endNode();
    }

    protected void writeOccurrence(final String occurrence, final JsonObject object) {
        object.addProperty("Occurrence", occurrence);
    }

    protected void writeGeoLocationType(final String geoLocationType, final HierarchicalStreamWriter writer) {
        writer.startNode("GeoLocationType");
        writer.setValue(geoLocationType);
        writer.endNode();
    }

    protected void writeGeoLocationType(final String geoLocationType, final JsonObject object) {
        object.addProperty("GeoLocationType", geoLocationType);
    }

    protected void writeGeoLocationResponseTime(final BigInteger geoLocationResponseTime,
            final HierarchicalStreamWriter writer) {
        writer.startNode("GeoLocationResponseTime");
        writer.setValue(geoLocationResponseTime.toString());
        writer.endNode();
    }

    protected void writeGeoLocationResponseTime(final BigInteger geoLocationResponseTime, final JsonObject object) {
        object.addProperty("GeoLocationResponseTime", geoLocationResponseTime);
    }

}
