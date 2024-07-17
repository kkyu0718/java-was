-- Drop tables if they exist to start with a clean slate
-- DROP TABLE IF EXISTS Comment;
-- DROP TABLE IF EXISTS Post;
-- DROP TABLE IF EXISTS `User`;

-- Create User table
CREATE TABLE IF NOT EXISTS `User` (
                                      user_id VARCHAR(20) PRIMARY KEY,
    password VARCHAR(50) NOT NULL,
    name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    image_url VARCHAR(200)
    );

-- Create Post table
CREATE TABLE IF NOT EXISTS Post (
                                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    user_id VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    image_url VARCHAR(200) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES `User`(user_id)
    );

-- Create Comment table
CREATE TABLE IF NOT EXISTS Comment (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       post_id BIGINT NOT NULL,
                                       user_id VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    FOREIGN KEY (post_id) REFERENCES Post(id),
    FOREIGN KEY (user_id) REFERENCES `User`(user_id)
    );

-- Insert data into User table (only if the table is empty)
INSERT INTO `User` (user_id, password, name, email, image_url)
SELECT * FROM (
                  SELECT '규원' AS user_id, '비번' AS password, '규원닉넴' AS name, '이멜' AS email, 'http://example.com/images/규원.jpg' AS image_url
                  UNION ALL
                  SELECT '피카츄', '피카?', '피카츄~', '피카', 'http://example.com/images/피카츄.jpg'
                  UNION ALL
                  SELECT '라이츄', '라이츄우', '라이~츄', '라이츄', 'http://example.com/images/라이츄.jpg'
                  UNION ALL
                  SELECT '파이리', '파이파이', '파이리파워', '파이리', 'http://example.com/images/파이리.jpg'
                  UNION ALL
                  SELECT '꼬부기', '꼬북꼬북', '꼬북파워', '꼬북이', 'http://example.com/images/꼬부기.jpg'
              ) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `User` LIMIT 1);

-- Insert data into Post table (only if the table is empty)
INSERT INTO Post (user_id, content, image_url)
SELECT * FROM (
                  SELECT '규원' AS user_id, '와아아 성공이당~' AS content, 'http://example.com/images/규원.jpg' AS image_url
                  UNION ALL
                  SELECT '규원', '이모지도 됨? 🔍', 'http://example.com/images/규원.jpg'
                  UNION ALL
                  SELECT '파이리', '화난다 파이파이리~', 'http://example.com/images/파이리.jpg'
                  UNION ALL
                  SELECT '꼬부기', '물폭탄 워터밤을 만들어보자', 'http://example.com/images/꼬부기.jpg'
                  UNION ALL
                  SELECT '피카츄', '피카츄는 언제 은퇴할까 🫨', 'http://example.com/images/피카츄.jpg'
                  UNION ALL
                  SELECT '규원', '이번 주도 아주 짜릿하다 🫢', 'http://example.com/images/규원.jpg'
              ) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM Post LIMIT 1);

