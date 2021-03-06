package com.wangym.gitlab.service;

import com.wangym.gitlab.configuration.GitlabConf;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.GroupApi;
import org.gitlab4j.api.RepositoryApi;
import org.gitlab4j.api.models.Branch;
import org.gitlab4j.api.models.CompareResults;
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
                log.info("开始查询项目分支信息，group：{},project：{}", p.getNamespace().getFullPath(), p.getName());
                Integer projectId = p.getId();
                List<Branch> branches = repositoryApi.getBranches(projectId);
                List<Branch> branchs = branches.stream()
                        // 已经合并入master的分支可以考虑去删除
                        .filter(Branch::getMerged)
                        // 过滤出不是想要保留的分支
                        .filter(it -> !protectBranch.contains(it.getName()))
                        .collect(Collectors.toList());
                for (Branch branch : branchs) {
                    String branchName = branch.getName();
                    log.info("[当前分支已合并进入master]:{},{},{}", p.getName(), branchName, branch.getMerged());
                    deleteBranch(projectId, branchName);
                }
                List<Branch> branchs2 = branches.stream()
                        // 已经合并入master的分支可以考虑去删除
                        .filter(it -> !it.getMerged())
                        // 过滤出不是想要保留的分支
                        .filter(it -> !protectBranch.contains(it.getName()))
                        .collect(Collectors.toList());
                compareTest(branchs2, projectId);

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

    private void compareTest(List<Branch> branchs, Integer projectId) throws GitLabApiException {
        RepositoryApi repositoryApi = gitLabApi.getRepositoryApi();
        for (Branch branch : branchs) {
            String name = branch.getName();
            CompareResults result = repositoryApi.compare(projectId, "master", name);
            if (result.getDiffs().isEmpty()) {
                log.info("[当前分支的全部变更已在master中存在，此分支可以去除],branch：{}", name);
                deleteBranch(projectId, name);
            }
        }
    }

    private void deleteBranch(Integer projectId, String branchName) {
        RepositoryApi repositoryApi = gitLabApi.getRepositoryApi();
        try {
            repositoryApi.deleteBranch(projectId, branchName);
        } catch (GitLabApiException e) {
            log.error("删除分支失败，分支名:{}", branchName, e);
        }
    }
}
