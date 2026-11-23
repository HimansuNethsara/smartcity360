-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               8.0.33 - MySQL Community Server - GPL
-- Server OS:                    Win64
-- HeidiSQL Version:             12.4.0.6659
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- Dumping database structure for smartcity360
CREATE DATABASE IF NOT EXISTS `smartcity360` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `smartcity360`;

-- Dumping structure for table smartcity360.audit_log
CREATE TABLE IF NOT EXISTS `audit_log` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned DEFAULT NULL,
  `action` varchar(60) NOT NULL,
  `entity_type` varchar(40) NOT NULL,
  `entity_id` bigint unsigned DEFAULT NULL,
  `detail_json` json DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_audit_log_users1_idx` (`user_id`),
  KEY `ix_audit_log_entity` (`entity_type`,`entity_id`),
  CONSTRAINT `fk_audit_log_users1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table smartcity360.city_policies
CREATE TABLE IF NOT EXISTS `city_policies` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(80) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `dsl_text` text NOT NULL,
  `enabled` tinyint(1) NOT NULL DEFAULT '1',
  `version` int NOT NULL DEFAULT '1',
  `created_by` bigint unsigned NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_UNIQUE` (`name`),
  KEY `fk_city_policies_users1_idx` (`created_by`),
  CONSTRAINT `fk_city_policies_users1` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table smartcity360.complaints
CREATE TABLE IF NOT EXISTS `complaints` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `citizen_id` bigint unsigned NOT NULL,
  `zone_id` bigint unsigned DEFAULT NULL,
  `streetlight_id` bigint unsigned DEFAULT NULL,
  `category` enum('STREETLIGHT','NOISE','POWER','PARKING','OTHER') NOT NULL,
  `title` varchar(150) NOT NULL,
  `description` text NOT NULL,
  `severity` enum('LOW','MEDIUM','HIGH','CRITICAL') NOT NULL,
  `status` enum('OPEN','IN_PROGRESS','ESCALATED','RESOLVED','CLOSED') NOT NULL DEFAULT 'OPEN',
  `current_level` enum('STAFF','SUPERVISOR','CITY_MANAGER') NOT NULL,
  `assigned_to` bigint unsigned DEFAULT NULL,
  `escalate_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `resolved_at` datetime DEFAULT NULL,
  `version` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fk_complaints_users1_idx` (`citizen_id`),
  KEY `fk_complaints_zones1_idx` (`zone_id`),
  KEY `fk_complaints_streetlights1_idx` (`streetlight_id`),
  KEY `fk_complaints_users2_idx` (`assigned_to`),
  KEY `ix_complaints_status_escalate` (`status`,`escalate_at`),
  CONSTRAINT `fk_complaints_streetlights1` FOREIGN KEY (`streetlight_id`) REFERENCES `streetlights` (`id`),
  CONSTRAINT `fk_complaints_users1` FOREIGN KEY (`citizen_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_complaints_users2` FOREIGN KEY (`assigned_to`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_complaints_zones1` FOREIGN KEY (`zone_id`) REFERENCES `zones` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table smartcity360.complaint_escalations
