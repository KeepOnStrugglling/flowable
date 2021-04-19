package com.javatest.flowable.contoller;

import com.javatest.flowable.common.response.Result;
import com.javatest.flowable.entity.vo.ProcessDefinitionQueryVo;
import com.javatest.flowable.entity.vo.ProcessDefinitionVo;
import com.javatest.flowable.service.ProcessDefinitionService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.impl.util.IoUtil;
import org.flowable.engine.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;

/**
 * @author: Javen
 * @date: 2021/3/3 15:43
 * @description: 管理部署后生成的流程定义
 */
@RestController
@RequestMapping("definition")
@Slf4j
public class ProcessDefinitionController {

    @Autowired
    private ProcessDefinitionService processDefinitionService;
    @Autowired
    private RepositoryService repositoryService;

    /**
     * 激活或者挂起流程定义
     * @param id    流程id
     * @param suspensionState   执行操作类型，0-激活，1-挂起
     */
    @PostMapping(value = "/proceedDefinitionById")
    public Result proceedDefinitionById(String id, int suspensionState) {
        return processDefinitionService.suspendOrActivateProcessDefinitionById(id,suspensionState);
    }

    /**
     * 分页查询流程定义列表
     * @param params 参数
     */
    @PostMapping(value = "/page-model")
    public Result pageModel(ProcessDefinitionQueryVo params) {
        return Result.success(processDefinitionService.getPagerModel(params));
    }

    /**
     * 删除流程定义
     * @param deploymentId 部署id
     */
    @PostMapping(value = "/deleteDeployment")
    public Result deleteDeployment(String deploymentId) {
        repositoryService.deleteDeployment(deploymentId, true);
        return Result.success();
    }

    /**
     * 通过id和类型获取图片
     *
     * @param id       定义id
     * @param type     类型
     * @param response response
     */
    @GetMapping(value = "/processFile/{type}/{id}")
    public void processFile(@PathVariable String id, @PathVariable String type, HttpServletResponse response) {
        try {
            byte[] b = null;
            ProcessDefinitionVo pd = processDefinitionService.getById(id);
            if (pd != null) {
                if ("xml".equals(type)) {
                    response.setHeader("Content-type", "text/xml;charset=UTF-8");
                    InputStream inputStream = repositoryService.getResourceAsStream(pd.getDeploymentId(), pd.getResourceName());
                    b = IoUtil.readInputStream(inputStream, "image inputStream name");
                } else {
                    response.setHeader("Content-Type", "image/png");
                    InputStream inputStream = repositoryService.getResourceAsStream(pd.getDeploymentId(), pd.getDgrmResourceName());
                    b = IoUtil.readInputStream(inputStream, "image inputStream name");
                }
                response.getOutputStream().write(b);
            }
        } catch (Exception e) {
            log.error("ApiFlowableModelResource-loadXmlByModelId:" + e);
            e.printStackTrace();
        }
    }
}
