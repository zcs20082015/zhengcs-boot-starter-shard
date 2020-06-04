package com.zhengcs.boot.starter.shard;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

/**
 * @author zhengcs
 * @version 1.0
 * @description
 * @date 2020/6/4 10:48 上午
 **/
@Slf4j
@MapperScan(value = "${zhengcs.shard.path.mapper.scan}",
        annotationClass = Mapper.class,
        sqlSessionTemplateRef = "shardingSqlSessionTemplate")
@Configuration
@ConditionalOnProperty(prefix = "zhengcs.shard", name = "open", havingValue = "true", matchIfMissing = false)
public class ShardingJdbcAutoConfiguration implements DisposableBean {

    @Value("classpath*:${zhengcs.shard.path.mapper.xml}")
    private String mapperXmlPath;

    @Value("${zhengcs.shard.path.mybatis.config:}")
    private String mybatisConfigPath;

    private final static String defaultMybatisConfigPath = "mybatis-configuration.xml";

    private final DataSource shardingDataSource;

    public ShardingJdbcAutoConfiguration(@Qualifier("shardingDataSource") DataSource shardingDataSource) {
        this.shardingDataSource = shardingDataSource;
    }

    @Bean
    public SqlSessionFactoryBean shardingSqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(shardingDataSource);
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sqlSessionFactoryBean.setMapperLocations(resolver.getResources(mapperXmlPath));
        sqlSessionFactoryBean.setConfigLocation(new ClassPathResource(StringUtils.isEmpty(mybatisConfigPath) ? defaultMybatisConfigPath : mybatisConfigPath));
        //sqlSessionFactoryBean.setTypeAliasesPackage("com.clubfactory.sc.entity.po");
        return sqlSessionFactoryBean;
    }

    @Bean
    public SqlSessionTemplate shardingSqlSessionTemplate(
            @Qualifier("shardingSqlSessionFactory") SqlSessionFactory shardingSqlSessionFactory) {
        return new SqlSessionTemplate(shardingSqlSessionFactory);
    }

    @Bean
    public DataSourceTransactionManager shardingTransactionManager() {
        return new DataSourceTransactionManager(shardingDataSource);
    }

    @Bean
    public TransactionTemplate shardingTransactionTemplate() {
        return new TransactionTemplate(shardingTransactionManager());
    }

    @Override
    public void destroy() throws Exception {
        log.info("shutdown....");
    }
}
