/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
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
package au.com.integradev.delphi.type.factory;

import au.com.integradev.delphi.type.DelphiType;
import au.com.integradev.delphi.type.Type;
import au.com.integradev.delphi.type.Type.FileType;
import au.com.integradev.delphi.type.generic.TypeSpecializationContext;

class DelphiFileType extends DelphiType implements FileType {
  private final Type fileType;
  private final int size;

  DelphiFileType(Type fileType, int size) {
    this.fileType = fileType;
    this.size = size;
  }

  @Override
  public String getImage() {
    return "file of " + fileType().getImage();
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public Type fileType() {
    return fileType;
  }

  @Override
  public boolean isFile() {
    return true;
  }

  @Override
  public Type specialize(TypeSpecializationContext context) {
    if (fileType().isTypeParameter()) {
      return new DelphiFileType(fileType().specialize(context), size);
    }
    return this;
  }
}
