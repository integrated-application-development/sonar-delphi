package org.sonar.plugins.communitydelphi.api.check;

import au.com.integradev.delphi.antlr.ast.visitors.DelphiParserVisitor;

public interface DelphiCheck extends DelphiParserVisitor<DelphiCheckContext> {}
