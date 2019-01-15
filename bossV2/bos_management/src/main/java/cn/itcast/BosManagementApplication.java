package cn.itcast;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;


/**
 * @author Administrator
 * @version 1.0
 **/
@SpringBootApplication
@EntityScan("cn.itcast.bos.domain")//扫描实体类
public class BosManagementApplication {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(BosManagementApplication.class, args);
    }
}
