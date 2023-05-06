package com.oppo.cloud.diagnosis.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * MyBatis相关配置
 */
@Configuration
@EnableTransactionManagement
@MapperScan({"com.oppo.cloud.mapper", "com.oppo.cloud.portal.dao"})
public class MyBatisConfig {

}
