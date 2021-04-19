package com.javatest.flowable.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.javatest.flowable.common.constant.Constant;
import com.javatest.flowable.common.page.PageUtils;
import com.javatest.flowable.common.response.Result;
import com.javatest.flowable.dao.ProcessDefinitionDao;
import com.javatest.flowable.entity.vo.ProcessDefinitionQueryVo;
import com.javatest.flowable.entity.vo.ProcessDefinitionVo;
import com.javatest.flowable.service.ProcessDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: Javen
 * @date: 2021/4/8 16:19
 * @description: 管理部署后生成的流程定义
 */
@Service
public class ProcessDefinitionServiceImpl extends BaseProcessService implements ProcessDefinitionService{

    @Autowired
    private ProcessDefinitionDao processDefinitionDao;

    @Override
    public Result suspendOrActivateProcessDefinitionById(String id, int suspensionState) {
        if (suspensionState == Constant.STATUS_HANGON) {
            repositoryService.suspendProcessDefinitionById(id,true,null);
            return Result.success();
        } else {
            repositoryService.activateProcessDefinitionById(id,true,null);
            return Result.success();
        }
    }

    @Override
    public PageUtils getPagerModel(ProcessDefinitionQueryVo params) {
        Page<ProcessDefinitionQueryVo> page = new Page<>(params.getCurPage(),params.getLimit());
        IPage<ProcessDefinitionVo> list = processDefinitionDao.getPagerModel(params);
        return new PageUtils(list);
    }

    @Override
    public ProcessDefinitionVo getById(String processDefinitionId) {
        return processDefinitionDao.getById(processDefinitionId);
    }
}
