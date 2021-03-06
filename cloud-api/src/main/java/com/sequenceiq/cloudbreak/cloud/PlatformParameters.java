package com.sequenceiq.cloudbreak.cloud;

import java.util.List;
import java.util.Map;

/**
 * Platform parameters.
 *
 * Note: this interface is going to be completely refactored
 */
public interface PlatformParameters {

    String diskPrefix();

    Integer startLabel();

    Map<String, String> diskTypes();

    String defaultDiskType();

    Map<String, String> regions();

    String defaultRegion();

    Map<String, List<String>> availabiltyZones();

    Map<String, String> virtualMachines();

    String defaultVirtualMachine();
}
