package cn.huanju.edu100.alap.gateway.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@ImportResource(locations={"classpath:uri-tokencheck-adapter-spring.xml"})
public class AlapGatewayAppApplication {

    public static void main(String[] args) {
        try{
            SpringApplication.run(AlapGatewayAppApplication.class, args);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

}
