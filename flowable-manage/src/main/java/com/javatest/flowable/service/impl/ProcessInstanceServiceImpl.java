package com.javatest.flowable.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.javatest.flowable.common.constant.FlowConstant;
import com.javatest.flowable.common.enums.CommentTypeEnum;
import com.javatest.flowable.common.enums.ReturnCode;
import com.javatest.flowable.common.response.Result;
import com.javatest.flowable.config.cmd.DeleteFlowableProcessInstanceCmd;
import com.javatest.flowable.dao.ProcessInstanceDao;
import com.javatest.flowable.entity.vo.*;
import com.javatest.flowable.service.FlowBpmnModelService;
import com.javatest.flowable.service.FlowTaskService;
import com.javatest.flowable.service.ProcessInstanceService;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.Activity;
import org.flowable.bpmn.model.EndEvent;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.idm.api.User;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Javen
 * @date: 2021/4/19 16:41
 * @description: 管理流程实例
 */
public class ProcessInstanceServiceImpl extends BaseProcessService implements ProcessInstanceService {

    @Autowired
    private ProcessInstanceDao processInstanceDao;

    @Autowired
    private FlowBpmnModelService flowBpmnModelService;

    @Autowired
    private FlowTaskService flowTaskService;

    /**
     * 启动流程
     */
    @Override
    @SuppressWarnings("unchecked")
    public Result<ProcessInstance> startProcessInstanceByKey(StartProcessInstanceVo startProcessInstanceVo) {
        if (StringUtils.isNotBlank(startProcessInstanceVo.getProcessDefinitionKey())
                && StringUtils.isNotBlank(startProcessInstanceVo.getBusinessKey())
                && StringUtils.isNotBlank(startProcessInstanceVo.getSystemSn())) {
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(startProcessInstanceVo.getProcessDefinitionKey())
                    .latestVersion().singleResult();
            if (processDefinition != null && processDefinition.isSuspended()) {
                return Result.fail(ReturnCode.INSTANCE_PROCESS_ALREADY_HANG_ON);
            }
            /**
             * 1、设置变量
             * 1.1、设置提交人字段为空字符串让其自动跳过
             * 1.2、设置可以自动跳过
             * 1.3、汇报线的参数设置
             */
            //1.1、设置提交人字段为空字符串让其自动跳过
            startProcessInstanceVo.getVariables().put(FlowConstant.FLOW_SUBMITTER_VAR, "");
            //1.2、设置可以自动跳过
            startProcessInstanceVo.getVariables().put(FlowConstant.FLOWABLE_SKIP_EXPRESSION_ENABLED, true);
            // TODO 1.3、汇报线的参数设置
            //2、当我们流程创建人和发起人为空，设置为当前用户
            String creator = startProcessInstanceVo.getCreator();
            if (StringUtils.isBlank(creator)) {
                creator = startProcessInstanceVo.getCurrentUserCode();
                startProcessInstanceVo.setCreator(creator);
            }
            //3.启动流程
            identityService.setAuthenticatedUserId(creator);
            ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
                    .processDefinitionKey(startProcessInstanceVo.getProcessDefinitionKey().trim())
                    .name(startProcessInstanceVo.getFormName().trim())
                    .businessKey(startProcessInstanceVo.getBusinessKey().trim())
                    .variables(startProcessInstanceVo.getVariables())
                    .tenantId(startProcessInstanceVo.getSystemSn().trim())
                    .start();
            //4.添加审批记录
            this.addComment(startProcessInstanceVo.getCurrentUserCode(), processInstance.getProcessInstanceId(),
                    CommentTypeEnum.TJ.toString(), startProcessInstanceVo.getFormName() + "提交");
            //5.TODO 推送消息数据

            return Result.success(processInstance);
        } else {
            return new Result(ReturnCode.PARAM_NOT_COMPLETE.getCode(), null, "请填写 这三个字段 ProcessDefinitionKey,BusinessKey,SystemSn");
        }
    }

    /**
     * 分页查询流程实例
     *
     * @param params ProcessInstanceQueryVo
     */
    @Override
    public PageInfo<ProcessInstanceVo> getPagerModel(ProcessInstanceQueryVo params) {
        return getProcessInstanceVoPageInfo(params);
    }

    private PageInfo<ProcessInstanceVo> getProcessInstanceVoPageInfo(ProcessInstanceQueryVo params) {
        PageHelper.startPage(params.getCurPage(), params.getLimit());
        List<ProcessInstanceVo> list = processInstanceDao.getPage(params);
        return new PageInfo<>(list);
    }

