-- Add some gadgets to play with ------------------------
--

DELETE FROM `orng_apps`;

INSERT INTO `orng_apps` (`appid`, `name`, `url`, `PersonFilterID`, `enabled`, `channels`) VALUES
(100, 'Google Search', 'http://stage-profiles.ucsf.edu/apps/ucsfsearch.xml', NULL, 1, NULL),
(101, 'Featured Presentations', 'http://stage-profiles.ucsf.edu/apps/SlideShare.xml', NULL, 1, NULL),
(102, 'Faculty Mentor', 'http://stage-profiles.ucsf.edu/apps/Mentor.xml', NULL, 0, NULL),
(103, 'Websites', 'http://stage-profiles.ucsf.edu/apps/Links.xml', NULL, 1, NULL),
(104, 'Profile List', 'http://stage-profiles.ucsf.edu/apps/ProfileListTool.xml', NULL, 1, 'JSONPersonIds'),
(106, 'RDF Test Gadget', 'http://stage-profiles.ucsf.edu/apps/RDFTest.xml', NULL, 1, NULL),
(112, 'Twitter', 'http://stage-profiles.ucsf.edu/apps/Twitter.xml', NULL, 1, NULL);

DELETE FROM `orng_app_views`;

INSERT INTO `orng_app_views` (`appid`, `viewer_req`, `owner_req`, `page`, `view`, `chromeId`, `opt_params`, `display_order`) VALUES
(100, NULL, NULL, 'search', NULL, 'gadgets-search', NULL, NULL),
(101, NULL, 'R', 'individual', 'profile', 'gadgets-view', '{''gadget_class'':''ORNGToggleGadget'', ''start_closed'':1, ''closed_width'':290}', 4),
(101, NULL, NULL, 'individual-EDIT-MODE', 'home', 'gadgets-edit', '{''gadget_class'':''ORNGToggleGadget'', ''start_closed'':1, ''closed_width'':700}', 4),
(102, NULL, 'R', 'individual', 'profile', 'gadgets-view', '{''gadget_class'':''ORNGToggleGadget'', ''start_closed'':1, ''closed_width'':290}', 3),
(102, NULL, NULL, 'individual-EDIT-MODE', 'home', 'gadgets-edit', '{''gadget_class'':''ORNGToggleGadget'', ''start_closed'':1, ''closed_width'':700}', 3),
(103, NULL, NULL, 'individual-EDIT-MODE', 'home', 'gadgets-edit', '{''gadget_class'':''ORNGToggleGadget'', ''start_closed'':1, ''closed_width'':700}', NULL),
(103, NULL, 'R', 'individual', 'profile', 'gadgets-view', '{''gadget_class'':''ORNGToggleGadget'', ''start_closed'':0, ''closed_width'':290}', 1),
(104, 'U', NULL, 'search', 'small', 'gadgets-tools', NULL, NULL),
(104, 'U', NULL, 'gadgetDetails', 'canvas', 'gadgets-detail', NULL, NULL),
(104, 'U', NULL, 'individual', 'small', 'gadgets-view', NULL, NULL),
(106, NULL, NULL, 'individual-EDIT-MODE', 'home', 'gadgets-edit', '{''gadget_class'':''ORNGToggleGadget'', ''start_closed'':1, ''closed_width'':700}', NULL),
(112, NULL, 'R', 'individual', 'profile', 'gadgets-view', '{''gadget_class'':''ORNGToggleGadget'', ''start_closed'':0, ''closed_width'':290}', 2),
(112, NULL, NULL, 'individual-EDIT-MODE', 'home', 'gadgets-edit', '{''gadget_class'':''ORNGToggleGadget'', ''start_closed'':1, ''closed_width'':700}', 2);

