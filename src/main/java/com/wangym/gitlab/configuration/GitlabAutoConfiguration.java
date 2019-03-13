package com.wangym.gitlab.configuration;

import org.gitlab4j.api.GitLabApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author wangym
 * @version 创建时间：2019年3月13日 下午2:05:42
 */
@Configuration
public class GitlabAutoConfiguration {

    @Bean
    public GitLabApi GitLabApi(@Value("${gitlab.hostUrl}") String hostUrl,
            @Value("${gitlab.privateToken}") String privateToken) {
        // Create a GitLabApi instance to communicate with your GitLab server
        GitLabApi gitLabApi = new GitLabApi(hostUrl, privateToken);
        return gitLabApi;
    }

}
