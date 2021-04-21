package com.javatest.flowable.service.impl;

import com.javatest.flowable.service.FlowBpmnModelService;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.*;
import org.flowable.bpmn.model.Process;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * @author: Javen
 * @date: 2021/4/20 10:08
 * @description: 用于管理bpmn模型
 */
@Service
public class FlowBpmnModelServiceImpl extends BaseProcessService implements FlowBpmnModelService {

    @Override
    public List<EndEvent> findEndFlowElement(String processDefinitionId) {
        BpmnModel bpmnModel = this.getBpmnModelByProcessDefId(processDefinitionId);
        if (bpmnModel != null) {
            Process process = bpmnModel.getMainProcess();
            return process.findFlowElementsOfType(EndEvent.class);
        } else {
            return null;
        }
    }

    @Override
    public Activity findActivityByName(String processDefinitionId, String name) {
        Activity activity = null;
        BpmnModel bpmnModel = this.getBpmnModelByProcessDefId(processDefinitionId);
        Process process = bpmnModel.getMainProcess();
        Collection<FlowElement> list = process.getFlowElements();
        for (FlowElement f : list) {
            if (StringUtils.isNotBlank(name)) {
                if (name.equals(f.getName())) {
                    activity = (Activity) f;
                    break;
                }
            }
        }
        return activity;
    }

    @Override
    public BpmnModel getBpmnModelByProcessDefId(String processDefId) {
        return repositoryService.getBpmnModel(processDefId);
    }
}
