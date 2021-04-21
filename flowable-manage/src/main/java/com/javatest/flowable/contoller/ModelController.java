package com.javatest.flowable.contoller;

import com.javatest.flowable.common.enums.ReturnCode;
import com.javatest.flowable.common.page.PageUtils;
import com.javatest.flowable.common.response.Result;
import com.javatest.flowable.service.FlowModelService;
import com.javatest.flowable.service.impl.FlowProcessDiagramGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.IdentityService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.idm.api.User;
import org.flowable.ui.common.service.exception.BadRequestException;
import org.flowable.ui.modeler.domain.AbstractModel;
import org.flowable.ui.modeler.domain.Model;
import org.flowable.ui.modeler.serviceapi.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Javen
 * @date: 2021/3/3 15:40
 * @description: 用于管理模型
 */
@RestController
@RequestMapping("model")
@Slf4j
public class ModelController extends BaseController {

    @Autowired
    private ModelService modelService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private IdentityService identityService;
    @Autowired
    private FlowModelService flowModelService;
    @Autowired
    private FlowProcessDiagramGenerator flowProcessDiagramGenerator;

    /**
     * 分页查询所有接口
     */
    @GetMapping(value = "/page-model")
    public Result<PageUtils<AbstractModel>> pageModel(@RequestParam Map<String, Object> params) {
        List<AbstractModel> datas = modelService.getModelsByModelType(AbstractModel.MODEL_TYPE_BPMN);
        int pageSize = params.get("pageSize") != null ? (int) params.get("pageSize") :1;
        int currPage = params.get("currPage") != null ? (int) params.get("currPage") :10;
        PageUtils<AbstractModel> pm = new PageUtils<>(datas,datas.size(),pageSize,currPage);
        pm.getList().forEach(abstractModel -> {
            User user = identityService.createUserQuery().userId(abstractModel.getCreatedBy()).singleResult();
            abstractModel.setCreatedBy(user.getFirstName());
        });
        return Result.success(pm);
    }

    /**
     * 导入bpmn模型(bpmn文件)
     */
    @PostMapping(value = "/import-process-model")
    public Result importProcessModel(@RequestParam("file") MultipartFile file) {
        try {
            flowModelService.importProcessModel(file);
        }catch (BadRequestException e){
            return Result.fail(ReturnCode.MY_EXCEPTION,e.getMessage());
        }
        return Result.success();
    }

    /**
     * 根据modelid加载xml(返回xml文件的字节流)
     */
    @GetMapping(value = "/loadXmlByModelId/{modelId}")
    public void loadXmlByModelId(@PathVariable String modelId, HttpServletResponse response) {
        try {
            Model model = modelService.getModel(modelId);
            byte[] b = modelService.getBpmnXML(model);
            response.setHeader("Content-type", "text/xml;charset=UTF-8");
            response.getOutputStream().write(b);
        } catch (Exception e) {
            log.error("ApiFlowableModelResource-loadXmlByModelId:" + e);
            e.printStackTrace();
        }
    }

    /**
     * 根据模型id部署
     */
    @PostMapping(value = "/deploy")
    public Result deploy(String modelId) {
        if (StringUtils.isBlank(modelId)) {
            return Result.fail(ReturnCode.MODEL_ID_NULL);
        }
        Deployment deploy = null;
        try {
            Model model = modelService.getModel(modelId.trim());
            // TODO 后续根据需求追加添加分类
            String categoryCode = "";
            BpmnModel bpmnModel = modelService.getBpmnModel(model);
            // 添加隔离信息，可用于多用户隔离
            String tenantId = "flow";
            // 必须指定文件后缀名否则部署不成功
            deploy = repositoryService.createDeployment()
                    .name(model.getName())
                    .key(model.getKey())
                    .category(categoryCode)
                    .tenantId(tenantId)
                    .addBpmnModel(model.getKey() + ".bpmn", bpmnModel)
                    .deploy();
        } catch (Exception e) {
            log.error("流程部署失败.",e);
            return Result.fail(ReturnCode.MODEL_DEPLOY_ERROR);
        }
        return Result.success(deploy.getId());
    }

    /**
     * 根据模型id加载流程图到页面
     */
    @GetMapping(value = "/loadPngByModelId/{modelId}")
    public void loadPngByModelId(@PathVariable String modelId, HttpServletResponse response) {
        Model model = modelService.getModel(modelId);
        BpmnModel bpmnModel = modelService.getBpmnModel(model, new HashMap<>(), new HashMap<>());
        InputStream is = flowProcessDiagramGenerator.generateDiagram(bpmnModel);
        try {
            response.setHeader("Content-Type", "image/png");
            byte[] b = new byte[1024];
            int len;
            while ((len = is.read(b, 0, 1024)) != -1) {
                response.getOutputStream().write(b, 0, len);
            }
        } catch (Exception e) {
            log.error("ApiFlowableModelResource-loadPngByModelId:" + e);
            e.printStackTrace();
        }
    }
}
