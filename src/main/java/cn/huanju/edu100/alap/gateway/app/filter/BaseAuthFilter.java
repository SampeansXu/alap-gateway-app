package cn.huanju.edu100.alap.gateway.app.filter;

import cn.huanju.edu100.alap.gateway.app.auth.AlapTokenCacheRepository;
import cn.huanju.edu100.alap.gateway.app.filter.adapter.URIRegex;
import cn.huanju.edu100.alap.gateway.app.filter.adapter.URITokenCheckAdapter;
import cn.huanju.edu100.alaplat.core.shared.jwt.AuthToken;
import cn.huanju.edu100.alaplat.core.shared.utils.AlapTokenUtils;
import cn.huanju.edu100.hqwx.usercenter.api.UCTokenService;
import cn.huanju.edu100.hqwx.usercenter.model.api.AuthTokenDTO;
import cn.huanju.edu100.hqwx.usercenter.model.api.HQUCResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @Description: token
 * @Author: xushengbin@hqwx.com
 * @Date: 2021-08-22
 */
@Slf4j
public class BaseAuthFilter {

    @Resource
    URITokenCheckAdapter uriTokenCheckAdapter;
    @Resource
    UCTokenService ucTokenService;
    @Resource
    AlapTokenCacheRepository tokenCacheRepository;

    /**
     * 是否忽略token鉴权
     *
     * @param exchange
     * @return
     */
    protected boolean isIgnoreCheck(ServerWebExchange exchange) {
        if (Objects.isNull(uriTokenCheckAdapter)) {
            return false;
        }

        String httpURI = exchange.getRequest().getURI().getPath();
        if (Strings.isBlank(httpURI)) {
            return false;
        }

        //1.黑名单
        if (!CollectionUtils.isEmpty(uriTokenCheckAdapter.getBlackList())) {
            for (URIRegex rg : uriTokenCheckAdapter.getBlackList()) {
                if (rg.isMatched(httpURI)) {
                    //必须鉴权
                    log.debug("Black URI matched, HttpURI={}, {}", httpURI, rg.toString());
                    return false;
                }
            }
        }

        //2.白名单
        if (!CollectionUtils.isEmpty(uriTokenCheckAdapter.getWhiteList())) {
            for (URIRegex rg : uriTokenCheckAdapter.getWhiteList()) {
                if (rg.isMatched(httpURI)) {
                    //不需要鉴权
                    log.debug("White URI matched, HttpURI={}, {}", httpURI, rg.toString());
                    return true;
                }
            }
        }

        return false;
    }

    protected void addAlapTokenToHttpHeader(ServerWebExchange exchange, String alapToken) {
        if (StringUtils.isBlank(alapToken)) {
            return;
        }

        Consumer<HttpHeaders> httpHeaders = httpHeader -> {
            httpHeader.set("alap-token", alapToken);
        };

        ServerHttpRequest serverHttpRequest = exchange.getRequest().mutate().headers(httpHeaders).build();
        exchange.mutate().request(serverHttpRequest).build();
    }

    /**
     * C端用户
     *
     * @param schId
     * @param ucToken
     * @return
     */
    protected String generateAppAlapToken(Long schId, String ucToken) {
        if (StringUtils.isBlank(ucToken)) {
            return null;
        }

        try {
            //1.Redis缓存获取
            String alapToken = tokenCacheRepository.getToken(ucToken);

            //2.验证ucToken
            if (!AlapTokenUtils.checkAlapToken(alapToken)) {
                HQUCResponse<AuthTokenDTO> hqucResponse = ucTokenService.authToken(schId, ucToken);
                if (!hqucResponse.isOk() || Objects.isNull(hqucResponse.getData())) {
                    log.error("UserCenter authToken failed.UCToken={}, {}",ucToken, hqucResponse.toString());
                    return null;
                }

                AuthToken authToken = new AuthToken(schId, hqucResponse.getData().getUid());
                authToken.setTopSchId(hqucResponse.getData().getTopOrg());
                authToken.setUid(hqucResponse.getData().getUid());
                authToken.setuName(hqucResponse.getData().getuName());
                authToken.setAppId(hqucResponse.getData().gettAppid());
                authToken.setPlatform(hqucResponse.getData().gettPlatform());
                authToken.setLoginTime(System.currentTimeMillis());

                alapToken = AlapTokenUtils.generateJwtToken(authToken);
                tokenCacheRepository.setToken(ucToken, authToken.getAppId(), alapToken);
            }

            return alapToken;
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("generateAppAlapToken failed.", ex);
            return null;
        }
    }

