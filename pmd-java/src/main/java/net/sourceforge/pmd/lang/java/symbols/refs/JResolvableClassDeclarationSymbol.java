/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.symbols.refs;

import java.util.Objects;
import java.util.Optional;

import net.sourceforge.pmd.lang.java.ast.ASTAnyTypeDeclaration;
import net.sourceforge.pmd.lang.java.qname.JavaTypeQualifiedName;
import net.sourceforge.pmd.lang.java.qname.QualifiedNameFactory;


/**
 * Symbolic version of {@link JClassSymbol}, which doesn't load a type
 * but provides access to its FQCN. It can try building a full type reference,
 * but this may fail. This kind of reference may be used by functions like typeIs() or
 * TypeHelper to test the type in the absence of a complete auxclasspath, but cannot
 * be used properly by type resolution since it needs access to eg supertypes and members.
 *
 * @author Clément Fournier
 * @since 7.0.0
 */
public class JResolvableClassDeclarationSymbol extends AbstractDeclarationSymbol<ASTAnyTypeDeclaration> implements JSimpleTypeDeclarationSymbol<ASTAnyTypeDeclaration> {

    private final JavaTypeQualifiedName qualifiedName;


    /**
     * Builds a symbolic reference to a type using its qualified name.
     *
     * @param fqcn           Fully-qualified class name
     */
    public JResolvableClassDeclarationSymbol(JavaTypeQualifiedName fqcn) {
        super(fqcn.getClassSimpleName());
        this.qualifiedName = fqcn;
    }


    /**
     * Builds a symbolic reference to a type that has already been resolved.
     *
     * @param alreadyResolved Already resolved type
     */
    public JResolvableClassDeclarationSymbol(Class<?> alreadyResolved) {
        super(alreadyResolved.getSimpleName());
        this.qualifiedName = QualifiedNameFactory.ofClass(Objects.requireNonNull(alreadyResolved));
    }


    /**
     * Returns the qualified name representing this class.
     *
     * @return a qualified name
     */
    public JavaTypeQualifiedName getQualifiedName() {
        return qualifiedName;
    }


    /**
     * Attempts to convert this reference into the richer {@link JClassSymbol}
     * by loading the class. If the class can't be resolved (incomplete classpath),
     * returns an empty optional.
     */
    public Optional<JClassSymbol> loadClass() {
        Class<?> type = qualifiedName.getType();

        if (type == null) {
            return Optional.empty();
        }

        return Optional.of(new JClassSymbol(type));
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JResolvableClassDeclarationSymbol that = (JResolvableClassDeclarationSymbol) o;
        return Objects.equals(qualifiedName, that.qualifiedName);
    }


    @Override
    public int hashCode() {
        return Objects.hash(qualifiedName);
    }
}
