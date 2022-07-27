package org.openrewrite.houston;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.J;

import java.util.ArrayList;
import java.util.List;

public class UnwrapRepeatableAnnotations extends Recipe {
    @Override
    public String getDisplayName() {
        return "Unwrap `@Repeatable` annotations.";
    }

    @Override
    public String getDescription() {
        return "Java 8 introduced the concept of `@Repeatable` annotations, " +
               "making the wrapper annotation unnecessary.";
    }

    @Override
    protected JavaVisitor<ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<>() {
            @Override
            public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext ctx) {
                J.MethodDeclaration m = super.visitMethodDeclaration(method, ctx);
                List<J.Annotation> ann = unwrap(m.getLeadingAnnotations());
                return maybeAutoFormat(m, m.withLeadingAnnotations(ann), ann.get(ann.size() - 1), ctx,
                        getCursor().getParentOrThrow());
            }

            @Override
            public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext ctx) {
                J.ClassDeclaration c = super.visitClassDeclaration(classDecl, ctx);
                return c.withLeadingAnnotations(unwrap(c.getLeadingAnnotations()));
            }

            private List<J.Annotation> unwrap(List<J.Annotation> annotations) {
                return ListUtils.flatMap(annotations, a -> {
                    List<J.Annotation> unwrapped = new ArrayList<>(1);

                    new JavaVisitor<Integer>() {
                        @Override
                        public J visitAnnotation(J.Annotation annotation, Integer p) {
                            if (annotation != a && FindRepeatableAnnotations.isRepeatable(annotation.getType())) {
                                unwrapped.add(annotation);
                            }
                            return super.visitAnnotation(annotation, p);
                        }
                    }.visit(a, 0);

                    return unwrapped.isEmpty() ? a : unwrapped;
                });
            }
        };
    }
}
