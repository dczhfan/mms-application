-- Autogenerated: do not edit this file

CREATE TABLE GLOBAL_CONFIGURATION(
	CONFIG_ID INT  NOT NULL PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY,
	TITLE VARCHAR(200),
	NAME VARCHAR(50) NOT NULL,
	CODE VARCHAR(200) NOT NULL,
	CONTENT VARCHAR(200),
	DESCRIPTION VARCHAR(200),
	SEQUENCE INT NOT NULL DEFAULT 0,
	ACTIVE CHAR(1) NOT NULL DEFAULT 'Y',
	CREATE_TIME TIMESTAMP NOT NULL default CURRENT_TIMESTAMP,
	UPDATE_TIME TIMESTAMP NOT NULL default CURRENT_TIMESTAMP
);

INSERT INTO GLOBAL_CONFIGURATION(TITLE, NAME, CODE, DESCRIPTION) VALUES ('版本号', 'GLOBAL_VERSION','1','GLOBAL VERSION NUMBER');

INSERT INTO GLOBAL_CONFIGURATION(NAME, CODE, DESCRIPTION) VALUES ('ATTACHMENT_AS_CONTENT_ENABLE','Y','允许将附件转换为正文内容');

INSERT INTO GLOBAL_CONFIGURATION(NAME, CODE, DESCRIPTION) VALUES ('ATTACHMENT_AS_CONTENT','htm','将HTML文件转变为征文');
INSERT INTO GLOBAL_CONFIGURATION(NAME, CODE, DESCRIPTION) VALUES ('ATTACHMENT_AS_CONTENT','html','将HTML文件转变为征文');
INSERT INTO GLOBAL_CONFIGURATION(NAME, CODE, DESCRIPTION) VALUES ('ATTACHMENT_AS_CONTENT','txt','将文本文件转变为征文');