package com.javatest.flowable.entity.vo;

import com.javatest.flowable.common.page.PageParams;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author: Javen
 * @date: 2021/4/8 16:43
 * @description: 流程定义模块的查询条件vo
 */
@Getter
@Setter
@NoArgsConstructor
public class ProcessDefinitionQueryVo extends PageParams implements Serializable {

    private static final long serialVersionUID = 1223456686631514174L;
    private String name;
    private String modelKey;
}
