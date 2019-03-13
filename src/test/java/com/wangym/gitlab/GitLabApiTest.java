package com.wangym.gitlab;

import com.alibaba.fastjson.JSON;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.RepositoryApi;
import org.gitlab4j.api.models.Branch;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
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

}
