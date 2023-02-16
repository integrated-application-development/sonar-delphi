package au.com.integradev.delphi.antlr.ast.node;

import static org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope.unknownScope;

import au.com.integradev.delphi.antlr.DelphiParser;
import au.com.integradev.delphi.antlr.ast.DelphiTreeAdaptor;
import au.com.integradev.delphi.antlr.ast.token.DelphiToken;
import au.com.integradev.delphi.type.factory.TypeFactory;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.apache.commons.lang3.ArrayUtils;
import org.sonar.plugins.communitydelphi.api.ast.DelphiAst;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.MutableDelphiNode;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;

public abstract class DelphiNodeImpl implements DelphiNode, MutableDelphiNode {
  private final int id;
  private final DelphiToken token;
  protected DelphiNode parent;
  private DelphiNode[] children;
  private int childIndex;
  private DelphiToken firstToken;
  private DelphiToken lastToken;
  private DelphiScope scope;

  /**
   * All nodes must implement this constructor. Used to create a node with a concrete token. Also
   * used by {@link DelphiTreeAdaptor#dupNode}}
   *
   * @param token Token to create the node with
   */
  protected DelphiNodeImpl(@Nonnull Token token) {
    this.id = token.getType();
    this.token = new DelphiToken(token);
  }

  /**
   * Nodes created from imaginary tokens must implement this constructor.
   *
   * @param tokenType Token type
   */
  protected DelphiNodeImpl(int tokenType) {
    this(new CommonToken(tokenType, DelphiParser.tokenNames[tokenType]));
  }

  @Override
  public int jjtGetId() {
    return this.id;
  }

  @Override
  public int getBeginLine() {
    return jjtGetFirstToken().getBeginLine();
  }

  @Override
  public int getBeginColumn() {
    return jjtGetFirstToken().getBeginColumn();
  }

  @Override
  public int getEndLine() {
    return jjtGetLastToken().getEndLine();
  }

  @Override
  public int getEndColumn() {
    return jjtGetLastToken().getEndColumn();
  }

  @Override
  public String getImage() {
    return token.getImage();
  }

  @Override
  public void setScope(DelphiScope scope) {
    this.scope = scope;
  }

  @Override
  @Nonnull
  public DelphiScope getScope() {
    if (scope == null) {
      if (parent != null) {
        return parent.getScope();
      }
      return unknownScope();
    }
    return scope;
  }

  @Override
  public DelphiNode jjtGetChild(int index) {
    if (ArrayUtils.isArrayIndexValid(this.children, index)) {
      return this.children[index];
    }
    return null;
  }

  @Override
  public void jjtAddChild(@Nonnull DelphiNode node) {
    DelphiNodeImpl child = (DelphiNodeImpl) node;
    if (child.getToken().isNil()) {
      boolean sameChildren = this.children != null && Arrays.equals(this.children, child.children);
      Preconditions.checkArgument(!sameChildren, "Cannot add child list to itself!");

      int count = node.jjtGetNumChildren();
      for (int i = 0; i < count; ++i) {
        jjtAddChild(node.jjtGetChild(i), jjtGetNumChildren());
      }
    } else {
      jjtAddChild(node, jjtGetNumChildren());
    }

    ((DelphiNodeImpl) node).jjtSetParent(this);
  }

  private void jjtAddChild(DelphiNode child, int index) {
    if (this.children == null) {
      this.children = new DelphiNode[index + 1];
    } else if (index >= this.children.length) {
      DelphiNode[] newChildren = new DelphiNode[index + 1];
      System.arraycopy(this.children, 0, newChildren, 0, this.children.length);
      this.children = newChildren;
    }

    this.children[index] = child;
    child.jjtSetChildIndex(index);
  }

  @Override
  public int getTokenIndex() {
    return jjtGetFirstToken().getIndex();
  }

  @Override
  public DelphiToken getToken() {
    return token;
  }

  @Override
  public DelphiToken jjtGetFirstToken() {
    if (this.firstToken == null) {
      this.firstToken = findFirstToken();
    }
    return this.firstToken;
  }

  @Override
  public DelphiToken jjtGetLastToken() {
    if (this.lastToken == null) {
      this.lastToken = findLastToken();
    }
    return this.lastToken;
  }

  private DelphiToken findFirstToken() {
    DelphiToken result = this.token;
    if (result.isImaginary()) {
      int index = result.getIndex();

      for (int i = 0; i < jjtGetNumChildren(); ++i) {
        DelphiToken childToken = jjtGetChild(i).jjtGetFirstToken();
        int tokenIndex = childToken.getIndex();
        if (!childToken.isImaginary() && tokenIndex < index) {
          result = childToken;
        }
      }
    }
    return result;
  }

  private DelphiToken findLastToken() {
    DelphiToken result = this.jjtGetFirstToken();
    int index = result.getIndex();

    for (int i = 0; i < jjtGetNumChildren(); ++i) {
      DelphiToken childToken = jjtGetChild(i).jjtGetFirstToken();
      int tokenIndex = childToken.getIndex();
      if (!childToken.isImaginary() && tokenIndex > index) {
        result = childToken;
      }
    }
    return result;
  }

