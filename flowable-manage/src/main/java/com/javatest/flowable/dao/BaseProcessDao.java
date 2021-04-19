package com.javatest.flowable.dao;

import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author: Javen
 * @date: 2021/4/19 15:26
 * @description: 用于对基本流程的数据库操作
 */
@Repository
public interface BaseProcessDao {

    void deleteRunActinstsByIds(List<String> runActivityIds);

    void deleteHisActinstsByIds(List<String> runActivityIds);
}
