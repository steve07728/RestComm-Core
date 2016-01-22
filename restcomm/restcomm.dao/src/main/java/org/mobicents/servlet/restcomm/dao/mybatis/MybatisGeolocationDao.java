
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

package org.mobicents.servlet.restcomm.dao.mybatis;

import static org.mobicents.servlet.restcomm.dao.DaoUtils.readBigInteger;
import static org.mobicents.servlet.restcomm.dao.DaoUtils.readDateTime;
import static org.mobicents.servlet.restcomm.dao.DaoUtils.readInteger;
import static org.mobicents.servlet.restcomm.dao.DaoUtils.readSid;
import static org.mobicents.servlet.restcomm.dao.DaoUtils.readString;
import static org.mobicents.servlet.restcomm.dao.DaoUtils.readUri;
import static org.mobicents.servlet.restcomm.dao.DaoUtils.writeDateTime;
import static org.mobicents.servlet.restcomm.dao.DaoUtils.writeSid;
import static org.mobicents.servlet.restcomm.dao.DaoUtils.writeUri;

import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.joda.time.DateTime;
import org.mobicents.servlet.restcomm.dao.GeolocationDao;
import org.mobicents.servlet.restcomm.entities.Geolocation;
import org.mobicents.servlet.restcomm.entities.Sid;

/**
 * @author Fernando Mendioroz
 *
 */
public class MybatisGeolocationDao implements GeolocationDao {

    private static final String namespace = "org.mobicents.servlet.sip.restcomm.dao.GeolocationDao.";
    private final SqlSessionFactory sessions;

    public MybatisGeolocationDao(final SqlSessionFactory sessions) {
        super();
        this.sessions = sessions;
    }

    @Override
    public void addGeolocation(Geolocation gl) {
        final SqlSession session = sessions.openSession();
        try {
            session.insert(namespace + "addGeolocation", toMap(gl));
            session.commit();
        } finally {
            session.close();
        }

    }

    @Override
    public Geolocation getGeolocation(Sid sid) {
        return getGeolocation(namespace + "getGeolocation", sid.toString());
    }

    private Geolocation getGeolocation(final String selector, final String parameter) {
        final SqlSession session = sessions.openSession();
        try {
            final Map<String, Object> result = session.selectOne(selector, parameter);
            if (result != null) {
                return toGeolocation(result);
            } else {
                return null;
            }
        } finally {
            session.close();
        }
    }

    @Override
    public List<Geolocation> getGeolocations(Sid accountSid) {
        final SqlSession session = sessions.openSession();
        try {
            final List<Map<String, Object>> results = session.selectList(namespace + "getGeolocations", accountSid.toString());
            final List<Geolocation> geolocations = new ArrayList<Geolocation>();
            if (results != null && !results.isEmpty()) {
                for (final Map<String, Object> result : results) {
                    geolocations.add(toGeolocation(result));
                }
            }
            return geolocations;
        } finally {
            session.close();
        }
    }

    @Override
    public void removeGeolocation(Sid sid) {
        removeGeolocations(namespace + "removeGeolocation", sid);

    }

    @Override
    public void removeGeolocations(final Sid accountSid) {
        removeGeolocations(namespace + "removeGeolocations", accountSid);
    }

    private void removeGeolocations(final String selector, final Sid sid) {
        final SqlSession session = sessions.openSession();
        try {
            session.delete(selector, sid.toString());
            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public void updateGeolocation(Geolocation gl) {
        final SqlSession session = sessions.openSession();
        try {
            session.update(namespace + "updateGeolocation", toMap(gl));
            session.commit();
        } finally {
            session.close();
        }

    }

    private Map<String, Object> toMap(Geolocation gl) {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("sid", writeSid(gl.getSid()));
        map.put("date_created", writeDateTime(gl.getDateCreated()));
        map.put("date_updated", writeDateTime(gl.getDateUpdated()));
        map.put("date_executed", writeDateTime(gl.getDateExecuted()));
        map.put("account_sid", writeSid(gl.getAccountSid()));
        map.put("source", gl.getSource());
        map.put("device_identifier", gl.getDeviceIdentifier());
        map.put("global_cell_id", gl.getGlobalCellId());
        map.put("location_area_id", gl.getLocationAreaId());
        map.put("age_of_location_info", gl.getAgeOfLocationInfo());
        map.put("mobile_country_code", gl.getMobileCountryCode());
        map.put("mobile_network_code", gl.getMobileCountryCode());
        map.put("network_entity_address", gl.getNetworkEntityAddress());
        map.put("device_latitude", gl.getDeviceIdentifier());
        map.put("device_longitude", gl.getDeviceLongitude());
        map.put("physical_address", gl.getPhysicalAddress());
        map.put("internet_address", gl.getInternetAddress());
        map.put("radius", gl.getRadius());
        map.put("interval", gl.getInterval());
        map.put("occurrence", gl.getOccurrence());
        map.put("geo_location_type", gl.getGeoLocationType());
        map.put("geo_location_response_time", gl.getGeoLocationResponseTime());
        map.put("status", gl.getStatus());
        map.put("api_version", gl.getApiVersion());
        map.put("uri", writeUri(gl.getUri()));
        return map;
    }

    private Geolocation toGeolocation(final Map<String, Object> map) {
        final Sid sid = readSid(map.get("sid"));
        final DateTime date_created = readDateTime(map.get("date_created"));
        final DateTime date_updated = readDateTime(map.get("date_updated"));
        final DateTime date_executed = readDateTime(map.get("date_executed"));
        final Sid account_sid = readSid(map.get("account_sid"));
        final String source = readString(map.get("source"));
        final String device_identifier = readString(map.get("device_identifier"));
        final String global_cell_id = readString(map.get("global_cell_id"));
        final String location_area_id = readString(map.get("location_area_id"));
        final Integer age_of_location_info = readInteger(map.get("age_of_location_info"));
        final Integer mobile_country_code = readInteger(map.get("mobile_country_code"));
        final Integer mobile_network_code = readInteger(map.get("mobile_network_code"));
        final BigInteger network_entity_address = readBigInteger(map.get("network_entity_address"));
        final String device_latitude = readString(map.get("device_latitude"));
        final String device_longitude = readString(map.get("device_longitude"));
        final String physical_address = readString(map.get("physical_address"));
        final String internet_address = readString(map.get("internet_address"));
        final BigInteger radius = readBigInteger(map.get("radius"));
        final BigInteger interval = readBigInteger(map.get("interval"));
        final String occurrence = readString(map.get("occurrence"));
        final String geo_location_type = readString(map.get("geo_location_type"));
        final BigInteger geo_location_response_time = readBigInteger(map.get("geo_location_response_time"));
        final String status = readString(map.get("status"));
        final String api_version = readString(map.get("api_version"));
        final URI uri = readUri(map.get("uri"));
        return new Geolocation(sid, date_created, date_updated, date_executed, account_sid, source, device_identifier,
                global_cell_id, location_area_id, age_of_location_info, mobile_country_code, mobile_network_code,
                network_entity_address, device_latitude, device_longitude, physical_address, internet_address, radius, interval,
                occurrence, geo_location_type, geo_location_response_time, status, api_version, uri);
    }

}
