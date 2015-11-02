-- // CLOUD-46697 Refactor platform parameters
-- Migration SQL that makes the change goes here.

ALTER TABLE cloudbreakevent ADD COLUMN availabilityZone text;
ALTER TABLE cloudbreakusage ADD COLUMN availabilityZone text;

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE cloudbreakevent DROP COLUMN availabilityZone;
ALTER TABLE cloudbreakusage DROP COLUMN availabilityZone;
