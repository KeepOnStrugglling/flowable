package com.javatest.flowable.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author: Javen
 * @date: 2021/4/8 16:52
 * @description: 流程实例vo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessDefinitionVo implements Serializable {

    protected String id;
    protected String modelKey;
    protected String name;
    protected int version;
    protected String category;
    protected String deploymentId;
    protected String resourceName;
    protected String dgrmResourceName;
    protected int suspensionState;
    protected String tenantId;
}
