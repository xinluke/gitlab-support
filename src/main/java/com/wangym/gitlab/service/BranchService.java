package com.wangym.gitlab.service;

import com.wangym.gitlab.configuration.GitlabConf;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.GroupApi;
import org.gitlab4j.api.RepositoryApi;
import org.gitlab4j.api.models.Branch;
import org.gitlab4j.api.models.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wangym
 * @version 创建时间：2019年3月13日 下午1:10:30
 */
@Service
@Slf4j
public class BranchService {

    @Autowired
    private GitLabApi gitLabApi;
    @Autowired
    private GitlabConf conf;

    public void handle() {
        try {
            List<String> groupScope = conf.getGroupScope();
            List<String> protectBranch = conf.getProtectBranch();
            GroupApi groupApi = gitLabApi.getGroupApi();
            RepositoryApi repositoryApi = gitLabApi.getRepositoryApi();
            List<Project> projects = groupApi.getGroups().stream()
                    // 只找出关注的组
                    .filter(g -> groupScope.contains(g.getName()))
                    // 找出组下面的项目
                    .map(g -> getProjects(g.getId()))
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
            for (Project p : projects) {
                Integer projectId = p.getId();
                List<Branch> branchs = repositoryApi.getBranches(projectId).stream()
                        // 已经合并入master的分支可以考虑去删除
                        .filter(Branch::getMerged)
                        // 过滤出不是想要保留的分支
                        .filter(it -> !protectBranch.contains(it.getName()))
                        // .filter(branch
                        // ->branch.getName().startsWith("feature/2018"))
                        .collect(Collectors.toList());
                for (Branch branch : branchs) {
                    String branchName = branch.getName();
                    log.info("result:{},{},{},{}", p.getNamespace().getFullPath(), p.getName(), branchName,
                            branch.getMerged());
                    // repositoryApi.deleteBranch(projectId, branchName);
                }

            }
        } catch (GitLabApiException e) {
            // TODO Auto-generated catch block
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
