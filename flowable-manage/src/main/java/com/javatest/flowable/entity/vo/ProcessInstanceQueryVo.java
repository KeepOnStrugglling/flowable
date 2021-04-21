package com.javatest.flowable.entity.vo;

import com.javatest.flowable.common.page.PageParams;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author : bruce.liu
 * @title: : QueryProcessInstanceVo
 * @projectName : flowable
 * @description: 查询流程实例VO
 * @date : 2019/11/2115:42
 */
@Getter
@Setter
@NoArgsConstructor
public class ProcessInstanceQueryVo extends PageParams implements Serializable {

    private String formName;
    private String userCode;
    private String userName;

}
