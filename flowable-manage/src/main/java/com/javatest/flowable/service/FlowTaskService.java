package com.javatest.flowable.service;

import org.flowable.idm.api.User;

import java.util.List;

/**
 * @author: Javen
 * @date: 2021/4/20 9:53
 * @description: 管理流程任务
 */
public interface FlowTaskService {

    List<User> getApprovers(String processInstanceId);
}
