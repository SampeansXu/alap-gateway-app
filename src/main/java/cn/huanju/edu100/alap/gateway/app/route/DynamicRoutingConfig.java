package cn.huanju.edu100.alap.gateway.app.route;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.yaml.snakeyaml.Yaml;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.Executor;


/**
 * @ClassName: DynamicRoutingConfig
 * @Description:
 * @Author: panxia
 * @Date: Create in 2020/6/5 2:54 下午
 * @Version:1.0
 */
@Component
public class DynamicRoutingConfig implements ApplicationEventPublisherAware {
    // 配置平台使用的路由配置
    private String dataId = "alap-gateway-router-app.yml";

    private String group = "DEFAULT_GROUP";

    @Value("${spring.cloud.nacos.config.server-addr}")
    private String serverAddr;
    @Value("${spring.cloud.nacos.config.namespace}")
    private String namespace;

    @Autowired
    private RouteDefinitionWriter routeDefinitionWriter;

    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    private static final List<String> ROUTE_LIST = new Vector<>();

    @PostConstruct
    public void dynamicRouteByNacosListener() {
        try {
            Properties properties = System.getProperties();
            properties.setProperty("serverAddr", serverAddr);
            properties.setProperty("namespace", namespace);
            ConfigService configService = NacosFactory.createConfigService(properties);
            String configInfo= configService.getConfig(dataId, group, 5000);
            loadConfig(configInfo);
            configService.addListener(dataId, group, new Listener() {
                @Override
                public void receiveConfigInfo(String configInfo) {
                    loadConfig(configInfo);
                }

                @Override
                public Executor getExecutor() {
                    return null;
                }
            });
        } catch (NacosException e) {
            e.printStackTrace();
        }
    }

    private void loadConfig(String configInfo) {
        try {
            Yaml yaml=new Yaml();
            Routes routes= yaml.loadAs(configInfo,Routes.class);
            if(!CollectionUtils.isEmpty(routes.getRoutes())){
                clearRoute();
                routes.getRoutes().forEach(routeDefinition -> addRoute(routeDefinition));
            }
            publish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearRoute() {
        for(String id : ROUTE_LIST) {
            this.routeDefinitionWriter.delete(Mono.just(id)).subscribe();
        }
        ROUTE_LIST.clear();
    }

    private void addRoute(RouteDefinition definition) {
        try {
            routeDefinitionWriter.save(Mono.just(definition)).subscribe();
            ROUTE_LIST.add(definition.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void publish() {
        this.applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this.routeDefinitionWriter));
    }

}
