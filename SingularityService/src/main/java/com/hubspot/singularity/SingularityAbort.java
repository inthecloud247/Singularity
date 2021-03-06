package com.hubspot.singularity;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.eclipse.jetty.server.Server;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.net.HostAndPort;
import com.google.inject.Inject;
import com.hubspot.mesos.JavaUtils;
import com.hubspot.singularity.config.EmailConfigurationEnums.EmailDestination;
import com.hubspot.singularity.config.EmailConfigurationEnums.EmailType;
import com.hubspot.singularity.config.SMTPConfiguration;
import com.hubspot.singularity.config.SingularityConfiguration;
import com.hubspot.singularity.sentry.SingularityExceptionNotifier;
import com.hubspot.singularity.smtp.SingularitySmtpSender;

@Singleton
public class SingularityAbort implements ConnectionStateListener {

  private static final Logger LOG = LoggerFactory.getLogger(SingularityAbort.class);

  private final Optional<SMTPConfiguration> maybeSmtpConfiguration;
  private final SingularitySmtpSender smtpSender;
  private final HostAndPort hostAndPort;
  private final SingularityExceptionNotifier exceptionNotifier;

  private final ServerProvider serverProvider;
  private final AtomicBoolean aborting = new AtomicBoolean();

  @Inject
  public SingularityAbort(SingularitySmtpSender smtpSender, ServerProvider serverProvider, SingularityConfiguration configuration, SingularityExceptionNotifier exceptionNotifier, @Named(SingularityMainModule.HTTP_HOST_AND_PORT) HostAndPort hostAndPort) {
    this.maybeSmtpConfiguration = configuration.getSmtpConfiguration();
    this.serverProvider = serverProvider;
    this.smtpSender = smtpSender;
    this.exceptionNotifier = exceptionNotifier;
    this.hostAndPort = hostAndPort;
  }

  @Override
  public void stateChanged(CuratorFramework client, ConnectionState newState) {
    if (newState == ConnectionState.LOST) {
      LOG.error("Aborting due to new connection state received from ZooKeeper: {}", newState);
      abort(AbortReason.LOST_ZK_CONNECTION);
    }
  }

  public enum AbortReason {
    LOST_ZK_CONNECTION, LOST_LEADERSHIP, UNRECOVERABLE_ERROR, TEST_ABORT, MESOS_ERROR;
  }

  public void abort(AbortReason abortReason) {
    if (!aborting.getAndSet(true)) {
      try {
        sendAbortNotification(abortReason);
        flushLogs();
      } finally {
        exit();
      }
    }
  }

  private void exit() {
    Optional<Server> server = serverProvider.get();
    if (server.isPresent()) {
      try {
        server.get().stop();
      } catch (Exception e) {
        LOG.warn("While aborting server", e);
      } finally {
        System.exit(1);
      }
    } else {
      LOG.warn("SingularityAbort called before server has fully initialized!");
      System.exit(1); // Use the hammer.
    }
  }

  private void sendAbortNotification(AbortReason abortReason) {
    final String message = String.format("Singularity on %s is aborting due to %s", hostAndPort.getHostText(), abortReason);

    sendAbortMail(message);

    exceptionNotifier.notify(message);
  }

  private void sendAbortMail(final String message) {
    if (!maybeSmtpConfiguration.isPresent()) {
      LOG.warn("Couldn't send abort mail because no SMTP configuration is present");
      return;
    }

    final List<EmailDestination> emailDestination = maybeSmtpConfiguration.get().getEmailConfiguration().get(EmailType.SINGULARITY_ABORTING);

    if (emailDestination.isEmpty() || !emailDestination.contains(EmailDestination.ADMINS)) {
      LOG.info("Not configured to send abort mail");
      return;
    }

    smtpSender.queueMail(maybeSmtpConfiguration.get().getAdmins(), ImmutableList.<String> of(), message, "");
  }

  private void flushLogs() {
    final long millisToWait = 100;

    LOG.info("Attempting to flush logs and wait {} ...", JavaUtils.durationFromMillis(millisToWait));

    ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
    if (loggerFactory instanceof LoggerContext) {
      LoggerContext context = (LoggerContext) loggerFactory;
      context.stop();
    }

    try {
      Thread.sleep(millisToWait);
    } catch (Exception e) {
      LOG.info("While sleeping for log flush", e);
    }
  }

}