    protected String findAppTokenFromRequest(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        String siteAppToken = request.getHeaders().getFirst("edu24ol-token");
        if (StringUtils.isBlank(siteAppToken)) {
            siteAppToken = request.getHeaders().getFirst("hqapp-token");
        }
        if (StringUtils.isBlank(siteAppToken)) {
            siteAppToken = request.getHeaders().getFirst("app-token");
        }
        if (StringUtils.isBlank(siteAppToken)) {
            // 有些低版本浏览器(IE10以下)跨域时没法通过请求头传递参数 20200925
            MultiValueMap<String, String> requestParamMap = request.getQueryParams();
            if (requestParamMap.containsKey("edu24ol-token")) {
                siteAppToken = requestParamMap.getFirst("edu24ol-token");
            }
        }

        if (StringUtils.isBlank(siteAppToken)) {
            // 如果请求头上没有则从cookie中再取一次
            MultiValueMap<String, HttpCookie> cookieMap = request.getCookies();
            if (cookieMap != null) {
                String tokenCookieKey = "passportCors";
                if (cookieMap.containsKey(tokenCookieKey)) {
                    siteAppToken = cookieMap.getFirst(tokenCookieKey).getValue();
                }
            }
        }

        return siteAppToken;
    }

    private Long findHttpRequestLong(ServerHttpRequest request, String headName) {
        String strVal = request.getHeaders().getFirst(headName);

        if (!StringUtils.isNumeric(strVal)) {
            return 0L;
        }

        return Long.valueOf(strVal);
    }

    private Long findRequestParamLong(MultiValueMap<String, String> requestParam, String paramName) {
        String strVal = requestParam.getFirst(paramName);

        if (!StringUtils.isNumeric(strVal)) {
            return 0L;
        }

        return Long.valueOf(strVal);
    }

    protected Long findSchIdFromRequest(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();

        Long schId = this.findHttpRequestLong(request, "orgId");
        if (schId != null && schId > 0L) {
            return schId;
        }

        schId = this.findHttpRequestLong(request, "orgid");
        if (schId != null && schId > 0L) {
            return schId;
        }

        schId = this.findHttpRequestLong(request, "schId");
        if (schId != null && schId > 0L) {
            return schId;
        }

        // 有些低版本浏览器(IE10以下)跨域时没法通过请求头传递参数 20200925
        MultiValueMap<String, String> requestParamMap = request.getQueryParams();
        if (requestParamMap.containsKey("orgId")) {
            schId = this.findRequestParamLong(requestParamMap, "orgId");
            if (schId != null && schId > 0L) {
                return schId;
            }
        }

        return 0L;
    }

    protected String findAppIdFromRequest(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();

        String appId = request.getHeaders().getFirst("appId");
        if (StringUtils.isNotBlank(appId)) {
            return appId;
        }

        appId = request.getHeaders().getFirst("appid");
        if (StringUtils.isNotBlank(appId)) {
            return appId;
        }

        return "";
    }

    protected Mono<Void> errorResponse(ServerWebExchange exchange, String msg, Integer httpStatus) {
        return Mono.defer(() -> {
            ServerHttpResponse response = exchange.getResponse();
            response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
            response.setRawStatusCode(httpStatus);
            DataBuffer buffer = response.bufferFactory().wrap(msg.getBytes());
            return response.writeWith(Flux.just(buffer));
        });
    }
}
