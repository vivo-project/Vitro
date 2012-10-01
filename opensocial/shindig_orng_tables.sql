
--
-- Table structure for table `orng_activity`
--

DROP TABLE IF EXISTS `orng_activity`;
CREATE TABLE `orng_activity` (
  `activityId` int(11) NOT NULL AUTO_INCREMENT,
  `userId` varchar(255) default NULL,
  `appId` int(11) default NULL,
  `createdDT` datetime default NULL,
  `activity` text,
  PRIMARY KEY  (`activityId`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `orng_appdata`
--

DROP TABLE IF EXISTS `orng_appdata`;
CREATE TABLE `orng_appdata` (
  `userId` varchar(255) NOT NULL,
  `appId` int(11) NOT NULL,
  `keyname` varchar(255) NOT NULL,
  `value` varchar(4000) default NULL,
  `createdDT` datetime default NULL,
  `updatedDT` datetime default NULL,
  KEY `userId` (`userId`,`appId`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `orng_apps`
--

DROP TABLE IF EXISTS `orng_apps`;
CREATE TABLE `orng_apps` (
  `appid` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `url` varchar(255) NOT NULL,
  `PersonFilterID` int(11) default NULL,
  `enabled` tinyint(1) NOT NULL default '1',
  `channels` varchar(255) default NULL,
  PRIMARY KEY  (`appid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `orng_app_registry`
--

DROP TABLE IF EXISTS `orng_app_registry`;
CREATE TABLE `orng_app_registry` (
  `appid` int(11) NOT NULL,
  `personId` varchar(255) NOT NULL,
  `createdDT` datetime NOT NULL,
  PRIMARY KEY  (`appid`, `personId` )
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `orng_app_views`
--

DROP TABLE IF EXISTS `orng_app_views`;
CREATE TABLE `orng_app_views` (
  `appid` int(11) NOT NULL,
  `viewer_req` char(1) default NULL,
  `owner_req` char(1) default NULL,
  `page` varchar(50) default NULL,
  `view` varchar(50) default NULL,
  `chromeId` varchar(50) default NULL,
  `display_order` int(11) default NULL,
  `opt_params` varchar(255) default NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `orng_messages`
--

DROP TABLE IF EXISTS `orng_messages`;
CREATE TABLE `orng_messages` (
  `msgId` varchar(255) NOT NULL,
  `senderId` varchar(255) default NULL,
  `recipientId` varchar(255) default NULL,
  `coll` varchar(255) default NULL,
  `title` varchar(255) default NULL,
  `body` varchar(4000) default NULL,
  `createdDT` datetime default NULL,
  PRIMARY KEY  (`msgId`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------


DROP PROCEDURE IF EXISTS `orng_registerAppPerson`;
DROP PROCEDURE IF EXISTS `orng_upsertAppData`;
DROP PROCEDURE IF EXISTS `orng_deleteAppData`;

DELIMITER // 
CREATE PROCEDURE orng_registerAppPerson (uid varchar(255), aid INT, v BOOL)
BEGIN
	IF (v)
	THEN
		INSERT INTO orng_app_registry (appId, personId, createdDT) values (aid, uid, now());
	ELSE 
		DELETE FROM orng_app_registry where appId = aid AND personId = uid;
	END IF;	
END // 
DELIMITER ; 

DELIMITER // 
CREATE PROCEDURE orng_upsertAppData(uid varchar(255), aid INT, kn varchar(255),v varchar(4000))
BEGIN
	DECLARE cnt int;
	SELECT count(*) FROM orng_appdata WHERE userId = uid AND appId = aid and keyname = kn INTO cnt; 
	IF (cnt > 0)
	THEN
		UPDATE orng_appdata set `value` = v, updatedDT = NOW() WHERE userId = uid AND appId = aid and keyname = kn;
	ELSE
		INSERT INTO orng_appdata (userId, appId, keyname, `value`) values (uid, aid, kn, v);
	END IF;
		-- if keyname is VISIBLE, do more
	IF (kn = 'VISIBLE' AND v = 'Y') 
	THEN
		CALL orng_registerAppPerson(uid, aid, 1);
	ELSEIF (kn = 'VISIBLE' )
	THEN
		CALL orng_registerAppPerson(uid, aid, 0);
	END IF;
END // 
DELIMITER ;					

DELIMITER // 
CREATE PROCEDURE orng_deleteAppData(uid varchar(255), aid INT, kn varchar(255))
BEGIN
	DELETE FROM orng_appdata WHERE userId = uid AND appId = aid and keyname = kn;
		-- if keyname is VISIBLE, do more
	IF (kn = 'VISIBLE' ) 
	THEN
		CALL orng_registerAppPerson(uid, aid, 0);
	END IF;
END // 
DELIMITER ;


