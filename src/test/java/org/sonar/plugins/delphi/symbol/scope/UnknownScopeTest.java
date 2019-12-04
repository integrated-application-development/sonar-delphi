package org.sonar.plugins.delphi.symbol.scope;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.plugins.delphi.symbol.declaration.VariableNameDeclaration.compilerVariable;
import static org.sonar.plugins.delphi.symbol.scope.UnknownScope.unknownScope;
import static org.sonar.plugins.delphi.type.DelphiType.unknownType;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import net.sourceforge.pmd.lang.symboltable.NameOccurrence;
import org.junit.Test;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.symbol.DelphiNameOccurrence;
import org.sonar.plugins.delphi.symbol.declaration.VariableNameDeclaration;

public class UnknownScopeTest {
  private UnknownScope unknownScope = unknownScope();

  @Test
  public void testConstructorIsPrivate() throws Exception {
    Constructor<UnknownScope> constructor = UnknownScope.class.getDeclaredConstructor();
    assertThat(constructor.canAccess(null)).isFalse();
  }

  @Test
  public void testGetEnclosingScope() {
    unknownScope.setParent(unknownScope());
    assertThat(unknownScope.getEnclosingScope(UnknownScope.class)).isNull();
  }

  @Test
  public void testGetDeclarations() {
    unknownScope.addDeclaration(mock(NameDeclaration.class));
    assertThat(unknownScope.getDeclarations()).isNull();
  }

  @Test
  public void testGetDeclarationsByClass() {
    var declaration = compilerVariable("Image", unknownType(), unknownScope());
    unknownScope.addDeclaration(declaration);
    assertThat(unknownScope.getDeclarations(VariableNameDeclaration.class)).isNull();
  }

  @Test
  public void testContains() {
    VariableNameDeclaration declaration = mock(VariableNameDeclaration.class);
    when(declaration.getImage()).thenReturn("Image");
    unknownScope.addDeclaration(declaration);
    NameOccurrence occurrence = new DelphiNameOccurrence(mock(DelphiNode.class), "Image");
    assertThat(unknownScope.contains(occurrence)).isFalse();
  }

  @Test
  public void testAddNameOccurrence() {
    VariableNameDeclaration declaration = mock(VariableNameDeclaration.class);
    when(declaration.getImage()).thenReturn("Image");
    unknownScope.addDeclaration(declaration);
    DelphiNameOccurrence occurrence = new DelphiNameOccurrence(mock(DelphiNode.class), "Image");
    assertThat(unknownScope.addNameOccurrence(occurrence)).isEmpty();
  }

  @Test
  public void testFindDeclaration() {
    VariableNameDeclaration declaration = mock(VariableNameDeclaration.class);
    when(declaration.getImage()).thenReturn("Image");
    unknownScope.addDeclaration(declaration);
    DelphiNameOccurrence occurrence = new DelphiNameOccurrence(mock(DelphiNode.class), "Image");
    assertThat(unknownScope.findDeclaration(occurrence)).isEmpty();
  }

  @Test
  public void testFindMethodOverloads() {
    DelphiNameOccurrence occurrence = new DelphiNameOccurrence(mock(DelphiNode.class), "Image");
    Set<NameDeclaration> result = new HashSet<>();
    unknownScope.findMethodOverloads(occurrence, result);
    assertThat(result).isEmpty();
  }

  @Test
  public void testGetParent() {
    unknownScope.setParent(unknownScope());
    assertThat(unknownScope.getParent()).isNull();
  }
}