CREATE TABLE IF NOT EXISTS `complaint_escalations` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `complaint_id` bigint unsigned NOT NULL,
  `from_level` enum('STAFF','SUPERVISOR','CITY_MANAGER') DEFAULT NULL,
  `to_level` enum('STAFF','SUPERVISOR','CITY_MANAGER') NOT NULL,
  `handled_by` bigint unsigned DEFAULT NULL,
  `reason` enum('INITIAL','SEVERITY','TIMEOUT','MANUAL') NOT NULL,
  `note` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_complaint_escalations_complaints1_idx` (`complaint_id`),
  KEY `fk_complaint_escalations_users1_idx` (`handled_by`),
  CONSTRAINT `fk_complaint_escalations_complaints1` FOREIGN KEY (`complaint_id`) REFERENCES `complaints` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_complaint_escalations_users1` FOREIGN KEY (`handled_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table smartcity360.energy_meters
CREATE TABLE IF NOT EXISTS `energy_meters` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `meter_code` varchar(30) NOT NULL,
  `zone_id` bigint unsigned NOT NULL,
  `status` enum('ACTIVE','FAULTY','OFFLINE') NOT NULL DEFAULT 'ACTIVE',
  `installed_at` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `meter_code_UNIQUE` (`meter_code`),
  KEY `fk_energy_meters_zones1_idx` (`zone_id`),
  CONSTRAINT `fk_energy_meters_zones1` FOREIGN KEY (`zone_id`) REFERENCES `zones` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table smartcity360.escalation_policies
CREATE TABLE IF NOT EXISTS `escalation_policies` (
  `id` tinyint unsigned NOT NULL AUTO_INCREMENT,
  `severity` enum('LOW','MEDIUM','HIGH','CRITICAL') NOT NULL,
  `start_level` enum('STAFF','SUPERVISOR','CITY_MANAGER') NOT NULL,
  `staff_timeout_min` smallint unsigned NOT NULL,
  `supervisor_timeout_min` smallint unsigned NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `severity_UNIQUE` (`severity`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table smartcity360.meter_readings
CREATE TABLE IF NOT EXISTS `meter_readings` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `meter_id` bigint unsigned NOT NULL,
  `kwh` decimal(12,3) NOT NULL,
  `voltage` decimal(6,2) DEFAULT NULL,
  `read_at` datetime(3) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_meter_readings_energy_meters1_idx` (`meter_id`) /*!80000 INVISIBLE */,
  KEY `ix_meter_readings_meter_read` (`meter_id`,`read_at`),
  CONSTRAINT `fk_meter_readings_energy_meters1` FOREIGN KEY (`meter_id`) REFERENCES `energy_meters` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=432 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table smartcity360.meter_stream_layers
CREATE TABLE IF NOT EXISTS `meter_stream_layers` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `meter_id` bigint unsigned NOT NULL,
  `layer_type` enum('AUTHENTICATION','COMPRESSION','ENCRYPTION') NOT NULL,
  `layer_order` tinyint unsigned NOT NULL,
  `config` varchar(255) DEFAULT NULL,
  `enabled` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_msl_meter_type` (`meter_id`,`layer_type`) /*!80000 INVISIBLE */,
  UNIQUE KEY `uq_msl_meter_order` (`meter_id`,`layer_order`) USING BTREE /*!80000 INVISIBLE */,
  KEY `fk_meter_stream_layers_energy_meters1_idx` (`meter_id`) /*!80000 INVISIBLE */,
  CONSTRAINT `fk_meter_stream_layers_energy_meters1` FOREIGN KEY (`meter_id`) REFERENCES `energy_meters` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table smartcity360.notifications
CREATE TABLE IF NOT EXISTS `notifications` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned NOT NULL,
  `type` enum('COMPLAINT_UPDATE','COMPLAINT_ESCALATED','POLICY_VIOLATION','REPORT_READY','SYSTEM') NOT NULL,
  `title` varchar(120) NOT NULL,
  `message` varchar(500) NOT NULL,
  `is_read` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_notifications_users1_idx` (`user_id`),
  KEY `ix_notifications_user_read` (`user_id`,`is_read`),
  CONSTRAINT `fk_notifications_users1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=153 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table smartcity360.policy_evaluations
CREATE TABLE IF NOT EXISTS `policy_evaluations` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `policy_id` bigint unsigned NOT NULL,
  `zone_id` bigint unsigned DEFAULT NULL,
  `input_json` json NOT NULL,
  `outcome` enum('PASS','VIOLATION','ERROR') NOT NULL,
  `action_result` varchar(255) DEFAULT NULL,
  `evaluated_at` datetime(3) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_policy_evaluations_city_policies1_idx` (`policy_id`),
  KEY `fk_policy_evaluations_zones1_idx` (`zone_id`),
  CONSTRAINT `fk_policy_evaluations_city_policies1` FOREIGN KEY (`policy_id`) REFERENCES `city_policies` (`id`),
  CONSTRAINT `fk_policy_evaluations_zones1` FOREIGN KEY (`zone_id`) REFERENCES `zones` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=500 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table smartcity360.policy_history
CREATE TABLE IF NOT EXISTS `policy_history` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `policy_id` bigint unsigned NOT NULL,
  `version_no` int NOT NULL,
  `dsl_text` text NOT NULL,
  `changed_by` bigint unsigned NOT NULL,
  `changed_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_policy_history_version` (`policy_id`,`version_no`),
  KEY `fk_policy_history_city_policies1_idx` (`policy_id`),
  KEY `fk_policy_history_users1_idx` (`changed_by`),
  CONSTRAINT `fk_policy_history_city_policies1` FOREIGN KEY (`policy_id`) REFERENCES `city_policies` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_policy_history_users1` FOREIGN KEY (`changed_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table smartcity360.report_jobs
CREATE TABLE IF NOT EXISTS `report_jobs` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `requested_by` bigint unsigned NOT NULL,
  `report_type` enum('ENERGY_USAGE','COMPLAINT_SUMMARY','POLICY_VIOLATIONS','STREETLIGHT_STATUS') NOT NULL,
  `params_json` json DEFAULT NULL,
  `status` enum('QUEUED','RUNNING','DONE','FAILED') NOT NULL DEFAULT 'QUEUED',
  `result_json` longtext,
  `error_message` varchar(500) DEFAULT NULL,
  `requested_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `completed_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_report_jobs_users1_idx` (`requested_by`),
  CONSTRAINT `fk_report_jobs_users1` FOREIGN KEY (`requested_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table smartcity360.roles
CREATE TABLE IF NOT EXISTS `roles` (
  `id` tinyint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(30) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_UNIQUE` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table smartcity360.sensors
CREATE TABLE IF NOT EXISTS `sensors` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `sensor_code` varchar(30) NOT NULL,
  `sensor_type` enum('NOISE','TEMPERATURE','AIR_QUALITY') NOT NULL,
  `zone_id` bigint unsigned NOT NULL,
  `unit` varchar(10) NOT NULL,
  `status` enum('ACTIVE','OFFLINE') NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY (`id`),
  UNIQUE KEY `sensor_code_UNIQUE` (`sensor_code`),
  KEY `fk_sensors_zones1_idx` (`zone_id`),
  CONSTRAINT `fk_sensors_zones1` FOREIGN KEY (`zone_id`) REFERENCES `zones` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table smartcity360.sensor_readings
CREATE TABLE IF NOT EXISTS `sensor_readings` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `sensor_id` bigint unsigned NOT NULL,
  `value` decimal(10,3) NOT NULL,
  `read_at` datetime(3) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_sensor_readings_sensors1_idx` (`sensor_id`),
  KEY `ix_sensor_readings_sensor_read` (`sensor_id`,`read_at`),
  CONSTRAINT `fk_sensor_readings_sensors1` FOREIGN KEY (`sensor_id`) REFERENCES `sensors` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=800 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table smartcity360.stream_audit
CREATE TABLE IF NOT EXISTS `stream_audit` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `meter_id` bigint unsigned NOT NULL,
  `layers_applied` varchar(120) NOT NULL,
  `raw_bytes` int unsigned NOT NULL,
  `final_bytes` int unsigned NOT NULL,
  `processed_at` datetime(3) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_stream_audit_energy_meters1_idx` (`meter_id`),
  CONSTRAINT `fk_stream_audit_energy_meters1` FOREIGN KEY (`meter_id`) REFERENCES `energy_meters` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table smartcity360.streetlights
CREATE TABLE IF NOT EXISTS `streetlights` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `type_id` smallint unsigned NOT NULL,
  `zone_id` bigint unsigned NOT NULL,
  `latitude` decimal(9,6) NOT NULL,
  `longitude` decimal(9,6) NOT NULL,
  `status` enum('ON','OFF','FAULTY') NOT NULL DEFAULT 'ON',
  `brightness` tinyint unsigned NOT NULL DEFAULT '100',
  `installed_at` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_streetlights_streetlight_types1_idx` (`type_id`),
  KEY `fk_streetlights_zones1_idx` (`zone_id`),
  CONSTRAINT `fk_streetlights_streetlight_types1` FOREIGN KEY (`type_id`) REFERENCES `streetlight_types` (`id`),
  CONSTRAINT `fk_streetlights_zones1` FOREIGN KEY (`zone_id`) REFERENCES `zones` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=251 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table smartcity360.streetlight_types
CREATE TABLE IF NOT EXISTS `streetlight_types` (
  `id` smallint unsigned NOT NULL AUTO_INCREMENT,
  `type_code` varchar(30) NOT NULL,
  `model` varchar(60) NOT NULL,
  `lamp_type` enum('LED','SODIUM','HALOGEN') NOT NULL,
  `wattage` smallint unsigned NOT NULL,
  `max_lumens` int unsigned NOT NULL,
  `color_temp_k` smallint unsigned NOT NULL,
  `firmware` varchar(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `type_code_UNIQUE` (`type_code`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table smartcity360.users
CREATE TABLE IF NOT EXISTS `users` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` char(64) NOT NULL,
  `email` varchar(120) NOT NULL,
  `full_name` varchar(100) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `enabled` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username_UNIQUE` (`username`),
  UNIQUE KEY `email_UNIQUE` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for table smartcity360.user_roles
CREATE TABLE IF NOT EXISTS `user_roles` (
  `user_id` bigint unsigned NOT NULL,
  `role_id` tinyint unsigned NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`),
  KEY `fk_users_has_roles_roles1_idx` (`role_id`),
  KEY `fk_users_has_roles_users1_idx` (`user_id`),
  CONSTRAINT `fk_user_roles_roles1` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`),
  CONSTRAINT `fk_user_roles_users1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for view smartcity360.v_realm_groups
-- Creating temporary table to overcome VIEW dependency errors
CREATE TABLE `v_realm_groups` (
	`username` VARCHAR(50) NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`group_name` VARCHAR(30) NOT NULL COLLATE 'utf8mb4_0900_ai_ci'
) ENGINE=MyISAM;

-- Dumping structure for view smartcity360.v_realm_users
-- Creating temporary table to overcome VIEW dependency errors
CREATE TABLE `v_realm_users` (
	`username` VARCHAR(50) NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`password` CHAR(64) NOT NULL COLLATE 'utf8mb4_0900_ai_ci'
) ENGINE=MyISAM;

-- Dumping structure for table smartcity360.zones
CREATE TABLE IF NOT EXISTS `zones` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `parent_id` bigint unsigned DEFAULT NULL,
  `zone_type` enum('CITY','DISTRICT','STREET','BUILDING') NOT NULL,
  `name` varchar(100) NOT NULL,
  `code` varchar(30) NOT NULL,
  `land_use` enum('RESIDENTIAL','COMMERCIAL','INDUSTRIAL','MIXED') NOT NULL,
  `power_status` enum('ON','OFF') NOT NULL DEFAULT 'ON',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code_UNIQUE` (`code`),
  UNIQUE KEY `uq_zones_parent_name` (`parent_id`,`name`) /*!80000 INVISIBLE */,
  KEY `fk_zones_parent_id_idx` (`parent_id`),
  CONSTRAINT `fk_zones_parent_id` FOREIGN KEY (`parent_id`) REFERENCES `zones` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Data exporting was unselected.

-- Dumping structure for view smartcity360.v_realm_groups
-- Removing temporary table and create final VIEW structure
DROP TABLE IF EXISTS `v_realm_groups`;
CREATE ALGORITHM=UNDEFINED SQL SECURITY DEFINER VIEW `v_realm_groups` AS select `u`.`username` AS `username`,`r`.`name` AS `group_name` from ((`users` `u` join `user_roles` `ur` on((`ur`.`user_id` = `u`.`id`))) join `roles` `r` on((`r`.`id` = `ur`.`role_id`)));

-- Dumping structure for view smartcity360.v_realm_users
-- Removing temporary table and create final VIEW structure
DROP TABLE IF EXISTS `v_realm_users`;
CREATE ALGORITHM=UNDEFINED SQL SECURITY DEFINER VIEW `v_realm_users` AS select `users`.`username` AS `username`,`users`.`password` AS `password` from `users` where (`users`.`enabled` = 1);

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
