package cn.huanju.edu100.alap.gateway.app.filter.adapter;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * @Description: URI 匹配器
 * @Author: xushengbin@hqwx.com
 * @Date: 2021-08-19
 */
@Data
public class URIRegex implements Serializable {
    /**
     * 比较类型
     */
    interface AdapterType {
        int Full = 1; //完全匹配
        int Regex = 2;//正则匹配
    }

    /**
     * URI是否匹配规则
     *
     * @param httpUri，请求的URI
     * @return
     */
    public boolean isMatched(String httpUri) {
        if (StringUtils.isBlank(httpUri)) {
            return false;
        }
        if (StringUtils.isBlank(this.pathRule)) {
            return false;
        }

        boolean isMatch = false;
        if (this.adapterType == AdapterType.Full) {
            isMatch = httpUri.equals(this.pathRule);
        } else if (this.adapterType == AdapterType.Regex) {
            isMatch = Pattern.matches(this.pathRule, httpUri);
        }

        return isMatch;
    }

    /**
     * URI是否匹配规则，忽略大小写
     *
     * @param httpUri，请求的URI
     * @return
     */
    public boolean isMatchedIgnoreCase(String httpUri) {
        if (StringUtils.isBlank(httpUri)) {
            return false;
        }
        if (StringUtils.isBlank(this.pathRule)) {
            return false;
        }

        boolean isMatch = false;
        if (this.adapterType == AdapterType.Full) {
            isMatch = httpUri.equalsIgnoreCase(this.pathRule);
        } else if (this.adapterType == AdapterType.Regex) {
            Pattern pattern = Pattern.compile(this.pathRule, Pattern.CASE_INSENSITIVE);
            isMatch = pattern.matcher(httpUri).matches();
        }

        return isMatch;
    }


    /**
     * 比较方式：1-完全匹配，2-正则式
     */
    private int adapterType = 1;

    /**
     * 比较的URI路径字符或正则
     */
    private String pathRule = null;

    /**
     * 说明描述
     */
    private String memo = "";
}
