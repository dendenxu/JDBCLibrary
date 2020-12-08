DROP USER IF EXISTS `jdbc`;

CREATE USER IF NOT EXISTS `jdbc` identified BY
    "jdbc";

GRANT ALL ON JDBCLibrary.* TO `jdbc`;

DROP DATABASE IF EXISTS JDBCLibrary;

CREATE DATABASE JDBCLibrary;

USE JDBCLibrary;

CREATE TABLE `book`
(
    `bno`      char(10),
    `category` varchar(40),
    `title`    varchar(100),
    `press`    varchar(40),
    `year`     int,
    `author`   varchar(40),
    `price`    decimal(7, 2),
    `total`    int,
    `stock`    int,
    PRIMARY KEY (`bno`),
    CONSTRAINT `bno_length` CHECK (CHAR_LENGTH(`bno`) = 10)
);

CREATE TABLE `card`
(
    `cno`        char(7),
    `name`       varchar(40),
    `department` varchar(40),
    `type`       char(1),
    PRIMARY KEY (`cno`),
    CONSTRAINT `type_in_S_T` CHECK (`type` IN ('T', 'S')),
    CONSTRAINT `cno_length` CHECK (CHAR_LENGTH(`cno`) = 7)
);

CREATE TABLE `borrow`
(
    `cno`         char(7),
    `bno`         char(10),
    `borrow_date` date,
    `return_date` date,
    PRIMARY KEY (`cno`, `bno`),
    FOREIGN KEY (`cno`) REFERENCES `card` (`cno`),
    FOREIGN KEY (`bno`) REFERENCES `book` (`bno`)
);

INSERT INTO `book`
VALUES ("0000000013", "English", "English Tutorial", "AnhuiEdu", 2004, "Mary", 94.00, 5, 5),
       ("0000000014", "Math", "Mathematical Analysis", "BeijingGrp", 2010, "Mary", 99.80, 8, 8),
       ("0000000015", "Chemistry", "Chemistry Tutorial", "Seismolog", 1994, "John", 34.02, 6, 6),
       ("0000000016", "Physics", "Physics Tutorial", "BeijingLib", 2019, "Jack", 45.03, 5, 5);

INSERT INTO `card`
VALUES ("0000001", "Admin", "CS", "T"),
       ("0000002", "Otis", "CS", "S"),
       ("0000003", "Yennefer", "CS", "S");

