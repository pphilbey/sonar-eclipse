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
package org.sonar.ide.eclipse.wsclient;

import java.util.Date;
import java.util.List;
import javax.annotation.CheckForNull;
import org.eclipse.core.runtime.IProgressMonitor;
import org.sonar.ide.eclipse.common.issues.ISonarIssue;
import org.sonar.ide.eclipse.common.issues.ISonarIssueWithPath;

public interface ISonarWSClientFacade {

  public static enum ConnectionTestStatus {
    OK, CONNECT_ERROR, AUTHENTICATION_ERROR;
  }

  public static class ConnectionTestResult {
    public final ConnectionTestStatus status;
    public final String message;

    public ConnectionTestResult(ConnectionTestStatus status) {
      this.status = status;
      this.message = "";
    }

    public ConnectionTestResult(ConnectionTestStatus status, String message) {
      this.status = status;
      this.message = message;
    }
  }

  ConnectionTestResult testConnection();

  String getServerVersion();

  List<ISonarRemoteModule> listAllRemoteModules();

  List<ISonarRemoteModule> searchRemoteModules(String partialName);

  boolean exists(String resourceKey);

  @CheckForNull
  Date getLastAnalysisDate(String resourceKey);

  String[] getRemoteCode(String resourceKey);

  List<ISonarIssueWithPath> getUnresolvedRemoteIssuesRecursively(String resourceKey, IProgressMonitor monitor);

  List<ISonarIssue> getUnresolvedRemoteIssues(String resourceKey, IProgressMonitor monitor);

}
