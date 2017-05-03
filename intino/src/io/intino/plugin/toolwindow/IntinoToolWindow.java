/*
 *  Copyright 2013-2016 SIANI - ULPGC
 *
 *  This File is part of JavaFMI Project
 *
 *  JavaFMI Project is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License.
 *
 *  JavaFMI Project is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with JavaFMI. If not, see <http://www.gnu.org/licenses/>.
 */

package io.intino.plugin.toolwindow;

import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;


class IntinoToolWindow extends SimpleToolWindowPanel implements DataProvider {

	IntinoToolWindow(Project project) {
		super(true, true);
		add(new IntinoProjectManagerView(project).contentPane());
	}
}
