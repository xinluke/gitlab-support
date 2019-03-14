package com.wangym.gitlab;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.RepositoryApi;
import org.gitlab4j.api.models.Branch;
import org.gitlab4j.api.models.CompareResults;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class GitLabApiTest {

    @Autowired
    private GitLabApi gitLabApi;

    @Test
    public void test() throws GitLabApiException {
        RepositoryApi repositoryApi = gitLabApi.getRepositoryApi();
        List<Branch> branchs = repositoryApi.getBranches(739).stream()
                // 已经合并入master的分支可以考虑去删除
                .filter(Branch::getMerged)
                .collect(Collectors.toList());
        for (Branch branch : branchs) {
            System.out.println(JSON.toJSONString(branch));
        }
    }

    @Test
    public void compareTest() throws GitLabApiException {
        RepositoryApi repositoryApi = gitLabApi.getRepositoryApi();
        List<Branch> branchs = repositoryApi.getBranches(739).stream()
                // 待比较的分支排除master
                .filter(it -> !it.getName().equals("master"))
                .collect(Collectors.toList());
        for (Branch branch : branchs) {
            CompareResults result = repositoryApi.compare(739, "master", branch.getName());
            if (result.getDiffs().isEmpty()) {
                log.info("当前分支的全部变更已在master中存在，此分支可以去除,branch：{},{}", branch.getName());
            }
        }
    }

}
