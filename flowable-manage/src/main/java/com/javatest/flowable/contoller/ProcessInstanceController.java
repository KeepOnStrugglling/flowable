package com.javatest.flowable.contoller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.javatest.flowable.common.enums.ReturnCode;
import com.javatest.flowable.common.page.PageUtils;
import com.javatest.flowable.common.page.Query;
import com.javatest.flowable.common.response.Result;
import com.javatest.flowable.entity.vo.EndProcessVo;
import com.javatest.flowable.entity.vo.ProcessInstanceQueryVo;
import com.javatest.flowable.entity.vo.ProcessInstanceVo;
import com.javatest.flowable.service.ProcessInstanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author: Javen
 * @date: 2021/3/3 15:44
 * @description: 管理流程实例
 */
@RestController
@RequestMapping("instance")
@Slf4j
public class ProcessInstanceController extends BaseController {

    @Autowired
    private ProcessInstanceService processInstanceService;

    /**
     * 分页查询流程定义列表
     * @param params 参数
     * @return
     */
    @PostMapping(value = "/page-model")
    public Result pageModel(ProcessInstanceQueryVo params) {
        return Result.success(processInstanceService.getPagerModel(params));
    }

    /**
     * 删除流程实例
     * 不建议直接删除实例，而是挂起
     * @param processInstanceId 实例id
     */
    @GetMapping(value = "/deleteProcessInstanceById/{processInstanceId}")
    public Result deleteProcessInstanceById(@PathVariable String processInstanceId) {
        return processInstanceService.deleteProcessInstanceById(processInstanceId);
    }

    /**
     * 激活或者挂起流程定义
     * @param id            需要操作的流程定义id
     * @param suspensionState   进行的操作 0-激活，1-挂起
     */
    @PostMapping(value = "/saProcessInstanceById")
    public Result saProcessInstanceById(String id, int suspensionState) {
        return processInstanceService.suspendOrActivateProcessInstanceById(id, suspensionState);
    }

    /**
     * 终止
     * @param params 参数
     * @return
     */
    @PostMapping(value = "/stopProcess")
    public Result stopProcess(EndProcessVo params) {
        if (this.isSuspended(params.getProcessInstanceId())){
            params.setMessage("后台执行终止");
            params.setUserCode(this.getLoginUser().getId());
            return processInstanceService.stopProcessInstanceById(params);
        }else{
            return Result.fail(ReturnCode.INSTANCE_STOP_PROCESS_ERROR);
        }
    }
}
