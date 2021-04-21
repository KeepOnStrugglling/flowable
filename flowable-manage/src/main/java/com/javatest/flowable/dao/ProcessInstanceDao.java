package com.javatest.flowable.dao;

import com.javatest.flowable.entity.vo.ProcessInstanceQueryVo;
import com.javatest.flowable.entity.vo.ProcessInstanceVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author: Javen
 * @date: 2021/4/19 17:26
 * @description: 管理部署后生成的流程实例
 */
@Repository
public interface ProcessInstanceDao {

    List<ProcessInstanceVo> getPage(@Param("params")ProcessInstanceQueryVo params);
}
