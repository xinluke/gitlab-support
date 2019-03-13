package com.wangym.gitlab.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author wangym
 * @version 创建时间：2019年3月13日 下午2:45:12
 */
@Configuration
@ConfigurationProperties(prefix = "gitlab")
@Data
public class GitlabConf {

    private List<String> groupScope;
    private List<String> protectBranch;
}
