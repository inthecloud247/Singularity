package com.hubspot.singularity.views;

import io.dropwizard.server.SimpleServerFactory;
import io.dropwizard.views.View;

import com.hubspot.singularity.SingularityService;
import com.hubspot.singularity.config.SingularityConfiguration;

public class IndexView extends View {

  private final String appRoot;
  private final String staticRoot;
  private final String apiRoot;
  private final String navColor;

  private final Integer defaultMemory;
  private final Integer defaultCpus;

  private final Boolean hideNewDeployButton;
  private final Boolean hideNewRequestButton;

  private final String title;

  private final Integer slaveHttpPort;
  private final Integer slaveHttpsPort;

  public IndexView(SingularityConfiguration configuration) {
    super("index.mustache");

    appRoot = configuration.getUiConfiguration().getBaseUrl().or(((SimpleServerFactory) configuration.getServerFactory()).getApplicationContextPath());
    staticRoot = String.format("%s/static", appRoot);
    apiRoot = String.format("%s%s", appRoot, SingularityService.API_BASE_PATH);

    title = configuration.getUiConfiguration().getTitle();

    slaveHttpPort = configuration.getMesosConfiguration().getSlaveHttpPort();
    slaveHttpsPort = configuration.getMesosConfiguration().getSlaveHttpsPort().orNull();

    defaultCpus = configuration.getMesosConfiguration().getDefaultCpus();
    defaultMemory = configuration.getMesosConfiguration().getDefaultMemory();

    hideNewDeployButton = configuration.getUiConfiguration().isHideNewDeployButton();
    hideNewRequestButton = configuration.getUiConfiguration().isHideNewRequestButton();

    navColor = configuration.getUiConfiguration().getNavColor();
  }

  public String getAppRoot() {
    return appRoot;
  }

  public String getStaticRoot() {
    return staticRoot;
  }

  public String getApiRoot() {
    return apiRoot;
  }

  public String getTitle() {
    return title;
  }

  public String getNavColor() {
    return navColor;
  }

  public Integer getSlaveHttpPort() {
    return slaveHttpPort;
  }

  public Integer getSlaveHttpsPort() {
    return slaveHttpsPort;
  }

  public Integer getDefaultMemory() {
    return defaultMemory;
  }

  public Integer getDefaultCpus() {
    return defaultCpus;
  }

  public Boolean getHideNewDeployButton() {
    return hideNewDeployButton;
  }

  public Boolean getHideNewRequestButton() {
    return hideNewRequestButton;
  }

  @Override
  public String toString() {
    return "IndexView [appRoot=" + appRoot + ", staticRoot=" + staticRoot + ", apiRoot=" + apiRoot + ", navColor=" + navColor + ", defaultMemory=" + defaultMemory + ", defaultCpus=" + defaultCpus
        + ", hideNewDeployButton=" + hideNewDeployButton + ", hideNewRequestButton=" + hideNewRequestButton + ", title=" + title + ", slaveHttpPort=" + slaveHttpPort + ", slaveHttpsPort="
        + slaveHttpsPort + "]";
  }

}
