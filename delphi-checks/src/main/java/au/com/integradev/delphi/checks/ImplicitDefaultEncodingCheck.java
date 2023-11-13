/*
 * Sonar Delphi Plugin
 * Copyright (C) 2023 Integrated Application Development
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
package au.com.integradev.delphi.checks;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;
import org.sonar.plugins.communitydelphi.api.type.Parameter;
import org.sonar.plugins.communitydelphi.api.type.Type;

@Rule(key = "ImplicitDefaultEncoding")
public class ImplicitDefaultEncodingCheck extends DelphiCheck {
  private static final String MESSAGE = "Explicitly pass the encoding to this routine.";

  private static final Map<String, Signature> FORBIDDEN_SIGNATURES =
      createSignatures(
          signature(
              "Vcl.Outline.TOutlineNode.WriteNode",
              List.of("array of System.Byte <DYNAMIC>", "System.Classes.TStream"),
              List.of("System.PWideChar", "System.Classes.TStream")),
          signature("Vcl.Outline.TCustomOutline.LoadFromFile", List.of("System.UnicodeString")),
          signature("Vcl.Outline.TCustomOutline.LoadFromStream", List.of("System.Classes.TStream")),
          signature("Vcl.Outline.TCustomOutline.SaveToFile", List.of("System.UnicodeString")),
          signature("Vcl.Outline.TCustomOutline.SaveToStream", List.of("System.Classes.TStream")),
          signature("System.Classes.TStrings.LoadFromFile", List.of("System.UnicodeString")),
          signature("System.Classes.TStrings.LoadFromStream", List.of("System.Classes.TStream")),
          signature("System.Classes.TStrings.SaveToFile", List.of("System.UnicodeString")),
          signature("System.Classes.TStrings.SaveToStream", List.of("System.Classes.TStream")),
          signature(
              "System.Classes.TStringStream.Create",
              Collections.emptyList(),
              List.of("System.UnicodeString"),
              List.of("System.RawByteString"),
              List.of("array of System.Byte <DYNAMIC>")),
          signature(
              "System.Classes.TStreamReader.Create",
              List.of("System.Classes.TStream"),
              List.of("System.Classes.TStream", "System.Boolean"),
              List.of("System.UnicodeString"),
              List.of("System.UnicodeString", "System.Boolean")),
          signature(
              "System.Classes.TStreamWriter.Create",
              List.of("System.Classes.TStream"),
              List.of("System.UnicodeString"),
              List.of("System.UnicodeString", "System.Boolean")));

  @Override
  public DelphiCheckContext visit(NameReferenceNode reference, DelphiCheckContext context) {
    NameDeclaration declaration = reference.getNameDeclaration();
    if (declaration instanceof RoutineNameDeclaration
        && isForbiddenOverload((RoutineNameDeclaration) declaration)) {
      reportIssue(context, reference.getIdentifier(), MESSAGE);
    }

    return super.visit(reference, context);
  }

  private static boolean isForbiddenOverload(RoutineNameDeclaration routine) {
    Signature signature = FORBIDDEN_SIGNATURES.get(routine.fullyQualifiedName());
    return signature != null
        && signature.hasParameterTypes(
            routine.getParameters().stream()
                .map(Parameter::getType)
                .collect(Collectors.toUnmodifiableList()));
  }

  private static Map<String, Signature> createSignatures(Signature... signatures) {
    TreeMap<String, Signature> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    for (var signature : signatures) {
      map.put(signature.getName(), signature);
    }
    return Collections.unmodifiableMap(map);
  }

  @SafeVarargs
  @SuppressWarnings("varargs")
  private static Signature signature(String name, List<String>... parameterLists) {
    return new Signature(name, parameterLists);
  }

  private static final class Signature {
    private final String name;
    private final List<String>[] parameterLists;

    private Signature(String name, List<String>[] parameterLists) {
      this.name = name;
      this.parameterLists = parameterLists;
    }

    public String getName() {
      return name;
    }

    public boolean hasParameterTypes(List<Type> types) {
      return Arrays.stream(parameterLists)
          .anyMatch(typeImages -> typesMatchImages(types, typeImages));
    }

    private static boolean typesMatchImages(List<Type> types, List<String> typeImages) {
      if (types.size() != typeImages.size()) {
        return false;
      }

      for (int i = 0; i < types.size(); ++i) {
        if (!types.get(i).is(typeImages.get(i))) {
          return false;
        }
      }

      return true;
    }
  }
}
