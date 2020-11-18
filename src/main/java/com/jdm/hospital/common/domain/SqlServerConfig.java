package com.jdm.hospital.common.domain;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
//com.jdm.hospital.common.dao
@Configuration
@MapperScan(basePackages ="com.jdm.Lis.dao",sqlSessionFactoryRef = "sqlserverSqlSessionFactory")
public class SqlServerConfig {
	@Bean(name = "sqlserverDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.sqlserver")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }
 
    @Bean(name = "sqlserverTransactionManager")
    public DataSourceTransactionManager transactionManager(@Qualifier("sqlserverDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
 
    }
 
    @Bean(name = "sqlserverSqlSessionFactory")
    public SqlSessionFactory basicSqlSessionFactory(@Qualifier("sqlserverDataSource") DataSource basicDataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(basicDataSource);
        factoryBean.setMapperLocations(///jdm/src/main/resources/mybatis/lis
                new PathMatchingResourcePatternResolver().getResources("mybatis/lis/*Mapper.xml"));
        return factoryBean.getObject();
    }
 
    @Bean(name = "sqlserverSqlSessionTemplate")
    public SqlSessionTemplate testSqlSessionTemplate(
            @Qualifier("sqlserverSqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
