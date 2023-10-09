package cn.huanju.edu100.alap.gateway.app.route;

import org.springframework.cloud.gateway.route.RouteDefinition;

import java.util.List;

/**
 * @ClassName: Routes
 * @Description:路由配置
 * @Author: panxia
 * @Date: Create in 2020/6/10 11:30 上午
 * @Version:1.0
 */
public class Routes {
    private List<RouteDefinition> routes;

    public List<RouteDefinition> getRoutes() {
        return routes;
    }

    public void setRoutes(List<RouteDefinition> routes) {
        this.routes = routes;
    }
}
