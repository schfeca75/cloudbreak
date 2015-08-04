package com.sequenceiq.cloudbreak.service.stack.connector.openstack.adapter;

import static com.sequenceiq.cloudbreak.service.stack.flow.ReflectionUtils.getDeclaredFields;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.cloud.event.PreProvisionCheckRequest;
import com.sequenceiq.cloudbreak.cloud.event.PreProvisionCheckResult;
import com.sequenceiq.cloudbreak.cloud.event.ProvisionSetupRequest;
import com.sequenceiq.cloudbreak.cloud.event.ProvisionSetupResult;
import com.sequenceiq.cloudbreak.cloud.event.context.CloudContext;
import com.sequenceiq.cloudbreak.cloud.model.CloudCredential;
import com.sequenceiq.cloudbreak.cloud.model.CloudStack;
import com.sequenceiq.cloudbreak.converter.spi.StackToCloudStackConverter;
import com.sequenceiq.cloudbreak.domain.CloudPlatform;
import com.sequenceiq.cloudbreak.domain.OpenStackCredential;
import com.sequenceiq.cloudbreak.domain.Stack;
import com.sequenceiq.cloudbreak.service.stack.connector.ProvisionSetup;
import com.sequenceiq.cloudbreak.service.stack.connector.openstack.OpenStackProvisionSetup;
import com.sequenceiq.cloudbreak.service.stack.event.ProvisionEvent;
import com.sequenceiq.cloudbreak.service.stack.event.ProvisionSetupComplete;

import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.rx.Promise;
import reactor.rx.Promises;

@Component
public class OpenStackProvisionSetupAdapter implements ProvisionSetup {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenStackProvisionSetupAdapter.class);

    @Value("${cb.openstack.experimental.connector:false}")
    private boolean experimentalConnector;

    @Inject
    private OpenStackProvisionSetup openStackProvisionSetup;

    @Inject
    private StackToCloudStackConverter cloudStackConverter;

    @Inject
    private EventBus eventBus;

    @Override
    public String preProvisionCheck(Stack stack) {
        if (experimentalConnector) {
            CloudContext cloudContext = new CloudContext(stack.getId(), stack.getName(), CloudPlatform.OPENSTACK.name());
            CloudCredential cloudCredential = buildCloudCredential(stack);
            CloudStack cloudStack = cloudStackConverter.convert(stack);
            Promise<PreProvisionCheckResult> promise = Promises.prepare();
            PreProvisionCheckRequest preProvisionCheckRequest = new PreProvisionCheckRequest(cloudContext, cloudCredential, cloudStack, promise);
            LOGGER.info("Triggering event: {}", preProvisionCheckRequest);
            eventBus.notify(preProvisionCheckRequest.selector(), Event.wrap(preProvisionCheckRequest));
            PreProvisionCheckResult res;
            try {
                res = promise.await(1, TimeUnit.HOURS);
                LOGGER.info("Result: {}", res);
            } catch (InterruptedException e) {
                LOGGER.error("Error while executing pre-provision check", e);
                return e.getMessage();
            }
            return res.getMessage();
        }
        return openStackProvisionSetup.preProvisionCheck(stack);
    }

    @Override
    public ProvisionEvent setupProvisioning(Stack stack) throws Exception {
        if (experimentalConnector) {
            CloudContext cloudContext = new CloudContext(stack.getId(), stack.getName(), CloudPlatform.OPENSTACK.name());
            CloudCredential cloudCredential = buildCloudCredential(stack);
            CloudStack cloudStack = cloudStackConverter.convert(stack);
            Promise<ProvisionSetupResult> promise = Promises.prepare();
            ProvisionSetupRequest provisionSetupRequest = new ProvisionSetupRequest(cloudContext, cloudCredential, cloudStack, promise);
            LOGGER.info("Triggering event: {}", provisionSetupRequest);
            eventBus.notify(provisionSetupRequest.selector(), Event.wrap(provisionSetupRequest));
            ProvisionSetupResult res;
            try {
                res = promise.await(1, TimeUnit.HOURS);
                LOGGER.info("Result: {}", res);
                if (res.isFailed()) {
                    throw res.getException();
                }
            } catch (InterruptedException e) {
                LOGGER.error("Error while executing pre-provision check", e);
            }
            return new ProvisionSetupComplete(getCloudPlatform(), stack.getId());
        }
        return openStackProvisionSetup.setupProvisioning(stack);
    }

    @Override
    public CloudPlatform getCloudPlatform() {
        return CloudPlatform.OPENSTACK;
    }

    private CloudCredential buildCloudCredential(Stack stack) {
        //TODO move to the new OpenStack module in some util class
        OpenStackCredential openstackCredential = (OpenStackCredential) stack.getCredential();
        return new CloudCredential(openstackCredential.getName(), getDeclaredFields(openstackCredential));
    }

}