package com.sequenceiq.cloudbreak.cloud.gcp;

import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.cloud.Authenticator;
import com.sequenceiq.cloudbreak.cloud.context.AuthenticatedContext;
import com.sequenceiq.cloudbreak.cloud.context.CloudContext;
import com.sequenceiq.cloudbreak.cloud.model.CloudCredential;
import com.sequenceiq.cloudbreak.common.type.CloudPlatform;

@Service
public class GcpAuthenticator implements Authenticator {

    @Override
    public String platform() {
        return CloudPlatform.GCP.name();
    }

    @Override
    public String variant() {
        return CloudPlatform.GCP.name();
    }

    @Override
    public AuthenticatedContext authenticate(CloudContext cloudContext, CloudCredential cloudCredential) {
        return new AuthenticatedContext(cloudContext, cloudCredential);
    }
}
