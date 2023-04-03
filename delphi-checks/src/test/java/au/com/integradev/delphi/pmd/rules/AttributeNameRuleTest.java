package au.com.integradev.delphi.pmd.rules;

import static au.com.integradev.delphi.utils.conditions.RuleKey.ruleKey;

import au.com.integradev.delphi.pmd.xml.DelphiRuleProperty;
import au.com.integradev.delphi.utils.builders.DelphiTestUnitBuilder;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AttributeNameRuleTest extends BasePmdRuleTest {
  private void setAttributeSuffixSetting(String setting) {
    DelphiRuleProperty property =
        Objects.requireNonNull(
            getRule(AttributeNameRule.class)
                .getProperty(AttributeNameRule.ATTRIBUTE_SUFFIX.name()));
    property.setValue(setting);
  }

  @ParameterizedTest
  @ValueSource(strings = {"allowed", "required"})
  void testAllowedOrRequiredAttributeTypeWithSuffixShouldNotAddIssue(String setting) {
    setAttributeSuffixSetting(setting);

    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  FooAttribute = class(TCustomAttribute)");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKey("AttributeNameRule"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"allowed", "forbidden"})
  void testAllowedOrForbiddenAttributeTypeWithoutSuffixShouldNotAddIssue(String setting) {
    setAttributeSuffixSetting(setting);

    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  Foo = class(TCustomAttribute)");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues().areNot(ruleKey("AttributeNameRule"));
  }

  @Test
  void testRequiredAttributeTypeWithoutSuffixShouldAddIssue() {
    setAttributeSuffixSetting("required");

    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  Foo = class(TCustomAttribute)");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKey("AttributeNameRule"));
  }

  @Test
  void testForbiddenAttributeTypeWithSuffixShouldAddIssue() {
    setAttributeSuffixSetting("forbidden");

    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  FooAttribute = class(TCustomAttribute)");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKey("AttributeNameRule"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"allowed", "required", "forbidden"})
  void testNonPascalCaseAttributeTypeShouldAddIssue(String setting) {
    setAttributeSuffixSetting(setting);

    DelphiTestUnitBuilder builder = new DelphiTestUnitBuilder();
    builder.appendDecl("type");
    builder.appendDecl("  fooAttribute = class(TCustomAttribute)");
    builder.appendDecl("  end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKey("AttributeNameRule"));
  }
}