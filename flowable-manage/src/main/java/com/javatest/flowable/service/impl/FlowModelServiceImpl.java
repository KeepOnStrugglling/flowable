package com.javatest.flowable.service.impl;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.javatest.flowable.common.enums.ReturnCode;
import com.javatest.flowable.common.response.Result;
import com.javatest.flowable.service.FlowModelService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.BpmnAutoLayout;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.editor.language.json.converter.BpmnJsonConverter;
import org.flowable.idm.api.User;
import org.flowable.ui.common.security.SecurityUtils;
import org.flowable.ui.common.service.exception.BadRequestException;
import org.flowable.ui.common.util.XmlUtil;
import org.flowable.ui.modeler.domain.AbstractModel;
import org.flowable.ui.modeler.domain.Model;
import org.flowable.ui.modeler.repository.ModelRepository;
import org.flowable.ui.modeler.serviceapi.ModelService;
import org.flowable.validation.ProcessValidator;
import org.flowable.validation.ProcessValidatorFactory;
import org.flowable.validation.ValidationError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.List;

/**
 * @author: Javen
 * @date: 2021/3/3 16:14
 * @description: 用于管理模型
 */
@Service
@Slf4j
public class FlowModelServiceImpl implements FlowModelService {

    @Autowired
    protected ModelRepository modelRepository;
    @Autowired
    protected ModelService modelService;

    protected BpmnXMLConverter bpmnXmlConverter = new BpmnXMLConverter();
    protected BpmnJsonConverter bpmnJsonConverter = new BpmnJsonConverter();

    @Override
    public Result importProcessModel(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName != null && (fileName.endsWith(".bpmn") || fileName.endsWith(".bpmn20.xml"))) {
            try {
                InputStream inputStream = file.getInputStream();
                return this.createModel(inputStream);
            } catch (IOException e) {
                return Result.fail(ReturnCode.MODEL_IMPORT_ERROR);
            }
        } else {
            return new Result(ReturnCode.MODEL_COMMON_ERROR.getCode(), null, "Invalid file name, only .bpmn and .bpmn20.xml files are supported not " + fileName);
        }
    }

    /**
     * 创建模型
     */
    private Result createModel(InputStream inputStream) {
        try {
            XMLInputFactory xif = XmlUtil.createSafeXmlInputFactory();
            InputStreamReader xmlIn = new InputStreamReader(inputStream, "UTF-8");
            XMLStreamReader xtr = xif.createXMLStreamReader(xmlIn);
            BpmnModel bpmnModel = bpmnXmlConverter.convertToBpmnModel(xtr);
            //模板验证
            ProcessValidator validator = new ProcessValidatorFactory().createDefaultProcessValidator();
            List<ValidationError> errors = validator.validate(bpmnModel);
            if (CollectionUtils.isNotEmpty(errors)) {
                StringBuffer es = new StringBuffer();
                errors.forEach(ve -> es.append(ve.toString()).append("/n"));
                return new Result(ReturnCode.MODEL_COMMON_ERROR.getCode(), null, "模板验证失败，原因: " + es.toString());
            }
            String fileName = bpmnModel.getMainProcess().getName();
            if (CollectionUtils.isEmpty(bpmnModel.getProcesses())) {
                return new Result(ReturnCode.MODEL_COMMON_ERROR.getCode(), null, "No process found in definition " + fileName);
            }
            if (bpmnModel.getLocationMap().size() == 0) {
                BpmnAutoLayout bpmnLayout = new BpmnAutoLayout(bpmnModel);
                bpmnLayout.execute();
            }
            ObjectNode modelNode = bpmnJsonConverter.convertToJson(bpmnModel);
            org.flowable.bpmn.model.Process process = bpmnModel.getMainProcess();
            String name = process.getId();
            if (StringUtils.isNotEmpty(process.getName())) {
                name = process.getName();
            }
            String description = process.getDocumentation();
            User createdBy = SecurityUtils.getCurrentUserObject();
            //查询是否已经存在流程模板
            Model newModel = new Model();
            List<Model> models = modelRepository.findByKeyAndType(process.getId(), AbstractModel.MODEL_TYPE_BPMN);
            if (CollectionUtils.isNotEmpty(models)) {
                Model updateModel = models.get(0);
                newModel.setId(updateModel.getId());
            }
            newModel.setName(name);
            newModel.setKey(process.getId());
            newModel.setModelType(AbstractModel.MODEL_TYPE_BPMN);
            newModel.setCreated(Calendar.getInstance().getTime());
            newModel.setCreatedBy(createdBy.getId());
            newModel.setDescription(description);
            newModel.setModelEditorJson(modelNode.toString());
            newModel.setLastUpdated(Calendar.getInstance().getTime());
            newModel.setLastUpdatedBy(createdBy.getId());
            modelService.createModel(newModel, SecurityUtils.getCurrentUserObject());
            return Result.success();
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Import failed for {}", e);
            return new Result(ReturnCode.MODEL_COMMON_ERROR.getCode(), null, "Import failed for , error message " + e.getMessage());
        }
    }
}
