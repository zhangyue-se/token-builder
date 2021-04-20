package javaparser;

/*
 * Copyright (C) 2007-2010 Júlio Vilmar Gesser.
 * Copyright (C) 2011, 2013-2020 The JavaParser Team.
 *
 * This file is part of JavaParser.
 *
 * JavaParser can be used either under the terms of
 * a) the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * b) the terms of the Apache License
 *
 * You should have received a copy of both licenses in LICENCE.LGPL and
 * LICENCE.APACHE. Please refer to those files for details.
 *
 * JavaParser is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 */

import static com.github.javaparser.utils.Utils.SYSTEM_EOL;
import static com.github.javaparser.utils.Utils.assertNotNull;
import static java.util.stream.Collectors.toList;

import java.util.List;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.metamodel.NodeMetaModel;
import com.github.javaparser.metamodel.PropertyMetaModel;

/**
 * Outputs a Graphviz diagram of the AST.
 */
public class MyPainter {

    private int nodeCount;
    private final boolean outputNodeType;

    public MyPainter(boolean outputNodeType) {
        this.outputNodeType = outputNodeType;
    }

    public String output(Node node) {
        nodeCount = 0;
        StringBuilder output = new StringBuilder();
        output(node, null, node.getParentNode().get().getClass().getName(), output);
        System.out.println(nodeCount);
        return output.toString();
    }

    public void output(Node node, String parentNodeName, String name, StringBuilder builder) {
        assertNotNull(node);
        NodeMetaModel metaModel = node.getMetaModel();
        List<PropertyMetaModel> allPropertyMetaModels = metaModel.getAllPropertyMetaModels();
        List<PropertyMetaModel> attributes = allPropertyMetaModels.stream().filter(PropertyMetaModel::isAttribute)
                .filter(PropertyMetaModel::isSingular).collect(toList());
        List<PropertyMetaModel> subNodes = allPropertyMetaModels.stream().filter(PropertyMetaModel::isNode)
                .filter(PropertyMetaModel::isSingular).collect(toList());
        List<PropertyMetaModel> subLists = allPropertyMetaModels.stream().filter(PropertyMetaModel::isNodeList)
                .collect(toList());

        String ndName = nextNodeName();
        if (outputNodeType)
            builder.append("type: " + escape(name) + "(" + metaModel.getTypeName()+ ")" + " --------->" + "token: null" + "\n");
        else
            builder.append("type: " + escape(name) + " --------->" + "token: null" +"\n");

        for (PropertyMetaModel a : attributes) {
            builder.append("type: " + escape(a.getName()) + " --------->" + "token:" + escape(a.getValue(node).toString()) + "\n");
        }

        for (PropertyMetaModel sn : subNodes) {
            Node nd = (Node) sn.getValue(node);
            if (nd != null)
                output(nd, ndName, sn.getName(), builder);
        }

        for (PropertyMetaModel sl : subLists) {
            NodeList<? extends Node> nl = (NodeList<? extends Node>) sl.getValue(node);
            if (nl != null && nl.isNonEmpty()) {
                String ndLstName = nextNodeName();
                builder.append("type: " + escape(sl.getName()) + " --------->" + "token: null" + "\n");
                String slName = sl.getName().substring(0, sl.getName().length() - 1);
                for (Node nd : nl)
                    output(nd, ndLstName, slName, builder);
            }
        }
    }

    private String nextNodeName() {
        return "n" + (nodeCount++);
    }

    private static String escape(String value) {
        return value.replace("\"", "\\\"");
    }
}

