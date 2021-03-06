package com.hubspot.singularity;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.hubspot.deploy.ExecutorData;
import com.hubspot.mesos.Resources;
import com.hubspot.mesos.SingularityContainerInfo;

public class SingularityDeploy extends SingularityJsonObject {

  private final String requestId;

  private final String id;

  private final Optional<String> version;
  private final Optional<Long> timestamp;
  private final Optional<Map<String, String>> metadata;

  private final Optional<SingularityContainerInfo> containerInfo;

  private final Optional<String> customExecutorCmd;
  private final Optional<String> customExecutorId;
  private final Optional<String> customExecutorSource;
  private final Optional<Resources> resources;

  private final Optional<String> command;
  private final Optional<List<String>> arguments;
  private final Optional<Map<String, String>> env;
  private final Optional<List<String>> uris;
  private final Optional<ExecutorData> executorData;

  private final Optional<String> healthcheckUri;
  private final Optional<Long> healthcheckIntervalSeconds;
  private final Optional<Long> healthcheckTimeoutSeconds;
  private final Optional<Boolean> skipHealthchecksOnDeploy;

  private final Optional<Long> deployHealthTimeoutSeconds;

  private final Optional<Long> considerHealthyAfterRunningForSeconds;

  private final Optional<String> serviceBasePath;
  private final Optional<List<String>> loadBalancerGroups;
  private final Optional<Map<String, Object>> loadBalancerOptions;

  public static SingularityDeployBuilder newBuilder(String requestId, String id) {
    return new SingularityDeployBuilder(requestId, id);
  }

  public static SingularityDeploy fromBytes(byte[] bytes, ObjectMapper objectMapper) {
    try {
      return objectMapper.readValue(bytes, SingularityDeploy.class);
    } catch (IOException e) {
      throw new SingularityJsonException(e);
    }
  }

  @JsonCreator
  public SingularityDeploy(@JsonProperty("requestId") String requestId, @JsonProperty("id") String id, @JsonProperty("command") Optional<String> command, @JsonProperty("arguments") Optional<List<String>> arguments, @JsonProperty("containerInfo") Optional<SingularityContainerInfo> containerInfo, @JsonProperty("customExecutorCmd") Optional<String> customExecutorCmd,  @JsonProperty("customExecutorId") Optional<String> customExecutorId,
      @JsonProperty("customExecutorSource") Optional<String> customExecutorSource, @JsonProperty("resources") Optional<Resources> resources, @JsonProperty("env") Optional<Map<String, String>> env, @JsonProperty("uris") Optional<List<String>> uris, @JsonProperty("metadata") Optional<Map<String, String>> metadata,
      @JsonProperty("executorData") Optional<ExecutorData> executorData, @JsonProperty("version") Optional<String> version, @JsonProperty("timestamp") Optional<Long> timestamp, @JsonProperty("deployHealthTimeoutSeconds") Optional<Long> deployHealthTimeoutSeconds,
      @JsonProperty("healthcheckUri") Optional<String> healthcheckUri, @JsonProperty("healthcheckIntervalSeconds") Optional<Long> healthcheckIntervalSeconds, @JsonProperty("healthcheckTimeoutSeconds") Optional<Long> healthcheckTimeoutSeconds,
      @JsonProperty("serviceBasePath") Optional<String> serviceBasePath, @JsonProperty("loadBalancerGroups") Optional<List<String>> loadBalancerGroups, @JsonProperty("considerHealthyAfterRunningForSeconds") Optional<Long> considerHealthyAfterRunningForSeconds,
      @JsonProperty("loadBalancerOptions") Optional<Map<String, Object>> loadBalancerOptions, @JsonProperty("skipHealthchecksOnDeploy") Optional<Boolean> skipHealthchecksOnDeploy) {
    this.requestId = requestId;

    this.command = command;
    this.arguments = arguments;
    this.resources = resources;

    this.containerInfo = containerInfo;

    this.customExecutorCmd = customExecutorCmd;
    this.customExecutorId = customExecutorId;
    this.customExecutorSource = customExecutorSource;

    this.metadata = metadata;
    this.version = version;
    this.id = id;
    this.timestamp = timestamp;
    this.env = env;
    this.uris = uris;
    this.executorData = executorData;

    this.healthcheckUri = healthcheckUri;
    this.healthcheckIntervalSeconds = healthcheckIntervalSeconds;
    this.healthcheckTimeoutSeconds = healthcheckTimeoutSeconds;
    this.skipHealthchecksOnDeploy = skipHealthchecksOnDeploy;

    this.considerHealthyAfterRunningForSeconds = considerHealthyAfterRunningForSeconds;

    this.deployHealthTimeoutSeconds = deployHealthTimeoutSeconds;

    this.serviceBasePath = serviceBasePath;
    this.loadBalancerGroups = loadBalancerGroups;
    this.loadBalancerOptions = loadBalancerOptions;
  }

