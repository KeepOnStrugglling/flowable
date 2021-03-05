package com.javatest.flowable.service;

import com.javatest.flowable.common.page.PageUtils;
import com.javatest.flowable.common.response.Result;
import org.flowable.ui.modeler.domain.AbstractModel;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author: Javen
 * @date: 2021/3/3 16:13
 * @description: 管理模块相关的业务逻辑
 */
public interface FlowModelService {

    Result importProcessModel(MultipartFile file);
}
