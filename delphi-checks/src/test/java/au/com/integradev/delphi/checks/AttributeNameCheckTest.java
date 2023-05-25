package au.com.integradev.delphi.checks;

import static au.com.integradev.delphi.conditions.RuleKey.ruleKey;

import au.com.integradev.delphi.CheckTest;
import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import au.com.integradev.delphi.pmd.xml.DelphiRuleProperty;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AttributeNameCheckTest extends CheckTest {
  private void setAttributeSuffixSetting(String setting) {
    DelphiRuleProperty property =
        Objects.requireNonNull(
            getRule(AttributeNameCheck.class)
                .getProperty(AttributeNameCheck.ATTRIBUTE_SUFFIX.name()));
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
