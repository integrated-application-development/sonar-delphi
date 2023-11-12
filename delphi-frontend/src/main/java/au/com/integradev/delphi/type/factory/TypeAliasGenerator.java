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
package au.com.integradev.delphi.type.factory;

import static net.bytebuddy.description.type.TypeDescription.Generic.Builder.parameterizedType;
import static net.bytebuddy.implementation.MethodCall.invoke;
import static net.bytebuddy.matcher.ElementMatchers.named;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.NamingStrategy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodCall.MethodLocator;
import net.bytebuddy.matcher.ElementMatchers;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.AliasType;
import org.sonar.plugins.communitydelphi.api.type.Type.AnsiStringType;
import org.sonar.plugins.communitydelphi.api.type.Type.BooleanType;
import org.sonar.plugins.communitydelphi.api.type.Type.CharacterType;
import org.sonar.plugins.communitydelphi.api.type.Type.ClassReferenceType;
import org.sonar.plugins.communitydelphi.api.type.Type.CollectionType;
import org.sonar.plugins.communitydelphi.api.type.Type.EnumType;
import org.sonar.plugins.communitydelphi.api.type.Type.FileType;
import org.sonar.plugins.communitydelphi.api.type.Type.HelperType;
import org.sonar.plugins.communitydelphi.api.type.Type.IntegerType;
import org.sonar.plugins.communitydelphi.api.type.Type.PointerType;
import org.sonar.plugins.communitydelphi.api.type.Type.ProceduralType;
import org.sonar.plugins.communitydelphi.api.type.Type.RealType;
import org.sonar.plugins.communitydelphi.api.type.Type.StringType;
import org.sonar.plugins.communitydelphi.api.type.Type.StructType;
import org.sonar.plugins.communitydelphi.api.type.Type.SubrangeType;
import org.sonar.plugins.communitydelphi.api.type.Type.TypeParameterType;
import org.sonar.plugins.communitydelphi.api.type.Type.UnknownType;
import org.sonar.plugins.communitydelphi.api.type.Type.UnresolvedType;
import org.sonar.plugins.communitydelphi.api.type.Type.VariantType;

final class TypeAliasGenerator {
  private static final List<Class<? extends Type>> TYPE_INTERFACES =
      List.of(
          CollectionType.class,
          HelperType.class,
          StructType.class,
          PointerType.class,
          ProceduralType.class,
          FileType.class,
          EnumType.class,
          SubrangeType.class,
          ClassReferenceType.class,
          TypeParameterType.class,
          IntegerType.class,
          RealType.class,
          BooleanType.class,
          CharacterType.class,
          AnsiStringType.class,
          StringType.class,
          VariantType.class,
          UnresolvedType.class,
          UnknownType.class);

  private final Map<Class<? extends Type>, Class<? extends AliasType>> cache;

  public TypeAliasGenerator() {
    this.cache = new HashMap<>();
  }

  public AliasType generate(String aliasImage, Type aliased, boolean strong) {
    for (Class<? extends Type> typeInterface : TYPE_INTERFACES) {
      if (typeInterface.isAssignableFrom(aliased.getClass())) {
        var clazz = cache.computeIfAbsent(typeInterface, TypeAliasGenerator::generateAliasClass);
        try {
          Constructor<? extends AliasType> constructor =
              clazz.getDeclaredConstructor(String.class, typeInterface, boolean.class);
          return constructor.newInstance(aliasImage, aliased, strong);
        } catch (Exception e) {
          // Unreachable
        }
      }
    }
    throw new AssertionError("Unhandled class could not be aliased: " + aliased.getClass());
  }

  @SuppressWarnings("unchecked")
  private static Class<? extends AliasType> generateAliasClass(
      Class<? extends Type> interfaceType) {
    DynamicType.Builder<?> builder =
        new ByteBuddy()
            .with(new NamingStrategy.SuffixingRandom(interfaceType.getSimpleName()))
            .subclass(parameterizedType(AliasTypeImpl.class, interfaceType).build())
            .implement(interfaceType, AliasType.class)
            .method(ElementMatchers.isAbstract())
            .intercept(
                invoke(MethodLocator.ForInstrumentedMethod.INSTANCE)
                    .onMethodCall(invoke(named("aliasedType")))
                    .withAllArguments());

    try (DynamicType.Unloaded<?> unloaded = builder.make()) {
      return (Class<? extends AliasType>)
          unloaded.load(TypeAliasGenerator.class.getClassLoader()).getLoaded();
    }
  }
}
