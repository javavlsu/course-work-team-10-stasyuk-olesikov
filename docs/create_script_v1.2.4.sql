-- MySQL dump 10.13  Distrib 9.2.0, for Win64 (x86_64)
--
-- Host: localhost    Database: myngdb
-- ------------------------------------------------------
-- Server version	9.2.0

CREATE DATABASE IF NOT EXISTS myngdb 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Выбор созданной базы
USE myngdb;

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
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
  `reason` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ban`
--

LOCK TABLES `ban` WRITE;
/*!40000 ALTER TABLE `ban` DISABLE KEYS */;
/*!40000 ALTER TABLE `ban` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `collection`
--

DROP TABLE IF EXISTS `collection`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `collection` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `fk_user` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`,`fk_user`),
  KEY `fk_collection_user` (`fk_user`),
  CONSTRAINT `fk_collection_user` FOREIGN KEY (`fk_user`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `collection`
--

LOCK TABLES `collection` WRITE;
/*!40000 ALTER TABLE `collection` DISABLE KEYS */;
/*!40000 ALTER TABLE `collection` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dev_application`
--

DROP TABLE IF EXISTS `dev_application`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dev_application` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `github_username` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `text` text COLLATE utf8mb4_unicode_ci,
  `fk_user` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `fk_user` (`fk_user`),
  CONSTRAINT `fk_dev_app_user` FOREIGN KEY (`fk_user`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dev_application`
--

LOCK TABLES `dev_application` WRITE;
/*!40000 ALTER TABLE `dev_application` DISABLE KEYS */;
/*!40000 ALTER TABLE `dev_application` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `game`
--

DROP TABLE IF EXISTS `game`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `game` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `descr` text COLLATE utf8mb4_unicode_ci,
  `repo` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `genre` enum('action','adventure','rpg','simulation','strategy','sports','puzzle','horror','platformer','sandbox','visual-novel','roguelike') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fk_dev` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `repo` (`repo`),
  KEY `fk_game_dev` (`fk_dev`),
  CONSTRAINT `fk_game_dev` FOREIGN KEY (`fk_dev`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `game`
--

LOCK TABLES `game` WRITE;
/*!40000 ALTER TABLE `game` DISABLE KEYS */;
INSERT INTO `game` VALUES (1,'Cyber Knight','A futuristic cyberpunk RPG.','https://github.com/alice/cyber-knight','rpg',1),(2,'Pixel Adventure','Retro-style platformer game.','https://github.com/alice/pixel-adventure','platformer',1),(3,'Mystery Manor','Solve puzzles and explore the haunted mansion.','https://github.com/alice/mystery-manor','puzzle',1);
/*!40000 ALTER TABLE `game` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `game_collection`
--

LOCK TABLES `game_collection` WRITE;
/*!40000 ALTER TABLE `game_collection` DISABLE KEYS */;
/*!40000 ALTER TABLE `game_collection` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `game_tag`
--

LOCK TABLES `game_tag` WRITE;
/*!40000 ALTER TABLE `game_tag` DISABLE KEYS */;
/*!40000 ALTER TABLE `game_tag` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `game_version`
--

DROP TABLE IF EXISTS `game_version`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `game_version` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `commit_hash` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `changelog` text COLLATE utf8mb4_unicode_ci,
  `fk_game` int NOT NULL,
  `name` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `archive_file` longblob NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `fk_game` (`fk_game`,`commit_hash`),
  CONSTRAINT `fk_game_version_game` FOREIGN KEY (`fk_game`) REFERENCES `game` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `game_version`
--

LOCK TABLES `game_version` WRITE;
/*!40000 ALTER TABLE `game_version` DISABLE KEYS */;
/*!40000 ALTER TABLE `game_version` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `moderation_verdict`
--

DROP TABLE IF EXISTS `moderation_verdict`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `moderation_verdict` (
  `id` int NOT NULL AUTO_INCREMENT,
  `approved` tinyint(1) DEFAULT NULL,
  `reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
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
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `moderation_verdict`
--

LOCK TABLES `moderation_verdict` WRITE;
/*!40000 ALTER TABLE `moderation_verdict` DISABLE KEYS */;
INSERT INTO `moderation_verdict` VALUES (6,1,NULL,NULL,NULL,1,3),(7,1,NULL,NULL,NULL,2,3),(8,1,NULL,NULL,NULL,3,3),(9,0,'Offensive language detected.',NULL,NULL,4,3),(10,0,'Spam or misleading content.',NULL,NULL,5,3);
/*!40000 ALTER TABLE `moderation_verdict` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notification`
--

DROP TABLE IF EXISTS `notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `type` enum('system','warning','moderation','news') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `text` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification`
--

LOCK TABLES `notification` WRITE;
/*!40000 ALTER TABLE `notification` DISABLE KEYS */;
/*!40000 ALTER TABLE `notification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `review`
--

DROP TABLE IF EXISTS `review`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `review` (
  `id` int NOT NULL AUTO_INCREMENT,
  `rating` tinyint unsigned NOT NULL,
  `text` text COLLATE utf8mb4_unicode_ci,
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
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `review`
--

LOCK TABLES `review` WRITE;
/*!40000 ALTER TABLE `review` DISABLE KEYS */;
INSERT INTO `review` VALUES (1,5,'Amazing RPG experience, loved the cyberpunk setting!','2026-02-22 16:26:46',0,2,1),(2,4,'Fun platformer with charming graphics.','2026-02-22 16:26:46',0,2,2),(3,3,'Interesting puzzles but a bit too short.','2026-02-22 16:26:46',0,1,3),(4,2,'Not my style, too many bugs.','2026-02-22 16:26:46',0,4,2),(5,1,'Terrible gameplay, crashes constantly.','2026-02-22 16:26:46',0,3,1);
/*!40000 ALTER TABLE `review` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tag`
--

DROP TABLE IF EXISTS `tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tag` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  CONSTRAINT `chk_tag_format` CHECK (regexp_like(`name`,_utf8mb4'^[a-z0-9]+(-[a-z0-9]+)*$'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tag`
--

LOCK TABLES `tag` WRITE;
/*!40000 ALTER TABLE `tag` DISABLE KEYS */;
/*!40000 ALTER TABLE `tag` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password_hash` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `bio` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `profile_pic` longblob,
  `registered_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `role` enum('user','dev','mod','admin') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'user',
  `github_username` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `github_username` (`github_username`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'alice','$2a$10$abcdefghijklmnopqrstuv','Indie game developer',NULL,'2026-02-22 16:23:12','dev','aliceGH'),(2,'bob','$2a$10$mnopqrstuvwxyzabcdefghi','Just a regular player',NULL,'2026-02-22 16:23:12','user',NULL),(3,'carol','$2a$10$1234567890abcdefghijklm','Moderator of the platform',NULL,'2026-02-22 16:23:12','mod',NULL),(4,'dave','$2a$10$zxywvutsrqponmlkjihgfedc','Administrator',NULL,'2026-02-22 16:23:12','admin',NULL);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `user_notification`
--

LOCK TABLES `user_notification` WRITE;
/*!40000 ALTER TABLE `user_notification` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_notification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `warning`
--

DROP TABLE IF EXISTS `warning`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `warning` (
  `id` int NOT NULL AUTO_INCREMENT,
  `reason` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `fk_mod` int NOT NULL,
  `fk_user` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_warning_mod` (`fk_mod`),
  KEY `fk_warning_user` (`fk_user`),
  CONSTRAINT `fk_warning_mod` FOREIGN KEY (`fk_mod`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_warning_user` FOREIGN KEY (`fk_user`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `warning`
--

LOCK TABLES `warning` WRITE;
/*!40000 ALTER TABLE `warning` DISABLE KEYS */;
/*!40000 ALTER TABLE `warning` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-02-22 20:42:31
