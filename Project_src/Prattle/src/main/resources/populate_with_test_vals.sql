USE prattle;

INSERT INTO Users(username,password) VALUES('Sarah', AES_ENCRYPT('password','SecretString'));
INSERT INTO Users(username,password) VALUES('Cole', AES_ENCRYPT('password','SecretString'));
INSERT INTO Users(username,password) VALUES('Avik', AES_ENCRYPT('password','SecretString'));
INSERT INTO Users(username,password) VALUES('Tanmay', AES_ENCRYPT('password','SecretString'));
INSERT INTO Users(username,password) VALUES('tim', AES_ENCRYPT('pass321','SecretString'));

INSERT INTO Groups(groupname) VALUES('team');
INSERT INTO Group_membership(user_id, group_id) values(1,1);
INSERT INTO Group_membership(user_id, group_id) values(2,1);
INSERT INTO Group_membership(user_id, group_id) values(3,1);
