package com.smartordering;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 智能点餐系统启动类
 *
 * @author smartordering
 */
@SpringBootApplication
@MapperScan("com.smartordering.modules.**.mapper")  // 扫描所有模块的 Mapper 接口
public class SmartOrderingApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartOrderingApplication.class, args);
    }
}