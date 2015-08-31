/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions and Fabricio Colombo
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
 * Fabricio Colombo (fabricio.colombo.mva@gmail.com)
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
package org.sonar.plugins.delphi.pmd;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DelphiUnitBuilderTest {

    private StringBuilder declaration = new StringBuilder();
    private StringBuilder implementation = new StringBuilder();

    private int offset;

    public String className = "TTest";

    public DelphiUnitBuilderTest appendDecl(String value) {
        declaration.append(value + "\n");
        offset++;
        return this;
    }

    public DelphiUnitBuilderTest appendImpl(String value) {
        implementation.append(value + "\n");
        return this;
    }

    public String declaration() {
        return declaration.toString();
    }

    public String implementation() {
        return implementation.toString();
    }

    public File buildFile(File baseDir) {
        offset = offset + 7;
        StringBuilder source = new StringBuilder();
        source.append("unit Unit1;\n");
        source.append("interface\n");
        source.append("type\n");
        source.append(this.className + " = class\n");
        source.append(this.declaration() + "\n");
        source.append("end;\n");
        source.append("implementation\n");
        source.append(this.implementation() + "\n");
        source.append("end.\n");

        try {
            File file = File.createTempFile("unit", ".pas", baseDir);
            file.deleteOnExit();

            FileWriter fileWriter = new FileWriter(file);
            try {
                fileWriter.write(source.toString());
                fileWriter.flush();
            } finally {
                fileWriter.close();
            }
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    protected int getOffSet() {
        return offset;
    }
}
