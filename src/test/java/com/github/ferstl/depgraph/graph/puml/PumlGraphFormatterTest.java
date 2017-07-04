package com.github.ferstl.depgraph.graph.puml;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import com.github.ferstl.depgraph.dependency.DependencyNode;
import com.github.ferstl.depgraph.dependency.DependencyNodeUtil;
import com.github.ferstl.depgraph.dependency.NodeIdRenderers;
import com.github.ferstl.depgraph.dependency.PumlDependencyEgdeRenderer;
import com.github.ferstl.depgraph.dependency.PumlDependencyNodeNameRenderer;
import com.github.ferstl.depgraph.graph.Edge;
import com.github.ferstl.depgraph.graph.Node;
import com.github.ferstl.depgraph.graph.NodeRenderer;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

import static org.junit.Assert.assertEquals;

public class PumlGraphFormatterTest {

  private final PumlGraphFormatter formatter = new PumlGraphFormatter();

  private final NodeRenderer<DependencyNode> nodeIdRenderer = NodeIdRenderers.VERSIONLESS_ID;

  private final PumlDependencyNodeNameRenderer nodeInfoRenderer = new PumlDependencyNodeNameRenderer(true, true, true);

  private final PumlDependencyEgdeRenderer edgeInfoRenderer = new PumlDependencyEgdeRenderer();

  private final List<Tuple> dependencies = Arrays.asList(
      new Tuple("com.github.ferstl:depgraph-maven-plugin:2.2.1-SNAPSHOT:compile", false),
      new Tuple("com.fasterxml.jackson.core:jackson-databind:2.8.7:compile", false),
      new Tuple("com.google.guava:guava:21.0:compile", false),
      new Tuple("org.apache.maven:maven-core:jar:3.3.9:provided", false),
      new Tuple("com.google.inject:guice:4.0:provided", false),
      new Tuple("com.google.guava:guava:16.0.1:provided", true),
      new Tuple("junit:junit:4.12:test", false)
  );

  private final List<Node<?>> nodes = Lists.transform(this.dependencies,
      new Function<Tuple, Node<?>>() {

        @Override
        public Node<?> apply(Tuple tuple) {
          return makeNode(tuple.description, tuple.conflict);
        }
      });

  private final Collection<Edge> edges = Arrays.asList(
      makeEgde(this.dependencies.get(0), this.dependencies.get(1)),
      makeEgde(this.dependencies.get(0), this.dependencies.get(2)),
      makeEgde(this.dependencies.get(0), this.dependencies.get(3)),
      makeEgde(this.dependencies.get(0), this.dependencies.get(6)),
      makeEgde(this.dependencies.get(3), this.dependencies.get(4)),
      makeEgde(this.dependencies.get(4), this.dependencies.get(5))
  );

  @Test
  public void testFormatDependenciesGraphAsPumlDiagram() throws Exception {
    final String puml = this.formatter.format("graphName", this.nodes, this.edges);
    assertEquals("@startuml\n" +
        "skinparam rectangle {\n" +
        "  BackgroundColor<<test>> lightGreen\n" +
        "  BackgroundColor<<runtime>> lightBlue\n" +
        "  BackgroundColor<<provided>> lightGray\n" +
        "}\n" +
        "rectangle \"com.github.ferstl:depgraph-maven-plugin:2.2.1-SNAPSHOT\" as com_github_ferstl_depgraph_maven_plugin_\n" +
        "rectangle \"com.fasterxml.jackson.core:jackson-databind:2.8.7\" as com_fasterxml_jackson_core_jackson_databind_\n" +
        "rectangle \"com.google.guava:guava:21.0\" as com_google_guava_guava_\n" +
        "rectangle \"org.apache.maven:maven-core:jar\" as org_apache_maven_maven_core_<<3.3.9>>\n" +
        "rectangle \"com.google.inject:guice:4.0\" as com_google_inject_guice_<<provided>>\n" +
        "rectangle \"com.google.guava:guava:16.0.1\" as com_google_guava_guava_<<provided>>\n" +
        "rectangle \"junit:junit:4.12\" as junit_junit_<<test>>\n" +
        "com_github_ferstl_depgraph_maven_plugin_ -[#000000]-> com_fasterxml_jackson_core_jackson_databind_\n" +
        "com_github_ferstl_depgraph_maven_plugin_ -[#000000]-> com_google_guava_guava_\n" +
        "com_github_ferstl_depgraph_maven_plugin_ -[#000000]-> org_apache_maven_maven_core_\n" +
        "com_github_ferstl_depgraph_maven_plugin_ -[#000000]-> junit_junit_\n" +
        "org_apache_maven_maven_core_ -[#000000]-> com_google_inject_guice_\n" +
        "com_google_inject_guice_ .[#FF0000].> com_google_guava_guava_: 16.0.1\n" +
        "@enduml", puml);
  }

  private Node<?> makeNode(String description, boolean conflict) {

    final DependencyNode dependencyNode = makeDependencyNode(description, conflict);

    final String nodeId = this.nodeIdRenderer.render(dependencyNode);

    final String nodeInfo = this.nodeInfoRenderer.render(dependencyNode);

    return new Node<>(nodeId, nodeInfo, new Object());

  }

  private DependencyNode makeDependencyNode(String description, boolean conflict) {
    final String[] parts = description.split(":");

    return conflict
        ? DependencyNodeUtil.createDependencyNodeWithConflict(parts[0], parts[1], parts[2], parts[3])
        : DependencyNodeUtil.createDependencyNode(parts[0], parts[1], parts[2], parts[3]);

  }

  private Edge makeEgde(Tuple from, Tuple to) {
    final DependencyNode fromNode = makeDependencyNode(from.getDescription(), from.isConflict());

    final DependencyNode toNode = makeDependencyNode(to.getDescription(), to.isConflict());

    return new Edge(this.nodeIdRenderer.render(fromNode),
        this.nodeIdRenderer.render(toNode),
        this.edgeInfoRenderer.render(fromNode, toNode));
  }

  private static class Tuple {

    private final String description;
    private boolean conflict = false;

    Tuple(String description, boolean conflict) {
      this.description = description;
      this.conflict = conflict;
    }

    String getDescription() {
      return this.description;
    }

    boolean isConflict() {
      return this.conflict;
    }
  }
}
