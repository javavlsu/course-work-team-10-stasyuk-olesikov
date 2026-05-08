-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: localhost    Database: myngdb
-- ------------------------------------------------------
-- Server version	8.0.41

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `ban`
--

DROP TABLE IF EXISTS `ban`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ban` (
  `id` int NOT NULL AUTO_INCREMENT,
  `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `start_time` timestamp NOT NULL,
  `end_time` timestamp NOT NULL,
  `fk_mod` int NOT NULL,
  `fk_user` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_ban_mod` (`fk_mod`),
  KEY `fk_ban_user` (`fk_user`),
  CONSTRAINT `fk_ban_mod` FOREIGN KEY (`fk_mod`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ban_user` FOREIGN KEY (`fk_user`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_ban_time` CHECK ((`end_time` > `start_time`))
) ENGINE=InnoDB AUTO_INCREMENT=79 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `collection`
--

DROP TABLE IF EXISTS `collection`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `collection` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `fk_user` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`,`fk_user`),
  KEY `fk_collection_user` (`fk_user`),
  CONSTRAINT `fk_collection_user` FOREIGN KEY (`fk_user`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `dev_application`
--

DROP TABLE IF EXISTS `dev_application`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dev_application` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `github_username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `fk_user` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_dev_app_user` (`fk_user`),
  CONSTRAINT `fk_dev_app_user` FOREIGN KEY (`fk_user`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `game`
--

DROP TABLE IF EXISTS `game`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `game` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `descr` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `repo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `genre` enum('action','adventure','rpg','simulation','strategy','sports','puzzle','horror','platformer','sandbox','visual-novel','roguelike') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fk_dev` int DEFAULT NULL,
  `image` mediumblob,
  PRIMARY KEY (`id`),
  UNIQUE KEY `repo` (`repo`),
  KEY `fk_game_dev` (`fk_dev`),
  CONSTRAINT `fk_game_dev` FOREIGN KEY (`fk_dev`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `game_collection`
--

DROP TABLE IF EXISTS `game_collection`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `game_collection` (
  `fk_game` int NOT NULL,
  `fk_collection` int NOT NULL,
  PRIMARY KEY (`fk_game`,`fk_collection`),
  KEY `fk_game_collection_collection` (`fk_collection`),
  CONSTRAINT `fk_game_collection_collection` FOREIGN KEY (`fk_collection`) REFERENCES `collection` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_game_collection_game` FOREIGN KEY (`fk_game`) REFERENCES `game` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `game_stats`
--

DROP TABLE IF EXISTS `game_stats`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `game_stats` (
  `id` int NOT NULL AUTO_INCREMENT,
  `fk_game` int NOT NULL,
  `event_type` enum('view','launch') COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Тип события: просмотр или запуск',
  `event_date` date NOT NULL COMMENT 'Дата события',
  `count` int NOT NULL DEFAULT '1' COMMENT 'Количество событий за день',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_daily_stat` (`fk_game`,`event_type`,`event_date`),
  KEY `idx_game_stats_game` (`fk_game`),
  KEY `idx_game_stats_date` (`event_date`),
  CONSTRAINT `fk_game_stats_game` FOREIGN KEY (`fk_game`) REFERENCES `game` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `game_tag`
--

DROP TABLE IF EXISTS `game_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `game_tag` (
  `fk_game` int NOT NULL,
  `fk_tag` int NOT NULL,
  PRIMARY KEY (`fk_game`,`fk_tag`),
  KEY `fk_game_tag_tag` (`fk_tag`),
  CONSTRAINT `fk_game_tag_game` FOREIGN KEY (`fk_game`) REFERENCES `game` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_game_tag_tag` FOREIGN KEY (`fk_tag`) REFERENCES `tag` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `game_version`
--

DROP TABLE IF EXISTS `game_version`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `game_version` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `commit_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `changelog` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `fk_game` int NOT NULL,
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `files` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`),
  UNIQUE KEY `fk_game` (`fk_game`,`commit_hash`),
  CONSTRAINT `fk_game_version_game` FOREIGN KEY (`fk_game`) REFERENCES `game` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `moderation_verdict`
--

DROP TABLE IF EXISTS `moderation_verdict`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `moderation_verdict` (
  `id` int NOT NULL AUTO_INCREMENT,
  `approved` tinyint(1) DEFAULT NULL,
  `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fk_game_version` int DEFAULT NULL,
  `fk_dev_application` int DEFAULT NULL,
  `fk_review` int DEFAULT NULL,
  `fk_mod` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_moderation_game_version` (`fk_game_version`),
  UNIQUE KEY `uq_moderation_review` (`fk_review`),
  KEY `fk_moderation_dev_app` (`fk_dev_application`),
  KEY `fk_moderation_verdict_mod` (`fk_mod`),
  CONSTRAINT `fk_moderation_dev_app` FOREIGN KEY (`fk_dev_application`) REFERENCES `dev_application` (`id`),
  CONSTRAINT `fk_moderation_game_version` FOREIGN KEY (`fk_game_version`) REFERENCES `game_version` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_moderation_review` FOREIGN KEY (`fk_review`) REFERENCES `review` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_moderation_verdict_mod` FOREIGN KEY (`fk_mod`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `chk_moderation_one_target` CHECK (((((`fk_game_version` is not null) + (`fk_dev_application` is not null)) + (`fk_review` is not null)) = 1))
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `notification`
--

DROP TABLE IF EXISTS `notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `type` enum('system','warning','moderation','news') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=108 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `review`
--

DROP TABLE IF EXISTS `review`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `review` (
  `id` int NOT NULL AUTO_INCREMENT,
  `rating` tinyint unsigned NOT NULL,
  `text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `report_count` smallint unsigned DEFAULT '0',
  `fk_user` int NOT NULL,
  `fk_game` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `fk_user` (`fk_user`,`fk_game`),
  KEY `fk_review_game` (`fk_game`),
  CONSTRAINT `fk_review_game` FOREIGN KEY (`fk_game`) REFERENCES `game` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_review_user` FOREIGN KEY (`fk_user`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_review_rating` CHECK ((`rating` between 1 and 5))
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tag`
--

DROP TABLE IF EXISTS `tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tag` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  CONSTRAINT `chk_tag_format` CHECK (regexp_like(`name`,_utf8mb4'^[a-z0-9]+(-[a-z0-9]+)*$'))
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password_hash` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `bio` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `profile_pic` longblob,
  `registered_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `role` enum('user','dev','mod','admin') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'user',
  `github_username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `github_username` (`github_username`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_notification`
--

DROP TABLE IF EXISTS `user_notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_notification` (
  `fk_user` int NOT NULL,
  `fk_notification` int NOT NULL,
  PRIMARY KEY (`fk_user`,`fk_notification`),
  KEY `fk_notification` (`fk_notification`),
  CONSTRAINT `user_notification_ibfk_1` FOREIGN KEY (`fk_user`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `user_notification_ibfk_2` FOREIGN KEY (`fk_notification`) REFERENCES `notification` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `warning`
--

DROP TABLE IF EXISTS `warning`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `warning` (
  `id` int NOT NULL AUTO_INCREMENT,
  `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `fk_mod` int NOT NULL,
  `fk_user` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_warning_mod` (`fk_mod`),
  KEY `fk_warning_user` (`fk_user`),
  CONSTRAINT `fk_warning_mod` FOREIGN KEY (`fk_mod`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_warning_user` FOREIGN KEY (`fk_user`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=92 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-08 19:26:55
-- темуля стрельбануля
