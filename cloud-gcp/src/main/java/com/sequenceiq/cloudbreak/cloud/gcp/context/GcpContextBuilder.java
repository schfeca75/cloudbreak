package com.sequenceiq.cloudbreak.cloud.gcp.context;

import org.springframework.stereotype.Service;

import com.google.api.services.compute.Compute;
import com.sequenceiq.cloudbreak.cloud.event.context.AuthenticatedContext;
import com.sequenceiq.cloudbreak.cloud.event.context.CloudContext;
import com.sequenceiq.cloudbreak.cloud.gcp.util.GcpStackUtil;
import com.sequenceiq.cloudbreak.cloud.model.CloudCredential;
import com.sequenceiq.cloudbreak.cloud.template.ResourceContextBuilder;
import com.sequenceiq.cloudbreak.domain.CloudPlatform;

@Service
public class GcpContextBuilder implements ResourceContextBuilder<GcpContext> {

    @Override
    public GcpContext contextInit(CloudContext context, AuthenticatedContext auth, boolean build) {
        CloudCredential credential = auth.getCloudCredential();
        String projectId = GcpStackUtil.getProjectId(credential);
        Compute compute = GcpStackUtil.buildCompute(credential);
        return new GcpContext(context.getStackName(), context.getRegion(), projectId, compute, context.getParallelResourceRequest(), build);
    }

    @Override
    public String platform() {
        return CloudPlatform.GCP.name();
    }
}