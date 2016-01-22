#SQL Script for MySQL/MariaDB to update DB with the schema changes for issue GeoLocation API draft
#Date: Jan 20
#Author: Fernando Mendioroz

#To run the script use mysql client:
#mysql -u yourusername -p yourpassword yourdatabase < sql_update_script.sql

USE restcomm;

SELECT IFNULL(table_name, '') INTO @tableName
FROM information_schema.columns 
WHERE table_name = 'restcomm_geolocation'

IF @tableName = '' THEN 
    -- ALTER COMMAND GOES HERE --
#Drop and create again the "restcomm_applications" table
CREATE TABLE restcomm_geolocation (
sid VARCHAR(34) NOT NULL PRIMARY KEY,
date_created DATETIME NOT NULL,
date_updated DATETIME NOT NULL,
date_executed DATETIME NOT NULL,
account_sid VARCHAR(34) NOT NULL,
source VARCHAR(30) NOT NULL,
global_cell_id VARCHAR(10),
location_area_id VARCHAR(10),
age_of_location_info INTEGER,
mobile_country_code INTEGER,
mobile_network_code INTEGER,
network_entity_address BIGINT,
device_latitude VARCHAR(15),
device_longitude VARCHAR(15),
physical_address VARCHAR(30),
internet_address VARCHAR(36), 
radius BIGINT, 
interval BIGINT, occurrence VARCHAR(500),
geo_location_type VARCHAR(15) NOT NULL, 
geo_location_response_time BIGINT, 
status VARCHAR(30), 
api_version VARCHAR(10) NOT NULL,
uri MEDIUMTEXT NOT NULL
);
END IF;
