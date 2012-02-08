/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
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
package org.sonar.plugins.delphi.metrics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.PersistenceMode;
import org.sonar.api.measures.RangeDistributionBuilder;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.plugins.delphi.antlr.ast.DelphiLCOMNode;
import org.sonar.plugins.delphi.core.DelphiFile;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.core.language.ClassFieldInterface;
import org.sonar.plugins.delphi.core.language.ClassInterface;
import org.sonar.plugins.delphi.core.language.FunctionInterface;
import org.sonar.plugins.delphi.core.language.StatementInterface;
import org.sonar.plugins.delphi.core.language.UnitInterface;

/**
 * Class used to analyse LCOM4 metric for a file with a given set of classes and functions
 */
public class LCOM4Metrics extends DefaultMetrics implements MetricsInterface {

  private static final Number[] LCOM4_DISTRIB_BOTTOM_LIMITS = { 0, 1, 2, 3, 4, 5, 10 };
  private Set<Integer> tags = new HashSet<Integer>(); // used for loc4 calculations
  private Set<DelphiLCOMNode> visited = new HashSet<DelphiLCOMNode>();// set of visited nodes
  private RangeDistributionBuilder distribution = new RangeDistributionBuilder(CoreMetrics.LCOM4_DISTRIBUTION, LCOM4_DISTRIB_BOTTOM_LIMITS);

  /**
   * {@inheritDoc}
   */
  public LCOM4Metrics(Project delphiProject) {
    super(delphiProject);
  }

  /**
   * {@inheritDoc}
   */

  public void save(Resource resource, SensorContext sensorContext) {
    sensorContext.saveMeasure(resource, CoreMetrics.LCOM4, getMetric("loc4"));
    sensorContext.saveMeasure(resource, distribution.build().setPersistenceMode(PersistenceMode.MEMORY));
  }

  private void processNode(DelphiLCOMNode node, int tag) {
    if (visited.contains(node)) {
      return;
    }
    visited.add(node);

    for (int i = 0; i < node.getChildCount(); ++i) {
      DelphiLCOMNode child = node.getChild(i);
      processNode(child, tag);
    }

    tags.add(tag);
    node.setTag(tag);
  }

  private void processFunction(FunctionInterface function, Map<String, DelphiLCOMNode> nodes, ClassInterface cl, int num) {
    if (function.isAccessor()) {
      return; // don't count accessors
    }
    DelphiLCOMNode funcNode = new DelphiLCOMNode(function); // new node that stores function
    String funcName = function.getShortName();
    if (num > 0) {
      funcName += "_" + num;
    }
    nodes.put(funcName, funcNode); // put node, connect it with function name

    for (StatementInterface statement : function.getStatements()) // now check if function does relate to some class field
    {
      ClassFieldInterface[] fields = statement.getFields(cl); // get fields from statement
      if (fields == null || fields.length == 0) {
        continue; // if no fields, continue
      }

      for (ClassFieldInterface field : fields) {
        DelphiLCOMNode fieldNode = nodes.get(field.toString());
        if (fieldNode == null) {
          fieldNode = new DelphiLCOMNode(field); // create new node
          nodes.put(field.toString(), fieldNode); // put node, connect it with class field
        }
        funcNode.addChild(fieldNode); // make connections
        fieldNode.addChild(funcNode); // circular reference
      }
    }

    int index = 0;
    for (FunctionInterface overload : function.getOverloadedFunctions()) { // process all overloaded functions for this function
      processFunction(overload, nodes, cl, ++index);
    }

  }

  /**
   * {@inheritDoc}
   */

  public void analyse(DelphiFile resource, SensorContext sensorContext, List<ClassInterface> classes, List<FunctionInterface> functions,
      List<UnitInterface> units) {
    double gLOC4 = 0; // global for whole file
    if (classes != null) {
      for (ClassInterface cl : classes) {
        int loc4 = 0; // set loc4 of this class to 0
        tags.clear(); // clear tags
        visited.clear(); // clear visited

        Map<String, DelphiLCOMNode> nodes = new HashMap<String, DelphiLCOMNode>(); // nodes map
        for (FunctionInterface function : cl.getFunctions()) { // create nodes from class methods
          processFunction(function, nodes, cl, 0);
        }

        for(Map.Entry<String, DelphiLCOMNode> entry : nodes.entrySet()) 
        {
          DelphiLCOMNode node = entry.getValue();
          Object reference = node.getReference();
          if (reference instanceof FunctionInterface) // if reference is function
          {
            FunctionInterface refFunction = (FunctionInterface) reference;
            for (FunctionInterface calledFunc : refFunction.getCalledFunctions()) {
              if ( !cl.hasFunction(calledFunc)) {
                continue; // function is not a class member
              }
              if ( !refFunction.isCalling(calledFunc)) {
                continue; // function does not call calledFunc from its body
              }

              DelphiLCOMNode calledNode = nodes.get(calledFunc.getShortName()); // calledFunc node
              if (calledNode == null) {
                continue; // are we calling an accessor?
              }

              node.addChild(calledNode); // we add child to current node
              calledNode.addChild(node); // we add child to callNode, for circular reference
            }
          }// reference
        }// nodes

        int index = 1;
        for(Map.Entry<String, DelphiLCOMNode> entry : nodes.entrySet()) { // process all nodes to calculate loc4
          processNode(entry.getValue(), index++);
        }

        loc4 = tags.size();
        distribution.add(loc4); // class loc4 distribution
        gLOC4 += loc4;
      }// class
    }

    setMetric("loc4", gLOC4); // set class loc4 metric
  }

  /**
   * {@inheritDoc}
   */

  public boolean executeOnResource(DelphiFile resource) {
    String[] endings = DelphiLanguage.instance.getFileSuffixes();
    for (String ending : endings) {
      if (resource.getPath().endsWith("." + ending)) {
        return true;
      }
    }
    return false;
  }

}
