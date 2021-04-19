package com.javatest.flowable.service.impl;

import com.javatest.flowable.dao.CommentDao;
import com.javatest.flowable.entity.vo.AddHisCommentCmd;
import com.javatest.flowable.entity.vo.CommentVo;
import com.javatest.flowable.service.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: Javen
 * @date: 2021/4/19 10:03
 * @description: com.javatest.flowable.service.impl
 */
@Service
@Slf4j
public class CommentServiceImpl extends BaseProcessService implements CommentService {

    @Autowired
    private CommentDao commentDao;

    @Override
    public void addComment(CommentVo comment) {
        managementService.executeCommand(new AddHisCommentCmd(comment.getTaskId(), comment.getUserId(), comment.getProcessInstanceId(),
                comment.getType(), comment.getMessage()));
    }
}
