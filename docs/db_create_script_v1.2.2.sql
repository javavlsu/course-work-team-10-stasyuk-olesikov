-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema mydb
-- -----------------------------------------------------
-- -----------------------------------------------------
-- Schema myngdb
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema myngdb
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `myngdb` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci ;
USE `myngdb` ;

-- -----------------------------------------------------
-- Table `myngdb`.`user`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `myngdb`.`user` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(20) NOT NULL,
  `bio` VARCHAR(255) NULL DEFAULT NULL,
  `profile_pic` LONGBLOB NULL DEFAULT NULL,
  `registered_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `role` ENUM('user', 'dev', 'mod', 'admin') NOT NULL DEFAULT 'user',
  `github_username` VARCHAR(255) NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `username` (`username` ASC) VISIBLE,
  UNIQUE INDEX `github_username` (`github_username` ASC) VISIBLE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;


-- -----------------------------------------------------
-- Table `myngdb`.`ban`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `myngdb`.`ban` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `reason` VARCHAR(255) NOT NULL,
  `start_time` TIMESTAMP NOT NULL,
  `end_time` TIMESTAMP NOT NULL,
  `fk_mod` INT NOT NULL,
  `fk_user` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_ban_mod` (`fk_mod` ASC) VISIBLE,
  INDEX `fk_ban_user` (`fk_user` ASC) VISIBLE,
  CONSTRAINT `fk_ban_mod`
    FOREIGN KEY (`fk_mod`)
    REFERENCES `myngdb`.`user` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_ban_user`
    FOREIGN KEY (`fk_user`)
    REFERENCES `myngdb`.`user` (`id`)
    ON DELETE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;


-- -----------------------------------------------------
-- Table `myngdb`.`collection`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `myngdb`.`collection` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `fk_user` INT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `name` (`name` ASC, `fk_user` ASC) VISIBLE,
  INDEX `fk_collection_user` (`fk_user` ASC) VISIBLE,
  CONSTRAINT `fk_collection_user`
    FOREIGN KEY (`fk_user`)
    REFERENCES `myngdb`.`user` (`id`)
    ON DELETE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;


-- -----------------------------------------------------
-- Table `myngdb`.`dev_application`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `myngdb`.`dev_application` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `github_username` VARCHAR(255) NULL DEFAULT NULL,
  `text` TEXT NULL DEFAULT NULL,
  `fk_user` INT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `fk_user` (`fk_user` ASC) VISIBLE,
  CONSTRAINT `fk_dev_app_user`
    FOREIGN KEY (`fk_user`)
    REFERENCES `myngdb`.`user` (`id`)
    ON DELETE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;


-- -----------------------------------------------------
-- Table `myngdb`.`game`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `myngdb`.`game` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `descr` TEXT NULL DEFAULT NULL,
  `repo` VARCHAR(255) NOT NULL,
  `genre` ENUM('action', 'adventure', 'rpg', 'simulation', 'strategy', 'sports', 'puzzle', 'horror', 'platformer', 'sandbox', 'visual-novel', 'roguelike') NULL DEFAULT NULL,
  `fk_dev` INT NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `repo` (`repo` ASC) VISIBLE,
  INDEX `fk_game_dev` (`fk_dev` ASC) VISIBLE,
  CONSTRAINT `fk_game_dev`
    FOREIGN KEY (`fk_dev`)
    REFERENCES `myngdb`.`user` (`id`)
    ON DELETE SET NULL)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;


-- -----------------------------------------------------
-- Table `myngdb`.`game_collection`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `myngdb`.`game_collection` (
  `fk_game` INT NOT NULL,
  `fk_collection` INT NOT NULL,
  PRIMARY KEY (`fk_game`, `fk_collection`),
  INDEX `fk_game_collection_collection` (`fk_collection` ASC) VISIBLE,
  CONSTRAINT `fk_game_collection_collection`
    FOREIGN KEY (`fk_collection`)
    REFERENCES `myngdb`.`collection` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_game_collection_game`
    FOREIGN KEY (`fk_game`)
    REFERENCES `myngdb`.`game` (`id`)
    ON DELETE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;


-- -----------------------------------------------------
-- Table `myngdb`.`tag`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `myngdb`.`tag` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `name` (`name` ASC) VISIBLE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;


