/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.symbols.table.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import net.sourceforge.pmd.lang.java.ast.ASTImportDeclaration;
import net.sourceforge.pmd.lang.java.qname.JavaTypeQualifiedName;
import net.sourceforge.pmd.lang.java.qname.QualifiedNameFactory;
import net.sourceforge.pmd.lang.java.symbols.refs.JFieldSymbol;
import net.sourceforge.pmd.lang.java.symbols.refs.JMethodSymbol;
import net.sourceforge.pmd.lang.java.symbols.refs.JResolvableClassDeclarationSymbol;
import net.sourceforge.pmd.lang.java.symbols.table.JSymbolTable;


/**
 * Scope for single imports. Has the highest precedence among imports.
 *
 * @author Clément Fournier
 * @since 7.0.0
 */
public final class SingleImportSymbolTable extends AbstractImportSymbolTable {

    private static final Logger LOG = Logger.getLogger(SingleImportSymbolTable.class.getName());


    /**
     * Creates a scope for single imports, linking it to its parent, which cares about
     * import on demand declarations.
     *
     * @param parent        Parent table
     * @param helper        Resolve helper
     * @param singleImports Import declarations, must not be on-demand!
     */
    public SingleImportSymbolTable(JSymbolTable parent, SymbolTableResolveHelper helper, List<ASTImportDeclaration> singleImports) {
        super(parent, helper);

        for (ASTImportDeclaration anImport : singleImports) {
            if (anImport.isImportOnDemand()) {
                throw new IllegalArgumentException();
            }

            String simpleName = anImport.getImportedSimpleName();
            String name = anImport.getImportedName();

            if (anImport.isStatic()) {
                // Single-Static-Import Declaration
                // fields or methods having the same name

                String className = name.substring(0, name.lastIndexOf('.'));

                Class<?> containerClass = loadClassIgnoreFailure(className);

                if (containerClass != null) {

                    List<JMethodSymbol> methods = Arrays.stream(containerClass.getDeclaredMethods())
                                                        .filter(m -> Modifier.isStatic(m.getModifiers()))
                                                        .filter(myResolveHelper::isAccessible)
                                                        .filter(m -> m.getName().equals(simpleName))
                                                        .map(JMethodSymbol::new)
                                                        .collect(Collectors.toList());

                    importedStaticMethods.put(simpleName, methods);

                    // check for fields

                    try {
                        Field field = containerClass.getDeclaredField(simpleName);
                        if (field != null && Modifier.isStatic(field.getModifiers())) {
                            importedStaticFields.put(simpleName, new JFieldSymbol(field));
                        }
                    } catch (NoSuchFieldException ignored) {
                        // we tried
                    }
                }

                // containerClass==null, the imports cannot be found

            } else {
                // Single-Type-Import Declaration

                importedTypes.put(simpleName, new JResolvableClassDeclarationSymbol(
                        // FIXME the qualifiedname resolver should resolve this itself
                        (JavaTypeQualifiedName) QualifiedNameFactory.ofString(name, myResolveHelper.getClassLoader())));
            }
        }
    }


    @Override
    protected Logger getLogger() {
        return LOG;
    }


}