    /**
     * 查询我发起的流程实例
     */
    @Override
    public PageInfo<ProcessInstanceVo> getMyProcessInstances(ProcessInstanceQueryVo params) {
        PageInfo<ProcessInstanceVo> pageInfo = this.getProcessInstanceVoPageInfo(params);
        List<ProcessInstanceVo> myProcesses = pageInfo.getList();
        myProcesses.forEach(processInstanceVo -> {
            setLastApprover(processInstanceVo);
            setStateApprover(processInstanceVo);
            setEnableRevokeAndStop(processInstanceVo);
        });
        return new PageInfo<>(myProcesses);
    }


    /**
     * 设置上一个审批人姓名和ID
     * @param processInstanceVo
     */
    private void setLastApprover(ProcessInstanceVo processInstanceVo) {
        // 从历史任务中找到该流程实例中的历史任务（与人相关的节点），并从中倒序找到最后一个审批的记录
        List<HistoricTaskInstance> historicTaskInstanceList = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceVo.getProcessInstanceId()).orderByTaskCreateTime().desc().list();
        // 如果为空，说明流程才刚走到第一个节点，此时没有上一个审批人
        if (historicTaskInstanceList != null && !historicTaskInstanceList.isEmpty()) {
            for (HistoricTaskInstance historicTaskInstance : historicTaskInstanceList) {
                if (historicTaskInstance.getEndTime() == null) {
                    // 说明流程走到该节点
                    continue;
                }
                // 因为倒序，当进入下面的代码时，该节点属于最新一个被审批的节点
                if (StringUtils.isNotBlank(historicTaskInstance.getAssignee())) {
                    User user = identityService.createUserQuery().userId(historicTaskInstance.getAssignee()).singleResult();
                    if (user != null) {
                        processInstanceVo.setLastApprover(user.getDisplayName());
                        break;
                    }
                }
                // 获取到最新被审批的节点也找不到对应的用户，那么上一个审批人有可能是一个用户组，而不是自然人，此时不做处理
                break;
            }
        }
    }

    /**
     * 设置状态和审核人
     * @param processInstanceVo
     */
    private void setStateApprover(ProcessInstanceVo processInstanceVo) {
        if (processInstanceVo.getEndTime() == null) {
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processInstanceVo.getProcessInstanceId()).singleResult();
            if (processInstance.isSuspended()) {
                processInstanceVo.setSuspensionState(FlowConstant.SUSPENSION_STATE);
            } else {
                processInstanceVo.setSuspensionState(FlowConstant.ACTIVATE_STATE);
            }
        }
        List<User> approvers = flowTaskService.getApprovers(processInstanceVo.getProcessInstanceId());
        String userNames = this.createApprovers(approvers);
        processInstanceVo.setApprover(userNames);
    }

    /**
     * 查看当前流程是否支持终止或撤回的功能
     * @param processInstanceVo
     * 0-支持，1-不支持
     */
    private void setEnableRevokeAndStop(ProcessInstanceVo processInstanceVo) {
        // 从历史数据中获取当前流程的活动，包含网关、开始/结束事件
        List<HistoricActivityInstance> list = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceVo.getProcessInstanceId()).orderByHistoricActivityInstanceStartTime().desc().list();
        if (list == null || list.isEmpty()) {
            processInstanceVo.setEnableRevokeFlag(FlowConstant.NOT_SUPPORT);
            processInstanceVo.setEnableStopFlag(FlowConstant.NOT_SUPPORT);
            return;
        }
        // 如果第一条记录的act类型为结束节点，则认为流程已经结束了。
        if(list.get(0).getActivityType().equals("endEvent")){
            processInstanceVo.setEnableRevokeFlag("1");
            processInstanceVo.setEnableStopFlag("1");
            return;
        }
        String processKey = processInstanceVo.getProcessDefinitionId().split(":")[0];

        boolean isStartNodeFlag = false;    // 开始节点标识

        // TODO: 如果有多种流程，可以再这里根据不同流程的要求进行判断，这里仅写一种做演示。假定对于TestProcess_前缀的流程进行处理
        if (processKey.equals("TestProcess_")) {
            for (HistoricActivityInstance instance : list) {
                // 比如对于开始节点，允许终止，不允许撤回
                // 这里的activityType的值也是自定义的
                if (instance.getActivityType().equals("userTask") && instance.getEndTime() == null) {
                    // 进入这里说明节点属于申请人当前节点
                    if (StringUtils.isNotBlank(instance.getActivityName()) && instance.getActivityName().equals("开始节点")) {
                        // 进入这里说明是开始节点。注意，这里是默认该流程的开始节点的节点名就叫做“开始节点”，但不同流程开始节点的名称不一定叫“开始节点”，所以具体流程具体分析
                        isStartNodeFlag = true;
                    }
                    break;
                }
            }

            // 我们规定在开始节点允许终止，不允许撤回
            if (isStartNodeFlag) {
                processInstanceVo.setEnableStopFlag(FlowConstant.SUPPORT);
                processInstanceVo.setEnableRevokeFlag(FlowConstant.NOT_SUPPORT);
            }
        }
    }

    /**
     * 获取流程图图片
     *
     * @param processInstanceId 流程实例id
     */
    @Override
    public byte[] createImage(String processInstanceId) {
        return new byte[0];
    }

    /**
     * 删除流程实例
     */
    @Override
    public Result deleteProcessInstanceById(String processInstanceId) {
        long count = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).count();
        if (count > 0) {
            DeleteFlowableProcessInstanceCmd cmd = new DeleteFlowableProcessInstanceCmd(processInstanceId, "删除流程实例", true);
            managementService.executeCommand(cmd);
            return Result.success();
        } else {
            historyService.deleteHistoricProcessInstance(processInstanceId);
            return Result.success();
        }
    }

    /**
     * 激活或者挂起流程定义
     *
     * @param processInstanceId 需要操作的流程定义id
     * @param suspensionState   进行的操作 2-激活，1-挂起
     */
    @Override
    public Result suspendOrActivateProcessInstanceById(String processInstanceId, int suspensionState) {
        if (suspensionState == 1) {
            runtimeService.suspendProcessInstanceById(processInstanceId);
            return new Result(ReturnCode.SUCCESS.getCode(), null, "挂起成功");
        } else {
            runtimeService.activateProcessInstanceById(processInstanceId);
            return new Result(ReturnCode.SUCCESS.getCode(), null, "激活成功");
        }
    }

    /**
     * 终止
     */
    @Override
    public Result stopProcessInstanceById(EndProcessVo endVo) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(endVo.getProcessInstanceId()).singleResult();
        if (processInstance != null) {
            //1、添加审批记录
            this.addComment(endVo.getUserCode(), endVo.getProcessInstanceId(), CommentTypeEnum.LCZZ.toString(),
                    endVo.getMessage());
            List<EndEvent> endNodes = flowBpmnModelService.findEndFlowElement(processInstance.getProcessDefinitionId());
            String endId = endNodes.get(0).getId();
            String processInstanceId = endVo.getProcessInstanceId();
            //2、执行终止
            List<Execution> executions = runtimeService.createExecutionQuery().parentId(processInstanceId).list();
            List<String> executionIds = new ArrayList<>();
            executions.forEach(execution -> executionIds.add(execution.getId()));
            this.moveExecutionsToSingleActivityId(executionIds, endId);
            return Result.success();
        } else {
            return Result.fail(ReturnCode.INSTANCE_PROCESS_NOT_FOUND);
        }
    }

    /**
     * 撤回流程
     */
    @Override
    public Result revokeProcess(RevokeProcessVo revokeVo) {
        if (StringUtils.isNotBlank(revokeVo.getProcessInstanceId())) {
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(revokeVo.getProcessInstanceId()).singleResult();
            if (processInstance != null) {
                //1.添加撤回意见
                this.addComment(revokeVo.getUserCode(), revokeVo.getProcessInstanceId(), CommentTypeEnum.CH.toString(), revokeVo.getMessage());
                //2.设置提交人
                runtimeService.setVariable(revokeVo.getProcessInstanceId(), FlowConstant.FLOW_SUBMITTER_VAR, processInstance.getStartUserId());
                //3.执行撤回
                Activity disActivity = flowBpmnModelService.findActivityByName(processInstance.getProcessDefinitionId(), FlowConstant.FLOW_SUBMITTER);
                //4.删除运行和历史的节点信息
                this.deleteActivity(disActivity.getId(), revokeVo.getProcessInstanceId());
                //5.执行跳转
                List<Execution> executions = runtimeService.createExecutionQuery().parentId(revokeVo.getProcessInstanceId()).list();
                List<String> executionIds = new ArrayList<>();
                executions.forEach(execution -> executionIds.add(execution.getId()));
                this.moveExecutionsToSingleActivityId(executionIds, disActivity.getId());
                return Result.success();
            }
        }
        return Result.fail(ReturnCode.INSTANCE_ID_NULL);
    }
}
