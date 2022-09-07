package com.wangym.gitlab.service;

import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.PipelineApi;
import org.gitlab4j.api.models.Pipeline;
import org.gitlab4j.api.models.PipelineFilter;
import org.gitlab4j.api.models.PipelineStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class PipelineService {

    @Autowired
    private GitLabApi gitLabApi;

    public void exportPipelinesWithMaster(long projectId, long beginDateTime, long endDateTime, String exportFilePath) throws Exception {
        PipelineApi pipelineApi = gitLabApi.getPipelineApi();
        // 过滤出master分支
        PipelineFilter pipelineFilter = new PipelineFilter();
        pipelineFilter.setRef("master");
        // 直接获取的pipeline列表没有时间信息
        List<Pipeline> pipelines = pipelineApi.getPipelines(projectId, pipelineFilter);
        try (FileWriter fileWriter = new FileWriter(exportFilePath, true)) {
            for (Pipeline pipeline : pipelines) {
                Long pipelineId = pipeline.getId();
                // 获取指定id的pipeline包含时间信息
                Pipeline p = pipelineApi.getPipeline(projectId, pipelineId);
                long createTime = p.getCreatedAt().getTime();
                if (createTime > endDateTime || createTime < beginDateTime) {
                    continue;
                }
                PipelineStatus status = p.getStatus();
                if (status != PipelineStatus.SUCCESS && status != PipelineStatus.FAILED) {
                    continue;
                }
                fileWriter.write(p.toString());
                fileWriter.flush();
            }
        } catch (IOException ioException) {
            log.error("writePipelinesWithMaster", ioException);
        }
    }

}
