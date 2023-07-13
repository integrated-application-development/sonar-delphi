package au.com.integradev.delphi.antlr.ast.node;

import static org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope.unknownScope;

import au.com.integradev.delphi.antlr.DelphiParser;
import au.com.integradev.delphi.antlr.ast.DelphiTreeAdaptor;
import au.com.integradev.delphi.antlr.ast.token.DelphiTokenImpl;
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
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;
import org.sonar.plugins.communitydelphi.api.token.DelphiTokenType;

public abstract class DelphiNodeImpl implements DelphiNode, MutableDelphiNode {
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
  protected DelphiNodeImpl(Token token) {
    this.token = new DelphiTokenImpl(token);
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
  public DelphiTokenType getTokenType() {
    return this.token.getType();
  }

  @Override
  public int getBeginLine() {
    return getFirstToken().getBeginLine();
  }

  @Override
  public int getBeginColumn() {
    return getFirstToken().getBeginColumn();
  }

  @Override
  public int getEndLine() {
    return getLastToken().getEndLine();
  }

  @Override
  public int getEndColumn() {
    return getLastToken().getEndColumn();
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
  public DelphiNode getChild(int index) {
    if (ArrayUtils.isArrayIndexValid(this.children, index)) {
      return this.children[index];
    }
    return null;
  }

  @Override
  public void addChild(@Nonnull DelphiNode node) {
    DelphiNodeImpl child = (DelphiNodeImpl) node;
    if (child.getToken().isNil()) {
      boolean sameChildren = this.children != null && Arrays.equals(this.children, child.children);
      Preconditions.checkArgument(!sameChildren, "Cannot add child list to itself!");

      int count = node.getChildrenCount();
      for (int i = 0; i < count; ++i) {
        DelphiNodeImpl grandchild = (DelphiNodeImpl) node.getChild(i);
        addChild(grandchild, getChildrenCount());
        grandchild.setParent(this);
      }
    } else {
      addChild(node, getChildrenCount());
    }

    ((DelphiNodeImpl) node).setParent(this);
  }

  private void addChild(DelphiNode child, int index) {
    if (this.children == null) {
      this.children = new DelphiNode[index + 1];
    } else if (index >= this.children.length) {
      DelphiNode[] newChildren = new DelphiNode[index + 1];
      System.arraycopy(this.children, 0, newChildren, 0, this.children.length);
      this.children = newChildren;
    }

    this.children[index] = child;
    child.setChildIndex(index);
  }

  @Override
  public int getTokenIndex() {
    return getFirstToken().getIndex();
  }

  @Override
  public DelphiToken getToken() {
    return token;
  }

  @Override
  public DelphiToken getFirstToken() {
    if (this.firstToken == null) {
      this.firstToken = findFirstToken();
    }
    return this.firstToken;
  }

  @Override
  public DelphiToken getLastToken() {
    if (this.lastToken == null) {
      this.lastToken = findLastToken();
    }
    return this.lastToken;
  }

  private DelphiToken findFirstToken() {
    DelphiToken result = this.token;
    if (result.isImaginary()) {
      int index = result.getIndex();

      for (int i = 0; i < getChildrenCount(); ++i) {
        DelphiToken childToken = getChild(i).getFirstToken();
        int tokenIndex = childToken.getIndex();
        if (!childToken.isImaginary() && tokenIndex < index) {
          result = childToken;
        }
      }
    }
    return result;
  }

  private DelphiToken findLastToken() {
    DelphiToken result = this.getFirstToken();
    int index = result.getIndex();

    for (int i = 0; i < getChildrenCount(); ++i) {
      DelphiToken childToken = getChild(i).getFirstToken();
      int tokenIndex = childToken.getIndex();
      if (!childToken.isImaginary() && tokenIndex > index) {
        result = childToken;
      }
    }
    return result;
  }

  @Override
  public DelphiNode getFirstChildWithTokenType(DelphiTokenType tokenType) {
    for (int i = 0; i < getChildrenCount(); ++i) {
      DelphiNode child = getChild(i);
      if (child.getToken().getType() == tokenType) {
        return child;
      }
    }
    return null;
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
  public void setParent(DelphiNode parent) {
    this.parent = parent;
  }

  @Override
  public DelphiNode getParent() {
    return this.parent;
  }

  @Override
  public void setChildIndex(int index) {
    this.childIndex = index;
  }

  @Override
  public int getChildIndex() {
    return this.childIndex;
  }

  @Override
  public int getChildrenCount() {
    return this.children == null ? 0 : this.children.length;
  }

  @Override
  public DelphiNode getNthParent(int n) {
    if (n <= 0) {
      throw new IllegalArgumentException();
    } else {
      DelphiNode result = this.getParent();

      for (int i = 1; i < n; ++i) {
        if (result == null) {
          return null;
        }

        result = result.getParent();
      }

      return result;
    }
  }

  @Override
  public <T> T getFirstParentOfType(Class<T> parentType) {
    DelphiNode parentNode;
    parentNode = this.getParent();
    while (parentNode != null && !parentType.isInstance(parentNode)) {
      parentNode = parentNode.getParent();
    }

    return parentType.cast(parentNode);
  }

  @Override
  public <T> List<T> getParentsOfType(Class<T> parentType) {
    List<T> parents = new ArrayList<>();

    for (DelphiNode parentNode = this.getParent();
        parentNode != null;
        parentNode = parentNode.getParent()) {
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
    for (int i = 0; i < node.getChildrenCount(); ++i) {
      DelphiNode child = node.getChild(i);
      if (targetType.isAssignableFrom(child.getClass())) {
        results.add(targetType.cast(child));
      }

      findDescendantsOfType(child, targetType, results);
    }
  }

  @Override
  public <T> List<T> findChildrenOfType(Class<T> targetType) {
    List<T> list = new ArrayList<>();

    for (int i = 0; i < this.getChildrenCount(); ++i) {
      DelphiNode child = this.getChild(i);
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
    for (int i = 0; i < this.getChildrenCount(); ++i) {
      DelphiNode child = this.getChild(i);
      if (childType.isInstance(child)) {
        return childType.cast(child);
      }
    }

    return null;
  }

  private static <T> T getFirstDescendantOfType(Class<T> descendantType, DelphiNode node) {
    for (int i = 0; i < node.getChildrenCount(); ++i) {
      DelphiNode child = node.getChild(i);
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
  public void setFirstToken(DelphiToken token) {
    this.firstToken = token;
  }

  @Override
  public void setLastToken(DelphiToken token) {
    this.lastToken = token;
  }
}
