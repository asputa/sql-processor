DROP TABLE IF EXISTS CONTACT CASCADE

DROP TABLE IF EXISTS PERSON CASCADE

CREATE TABLE PERSON (
  ID BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  FIRST_NAME VARCHAR(100) NOT NULL,
  LAST_NAME VARCHAR(100) NOT NULL,
  DATE_OF_BIRTH DATE,
  SSN VARCHAR(100)
)

CREATE  INDEX IX_PERSON_LAST_NAME ON PERSON (LAST_NAME)

CREATE TABLE CONTACT (
  ID BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  PERSON_ID BIGINT NOT NULL,
  ADDRESS VARCHAR(100),
  PHONE_NUMBER VARCHAR(100)
)

ALTER TABLE CONTACT ADD CONSTRAINT FK_CONTACT_PERSON
	FOREIGN KEY (PERSON_ID) REFERENCES PERSON (ID) ON DELETE CASCADE
