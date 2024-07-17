-- DROP TABLE IF EXISTS Comment;
-- DROP TABLE IF EXISTS Post;
-- DROP TABLE IF EXISTS `User`;

CREATE TABLE `User` (
    user_id VARCHAR(20) PRIMARY KEY ,
    password VARCHAR(50),
    name VARCHAR(50),
    email VARCHAR(100)
                   );


CREATE TABLE Post (
                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                      user_id VARCHAR(20) NOT NULL,
                      content TEXT NOT NULL,
                      FOREIGN KEY (user_id) REFERENCES `User`(user_id)
);

CREATE TABLE Comment (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         post_id BIGINT NOT NULL,
                         user_id VARCHAR(20) NOT NULL,
                         content TEXT NOT NULL,
                         FOREIGN KEY (post_id) REFERENCES Post(id),
                         FOREIGN KEY (user_id) REFERENCES `User`(user_id)
);

