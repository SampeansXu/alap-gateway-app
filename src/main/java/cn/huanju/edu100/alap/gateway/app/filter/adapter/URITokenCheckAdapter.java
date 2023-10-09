package cn.huanju.edu100.alap.gateway.app.filter.adapter;

import com.google.common.collect.Lists;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: URI 是否token验证 适配器
 * 黑名单的优先级大于白名单，即同时在黑名单和白名单中的URI，黑名单有效，白名单失效
 *
 * @Author: xushengbin@hqwx.com
 * @Date: 2021-08-19
 */
@Data
public class URITokenCheckAdapter implements Serializable {

    /**
     * 忽略Token验证的 URI白名单
     */
    private List<URIRegex> whiteList = Lists.newArrayList();

    /**
     * 必须Token验证的 URI黑名单
     */
    private List<URIRegex> blackList = Lists.newArrayList();
}
