/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions and Fabricio Colombo
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
 * Fabricio Colombo (fabricio.colombo.mva@gmail.com)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.delphi.pmd.xml;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSetFactory;
import org.apache.commons.io.FileUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.Before;
import org.junit.Test;

public class DelphiRuleSetTest {

  private DelphiRuleSet ruleset;

  @Before
  public void init() {
    ruleset = new DelphiRuleSet();
  }

  @Test
  public void testName() {
    assertThat(ruleset.getName(), is(nullValue()));
    ruleset.setName("name");
    assertThat(ruleset.getName(), is("name"));
  }

  @Test
  public void testDescription() {
    assertThat(ruleset.getDescription(), is(nullValue()));
    ruleset.setDescription("desc");
    assertThat(ruleset.getDescription(), is("desc"));
  }

  @Test
  public void testRules() {
    assertThat(ruleset.getRules(), is(empty()));

    DelphiRule pmdRule = new DelphiRule("testRule");
    ruleset.addRule(pmdRule);

    assertThat(ruleset.getRules(), hasItems(pmdRule));
    assertThat(ruleset.getRules(), IsCollectionWithSize.hasSize(1));
  }

  @Test
  public void testWriteToIsValidPmdRuleSetSyntax() throws Exception {
    StringWriter writer = new StringWriter();
    String rulesXmlPath = "org/sonar/plugins/delphi/pmd/rules.xml";
    URL url = getClass().getClassLoader().getResource(rulesXmlPath);
    MatcherAssert.assertThat(url, is(not(nullValue())));
    InputStreamReader stream = new InputStreamReader(new FileInputStream(url.getPath()), UTF_8);

    DelphiRuleSet ruleSet = DelphiRuleSetHelper.createFrom(stream);
    ruleSet.writeTo(writer);
    String rulesXml = writer.toString();

    File ruleSetFile = File.createTempFile("delphiPmdRuleSet_", ".xml");
    FileUtils.writeStringToFile(ruleSetFile, rulesXml, UTF_8);

    RuleSetFactory ruleSetFactory = new RuleSetFactory();
    RuleSet parsedRuleSet = ruleSetFactory.createRuleSet(ruleSetFile.getAbsolutePath());

    MatcherAssert.assertThat(parsedRuleSet.getRules(), hasSize(ruleSet.getRules().size()));
  }
}
