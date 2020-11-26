package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;
import static org.sonar.plugins.delphi.utils.conditions.RuleKeyAtLine.ruleKeyAtLine;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;

class AssignedAndFreeRuleTest extends BasePmdRuleTest {

  @Test
  void testNilComparisonFollowedByFreeShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  if MyVar <> nil then begin")
            .appendImpl("    MyVar.Free;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("AssignedAndFreeRule", builder.getOffset() + 4));
  }

  @Test
  void testQualifiedNilComparisonFollowedByFreeShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  if MyClass.MyVar <> nil then begin")
            .appendImpl("    MyClass.MyVar.Free;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("AssignedAndFreeRule", builder.getOffset() + 4));
  }

  @Test
  void testBackwardsNilComparisonFollowedByFreeShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  if nil <> MyVar then begin")
            .appendImpl("    MyVar.Free;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("AssignedAndFreeRule", builder.getOffset() + 4));
  }

  @Test
  void testQualifiedBackwardsNilComparisonFollowedByFreeShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  if nil <> MyClass.MyVar then begin")
            .appendImpl("    MyClass.MyVar.Free;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("AssignedAndFreeRule", builder.getOffset() + 4));
  }

  @Test
  void testAssignedFollowedByFreeShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  if Assigned(MyVar) then begin")
            .appendImpl("    MyVar.Free;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("AssignedAndFreeRule", builder.getOffset() + 4));
  }

  @Test
  void testQualifiedAssignedFollowedByFreeShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  if Assigned(MyClass.MyVar) then begin")
            .appendImpl("    MyClass.MyVar.Free;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("AssignedAndFreeRule", builder.getOffset() + 4));
  }

  @Test
  void testStandaloneFreeShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  MyVar.Free;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testNilComparisonFollowedByFreeWithoutBeginShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  if MyVar <> nil then MyVar.Free")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("AssignedAndFreeRule", builder.getOffset() + 3));
  }

  @Test
  void testBackwardsNilComparisonFollowedByFreeWithoutBeginShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  if nil <> MyVar then MyVar.Free")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("AssignedAndFreeRule", builder.getOffset() + 3));
  }

  @Test
  void testAssignedFollowedByFreeWithoutBeginShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  if Assigned(MyVar) then MyVar.Free")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("AssignedAndFreeRule", builder.getOffset() + 3));
  }

  @Test
  void testNilComparisonFollowedByFreeAndNilShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  if MyVar <> nil then begin")
            .appendImpl("    FreeAndNil(MyVar);")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("AssignedAndFreeRule", builder.getOffset() + 4));
  }

  @Test
  void testQualifiedNilComparisonFollowedByFreeAndNilShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  if MyClass.MyVar <> nil then begin")
            .appendImpl("    FreeAndNil(MyClass.MyVar);")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("AssignedAndFreeRule", builder.getOffset() + 4));
  }

  @Test
  void testBackwardsNilComparisonFollowedByFreeAndNilShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  if nil <> MyVar then begin")
            .appendImpl("    FreeAndNil(MyVar);")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("AssignedAndFreeRule", builder.getOffset() + 4));
  }

  @Test
  void testAssignedFollowedByFreeAndNilShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  if Assigned(MyVar) then begin")
            .appendImpl("    FreeAndNil(MyVar);")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("AssignedAndFreeRule", builder.getOffset() + 4));
  }

  @Test
  void testQualifiedAssignedFollowedByFreeAndNilShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  if Assigned(MyClass.MyVar) then begin")
            .appendImpl("    FreeAndNil(MyClass.MyVar);")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues()
        .hasSize(1)
        .areExactly(1, ruleKeyAtLine("AssignedAndFreeRule", builder.getOffset() + 4));
  }

  @Test
  void testStandaloneFreeAndNilShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  FreeAndNil(MyVar);")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testNilComparisonFollowedByFreeAndNilWithoutBeginShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  if MyVar <> nil then FreeAndNil(MyVar)")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("AssignedAndFreeRule", builder.getOffset() + 3));
  }

  @Test
  void testBackwardsNilComparisonFollowedByFreeAndNilWithoutBeginShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  if nil <> MyVar then FreeAndNil(MyVar)")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("AssignedAndFreeRule", builder.getOffset() + 3));
  }

  @Test
  void testAssignedFollowedByFreeAndNilWithoutBeginShouldAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("begin")
            .appendImpl("  if Assigned(MyVar) then FreeAndNil(MyVar)")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areExactly(1, ruleKeyAtLine("AssignedAndFreeRule", builder.getOffset() + 3));
  }

  @Test
  void testAssignCheckFollowedByAdditionalConditionsShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure AndProcedure;")
            .appendImpl("begin")
            .appendImpl("  if Assigned(MyVar) and MyVar.ShouldBeFreed then begin")
            .appendImpl("    FreeAndNil(MyVar);")
            .appendImpl("  end;")
            .appendImpl("end;")
            .appendImpl("procedure OrProcedure;")
            .appendImpl("begin")
            .appendImpl("  if Assigned(MyVar) or ShouldFreeSomethingElse then begin")
            .appendImpl("    FreeAndNil(MyVar);")
            .appendImpl("    FreeAndNil(SomethingElse);")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testUnrelatedGuardConditionShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure AndProcedure;")
            .appendImpl("begin")
            .appendImpl("  if 2 + 2 = 4 then begin")
            .appendImpl("    FreeAndNil(MyVar);")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().isEmpty();
  }

  @Test
  void testEdgeCasesShouldNotAddIssue() {
    DelphiTestUnitBuilder builder =
        new DelphiTestUnitBuilder()
            .appendImpl("procedure MyProcedure;")
            .appendImpl("var")
            .appendImpl("  MyBool: Boolean;")
            .appendImpl("begin")
            .appendImpl("  MyBool := Assigned(MyVar);")
            .appendImpl("  if Assigned(MyVar) then begin")
            .appendImpl("    MyBool := False;")
            .appendImpl("  end;")
            .appendImpl("  if Assigned(MyVar) then begin")
            .appendImpl("    MyClass.DoSomeProcedure")
            .appendImpl("  end;")
            .appendImpl("  if Assigned(MyVar) then begin")
            .appendImpl("    // Do nothing")
            .appendImpl("  end;")
            .appendImpl("  if Assigned(MyVar) then begin")
            .appendImpl("    MyVar := nil;")
            .appendImpl("  end;")
            .appendImpl("  if Assigned.NotAnArgumentList then begin")
            .appendImpl("    FMyField.Free;")
            .appendImpl("  end;")
            .appendImpl("  if True then begin")
            .appendImpl("    FMyField.Free;")
            .appendImpl("  end;")
            .appendImpl("  if not True then begin")
            .appendImpl("    FMyField.Free;")
            .appendImpl("  end;")
            .appendImpl("  if Assigned() then begin")
            .appendImpl("    FMyField.Free;")
            .appendImpl("  end;")
            .appendImpl("end;");

    execute(builder);

    assertIssues().areNot(ruleKey("AssignedAndFreeRule"));
  }
}
