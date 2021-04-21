package com.javatest.flowable.service.impl;

import com.javatest.flowable.service.FlowTaskService;
import org.apache.commons.lang3.StringUtils;
import org.flowable.editor.language.json.converter.util.CollectionUtils;
import org.flowable.identitylink.api.IdentityLink;
import org.flowable.idm.api.User;
import org.flowable.task.api.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Javen
 * @date: 2021/4/20 15:38
 * @description: 管理流程任务
 */
public class FlowTaskServiceImpl extends BaseProcessService implements FlowTaskService {


    @Override
    public List<User> getApprovers(String processInstanceId) {
        List<User> users = new ArrayList<>();
        List<Task> list = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
        if (CollectionUtils.isNotEmpty(list)) {
            list.forEach(task -> {
                if (StringUtils.isNotBlank(task.getAssignee())) {
                    //1.审批人ASSIGNEE_是用户id
                    User user = identityService.createUserQuery().userId(task.getAssignee()).singleResult();
                    if (user != null) {
                        users.add(user);
                    }
                    //2.审批人ASSIGNEE_是组id
                    List<User> gusers = identityService.createUserQuery().memberOfGroup(task.getAssignee()).list();
                    if (CollectionUtils.isNotEmpty(gusers)) {
                        users.addAll(gusers);
                    }
                } else {
                    List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(task.getId());
                    if (CollectionUtils.isNotEmpty(identityLinks)) {
                        identityLinks.forEach(identityLink -> {
                            //3.审批人ASSIGNEE_为空,用户id
                            if (StringUtils.isNotBlank(identityLink.getUserId())) {
                                User user = identityService.createUserQuery().userId(identityLink.getUserId()).singleResult();
                                if (user != null) {
                                    users.add(user);
                                }
                            } else {
                                //4.审批人ASSIGNEE_为空,组id
                                List<User> gusers = identityService.createUserQuery().memberOfGroup(identityLink.getGroupId()).list();
                                if (CollectionUtils.isNotEmpty(gusers)) {
                                    users.addAll(gusers);
                                }
                            }
                        });
                    }
                }
            });
        }
        return users;
    }
}
