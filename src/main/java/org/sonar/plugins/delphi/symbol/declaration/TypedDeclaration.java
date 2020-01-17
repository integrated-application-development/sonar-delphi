package org.sonar.plugins.delphi.symbol.declaration;

import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.sonar.plugins.delphi.type.Typed;

public interface TypedDeclaration extends NameDeclaration, Typed {}
