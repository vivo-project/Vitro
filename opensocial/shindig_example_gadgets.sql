
-- Add some gadgets to play with ------------------------
--

INSERT INTO `shindig_apps` (`appid`, `name`, `url`, `PersonFilterID`, `enabled`, `channels`) VALUES
(100, 'Google Search', 'http://dev-profiles.ucsf.edu/apps/GoogleSearch.xml', NULL, 1, NULL),
(101, 'Featured Presentations', 'http://dev-profiles.ucsf.edu/apps/SlideShare.xml', NULL, 1, NULL),
(102, 'Faculty Mentor', 'http://dev-profiles.ucsf.edu/apps/Mentor.xml', NULL, 1, NULL),
(103, 'Websites', 'http://dev-profiles.ucsf.edu/apps/Links.xml', NULL, 1, NULL),
(104, 'Profile List', 'http://dev-profiles.ucsf.edu/apps/ProfileListTool.xml', NULL, 1, 'JSONPersonIds'),
(105, 'Publication Export', 'http://dev-profiles.ucsf.edu/apps/PubExportTool.xml', NULL, 1, 'JSONPubMedIds'),
(106, 'RDF Test Gadget', 'http://dev-profiles.ucsf.edu/gadgets/RDFTest.xml', NULL, 1, NULL);

INSERT INTO `shindig_app_views` (`appid`, `viewer_req`, `owner_req`, `page`, `view`, `closed_width`, `open_width`, `start_closed`, `chromeId`, `display_order`) VALUES
(100, NULL, NULL, 'search', NULL, 600, 600, 1, 'gadgets-search', NULL),
(101, NULL, 'R', 'individual', 'profile', 290, 600, 1, 'gadgets-view', 3),
(101, NULL, NULL, 'individual-EDIT-MODE', 'home', 700, 700, 1, 'gadgets-edit', NULL),
(102, NULL, 'R', 'individual', 'profile', 290, 600, 1, 'gadgets-view', 2),
(102, NULL, NULL, 'individual-EDIT-MODE', 'home', 700, 700, 1, 'gadgets-edit', NULL),
(103, NULL, NULL, 'individual-EDIT-MODE', 'home', 700, 700, 1, 'gadgets-edit', NULL),
(103, NULL, 'R', 'individual', 'profile', 290, 600, 0, 'gadgets-view', 1),
(104, 'U', NULL, 'search', 'small', 160, 160, 0, 'gadgets-tools', NULL),
(104, 'U', NULL, 'gadgetDetails', 'canvas', 700, 700, 0, 'gadgets-detail', NULL),
(104, 'U', NULL, 'SimilarPeople.aspx', 'small', 160, 160, 0, 'gadgets-tools', NULL),
(104, 'U', NULL, 'individual', 'small', 290, 290, 0, 'gadgets-view', NULL),
(104, 'U', NULL, 'CoAuthors.aspx', 'small', 160, 160, 0, 'gadgets-tools', NULL),
(105, 'U', NULL, 'individual', 'small', 290, 290, 0, 'gadgets-view', NULL),
(105, 'U', NULL, 'gadgetDetails', 'canvas', 700, 700, 0, 'gadgets-detail', NULL);
