package org.sonar.plugins.delphi.symbol.scope;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.plugins.delphi.symbol.declaration.VariableNameDeclaration.compilerVariable;
import static org.sonar.plugins.delphi.symbol.scope.DelphiScope.unknownScope;
import static org.sonar.plugins.delphi.type.DelphiType.unknownType;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.junit.Test;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.symbol.DelphiNameOccurrence;
import org.sonar.plugins.delphi.symbol.declaration.VariableNameDeclaration;

public class UnknownScopeTest {
  private final UnknownScope unknownScope = unknownScope();

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
    assertThat(unknownScope.getDeclarations()).isEmpty();
  }

  @Test
  public void testGetAllDeclarations() {
    unknownScope.addDeclaration(mock(NameDeclaration.class));
    assertThat(unknownScope.getAllDeclarations()).isEmpty();
  }

  @Test
  public void testGetDeclarationsByClass() {
    var declaration = compilerVariable("Image", unknownType(), unknownScope());
    unknownScope.addDeclaration(declaration);
    assertThat(unknownScope.getDeclarations(VariableNameDeclaration.class)).isNull();
  }

  @Test
  public void testContains() {
    VariableNameDeclaration declaration = compilerVariable("Image", unknownType(), unknownScope);
    unknownScope.addDeclaration(declaration);
    assertThat(unknownScope.contains(makeNameOccurrence())).isFalse();
  }

  @Test
  public void testAddNameOccurrence() {
    VariableNameDeclaration declaration = compilerVariable("Image", unknownType(), unknownScope);
    unknownScope.addDeclaration(declaration);
    assertThat(unknownScope.addNameOccurrence(makeNameOccurrence())).isEmpty();
  }

  @Test
  public void testFindDeclaration() {
    VariableNameDeclaration declaration = compilerVariable("Image", unknownType(), unknownScope);
    unknownScope.addDeclaration(declaration);
    assertThat(unknownScope.findDeclaration(makeNameOccurrence())).isEmpty();
  }

  @Test
  public void testGetOccurrencesFor() {
    VariableNameDeclaration declaration = compilerVariable("Image", unknownType(), unknownScope);
    unknownScope.addDeclaration(declaration);
    assertThat(unknownScope.addNameOccurrence(makeNameOccurrence())).isEmpty();
    assertThat(unknownScope.getOccurrencesFor(declaration)).isEmpty();
  }

  @Test
  public void testFindMethodOverloads() {
    Set<NameDeclaration> result = new HashSet<>();
    unknownScope.findMethodOverloads(makeNameOccurrence(), result);
    assertThat(result).isEmpty();
  }

  @Test
  public void testGetParent() {
    unknownScope.setParent(unknownScope());
    assertThat(unknownScope.getParent()).isNull();
  }

  @Test
  public void testGetHelperForType() {
    assertThat(unknownScope.getHelperForType(unknownType())).isNull();
  }

  @Test
  public void testGetUnitDeclarations() {
    assertThat(unknownScope.getUnitDeclarations()).isEmpty();
  }

  @Test
  public void testGetImportDeclarations() {
    assertThat(unknownScope.getImportDeclarations()).isEmpty();
  }

  @Test
  public void testGetTypeDeclarations() {
    assertThat(unknownScope.getTypeDeclarations()).isEmpty();
  }

  @Test
  public void testGetPropertyDeclarations() {
    assertThat(unknownScope.getPropertyDeclarations()).isEmpty();
  }

  @Test
  public void testGetMethodDeclarations() {
    assertThat(unknownScope.getMethodDeclarations()).isEmpty();
  }

  @Test
  public void testGetVariableDeclarations() {
    assertThat(unknownScope.getVariableDeclarations()).isEmpty();
  }

  private static DelphiNameOccurrence makeNameOccurrence() {
    DelphiNode location = mock(DelphiNode.class);
    when(location.getScope()).thenReturn(unknownScope());
    return new DelphiNameOccurrence(location, "Image");
  }
}
