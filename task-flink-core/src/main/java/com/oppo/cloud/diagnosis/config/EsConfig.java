package com.oppo.cloud.diagnosis.config;


import com.oppo.cloud.common.util.elastic.ElasticsearchClient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * es客户端配置
 */
@Configuration
@Slf4j
@Data
public class EsConfig {
    /**
     * es主机列表
     */
    @Value("${spring.elasticsearch.nodes}")
    private String hosts;
    /**
     * 用户名
     */
    @Value("${spring.elasticsearch.username}")
    private String username;
    /**
     * 密码
     */
    @Value("${spring.elasticsearch.password}")
    private String password;
    /**
     * bean名称
     */
    public final static String ELASTIC_CLIENT = "flinkElasticClient";

    @Primary
    @Bean(ELASTIC_CLIENT)
    public RestHighLevelClient restHighLevelClient() {
        return ElasticsearchClient.create(hosts, username, password);
    }

}
