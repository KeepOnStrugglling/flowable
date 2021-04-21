package com.javatest.flowable.service;

import org.flowable.bpmn.model.Activity;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.EndEvent;

import java.util.List;

/**
 * @author: Javen
 * @date: 2021/4/20 10:08
 * @description: 用于管理bpmn模型
 */
public interface FlowBpmnModelService {

    List<EndEvent> findEndFlowElement(String processDefinitionId);

    Activity findActivityByName(String processDefinitionId, String name);

    BpmnModel getBpmnModelByProcessDefId(String processDefId);
}