  public SingularityDeployBuilder toBuilder() {
    return new SingularityDeployBuilder(requestId, id)
    .setCommand(command)
    .setArguments(copyOfList(arguments))
    .setResources(resources)
    .setContainerInfo(containerInfo)
    .setCustomExecutorCmd(customExecutorCmd)
    .setCustomExecutorId(customExecutorId)
    .setCustomExecutorSource(customExecutorSource)

    .setHealthcheckUri(healthcheckUri)
    .setHealthcheckIntervalSeconds(healthcheckIntervalSeconds)
    .setHealthcheckTimeoutSeconds(healthcheckTimeoutSeconds)
    .setSkipHealthchecksOnDeploy(skipHealthchecksOnDeploy)

    .setConsiderHealthyAfterRunningForSeconds(considerHealthyAfterRunningForSeconds)
    .setDeployHealthTimeoutSeconds(deployHealthTimeoutSeconds)
    .setServiceBasePath(serviceBasePath)
    .setLoadBalancerGroups(copyOfList(loadBalancerGroups))
    .setLoadBalancerOptions(copyOfMap(loadBalancerOptions))

    .setMetadata(copyOfMap(metadata))
    .setVersion(version)
    .setTimestamp(timestamp)
    .setEnv(copyOfMap(env))
    .setUris(copyOfList(uris))
    .setExecutorData(executorData);
  }

  public Optional<Long> getDeployHealthTimeoutSeconds() {
    return deployHealthTimeoutSeconds;
  }

  public String getRequestId() {
    return requestId;
  }

  public String getId() {
    return id;
  }

  public Optional<String> getVersion() {
    return version;
  }

  public Optional<Long> getTimestamp() {
    return timestamp;
  }

  public Optional<Map<String, String>> getMetadata() {
    return metadata;
  }

  public Optional<SingularityContainerInfo> getContainerInfo() {
    return containerInfo;
  }

  public Optional<String> getCustomExecutorCmd() {
    return customExecutorCmd;
  }

  public Optional<String> getCustomExecutorId() {
    return customExecutorId;
  }

  public Optional<String> getCustomExecutorSource() { return customExecutorSource; }

  public Optional<Resources> getResources() {
    return resources;
  }

  public Optional<String> getCommand() {
    return command;
  }

  public Optional<List<String>> getArguments() {
    return arguments;
  }

  public Optional<Map<String, String>> getEnv() {
    return env;
  }

  public Optional<List<String>> getUris() {
    return uris;
  }

  public Optional<ExecutorData> getExecutorData() {
    return executorData;
  }

  public Optional<String> getHealthcheckUri() {
    return healthcheckUri;
  }

  public Optional<Long> getHealthcheckIntervalSeconds() {
    return healthcheckIntervalSeconds;
  }

  public Optional<Long> getHealthcheckTimeoutSeconds() {
    return healthcheckTimeoutSeconds;
  }

  public Optional<String> getServiceBasePath() {
    return serviceBasePath;
  }

  public Optional<Long> getConsiderHealthyAfterRunningForSeconds() {
    return considerHealthyAfterRunningForSeconds;
  }

  public Optional<List<String>> getLoadBalancerGroups() {
    return loadBalancerGroups;
  }

  public Optional<Map<String, Object>> getLoadBalancerOptions() {
    return loadBalancerOptions;
  }

  public Optional<Boolean> getSkipHealthchecksOnDeploy() {
    return skipHealthchecksOnDeploy;
  }

  @Override
  public String toString() {
    return "SingularityDeploy [" +
        "requestId='" + requestId + '\'' +
        ", id='" + id + '\'' +
        ", version=" + version +
        ", timestamp=" + timestamp +
        ", metadata=" + metadata +
        ", containerInfo=" + containerInfo +
        ", customExecutorCmd=" + customExecutorCmd +
        ", customExecutorId=" + customExecutorId +
        ", customExecutorSource=" + customExecutorSource +
        ", resources=" + resources +
        ", command=" + command +
        ", arguments=" + arguments +
        ", env=" + env +
        ", uris=" + uris +
        ", executorData=" + executorData +
        ", healthcheckUri=" + healthcheckUri +
        ", healthcheckIntervalSeconds=" + healthcheckIntervalSeconds +
        ", healthcheckTimeoutSeconds=" + healthcheckTimeoutSeconds +
        ", skipHealthchecksOnDeploy=" + skipHealthchecksOnDeploy +
        ", deployHealthTimeoutSeconds=" + deployHealthTimeoutSeconds +
        ", considerHealthyAfterRunningForSeconds=" + considerHealthyAfterRunningForSeconds +
        ", serviceBasePath=" + serviceBasePath +
        ", loadBalancerGroups=" + loadBalancerGroups +
        ", loadBalancerOptions=" + loadBalancerOptions +
        ']';
  }
}
