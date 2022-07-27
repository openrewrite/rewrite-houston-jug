package org.openrewrite.houston;

import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

public class FindRepeatableAnnotationsTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new FindRepeatableAnnotations())
          .parser(JavaParser.fromJavaVersion().classpath("mapstruct").build());
    }

    @Test
    void findRepeatable() {
        rewriteRun(
          java(
            """
              import org.mapstruct.*;
              
              class Test {
                  @ValueMappings({
                          @ValueMapping(source = "UNKNOWN", target = MappingConstants.NULL),
                          @ValueMapping(source = "UNRECOGNIZED", target = MappingConstants.NULL)
                  })
                  void test() {
                  }
              }""",
            """
              import org.mapstruct.*;
              
              class Test {
                  @ValueMappings({
                          /*~~>*/@ValueMapping(source = "UNKNOWN", target = MappingConstants.NULL),
                          /*~~>*/@ValueMapping(source = "UNRECOGNIZED", target = MappingConstants.NULL)
                  })
                  void test() {
                  }
              }"""
          )
        );
    }
}
