/*
 * SonarQube Eclipse
 * Copyright (C) 2010-2015 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.ide.eclipse.core.internal.servers;

import java.util.Properties;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.equinox.security.storage.EncodingUtils;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.sonar.ide.eclipse.common.servers.ISonarServer;
import org.sonar.ide.eclipse.core.internal.SonarCorePlugin;
import org.sonar.ide.eclipse.core.internal.SonarProperties;
import org.sonar.runner.api.EmbeddedRunner;

public final class SonarServer implements ISonarServer {

  private final String id;
  private String url;
  private boolean auth;
  private String version;
  private EmbeddedRunner runner;

  public SonarServer(String id, String url, String username, String password) {
    this(id, url, StringUtils.isNotBlank(password) && StringUtils.isNotBlank(username));
    if (auth) {
      setKeyForServerNode("username", username, false);
      setKeyForServerNode("password", password, true);
    }
  }

  public SonarServer(String id, String url) {
    this(id, url, false);
  }

  public SonarServer(String id, String url, boolean auth) {
    Assert.isNotNull(id);
    this.id = id;
    this.url = url;
    this.auth = auth;
  }

  @Override
  public String getId() {
    return this.id;
  }

  @Override
  public String getUrl() {
    return url;
  }

  @Override
  public boolean hasCredentials() {
    return StringUtils.isNotBlank(getPassword()) && StringUtils.isNotBlank(getUsername());
  }

  @Override
  public String getUsername() {
    return auth ? getKeyFromServerNode("username") : "";
  }

  @Override
  public String getPassword() {
    return auth ? getKeyFromServerNode("password") : "";
  }

  @CheckForNull
  @Override
  public String getVersion() {
    return version;
  }

  public void setVersion(@Nullable String version) {
    this.version = version;
  }

  private String getKeyFromServerNode(String key) {
    try {
      return SecurePreferencesFactory.getDefault().node(ServersManager.PREF_SERVERS).node(EncodingUtils.encodeSlashes(getUrl())).get(key, "");
    } catch (StorageException e) {
      return "";
    }
  }

  private void setKeyForServerNode(String key, String value, boolean encrypt) {
    try {
      ISecurePreferences serverNode = SecurePreferencesFactory.getDefault().node(ServersManager.PREF_SERVERS)
        .node(EncodingUtils.encodeSlashes(getUrl()));
      serverNode.put(key, value, encrypt);
    } catch (StorageException e) {
      SonarCorePlugin.getDefault().error(e.getMessage(), e);
    }
  }

  @Override
  public String toString() {
    return "SonarServer [url=" + url + ", auth=" + auth + "]";
  }

  @Override
  public int hashCode() {
    return getUrl().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof SonarServer) {
      SonarServer sonarServer = (SonarServer) obj;
      return getUrl().equals(sonarServer.getUrl());
    }
    return false;
  }

  @Override
  public synchronized void startAnalysis(Properties props, boolean debugEnabled) {
    if (runner == null) {
      Properties globalProps = new Properties();
      globalProps.setProperty(SonarProperties.SONAR_URL, getUrl());
      if (StringUtils.isNotBlank(getUsername())) {
        globalProps.setProperty(SonarProperties.SONAR_LOGIN, getUsername());
        globalProps.setProperty(SonarProperties.SONAR_PASSWORD, getPassword());
      }
      globalProps.setProperty(SonarProperties.ANALYSIS_MODE, SonarProperties.ANALYSIS_MODE_PREVIEW);
      globalProps.setProperty(SonarProperties.USE_HTTP_CACHE, "true");
      if (debugEnabled) {
        globalProps.setProperty(SonarProperties.VERBOSE_PROPERTY, "true");
      }
      globalProps.setProperty(SonarProperties.WORK_DIR, ResourcesPlugin.getWorkspace().getRoot().getLocation().append(".sonar").toString());
      globalProps.setProperty("sonar.enableHttpCache", "true");
      runner = EmbeddedRunner.create()
        .setApp("Eclipse", SonarCorePlugin.getDefault().getBundle().getVersion().toString())
        .addGlobalProperties(globalProps);
      runner.start();
    }
    runner.runAnalysis(props);
  }

  public void stop() {
    if (runner != null) {
      runner.stop();
      runner = null;
    }

  }

}
