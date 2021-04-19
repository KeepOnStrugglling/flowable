package com.javatest.flowable.service;

import com.javatest.flowable.entity.vo.CommentVo;

/**
 * @author: Javen
 * @date: 2021/4/15 17:06
 * @description: 评论控制
 */
public interface CommentService {

    /**
     * 添加备注
     * @param commentVo 备注信息
     */
    void addComment(CommentVo commentVo);
}
