package com.wangym.gitlab.service;

import com.wangym.gitlab.configuration.GitlabConf;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.GroupApi;
import org.gitlab4j.api.models.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProjectService {
    @Autowired
    private GitLabApi gitLabApi;
    @Autowired
    private GitlabConf conf;

    public void handle() {
        try {
            List<String> groupScope = conf.getGroupScope();
            GroupApi groupApi = gitLabApi.getGroupApi();
            List<Project> projects = groupApi.getGroups().stream()
                    // 只找出关注的组
                    .filter(g -> groupScope.contains(g.getName()))
                    // 找出组下面的项目
                    .map(g -> getProjects(g.getId())).flatMap(List::stream).collect(Collectors.toList());
            for (Project p : projects) {
                String realUrl = "git clone " + p.getSshUrlToRepo() + " "
                        + p.getPathWithNamespace();
                System.out.println(realUrl);

            }
        } catch (GitLabApiException e) {
            e.printStackTrace();
        }
    }

    private List<Project> getProjects(int groupId) {
        GroupApi groupApi = gitLabApi.getGroupApi();
        try {
            return groupApi.getProjects(groupId);
        } catch (GitLabApiException e) {
            throw new RuntimeException(e);
        }
    }

}
