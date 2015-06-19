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
package org.sonar.ide.eclipse.ui.internal.wizards;

import org.apache.commons.lang.StringUtils;
import org.sonar.ide.eclipse.common.servers.ISonarServer;
import org.sonar.ide.eclipse.core.internal.SonarCorePlugin;

public class EditServerLocationWizard extends AbstractServerLocationWizard {

  private final ISonarServer sonarServer;

  public EditServerLocationWizard(ISonarServer sonarServer) {
    super(new ServerLocationWizardPage(sonarServer), "Edit SonarQube Server");
    this.sonarServer = sonarServer;
  }

  @Override
  protected void doFinish(String serverId, String serverUrl, String username, String password) {
    String oldServerId = sonarServer.getId();
    if (StringUtils.isNotBlank(oldServerId) && (SonarCorePlugin.getServersManager().getServer(oldServerId) != null)) {
      SonarCorePlugin.getServersManager().removeServer(sonarServer);
    }
    super.doFinish(serverId, serverUrl, username, password);
  }
}
