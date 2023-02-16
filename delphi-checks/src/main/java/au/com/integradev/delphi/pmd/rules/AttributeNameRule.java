package au.com.integradev.delphi.pmd.rules;

import org.sonar.plugins.communitydelphi.api.ast.TypeDeclarationNode;
import org.sonar.plugins.communitydelphi.api.type.Type;
import au.com.integradev.delphi.utils.NameConventionUtils;
import java.util.Map;
import java.util.TreeMap;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;

public class AttributeNameRule extends AbstractDelphiRule {
  private enum AttributeSuffixSetting {
    REQUIRED,
    ALLOWED,
    FORBIDDEN
  }

  private static final Map<String, AttributeSuffixSetting> ATTRIBUTE_SUFFIX_SETTING_MAP;

  static {
    ATTRIBUTE_SUFFIX_SETTING_MAP = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    ATTRIBUTE_SUFFIX_SETTING_MAP.put("required", AttributeSuffixSetting.REQUIRED);
    ATTRIBUTE_SUFFIX_SETTING_MAP.put("allowed", AttributeSuffixSetting.ALLOWED);
    ATTRIBUTE_SUFFIX_SETTING_MAP.put("forbidden", AttributeSuffixSetting.FORBIDDEN);
  }

  public static final PropertyDescriptor<AttributeSuffixSetting> ATTRIBUTE_SUFFIX =
      PropertyFactory.enumProperty("attributeSuffix", ATTRIBUTE_SUFFIX_SETTING_MAP)
          .desc(
              "Whether to require, allow, or forbid the use of the 'Attribute' suffix for attribute"
                  + " classes. Options are: 'required', 'allowed', and 'forbidden'.")
          .defaultValue(AttributeSuffixSetting.ALLOWED)
          .build();

  public AttributeNameRule() {
    definePropertyDescriptor(ATTRIBUTE_SUFFIX);
  }

  private boolean isAttributeClass(Type type) {
    return type.isClass() && type.isSubTypeOf("System.TCustomAttribute");
  }

  @Override
  public RuleContext visit(TypeDeclarationNode type, RuleContext data) {
    if (isViolation(type)) {
      addViolation(data, type.getTypeNameNode());
    }

    return super.visit(type, data);
  }

  private boolean hasValidSuffix(String typeName) {
    switch (getProperty(ATTRIBUTE_SUFFIX)) {
      case REQUIRED:
        return typeName.toLowerCase().endsWith("attribute");
      case FORBIDDEN:
        return !typeName.toLowerCase().endsWith("attribute");
      default:
        return true;
    }
  }

  private boolean isViolation(TypeDeclarationNode type) {
    return isAttributeClass(type.getType())
        && !(NameConventionUtils.compliesWithPascalCase(type.simpleName())
            && hasValidSuffix(type.simpleName()));
  }
}
