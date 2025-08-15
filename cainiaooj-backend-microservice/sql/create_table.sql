# 数据库初始化
# @author <a href="https://github.com/liyupi">程序员鱼皮</a>
# @from <a href="https://yupi.icu">编程导航知识星球</a>

-- 创建库
create database if not exists ojSystem;

-- 切换库
use ojSystem;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    unionId      varchar(256)                           null comment '微信开放平台id',
    mpOpenId     varchar(256)                           null comment '公众号openId',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin/ban',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    index idx_unionId (unionId)
) comment '用户' collate = utf8mb4_unicode_ci;
-- 题目表
create table if not exists question
(
    id         bigint auto_increment comment 'id' primary key,
    title      varchar(512)                       null comment '标题',
    content    text                               null comment '内容',
    tags       varchar(1024)                      null comment '标签列表（json 数组）',
    answer     text                               null comment '题目答案',
    submitNum  int  default 0 not null comment '题目提交数',
    acceptedNum  int  default 0 not null comment '题目通过数',
    judgeCase text null comment '判题用例（json 数组）',
    judgeConfig text null comment '判题配置（json 对象）',
    thumbNum   int      default 0                 not null comment '点赞数',
    favourNum  int      default 0                 not null comment '收藏数',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    index idx_userId (userId)
    ) comment '题目' collate = utf8mb4_unicode_ci;
-- 题目提交表
create table if not exists question_submit
(
    id         bigint auto_increment comment 'id' primary key,
    language   varchar(128)                       not null comment '编程语言',
    code       text                               not null comment '用户代码',
    judgeInfo  text                               null comment '判题信息（json 对象）',
    status     int      default 0                 not null comment '判题状态（0 - 待判题、1 - 判题中、2 - 成功、3 - 失败）',
    questionId bigint                             not null comment '题目 id',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    index idx_questionId (questionId),
    index idx_userId (userId)
    ) comment '题目提交';

-- 帖子表
create table if not exists post
(
    id         bigint auto_increment comment 'id' primary key,
    title      varchar(512)                       null comment '标题',
    content    text                               null comment '内容',
    tags       varchar(1024)                      null comment '标签列表（json 数组）',
    thumbNum   int      default 0                 not null comment '点赞数',
    favourNum  int      default 0                 not null comment '收藏数',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    index idx_userId (userId)
) comment '帖子' collate = utf8mb4_unicode_ci;

-- 帖子点赞表（硬删除）
create table if not exists post_thumb
(
    id         bigint auto_increment comment 'id' primary key,
    postId     bigint                             not null comment '帖子 id',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_postId (postId),
    index idx_userId (userId)
) comment '帖子点赞';

-- 帖子收藏表（硬删除）
create table if not exists post_favour
(
    id         bigint auto_increment comment 'id' primary key,
    postId     bigint                             not null comment '帖子 id',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_postId (postId),
    index idx_userId (userId)
) comment '帖子收藏';

-- auto-generated definition
create table question_comment
(
    id          bigint                                     not null comment '主键id'
        primary key,
    questionId  bigint           default 0                 not null comment '问题id',
    userId      bigint           default 0                 not null comment '用户id',
    userName    varchar(50)                                null comment '用户昵称',
    userAvatar  varchar(255)                               null comment '用户头像',
    content     varchar(500)                               null comment '评论内容',
    parentId    bigint           default -1                null comment '父级评论id
',
    commentNum  int              default 0                 null comment '回复条数',
    likeCount   int              default 0                 null comment '点赞数量',
    isLike      tinyint(1)       default 0                 null comment '是否点赞',
    likeListId  varchar(255)                               null comment '点赞id列表',
    inputShow   tinyint(1)       default 0                 null comment '是否显示输入框',
    fromId      bigint                                     null comment '回复记录id
',
    fromName    varchar(255) collate utf8mb4_bin           null comment '回复人名称
',
    gmtCreate   datetime         default CURRENT_TIMESTAMP not null comment '创建时间',
    gmtModified datetime         default CURRENT_TIMESTAMP not null comment '更新时间',
    isDeleted   tinyint unsigned default '0'               not null comment '逻辑删除 1（true）已删除， 0（false）未删除'
)
    comment '评论' collate = utf8mb4_general_ci
                   row_format = DYNAMIC;

create index idx_questionId
    on question_comment (questionId);

create index idx_userId
    on question_comment (userId);
CREATE TABLE `question_solution` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `questionId` BIGINT NOT NULL COMMENT '题目ID',
  `answer` LONGTEXT NOT NULL COMMENT 'AI题解',
  PRIMARY KEY (`id`),
  INDEX `idx_question_id` (`questionId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ojsystem.question_submit_solution definition

CREATE TABLE `question_submit_solution` (
  `id` bigint DEFAULT NULL,
  `questionSubmitId` bigint DEFAULT NULL,
  `analysis` longtext,
  UNIQUE KEY `question_submit_solution_id_IDX` (`id`) USING BTREE,
  UNIQUE KEY `question_submit_solution_questionSubmitId_IDX` (`questionSubmitId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


INSERT INTO ojSystem.`user` (userAccount,userPassword,unionId,mpOpenId,userName,userAvatar,userProfile,userRole,createTime,updateTime,isDelete) VALUES
	 ('superroot','2e1bb9ce064bb3b89e6d04704da61a26',NULL,NULL,'root','https://miaobi-lite.bj.bcebos.com/miaobi/5mao/b%276Jyh56yU5bCP5paw5oOF5L6j5aS05YOPXzE3Mjg5NDgyODguMjQ1MjcyMg%3D%3D%27/0.png',NULL,'admin','2025-08-05 00:50:20','2025-08-05 16:36:44',0);