-- -----------------------------------------------------
-- Table `myngdb`.`game_tag`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `myngdb`.`game_tag` (
  `fk_game` INT NOT NULL,
  `fk_tag` INT NOT NULL,
  PRIMARY KEY (`fk_game`, `fk_tag`),
  INDEX `fk_game_tag_tag` (`fk_tag` ASC) VISIBLE,
  CONSTRAINT `fk_game_tag_game`
    FOREIGN KEY (`fk_game`)
    REFERENCES `myngdb`.`game` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_game_tag_tag`
    FOREIGN KEY (`fk_tag`)
    REFERENCES `myngdb`.`tag` (`id`)
    ON DELETE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;


-- -----------------------------------------------------
-- Table `myngdb`.`game_version`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `myngdb`.`game_version` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `commit_hash` VARCHAR(255) NOT NULL,
  `changelog` TEXT NULL DEFAULT NULL,
  `fk_game` INT NOT NULL,
  `name` VARCHAR(20) NOT NULL,
  `archive_file` LONGBLOB NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `fk_game` (`fk_game` ASC, `commit_hash` ASC) VISIBLE,
  CONSTRAINT `fk_game_version_game`
    FOREIGN KEY (`fk_game`)
    REFERENCES `myngdb`.`game` (`id`)
    ON DELETE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;


-- -----------------------------------------------------
-- Table `myngdb`.`review`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `myngdb`.`review` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `rating` TINYINT UNSIGNED NOT NULL,
  `text` TEXT NULL DEFAULT NULL,
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `report_count` SMALLINT UNSIGNED NULL DEFAULT '0',
  `fk_user` INT NOT NULL,
  `fk_game` INT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `fk_user` (`fk_user` ASC, `fk_game` ASC) VISIBLE,
  INDEX `fk_review_game` (`fk_game` ASC) VISIBLE,
  CONSTRAINT `fk_review_game`
    FOREIGN KEY (`fk_game`)
    REFERENCES `myngdb`.`game` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_review_user`
    FOREIGN KEY (`fk_user`)
    REFERENCES `myngdb`.`user` (`id`)
    ON DELETE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;


-- -----------------------------------------------------
-- Table `myngdb`.`moderation_verdict`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `myngdb`.`moderation_verdict` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `approved` TINYINT(1) NOT NULL,
  `reason` VARCHAR(255) NULL DEFAULT NULL,
  `fk_game_version` INT NULL DEFAULT NULL,
  `fk_dev_application` INT NULL DEFAULT NULL,
  `fk_review` INT NULL DEFAULT NULL,
  `fk_mod` INT NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uq_moderation_game_version` (`fk_game_version` ASC) VISIBLE,
  UNIQUE INDEX `uq_moderation_review` (`fk_review` ASC) VISIBLE,
  INDEX `fk_moderation_dev_app` (`fk_dev_application` ASC) VISIBLE,
  INDEX `fk_moderation_verdict_mod` (`fk_mod` ASC) VISIBLE,
  CONSTRAINT `fk_moderation_dev_app`
    FOREIGN KEY (`fk_dev_application`)
    REFERENCES `myngdb`.`dev_application` (`id`),
  CONSTRAINT `fk_moderation_game_version`
    FOREIGN KEY (`fk_game_version`)
    REFERENCES `myngdb`.`game_version` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_moderation_review`
    FOREIGN KEY (`fk_review`)
    REFERENCES `myngdb`.`review` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_moderation_verdict_mod`
    FOREIGN KEY (`fk_mod`)
    REFERENCES `myngdb`.`user` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;


-- -----------------------------------------------------
-- Table `myngdb`.`notification`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `myngdb`.`notification` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `type` ENUM('system', 'warning', 'moderation', 'news') NULL DEFAULT NULL,
  `text` TEXT NULL DEFAULT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;


-- -----------------------------------------------------
-- Table `myngdb`.`user_notification`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `myngdb`.`user_notification` (
  `fk_user` INT NOT NULL,
  `fk_notification` INT NOT NULL,
  PRIMARY KEY (`fk_user`, `fk_notification`),
  INDEX `fk_notification` (`fk_notification` ASC) VISIBLE,
  CONSTRAINT `user_notification_ibfk_1`
    FOREIGN KEY (`fk_user`)
    REFERENCES `myngdb`.`user` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `user_notification_ibfk_2`
    FOREIGN KEY (`fk_notification`)
    REFERENCES `myngdb`.`notification` (`id`)
    ON DELETE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;


-- -----------------------------------------------------
-- Table `myngdb`.`warning`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `myngdb`.`warning` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `reason` VARCHAR(255) NOT NULL,
  `fk_mod` INT NOT NULL,
  `fk_user` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_warning_mod` (`fk_mod` ASC) VISIBLE,
  INDEX `fk_warning_user` (`fk_user` ASC) VISIBLE,
  CONSTRAINT `fk_warning_mod`
    FOREIGN KEY (`fk_mod`)
    REFERENCES `myngdb`.`user` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_warning_user`
    FOREIGN KEY (`fk_user`)
    REFERENCES `myngdb`.`user` (`id`)
    ON DELETE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
