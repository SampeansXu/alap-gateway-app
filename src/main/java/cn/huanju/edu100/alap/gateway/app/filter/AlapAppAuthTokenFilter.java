package cn.huanju.edu100.alap.gateway.app.filter;

import cn.huanju.edu100.alap.gateway.app.constant.AlapGateWayAppConstant;
import cn.huanju.edu100.alaplat.core.shared.jwt.AuthToken;
import cn.huanju.edu100.alaplat.core.shared.utils.AlapTokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


/**
 * @Description: Token鉴权
 * @Author: xushengbin@hqwx.com
 * @Date: 2021-08-18
 */
@Slf4j
@Component
public class AlapAppAuthTokenFilter extends BaseAuthFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        if(exchange.getRequest().getURI().getPath().equals("/test163")){
//            //测试
//            return chain.filter(exchange);
//        }

//        Long schId = this.findSchIdFromRequest(exchange);
//        if (schId == 0L) {
//            return this.errorResponse(exchange, "need schId or orgId", HttpStatus.BAD_REQUEST.value());
//        }
//
//        boolean isIgnoreCheck = this.isIgnoreCheck(exchange);
//        String sourceToken = this.findAppTokenFromRequest(exchange);
//        String alapToken = null;
//        if (isIgnoreCheck) {
//            //可忽略 token
//            if (StringUtils.isNotBlank(sourceToken)) {
//                //有token则验证
//                alapToken = this.generateAppAlapToken(schId, sourceToken);
//                if (StringUtils.isBlank(alapToken)) {
//                    //验证失败
//                    return this.errorResponse(exchange, "token invalid", HttpStatus.UNAUTHORIZED.value());
//                }
//            } else {
//                //生成公共访问的alap-token
//                AuthToken authToken = new AuthToken(schId, 0L);
//                authToken.setAppId(this.findAppIdFromRequest(exchange));
//                alapToken = AlapTokenUtils.generateJwtToken(authToken);
//            }
//
//            this.addAlapTokenToHttpHeader(exchange, alapToken);
//
//            return chain.filter(exchange);
//        }
//
//        //验证 token
//        if (StringUtils.isBlank(sourceToken)) {
//            return this.errorResponse(exchange, "need token", HttpStatus.BAD_REQUEST.value());
//        }
//
//        alapToken = this.generateAppAlapToken(schId, sourceToken);
//        if (StringUtils.isBlank(alapToken)) {
//            //验证失败
//            return this.errorResponse(exchange, "token invalid", HttpStatus.UNAUTHORIZED.value());
//        }
//
//        this.addAlapTokenToHttpHeader(exchange, alapToken);

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return AlapGateWayAppConstant.FilterOrder.AuthToken;
    }
}
