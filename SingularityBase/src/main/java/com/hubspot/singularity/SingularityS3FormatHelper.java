package com.hubspot.singularity;

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class SingularityS3FormatHelper {

  private static final List<String> DISALLOWED_FOR_TASK = ImmutableList.of("%index", "%s", "%filename", "%fileext");
  private static final List<String> DISALLOWED_FOR_DEPLOY = ImmutableList.copyOf(Iterables.concat(DISALLOWED_FOR_TASK, ImmutableList.of("%host")));
  private static final List<String> DISALLOWED_FOR_REQUEST = ImmutableList.copyOf(Iterables.concat(DISALLOWED_FOR_DEPLOY, ImmutableList.of("%tag", "%deployId")));

  public static String getS3KeyFormat(String s3KeyFormat, String requestId) {
    s3KeyFormat = s3KeyFormat.replace("%requestId", requestId);

    return s3KeyFormat;
  }

  public static String getS3KeyFormat(String s3KeyFormat, String requestId, String deployId, Optional<String> loggingTag) {
    s3KeyFormat = getS3KeyFormat(s3KeyFormat, requestId);

    s3KeyFormat = s3KeyFormat.replace("%tag", loggingTag.or(""));
    s3KeyFormat = s3KeyFormat.replace("%deployId", deployId);

    return s3KeyFormat;
  }

  public static String getS3KeyFormat(String s3KeyFormat, SingularityTaskId taskId, Optional<String> loggingTag) {
    s3KeyFormat = getS3KeyFormat(s3KeyFormat, taskId.getRequestId(), taskId.getDeployId(), loggingTag);

    s3KeyFormat = s3KeyFormat.replace("%host", taskId.getHost());
    s3KeyFormat = s3KeyFormat.replace("%taskId", taskId.toString());

    return s3KeyFormat;
  }

  public static String getKey(String s3KeyFormat, int sequence, long timestamp, String filename) {
    final Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(timestamp);

    s3KeyFormat = s3KeyFormat.replace("%filename", filename);

    int lastPeriod = filename.lastIndexOf(".");

    if (lastPeriod > -1) {
      s3KeyFormat = s3KeyFormat.replace("%fileext", filename.substring(lastPeriod));
    }

    s3KeyFormat = s3KeyFormat.replace("%Y", getYear(calendar.get(Calendar.YEAR)));
    s3KeyFormat = s3KeyFormat.replace("%m", getDayOrMonth(getMonth(calendar)));
    s3KeyFormat = s3KeyFormat.replace("%d", getDayOrMonth(calendar.get(Calendar.DAY_OF_MONTH)));
    s3KeyFormat = s3KeyFormat.replace("%s", Long.toString(timestamp));
    s3KeyFormat = s3KeyFormat.replace("%index", Integer.toString(sequence));

    return s3KeyFormat;
  }

  public static String trimKeyFormat(String s3KeyFormat, List<String> disallowedKeys) {
    int lowestIndex = s3KeyFormat.length();

    for (String disallowedKey : disallowedKeys) {
      int index = s3KeyFormat.indexOf(disallowedKey);
      if (index != -1 && index < lowestIndex) {
        lowestIndex = index;
      }
    }

    if (lowestIndex == -1) {
      return s3KeyFormat;
    }

    return s3KeyFormat.substring(0, lowestIndex);
  }

  private static int getMonth(Calendar calender) {
    return calender.get(Calendar.MONTH) + 1;
  }

  private static String getYear(int year) {
    return Integer.toString(year);
  }

  private static String getDayOrMonth(int value) {
    return String.format("%02d", value);
  }

  public static Collection<String> getS3KeyPrefixes(String s3KeyFormat, String requestId, String deployId, Optional<String> tag, long start, long end) {
    String keyFormat = getS3KeyFormat(s3KeyFormat, requestId, deployId, tag);

    keyFormat = trimTaskId(keyFormat, requestId + "-" + deployId);

    return getS3KeyPrefixes(keyFormat, DISALLOWED_FOR_DEPLOY, start, end);
  }

  private static String trimTaskId(String s3KeyFormat, String replaceWith) {
    int index = s3KeyFormat.indexOf("%taskId");

    if (index > -1) {
      s3KeyFormat = s3KeyFormat.substring(0, index) + replaceWith;
    }

    return s3KeyFormat;
  }

  public static Collection<String> getS3KeyPrefixes(String s3KeyFormat, String requestId, long start, long end) {
    s3KeyFormat = getS3KeyFormat(s3KeyFormat, requestId);

    s3KeyFormat = trimTaskId(s3KeyFormat, requestId);

    return getS3KeyPrefixes(s3KeyFormat, DISALLOWED_FOR_REQUEST, start, end);
  }

  private static Collection<String>  getS3KeyPrefixes(String s3KeyFormat, List<String> disallowedKeys, long start, long end) {
    String trimKeyFormat = trimKeyFormat(s3KeyFormat, disallowedKeys);

    int indexOfY = trimKeyFormat.indexOf("%Y");
    int indexOfM = trimKeyFormat.indexOf("%m");
    int indexOfD = trimKeyFormat.indexOf("%d");

    if (indexOfY == -1 && indexOfM == -1 && indexOfD == -1) {
      return Collections.singleton(trimKeyFormat);
    }

    if (indexOfY > -1) {
      trimKeyFormat = trimKeyFormat.replace("%Y", "YYYY");
      if (indexOfM > -1) {
        indexOfM += 2;
      }
      if (indexOfD > -1) {
        indexOfD += 2;
      }
    }

    StringBuilder keyBuilder = new StringBuilder(trimKeyFormat);

    Set<String> keyPrefixes = Sets.newHashSet();

    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(start);

    while (calendar.getTimeInMillis() <= end) {
      if (indexOfY > -1) {
        keyBuilder.replace(indexOfY, indexOfY + 4, getYear(calendar.get(Calendar.YEAR)));
      }

      if (indexOfM > -1) {
        keyBuilder.replace(indexOfM, indexOfM + 2, getDayOrMonth(getMonth(calendar)));
      }

      if (indexOfD > -1) {
        keyBuilder.replace(indexOfD, indexOfD + 2, getDayOrMonth(calendar.get(Calendar.DAY_OF_MONTH)));
      }

      keyPrefixes.add(keyBuilder.toString());

      if (indexOfD > -1) {
        calendar.add(Calendar.DAY_OF_YEAR, 1);
      } else if (indexOfM > -1) {
        calendar.add(Calendar.MONTH, 1);
      } else {
        calendar.add(Calendar.YEAR, 1);
      }
    }

    return keyPrefixes;
  }

  public static Collection<String> getS3KeyPrefixes(String s3KeyFormat, SingularityTaskId taskId, Optional<String> tag, long start, long end) {
    String keyFormat = getS3KeyFormat(s3KeyFormat, taskId, tag);

    return getS3KeyPrefixes(keyFormat, DISALLOWED_FOR_TASK, start, end);
  }

  public static void main(String[] args) {
    SingularityTaskId taskId = new SingularityTaskId("rid", "did", System.currentTimeMillis(), 1, "host", "rackid");
    Optional<String> tag = Optional.<String> absent();
    Calendar c = Calendar.getInstance();

    c.add(Calendar.DAY_OF_YEAR, -1);

    final long now = System.currentTimeMillis();

    System.out.println(getS3KeyPrefixes("%requestId/%Y/%m/%taskId_%index-%s%fileext", taskId, tag, c.getTimeInMillis(), System.currentTimeMillis()));
    System.out.println(getS3KeyPrefixes("%Y/%m/%d", taskId, tag, c.getTimeInMillis(), System.currentTimeMillis()));

    System.out.println(getS3KeyPrefixes("%requestId/%Y/%m/%taskId_%index-%s%fileext", "requestId", "deployId", tag, c.getTimeInMillis(), System.currentTimeMillis()));


    System.out.println(getS3KeyPrefixes("%requestId/%Y/%m/%taskId_%index-%s%fileext", "rid", c.getTimeInMillis(), System.currentTimeMillis()));



    System.out.println(System.currentTimeMillis() - now);

  }

}
