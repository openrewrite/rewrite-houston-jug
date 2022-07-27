package org.openrewrite.houston;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaSourceFile;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;

public class FindRepeatableAnnotations extends Recipe {

    @Override
    public String getDisplayName() {
        return "Find uses of `@Repeatable` annotations.";
    }

    @Override
    public String getDescription() {
        return "Java 8 introduced the concept of `@Repeatable` annotations.";
    }

    @Override
    protected JavaVisitor<ExecutionContext> getSingleSourceApplicableTest() {
        return new JavaVisitor<ExecutionContext>() {
            @Override
            public J visitJavaSourceFile(JavaSourceFile cu, ExecutionContext executionContext) {
                for (JavaType javaType : cu.getTypesInUse().getTypesInUse()) {
                    if (isRepeatable(javaType)) {
                        return cu.withMarkers(cu.getMarkers().searchResult());
                    }
                }
                return cu;
            }
        };
    }

    @Override
    public JavaVisitor<ExecutionContext> getVisitor() {
        return new JavaVisitor<ExecutionContext>() {
            @Override
            public J visitAnnotation(J.Annotation annotation, ExecutionContext ctx) {
                if (isRepeatable(annotation.getType())) {
                    return annotation.withMarkers(annotation.getMarkers().searchResult());
                }
                return super.visitAnnotation(annotation, ctx);
            }
        };
    }

    static boolean isRepeatable(@Nullable JavaType javaType) {
        JavaType.FullyQualified type = TypeUtils.asFullyQualified(javaType);
        if (TypeUtils.isAssignableTo("java.lang.annotation.Annotation", type)) {
            for (JavaType.FullyQualified ann : type.getAnnotations()) {
                if (TypeUtils.isOfClassType(ann, "java.lang.annotation.Repeatable")) {
                    return true;
                }
            }
        }
        return false;
    }
}