  @Override
  public DelphiNode getFirstChildWithId(int nodeId) {
    for (int i = 0; i < jjtGetNumChildren(); ++i) {
      DelphiNode child = jjtGetChild(i);
      if (child.jjtGetId() == nodeId) {
        return child;
      }
    }
    return null;
  }

  /**
   * Gets child type, or -1 if child does not exist
   *
   * @param index Child index
   * @return Child type, or -1 if child is non-existent
   */
  @Override
  public int jjtGetChildId(int index) {
    DelphiNode child = jjtGetChild(index);
    return (child == null) ? -1 : child.jjtGetId();
  }

  @Override
  public String getUnitName() {
    return getAst().getFileHeader().getName();
  }

  @Override
  public final DelphiAst getAst() {
    if (this instanceof DelphiAst) {
      return (DelphiAst) this;
    }
    return getFirstParentOfType(DelphiAst.class);
  }

  @Override
  public final TypeFactory getTypeFactory() {
    return getAst().getDelphiFile().getTypeFactory();
  }

  @Override
  public List<DelphiToken> getComments() {
    return getAst().getCommentsInsideNode(this);
  }

  @Override
  public void jjtSetParent(DelphiNode parent) {
    this.parent = parent;
  }

  @Override
  public DelphiNode jjtGetParent() {
    return this.parent;
  }

  @Override
  public void jjtSetChildIndex(int index) {
    this.childIndex = index;
  }

  @Override
  public int jjtGetChildIndex() {
    return this.childIndex;
  }

  @Override
  public int jjtGetNumChildren() {
    return this.children == null ? 0 : this.children.length;
  }

  @Override
  public DelphiNode getNthParent(int n) {
    if (n <= 0) {
      throw new IllegalArgumentException();
    } else {
      DelphiNode result = this.jjtGetParent();

      for (int i = 1; i < n; ++i) {
        if (result == null) {
          return null;
        }

        result = result.jjtGetParent();
      }

      return result;
    }
  }

  @Override
  public <T> T getFirstParentOfType(Class<T> parentType) {
    DelphiNode parentNode;
    parentNode = this.jjtGetParent();
    while (parentNode != null && !parentType.isInstance(parentNode)) {
      parentNode = parentNode.jjtGetParent();
    }

    return parentType.cast(parentNode);
  }

  @Override
  public <T> List<T> getParentsOfType(Class<T> parentType) {
    List<T> parents = new ArrayList<>();

    for (DelphiNode parentNode = this.jjtGetParent();
        parentNode != null;
        parentNode = parentNode.jjtGetParent()) {
      if (parentType.isInstance(parentNode)) {
        parents.add(parentType.cast(parentNode));
      }
    }

    return parents;
  }

  @Override
  public <T> List<T> findDescendantsOfType(Class<T> targetType) {
    List<T> list = new ArrayList<>();
    findDescendantsOfType(this, targetType, list);
    return list;
  }

  private static <T> void findDescendantsOfType(
      DelphiNode node, Class<T> targetType, List<T> results) {
    for (int i = 0; i < node.jjtGetNumChildren(); ++i) {
      DelphiNode child = node.jjtGetChild(i);
      if (targetType.isAssignableFrom(child.getClass())) {
        results.add(targetType.cast(child));
      }

      findDescendantsOfType(child, targetType, results);
    }
  }

  @Override
  public <T> List<T> findChildrenOfType(Class<T> targetType) {
    List<T> list = new ArrayList<>();

    for (int i = 0; i < this.jjtGetNumChildren(); ++i) {
      DelphiNode child = this.jjtGetChild(i);
      if (targetType.isInstance(child)) {
        list.add(targetType.cast(child));
      }
    }

    return list;
  }

  @Override
  public <T> T getFirstDescendantOfType(Class<T> descendantType) {
    return getFirstDescendantOfType(descendantType, this);
  }

  @Override
  public <T> T getFirstChildOfType(Class<T> childType) {
    for (int i = 0; i < this.jjtGetNumChildren(); ++i) {
      DelphiNode child = this.jjtGetChild(i);
      if (childType.isInstance(child)) {
        return childType.cast(child);
      }
    }

    return null;
  }

  private static <T> T getFirstDescendantOfType(Class<T> descendantType, DelphiNode node) {
    for (int i = 0; i < node.jjtGetNumChildren(); ++i) {
      DelphiNode child = node.jjtGetChild(i);
      if (descendantType.isAssignableFrom(child.getClass())) {
        return descendantType.cast(child);
      }

      T descendant = getFirstDescendantOfType(descendantType, child);
      if (descendant != null) {
        return descendant;
      }
    }

    return null;
  }

  @Override
  public final <T> boolean hasDescendantOfType(Class<T> type) {
    return this.getFirstDescendantOfType(type) != null;
  }

  @Override
  public void jjtSetFirstToken(DelphiToken token) {
    this.firstToken = token;
  }

  @Override
  public void jjtSetLastToken(DelphiToken token) {
    this.lastToken = token;
  }
}
