package org.sqlproc.dsl.validation;

import static org.sqlproc.dsl.util.Constants.COLUMN_USAGE;
import static org.sqlproc.dsl.util.Constants.COLUMN_USAGE_EXTENDED;
import static org.sqlproc.dsl.util.Constants.CONSTANT_USAGE;
import static org.sqlproc.dsl.util.Constants.CONSTANT_USAGE_EXTENDED;
import static org.sqlproc.dsl.util.Constants.IDENTIFIER_USAGE;
import static org.sqlproc.dsl.util.Constants.IDENTIFIER_USAGE_EXTENDED;
import static org.sqlproc.dsl.util.Constants.MAPPING_USAGE;
import static org.sqlproc.dsl.util.Constants.MAPPING_USAGE_EXTENDED;
import static org.sqlproc.dsl.util.Constants.TABLE_USAGE;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.naming.IQualifiedNameConverter;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.scoping.IScopeProvider;
import org.eclipse.xtext.validation.Check;
import org.sqlproc.dsl.processorDsl.AbstractPojoEntity;
import org.sqlproc.dsl.processorDsl.Artifacts;
import org.sqlproc.dsl.processorDsl.Column;
import org.sqlproc.dsl.processorDsl.ColumnUsage;
import org.sqlproc.dsl.processorDsl.ColumnUsageExt;
import org.sqlproc.dsl.processorDsl.Constant;
import org.sqlproc.dsl.processorDsl.ConstantUsage;
import org.sqlproc.dsl.processorDsl.ConstantUsageExt;
import org.sqlproc.dsl.processorDsl.DatabaseColumn;
import org.sqlproc.dsl.processorDsl.DatabaseTable;
import org.sqlproc.dsl.processorDsl.Identifier;
import org.sqlproc.dsl.processorDsl.IdentifierUsage;
import org.sqlproc.dsl.processorDsl.IdentifierUsageExt;
import org.sqlproc.dsl.processorDsl.MappingColumn;
import org.sqlproc.dsl.processorDsl.MappingItem;
import org.sqlproc.dsl.processorDsl.MappingRule;
import org.sqlproc.dsl.processorDsl.MappingUsage;
import org.sqlproc.dsl.processorDsl.MappingUsageExt;
import org.sqlproc.dsl.processorDsl.MetaSql;
import org.sqlproc.dsl.processorDsl.MetaStatement;
import org.sqlproc.dsl.processorDsl.OptionalFeature;
import org.sqlproc.dsl.processorDsl.PackageDeclaration;
import org.sqlproc.dsl.processorDsl.PojoDefinition;
import org.sqlproc.dsl.processorDsl.PojoEntity;
import org.sqlproc.dsl.processorDsl.PojoProperty;
import org.sqlproc.dsl.processorDsl.PojoUsage;
import org.sqlproc.dsl.processorDsl.PojoUsageExt;
import org.sqlproc.dsl.processorDsl.ProcessorDslPackage;
import org.sqlproc.dsl.processorDsl.Property;
import org.sqlproc.dsl.processorDsl.TableDefinition;
import org.sqlproc.dsl.processorDsl.TableUsage;
import org.sqlproc.dsl.resolver.DbResolver;
import org.sqlproc.dsl.resolver.PojoResolverFactory;
import org.sqlproc.dsl.util.Utils;

import com.google.inject.Inject;

public class ProcessorDslJavaValidator extends AbstractProcessorDslJavaValidator {

    @Inject
    PojoResolverFactory pojoResolverFactory;

    @Inject
    DbResolver dbResolver;

    @Inject
    IScopeProvider scopeProvider;

    @Inject
    IQualifiedNameConverter qualifiedNameConverter;

    public enum ValidationResult {
        OK, WARNING, ERROR;
    }

    private static final List<String> F_TYPES = Collections.unmodifiableList(Arrays.asList(new String[] { "set",
            "update", "values", "where", "set=opt", "where=opt" }));

    @Check
    public void checkMetaSqlFtype(MetaSql metaSql) {
        if (metaSql.getFtype() == null)
            return;
        if (!findInListIgnoreCase(F_TYPES, metaSql.getFtype())) {
            error("Invalid ftype : " + metaSql.getFtype(), ProcessorDslPackage.Literals.META_SQL__FTYPE);
        }
    }

    private boolean findInListIgnoreCase(List<String> list, String value) {
        if (list == null)
            return false;
        for (String item : list) {
            if (item.equalsIgnoreCase(value))
                return true;
        }
        return false;
    }

    @Check
    public void checkUniqueMetaStatement(MetaStatement metaStatement) {
        Artifacts artifacts;
        EObject object = EcoreUtil.getRootContainer(metaStatement);
        if (!(object instanceof Artifacts))
            return;
        artifacts = (Artifacts) object;
        for (MetaStatement metaStmt : artifacts.getStatements()) {
            if (metaStmt == null || metaStmt == metaStatement)
                continue;
            if (equalsStatement(metaStatement, metaStmt)) {
                error("Duplicate name : " + metaStatement.getName() + "[" + metaStatement.getType() + "]",
                        ProcessorDslPackage.Literals.META_STATEMENT__NAME);
                return;
            }
        }
    }

    @Check
    public void checkUniqueMappingRule(MappingRule mappingRule) {
        Artifacts artifacts;
        EObject object = EcoreUtil.getRootContainer(mappingRule);
        if (!(object instanceof Artifacts))
            return;
        artifacts = (Artifacts) object;
        for (MappingRule rule : artifacts.getMappings()) {
            if (rule == null || rule == mappingRule)
                continue;
            if (equalsRule(mappingRule, rule)) {
                error("Duplicate name : " + mappingRule.getName() + "[" + mappingRule.getType() + "]",
                        ProcessorDslPackage.Literals.MAPPING_RULE__NAME);
                return;
            }
        }
    }

    @Check
    public void checkUniqueOptionalFeature(OptionalFeature optionalFeature) {
        Artifacts artifacts;
        EObject object = EcoreUtil.getRootContainer(optionalFeature);
        if (!(object instanceof Artifacts))
            return;
        artifacts = (Artifacts) object;
        for (OptionalFeature feature : artifacts.getFeatures()) {
            if (feature == null || feature == optionalFeature)
                continue;
            if (equalsFeature(optionalFeature, feature)) {
                error("Duplicate name : " + optionalFeature.getName() + "[" + optionalFeature.getType() + "]",
                        ProcessorDslPackage.Literals.OPTIONAL_FEATURE__NAME);
                return;
            }
        }
    }

    @Check
    public void checkUniquePojoDefinition(PojoDefinition pojoDefinition) {
        if (isResolvePojo(pojoDefinition) && !checkClass(pojoDefinition.getClass_()))
            error("Class name : " + pojoDefinition.getClass_() + " not exists",
                    ProcessorDslPackage.Literals.POJO_DEFINITION__NAME);
        Artifacts artifacts;
        EObject object = EcoreUtil.getRootContainer(pojoDefinition);
        if (!(object instanceof Artifacts))
            return;
        artifacts = (Artifacts) object;
        for (PojoDefinition definition : artifacts.getPojos()) {
            if (definition == null || definition == pojoDefinition)
                continue;
            if (pojoDefinition.getName().equals(definition.getName())) {
                error("Duplicate name : " + pojoDefinition.getName(),
                        ProcessorDslPackage.Literals.POJO_DEFINITION__NAME);
                return;
            }
        }
    }

    @Check
    public void checkUniqueColumnUsage(ColumnUsage columnUsage) {
        Artifacts artifacts;
        EObject object = EcoreUtil.getRootContainer(columnUsage);
        if (!(object instanceof Artifacts))
            return;
        artifacts = (Artifacts) object;
        for (PojoUsage usage : artifacts.getUsages()) {
            if (usage == null || usage == columnUsage || !(usage instanceof ColumnUsage))
                continue;
            ColumnUsage column = (ColumnUsage) usage;
            if (column.getStatement() == null)
                continue;
            if (columnUsage.getStatement().getName().equals(column.getStatement().getName())) {
                error("Duplicate name : " + columnUsage.getStatement().getName() + "[col]",
                        ProcessorDslPackage.Literals.COLUMN_USAGE__STATEMENT);
                return;
            }
        }
        for (PojoUsageExt usage : artifacts.getUsagesExt()) {
            if (usage == null || usage == columnUsage || !(usage instanceof ColumnUsageExt))
                continue;
            ColumnUsageExt column = (ColumnUsageExt) usage;
            if (column.getStatement() == null)
                continue;
            if (columnUsage.getStatement().getName().equals(column.getStatement().getName())) {
                error("Duplicate name : " + columnUsage.getStatement().getName() + "[col]",
                        ProcessorDslPackage.Literals.COLUMN_USAGE__STATEMENT);
                return;
            }
        }
    }

    @Check
    public void checkUniqueIdentifierUsage(IdentifierUsage identifierUsage) {
        Artifacts artifacts;
        EObject object = EcoreUtil.getRootContainer(identifierUsage);
        if (!(object instanceof Artifacts))
            return;
        artifacts = (Artifacts) object;
        for (PojoUsage usage : artifacts.getUsages()) {
            if (usage == null || usage == identifierUsage || !(usage instanceof IdentifierUsage))
                continue;
            IdentifierUsage ident = (IdentifierUsage) usage;
            if (ident.getStatement() == null)
                continue;
            if (identifierUsage.getStatement().getName().equals(ident.getStatement().getName())) {
                error("Duplicate name : " + identifierUsage.getStatement().getName() + "[ident]",
                        ProcessorDslPackage.Literals.IDENTIFIER_USAGE__STATEMENT);
                return;
            }
        }
        for (PojoUsageExt usage : artifacts.getUsagesExt()) {
            if (usage == null || usage == identifierUsage || !(usage instanceof IdentifierUsageExt))
                continue;
            IdentifierUsageExt ident = (IdentifierUsageExt) usage;
            if (ident.getStatement() == null)
                continue;
            if (identifierUsage.getStatement().getName().equals(ident.getStatement().getName())) {
                error("Duplicate name : " + identifierUsage.getStatement().getName() + "[ident]",
                        ProcessorDslPackage.Literals.IDENTIFIER_USAGE__STATEMENT);
                return;
            }
        }
    }

    @Check
    public void checkUniqueConstantUsage(ConstantUsage constantUsage) {
        Artifacts artifacts;
        EObject object = EcoreUtil.getRootContainer(constantUsage);
        if (!(object instanceof Artifacts))
            return;
        artifacts = (Artifacts) object;
        for (PojoUsage usage : artifacts.getUsages()) {
            if (usage == null || usage == constantUsage || !(usage instanceof ConstantUsage))
                continue;
            ConstantUsage constant = (ConstantUsage) usage;
            if (constant.getStatement() == null)
                continue;
            if (constantUsage.getStatement().getName().equals(constant.getStatement().getName())) {
                error("Duplicate name : " + constantUsage.getStatement().getName() + "[const]",
                        ProcessorDslPackage.Literals.CONSTANT_USAGE__STATEMENT);
                return;
            }
        }
        for (PojoUsageExt usage : artifacts.getUsagesExt()) {
            if (usage == null || usage == constantUsage || !(usage instanceof ConstantUsageExt))
                continue;
            ConstantUsageExt constant = (ConstantUsageExt) usage;
            if (constant.getStatement() == null)
                continue;
            if (constantUsage.getStatement().getName().equals(constant.getStatement().getName())) {
                error("Duplicate name : " + constantUsage.getStatement().getName() + "[const]",
                        ProcessorDslPackage.Literals.CONSTANT_USAGE__STATEMENT);
                return;
            }
        }
    }

    @Check
    public void checkUniqueMappingUsage(MappingUsage mappingUsage) {
        Artifacts artifacts;
        EObject object = EcoreUtil.getRootContainer(mappingUsage);
        if (!(object instanceof Artifacts))
            return;
        artifacts = (Artifacts) object;
        for (PojoUsage usage : artifacts.getUsages()) {
            if (usage == null || usage == mappingUsage || !(usage instanceof MappingUsage))
                continue;
            MappingUsage mapping = (MappingUsage) usage;
            if (mapping.getStatement() == null)
                continue;
            if (mappingUsage.getStatement().getName().equals(mapping.getStatement().getName())) {
                error("Duplicate name : " + mappingUsage.getStatement().getName() + "[col]",
                        ProcessorDslPackage.Literals.MAPPING_USAGE__STATEMENT);
                return;
            }
        }
        for (PojoUsageExt usage : artifacts.getUsagesExt()) {
            if (usage == null || usage == mappingUsage || !(usage instanceof MappingUsageExt))
                continue;
            MappingUsageExt mapping = (MappingUsageExt) usage;
            if (mapping.getStatement() == null)
                continue;
            if (mappingUsage.getStatement().getName().equals(mapping.getStatement().getName())) {
                error("Duplicate name : " + mappingUsage.getStatement().getName() + "[col]",
                        ProcessorDslPackage.Literals.MAPPING_USAGE__STATEMENT);
                return;
            }
        }
    }

    protected boolean equalsStatement(MetaStatement statement1, MetaStatement statement2) {
        if (statement1 == null && statement2 == null)
            return true;
        if (statement1 == null || statement1.getName() == null)
            return false;
        if (statement2 == null || statement2.getName() == null)
            return false;
        if (statement1.getName().equals(statement2.getName()) && statement1.getType().equals(statement2.getType())) {
            return equalsFilters(statement1.getFilters(), statement2.getFilters());
        }
        return false;
    }

    protected boolean equalsRule(MappingRule rule1, MappingRule rule2) {
        if (rule1 == null && rule2 == null)
            return true;
        if (rule1 == null || rule1.getName() == null)
            return false;
        if (rule2 == null || rule2.getName() == null)
            return false;
        if (rule1.getName().equals(rule2.getName()) && rule1.getType().equals(rule2.getType())) {
            return equalsFilters(rule1.getFilters(), rule2.getFilters());
        }
        return false;
    }

    protected boolean equalsFeature(OptionalFeature feature1, OptionalFeature feature2) {
        if (feature1 == null && feature2 == null)
            return true;
        if (feature1 == null || feature1.getName() == null)
            return false;
        if (feature2 == null || feature2.getName() == null)
            return false;
        if (feature1.getName().equals(feature2.getName()) && feature1.getType().equals(feature2.getType())) {
            return equalsFilters(feature1.getFilters(), feature2.getFilters());
        }
        return false;
    }

    protected boolean equalsFilters(List<String> filters1, List<String> filters2) {
        List<String> filteredFilters1 = filteredFilters(filters1);
        List<String> filteredFilters2 = filteredFilters(filters2);
        if (filteredFilters1 == null && filteredFilters2 == null)
            return true;
        if (filteredFilters1 == null)
            return false;
        if (filteredFilters2 == null)
            return false;
        if (filteredFilters1.isEmpty() && filteredFilters2.isEmpty())
            return true;
        // Filtry musi byt disjunktni, pro jednu shodu je vysledek komparace kladny
        for (String filter1 : filteredFilters1)
            for (String filter2 : filteredFilters2)
                if (filter1.equals(filter2))
                    return true;
        return false;
    }

    protected List<String> filteredFilters(List<String> filters) {
        if (filters == null)
            return null;
        List<String> filteredFilters = new ArrayList<String>();
        for (String filter : filters) {
            if (filter.indexOf('=') < 0)
                filteredFilters.add(filter);
        }
        return filteredFilters;
    }

    protected boolean checkClass(String className) {
        if (className == null || pojoResolverFactory.getPojoResolver() == null)
            return true;

        Class<?> clazz = pojoResolverFactory.getPojoResolver().loadClass(className);
        return clazz != null;
    }

    @Check
    public void checkColumn(Column column) {
        if (!isResolvePojo(column))
            return;
        MetaStatement statement = EcoreUtil2.getContainerOfType(column, MetaStatement.class);
        Artifacts artifacts = EcoreUtil2.getContainerOfType(statement, Artifacts.class);

        String entityName = Utils.getTokenFromFilter(statement, COLUMN_USAGE_EXTENDED);
        PojoEntity entity = (entityName != null) ? Utils.findEntity(qualifiedNameConverter, artifacts,
                scopeProvider.getScope(artifacts, ProcessorDslPackage.Literals.ARTIFACTS__POJO_PACKAGES), entityName)
                : null;
        if (entity == null) {
            IScope scope = scopeProvider.getScope(artifacts, ProcessorDslPackage.Literals.ARTIFACTS__USAGES_EXT);
            Iterable<IEObjectDescription> iterable = scope.getAllElements();
            for (Iterator<IEObjectDescription> iter = iterable.iterator(); iter.hasNext();) {
                IEObjectDescription description = iter.next();
                if (qualifiedNameConverter.toQualifiedName(statement.getName()).equals(description.getName())) {
                    PojoUsageExt pojoUsage = (PojoUsageExt) artifacts.eResource().getResourceSet()
                            .getEObject(description.getEObjectURI(), true);
                    if (pojoUsage instanceof ColumnUsageExt) {
                        entity = pojoUsage.getPojo();
                    }
                    if (pojoUsage instanceof MappingUsageExt) {
                        entity = pojoUsage.getPojo();
                    }
                }
            }
        }
        if (entity != null) {
            switch (checkEntityProperty(entity, column.getName())) {
            case WARNING:
                warning("Problem property : " + column.getName() + "[" + entity.getName() + "]",
                        ProcessorDslPackage.Literals.COLUMN__NAME);
                break;
            case ERROR:
                error("Cannot find property : " + column.getName() + "[" + entity.getName() + "]",
                        ProcessorDslPackage.Literals.COLUMN__NAME);
                break;
            }
            return;
        }

        String pojoName = Utils.getTokenFromFilter(statement, COLUMN_USAGE);
        PojoDefinition pojo = (pojoName != null) ? Utils.findPojo(qualifiedNameConverter, artifacts,
                scopeProvider.getScope(artifacts, ProcessorDslPackage.Literals.ARTIFACTS__POJOS), pojoName) : null;
        String columnUsageClass = (pojo != null) ? pojo.getClass_() : null;
        MappingUsage mappingUsage = null;
        if (columnUsageClass == null) {
            IScope scope = scopeProvider.getScope(artifacts, ProcessorDslPackage.Literals.ARTIFACTS__USAGES);
            Iterable<IEObjectDescription> iterable = scope.getAllElements();
            for (Iterator<IEObjectDescription> iter = iterable.iterator(); iter.hasNext();) {
                IEObjectDescription description = iter.next();
                if (qualifiedNameConverter.toQualifiedName(statement.getName()).equals(description.getName())) {
                    PojoUsage pojoUsage = (PojoUsage) artifacts.eResource().getResourceSet()
                            .getEObject(description.getEObjectURI(), true);
                    if (pojoUsage instanceof ColumnUsage) {
                        columnUsageClass = ((ColumnUsage) pojoUsage).getPojo().getClass_();
                    }
                    if (pojoUsage instanceof MappingUsage) {
                        mappingUsage = (MappingUsage) pojoUsage;
                    }
                }
            }
        }
        if (mappingUsage != null && mappingUsage.getStatement() != null
                && mappingUsage.getStatement().getMapping() != null
                && mappingUsage.getStatement().getMapping().getMappingItems() != null) {
            for (MappingItem mappingItem : mappingUsage.getStatement().getMapping().getMappingItems()) {
                if (mappingItem.getCol().equals(column.getName()))
                    return;
            }
        }
        switch (checkClassProperty(columnUsageClass, column.getName())) {
        case WARNING:
            warning("Problem property : " + column.getName() + "[" + columnUsageClass + "]",
                    ProcessorDslPackage.Literals.COLUMN__NAME);
            break;
        case ERROR:
            error("Cannot find property : " + column.getName() + "[" + columnUsageClass + "]",
                    ProcessorDslPackage.Literals.COLUMN__NAME);
            break;
        }
    }

    @Check
    public void checkIdentifier(Identifier identifier) {
        if (!isResolvePojo(identifier))
            return;
        MetaStatement statement = EcoreUtil2.getContainerOfType(identifier, MetaStatement.class);
        Artifacts artifacts = EcoreUtil2.getContainerOfType(statement, Artifacts.class);

        String entityName = Utils.getTokenFromFilter(statement, IDENTIFIER_USAGE_EXTENDED);
        PojoEntity entity = (entityName != null) ? Utils.findEntity(qualifiedNameConverter, artifacts,
                scopeProvider.getScope(artifacts, ProcessorDslPackage.Literals.ARTIFACTS__POJO_PACKAGES), entityName)
                : null;
        if (entity == null) {
            IScope scope = scopeProvider.getScope(artifacts, ProcessorDslPackage.Literals.ARTIFACTS__USAGES_EXT);
            Iterable<IEObjectDescription> iterable = scope.getAllElements();
            for (Iterator<IEObjectDescription> iter = iterable.iterator(); iter.hasNext();) {
                IEObjectDescription description = iter.next();
                if (qualifiedNameConverter.toQualifiedName(statement.getName()).equals(description.getName())) {
                    PojoUsageExt pojoUsage = (PojoUsageExt) artifacts.eResource().getResourceSet()
                            .getEObject(description.getEObjectURI(), true);
                    if (pojoUsage instanceof IdentifierUsageExt) {
                        entity = pojoUsage.getPojo();
                    }
                }
            }
        }
        if (entity != null) {
            switch (checkEntityProperty(entity, identifier.getName())) {
            case WARNING:
                warning("Problem property : " + identifier.getName() + "[" + entity.getName() + "]",
                        ProcessorDslPackage.Literals.IDENTIFIER__NAME);
                break;
            case ERROR:
                error("Cannot find property : " + identifier.getName() + "[" + entity.getName() + "]",
                        ProcessorDslPackage.Literals.IDENTIFIER__NAME);
                break;
            }
            return;
        }

        String pojoName = Utils.getTokenFromFilter(statement, IDENTIFIER_USAGE);
        PojoDefinition pojo = (pojoName != null) ? Utils.findPojo(qualifiedNameConverter, artifacts,
                scopeProvider.getScope(artifacts, ProcessorDslPackage.Literals.ARTIFACTS__POJOS), pojoName) : null;
        String identifierUsageClass = (pojo != null) ? pojo.getClass_() : null;
        if (identifierUsageClass == null) {
            IScope scope = scopeProvider.getScope(artifacts, ProcessorDslPackage.Literals.ARTIFACTS__USAGES);
            Iterable<IEObjectDescription> iterable = scope.getAllElements();
            for (Iterator<IEObjectDescription> iter = iterable.iterator(); iter.hasNext();) {
                IEObjectDescription description = iter.next();
                if (qualifiedNameConverter.toQualifiedName(statement.getName()).equals(description.getName())) {
                    PojoUsage pojoUsage = (PojoUsage) artifacts.eResource().getResourceSet()
                            .getEObject(description.getEObjectURI(), true);
                    if (pojoUsage instanceof IdentifierUsage) {
                        identifierUsageClass = ((IdentifierUsage) pojoUsage).getPojo().getClass_();
                        break;
                    }
                }
            }
        }
        switch (checkClassProperty(identifierUsageClass, identifier.getName())) {
        case WARNING:
            warning("Problem property : " + identifier.getName() + "[" + identifierUsageClass + "]",
                    ProcessorDslPackage.Literals.IDENTIFIER__NAME);
            break;
        case ERROR:
            error("Cannot find property : " + identifier.getName() + "[" + identifierUsageClass + "]",
                    ProcessorDslPackage.Literals.IDENTIFIER__NAME);
            break;
        }
    }

    @Check
    public void checkConstant(Constant constant) {
        if (!isResolvePojo(constant))
            return;
        MetaStatement statement = EcoreUtil2.getContainerOfType(constant, MetaStatement.class);
        Artifacts artifacts = EcoreUtil2.getContainerOfType(statement, Artifacts.class);

        String entityName = Utils.getTokenFromFilter(statement, CONSTANT_USAGE_EXTENDED);
        PojoEntity entity = (entityName != null) ? Utils.findEntity(qualifiedNameConverter, artifacts,
                scopeProvider.getScope(artifacts, ProcessorDslPackage.Literals.ARTIFACTS__POJO_PACKAGES), entityName)
                : null;
        if (entity == null) {
            IScope scope = scopeProvider.getScope(artifacts, ProcessorDslPackage.Literals.ARTIFACTS__USAGES_EXT);
            Iterable<IEObjectDescription> iterable = scope.getAllElements();
            for (Iterator<IEObjectDescription> iter = iterable.iterator(); iter.hasNext();) {
                IEObjectDescription description = iter.next();
                if (qualifiedNameConverter.toQualifiedName(statement.getName()).equals(description.getName())) {
                    PojoUsageExt pojoUsage = (PojoUsageExt) artifacts.eResource().getResourceSet()
                            .getEObject(description.getEObjectURI(), true);
                    if (pojoUsage instanceof ConstantUsageExt) {
                        entity = pojoUsage.getPojo();
                    }
                }
            }
        }
        if (entity != null) {
            switch (checkEntityProperty(entity, constant.getName())) {
            case WARNING:
                warning("Problem property : " + constant.getName() + "[" + entity.getName() + "]",
                        ProcessorDslPackage.Literals.CONSTANT__NAME);
                break;
            case ERROR:
                error("Cannot find property : " + constant.getName() + "[" + entity.getName() + "]",
                        ProcessorDslPackage.Literals.CONSTANT__NAME);
                break;
            }
            return;
        }

        String pojoName = Utils.getTokenFromFilter(statement, CONSTANT_USAGE);
        PojoDefinition pojo = (pojoName != null) ? Utils.findPojo(qualifiedNameConverter, artifacts,
                scopeProvider.getScope(artifacts, ProcessorDslPackage.Literals.ARTIFACTS__POJOS), pojoName) : null;
        String constantUsageClass = (pojo != null) ? pojo.getClass_() : null;
        if (constantUsageClass == null) {
            IScope scope = scopeProvider.getScope(artifacts, ProcessorDslPackage.Literals.ARTIFACTS__USAGES);
            Iterable<IEObjectDescription> iterable = scope.getAllElements();
            for (Iterator<IEObjectDescription> iter = iterable.iterator(); iter.hasNext();) {
                IEObjectDescription description = iter.next();
                if (qualifiedNameConverter.toQualifiedName(statement.getName()).equals(description.getName())) {
                    PojoUsage pojoUsage = (PojoUsage) artifacts.eResource().getResourceSet()
                            .getEObject(description.getEObjectURI(), true);
                    if (pojoUsage instanceof ConstantUsage) {
                        constantUsageClass = ((ConstantUsage) pojoUsage).getPojo().getClass_();
                        break;
                    }
                }
            }
        }
        switch (checkClassProperty(constantUsageClass, constant.getName())) {
        case WARNING:
            warning("Problem property : " + constant.getName() + "[" + constantUsageClass + "]",
                    ProcessorDslPackage.Literals.CONSTANT__NAME);
            break;
        case ERROR:
            error("Cannot find property : " + constant.getName() + "[" + constantUsageClass + "]",
                    ProcessorDslPackage.Literals.CONSTANT__NAME);
            break;
        }
    }

    @Check
    public void checkMappingColumn(MappingColumn column) {
        if (!isResolvePojo(column))
            return;
        MappingRule rule = EcoreUtil2.getContainerOfType(column, MappingRule.class);
        Artifacts artifacts = EcoreUtil2.getContainerOfType(rule, Artifacts.class);

        String entityName = Utils.getTokenFromFilter(rule, MAPPING_USAGE_EXTENDED);
        PojoEntity entity = (entityName != null) ? Utils.findEntity(qualifiedNameConverter, artifacts,
                scopeProvider.getScope(artifacts, ProcessorDslPackage.Literals.ARTIFACTS__POJO_PACKAGES), entityName)
                : null;
        if (entity == null) {
            IScope scope = scopeProvider.getScope(artifacts, ProcessorDslPackage.Literals.ARTIFACTS__USAGES_EXT);
            Iterable<IEObjectDescription> iterable = scope.getAllElements();
            for (Iterator<IEObjectDescription> iter = iterable.iterator(); iter.hasNext();) {
                IEObjectDescription description = iter.next();
                if (qualifiedNameConverter.toQualifiedName(rule.getName()).equals(description.getName())) {
                    PojoUsageExt pojoUsage = (PojoUsageExt) artifacts.eResource().getResourceSet()
                            .getEObject(description.getEObjectURI(), true);
                    if (pojoUsage instanceof MappingUsageExt) {
                        entity = pojoUsage.getPojo();
                    }
                }
            }
        }
        if (entity != null) {
            switch (checkEntityProperty(entity, column.getName())) {
            case WARNING:
                warning("Problem property : " + column.getName() + "[" + entity.getName() + "]",
                        ProcessorDslPackage.Literals.COLUMN__NAME);
                break;
            case ERROR:
                error("Cannot find property : " + column.getName() + "[" + entity.getName() + "]",
                        ProcessorDslPackage.Literals.COLUMN__NAME);
                break;
            }
            return;
        }

        String pojoName = Utils.getTokenFromFilter(rule, MAPPING_USAGE);
        PojoDefinition pojo = (pojoName != null) ? Utils.findPojo(qualifiedNameConverter, artifacts,
                scopeProvider.getScope(artifacts, ProcessorDslPackage.Literals.ARTIFACTS__POJOS), pojoName) : null;
        String mappingUsageClass = (pojo != null) ? pojo.getClass_() : null;
        if (mappingUsageClass == null) {
            IScope scope = scopeProvider.getScope(artifacts, ProcessorDslPackage.Literals.ARTIFACTS__USAGES);
            Iterable<IEObjectDescription> iterable = scope.getAllElements();
            for (Iterator<IEObjectDescription> iter = iterable.iterator(); iter.hasNext();) {
                IEObjectDescription description = iter.next();
                if (qualifiedNameConverter.toQualifiedName(rule.getName()).equals(description.getName())) {
                    PojoUsage pojoUsage = (PojoUsage) artifacts.eResource().getResourceSet()
                            .getEObject(description.getEObjectURI(), true);
                    if (pojoUsage instanceof MappingUsage) {
                        mappingUsageClass = ((MappingUsage) pojoUsage).getPojo().getClass_();
                        break;
                    }
                }
            }
        }
        switch (checkClassProperty(mappingUsageClass, column.getName())) {
        case WARNING:
            warning("Problem property : " + column.getName() + "[" + mappingUsageClass + "]",
                    ProcessorDslPackage.Literals.MAPPING_COLUMN__NAME);
            break;
        case ERROR:
            error("Cannot find property : " + column.getName() + "[" + mappingUsageClass + "]",
                    ProcessorDslPackage.Literals.MAPPING_COLUMN__NAME);
            break;

        }
    }

    @Check
    public void checkMetaStatement(MetaStatement statement) {
        Artifacts artifacts = EcoreUtil2.getContainerOfType(statement, Artifacts.class);

        if (statement.getFilters() == null || statement.getFilters().isEmpty())
            return;

        int index = 0;
        for (String filter : statement.getFilters()) {
            int ix = filter.indexOf('=');
            if (ix <= 0)
                continue;
            String key = filter.substring(0, ix);
            String val = filter.substring(ix + 1);
            if (IDENTIFIER_USAGE_EXTENDED.equals(key)) {
                PojoEntity entity = Utils.findEntity(qualifiedNameConverter, artifacts,
                        scopeProvider.getScope(artifacts, ProcessorDslPackage.Literals.ARTIFACTS__POJO_PACKAGES), val);
                if (entity == null) {
                    error("Cannot find entity : " + val + "[" + IDENTIFIER_USAGE_EXTENDED + "]",
                            ProcessorDslPackage.Literals.META_STATEMENT__FILTERS, index);
                }
            } else if (IDENTIFIER_USAGE.equals(key)) {
                PojoDefinition pojo = Utils.findPojo(qualifiedNameConverter, artifacts,
                        scopeProvider.getScope(artifacts, ProcessorDslPackage.Literals.ARTIFACTS__POJOS), val);
                if (pojo == null) {
                    error("Cannot find pojo : " + val + "[" + IDENTIFIER_USAGE + "]",
                            ProcessorDslPackage.Literals.META_STATEMENT__FILTERS, index);
                }
            } else if (COLUMN_USAGE_EXTENDED.equals(key)) {
                PojoEntity entity = Utils.findEntity(qualifiedNameConverter, artifacts,
                        scopeProvider.getScope(artifacts, ProcessorDslPackage.Literals.ARTIFACTS__POJO_PACKAGES), val);
                if (entity == null) {
                    error("Cannot find entity : " + val + "[" + COLUMN_USAGE_EXTENDED + "]",
                            ProcessorDslPackage.Literals.META_STATEMENT__FILTERS, index);
                }
            } else if (COLUMN_USAGE.equals(key)) {
                PojoDefinition pojo = Utils.findPojo(qualifiedNameConverter, artifacts,
                        scopeProvider.getScope(artifacts, ProcessorDslPackage.Literals.ARTIFACTS__POJOS), val);
                if (pojo == null) {
                    error("Cannot find pojo : " + val + "[" + COLUMN_USAGE + "]",
                            ProcessorDslPackage.Literals.META_STATEMENT__FILTERS, index);
                }
            } else if (CONSTANT_USAGE_EXTENDED.equals(key)) {
                PojoEntity entity = Utils.findEntity(qualifiedNameConverter, artifacts,
                        scopeProvider.getScope(artifacts, ProcessorDslPackage.Literals.ARTIFACTS__POJO_PACKAGES), val);
                if (entity == null) {
                    error("Cannot find entity : " + val + "[" + CONSTANT_USAGE_EXTENDED + "]",
                            ProcessorDslPackage.Literals.META_STATEMENT__FILTERS, index);
                }
            } else if (CONSTANT_USAGE.equals(key)) {
                PojoDefinition pojo = Utils.findPojo(qualifiedNameConverter, artifacts,
                        scopeProvider.getScope(artifacts, ProcessorDslPackage.Literals.ARTIFACTS__POJOS), val);
                if (pojo == null) {
                    error("Cannot find pojo : " + val + "[" + CONSTANT_USAGE + "]",
                            ProcessorDslPackage.Literals.META_STATEMENT__FILTERS, index);
                }
            } else if (TABLE_USAGE.equals(key)) {
                int ix1 = val.indexOf('=');
                if (ix1 >= 0)
                    val = val.substring(0, ix1);
                TableDefinition table = Utils.findTable(qualifiedNameConverter, artifacts,
                        scopeProvider.getScope(artifacts, ProcessorDslPackage.Literals.ARTIFACTS__TABLES), val);
                if (table == null) {
                    error("Cannot find table : " + val + "[" + TABLE_USAGE + "]",
                            ProcessorDslPackage.Literals.META_STATEMENT__FILTERS, index);
                }
            }
            index++;
        }
    }

    @Check
    public void checkMappingRule(MappingRule rule) {
        Artifacts artifacts = EcoreUtil2.getContainerOfType(rule, Artifacts.class);

        if (rule.getFilters() == null || rule.getFilters().isEmpty())
            return;

        int index = 0;
        for (String filter : rule.getFilters()) {
            int ix = filter.indexOf('=');
            if (ix <= 0)
                continue;
            String key = filter.substring(0, ix);
            String val = filter.substring(ix + 1);
            if (MAPPING_USAGE_EXTENDED.equals(key)) {
                PojoEntity entity = Utils.findEntity(qualifiedNameConverter, artifacts,
                        scopeProvider.getScope(artifacts, ProcessorDslPackage.Literals.ARTIFACTS__POJO_PACKAGES), val);
                if (entity == null) {
                    error("Cannot find entity : " + val + "[" + MAPPING_USAGE_EXTENDED + "]",
                            ProcessorDslPackage.Literals.MAPPING_RULE__FILTERS, index);
                }
            } else if (MAPPING_USAGE.equals(key)) {
                PojoDefinition pojo = Utils.findPojo(qualifiedNameConverter, artifacts,
                        scopeProvider.getScope(artifacts, ProcessorDslPackage.Literals.ARTIFACTS__POJOS), val);
                if (pojo == null) {
                    error("Cannot find pojo : " + val + "[" + MAPPING_USAGE + "]",
                            ProcessorDslPackage.Literals.MAPPING_RULE__FILTERS, index);
                }
            }
            index++;
        }
    }

    protected boolean isNumber(String param) {
        if (param == null)
            return false;
        for (int i = param.length() - 1; i >= 0; i--) {
            if (!Character.isDigit(param.charAt(i)))
                return false;
        }
        return true;
    }

    protected boolean isPrimitive(Class<?> clazz) {
        if (clazz == null)
            return true;
        if (clazz.isPrimitive())
            return true;
        if (clazz == String.class)
            return true;
        if (clazz == java.util.Date.class)
            return true;
        if (clazz == java.sql.Date.class)
            return true;
        if (clazz == java.sql.Time.class)
            return true;
        if (clazz == java.sql.Timestamp.class)
            return true;
        if (clazz == java.sql.Blob.class)
            return true;
        if (clazz == java.sql.Clob.class)
            return true;
        if (clazz == java.math.BigDecimal.class)
            return true;
        if (clazz == java.math.BigInteger.class)
            return true;
        return false;
    }

    /**
     * Validation property of class
     * 
     * @param className
     * @param property
     * @return validation result
     */
    protected ValidationResult checkClassProperty(String className, String property) {
        if (property == null || isNumber(property) || pojoResolverFactory.getPojoResolver() == null)
            return ValidationResult.OK;
        if (className == null)
            return ValidationResult.ERROR;
        PropertyDescriptor[] descriptors = pojoResolverFactory.getPojoResolver().getPropertyDescriptors(className);
        if (descriptors == null) {
            return ValidationResult.WARNING;
        }
        String checkProperty = property;
        int pos1 = checkProperty.indexOf('=');
        if (pos1 > 0) {
            int pos2 = checkProperty.indexOf('.', pos1);
            if (pos2 > pos1)
                checkProperty = checkProperty.substring(0, pos1) + checkProperty.substring(pos2);
        }
        String innerProperty = null;
        pos1 = checkProperty.indexOf('.');
        if (pos1 > 0) {
            innerProperty = checkProperty.substring(pos1 + 1);
            checkProperty = checkProperty.substring(0, pos1);
        }
        PropertyDescriptor innerDesriptor = null;
        for (PropertyDescriptor descriptor : descriptors) {
            if (descriptor.getName().equals(checkProperty)) {
                innerDesriptor = descriptor;
                break;
            }
        }
        if (innerDesriptor == null) {
            Class<?> clazz = pojoResolverFactory.getPojoResolver().loadClass(className);
            if (clazz != null && Modifier.isAbstract(clazz.getModifiers()))
                return ValidationResult.WARNING;
            return ValidationResult.ERROR;
        }
        if (innerProperty != null) {
            Class<?> innerClass = innerDesriptor.getPropertyType();
            if (innerClass.isArray()) {
                ParameterizedType type = (ParameterizedType) innerDesriptor.getReadMethod().getGenericReturnType();
                if (type.getActualTypeArguments() == null || type.getActualTypeArguments().length == 0)
                    return ValidationResult.WARNING;
                innerClass = (Class<?>) type.getActualTypeArguments()[0];
                if (isPrimitive(innerClass))
                    return ValidationResult.ERROR;
                return checkClassProperty(innerClass.getName(), innerProperty);
            } else if (Collection.class.isAssignableFrom(innerClass)) {
                ParameterizedType type = (ParameterizedType) innerDesriptor.getReadMethod().getGenericReturnType();
                if (type.getActualTypeArguments() == null || type.getActualTypeArguments().length == 0)
                    return ValidationResult.WARNING;
                innerClass = (Class<?>) type.getActualTypeArguments()[0];
                if (isPrimitive(innerClass))
                    return ValidationResult.ERROR;
                return checkClassProperty(innerClass.getName(), innerProperty);
            } else {
                if (isPrimitive(innerClass))
                    return ValidationResult.ERROR;
                return checkClassProperty(innerClass.getName(), innerProperty);
            }
        }
        return ValidationResult.OK;
    }

    protected ValidationResult checkEntityProperty(PojoEntity entity, String property) {
        if (property == null || isNumber(property))
            return ValidationResult.OK;
        String checkProperty = property;
        int pos1 = checkProperty.indexOf('=');
        if (pos1 > 0) {
            int pos2 = checkProperty.indexOf('.', pos1);
            if (pos2 > pos1)
                checkProperty = checkProperty.substring(0, pos1) + checkProperty.substring(pos2);
        }
        String innerProperty = null;
        pos1 = checkProperty.indexOf('.');
        if (pos1 > 0) {
            innerProperty = checkProperty.substring(pos1 + 1);
            checkProperty = checkProperty.substring(0, pos1);
        }

        for (PojoProperty pojoProperty : entity.getFeatures()) {
            if (pojoProperty.getName().equals(checkProperty)) {
                if (innerProperty == null)
                    return ValidationResult.OK;
                if (pojoProperty.getRef() != null)
                    return checkEntityProperty(pojoProperty.getRef(), innerProperty);
                if (pojoProperty.getGref() != null)
                    return checkEntityProperty(pojoProperty.getGref(), innerProperty);
                return ValidationResult.ERROR;
            }
        }
        PojoEntity superType = Utils.getSuperType(entity);
        if (superType != null) {
            ValidationResult result = checkEntityProperty(superType, property);
            if (result == ValidationResult.WARNING || result == ValidationResult.OK)
                return result;
        }
        if (Utils.isAbstract(entity))
            return ValidationResult.WARNING;
        else
            return ValidationResult.ERROR;
    }

    @Check
    public void checkUniqueProperty(Property property) {
        Artifacts artifacts;
        EObject object = EcoreUtil.getRootContainer(property);
        if (!(object instanceof Artifacts))
            return;
        artifacts = (Artifacts) object;
        for (Property prop : artifacts.getProperties()) {
            if (prop == null || prop == property)
                continue;
            if (prop.getName().equals(property.getName()) && !prop.getName().startsWith("pojogen")
                    && !prop.getName().startsWith("database") && !prop.getName().startsWith("metagen")) {
                error("Duplicate name : " + property.getName(), ProcessorDslPackage.Literals.PROPERTY__NAME);
                return;
            }
        }
    }

    @Check
    public void checkTableDefinition(TableDefinition tableDefinition) {
        Artifacts artifacts;
        EObject object = EcoreUtil.getRootContainer(tableDefinition);
        if (!(object instanceof Artifacts))
            return;
        artifacts = (Artifacts) object;
        for (TableDefinition table : artifacts.getTables()) {
            if (table == null || table == tableDefinition)
                continue;
            if (tableDefinition.getName().equals(table.getName())) {
                error("Duplicate name : " + tableDefinition.getName() + "[table]",
                        ProcessorDslPackage.Literals.TABLE_DEFINITION__NAME);
                return;
            }
        }
        if (isResolveDb(tableDefinition) && !dbResolver.checkTable(tableDefinition, tableDefinition.getTable())) {
            error("Cannot find table in DB : " + tableDefinition.getTable(),
                    ProcessorDslPackage.Literals.TABLE_DEFINITION__TABLE);
        }
    }

    @Check
    public void checkTableUsage(TableUsage tableUsage) {
        Artifacts artifacts;
        EObject object = EcoreUtil.getRootContainer(tableUsage);
        if (!(object instanceof Artifacts))
            return;
        artifacts = (Artifacts) object;
        for (TableUsage usage : artifacts.getTableUsages()) {
            if (usage == null || usage == tableUsage)
                continue;
            if (tableUsage.getStatement().getName().equals(usage.getStatement().getName())) {
                if (tableUsage.getTable().getName().equals(usage.getTable().getName())) {
                    if (tableUsage.getPrefix() == null && usage.getPrefix() == null) {
                        error("Duplicate name : " + tableUsage.getStatement().getName() + "["
                                + tableUsage.getTable().getName() + "][dbcol]",
                                ProcessorDslPackage.Literals.TABLE_USAGE__TABLE);
                        return;
                    }
                    if (tableUsage.getPrefix() != null && tableUsage.getPrefix().equals(usage.getPrefix())) {
                        error("Duplicate name : " + tableUsage.getStatement().getName() + "["
                                + tableUsage.getTable().getName() + ":" + tableUsage.getPrefix() + "][dbcol]",
                                ProcessorDslPackage.Literals.TABLE_USAGE__TABLE);
                        return;
                    }
                }
                if (tableUsage.getPrefix() != null && tableUsage.getPrefix().equals(usage.getPrefix())) {
                    error("Duplicate name : " + tableUsage.getStatement().getName() + "[" + tableUsage.getPrefix()
                            + "][dbcol]", ProcessorDslPackage.Literals.TABLE_USAGE__PREFIX);
                    return;
                }
            }
        }
    }

    @Check
    public void checkDatabaseColumn(DatabaseColumn databaseColumn) {
        if (!isResolveDb(databaseColumn))
            return;
        String prefix = databaseColumn.getName();
        String columnName = null;
        int pos = prefix.indexOf('.');
        if (pos > 0) {
            prefix = databaseColumn.getName().substring(0, pos);
            columnName = databaseColumn.getName().substring(pos + 1);
        } else {
            prefix = null;
            columnName = databaseColumn.getName();
        }
        MetaStatement statement = EcoreUtil2.getContainerOfType(databaseColumn, MetaStatement.class);
        Artifacts artifacts = EcoreUtil2.getContainerOfType(statement, Artifacts.class);

        String val = Utils.getTokenFromFilter(statement, TABLE_USAGE, prefix);
        TableDefinition tableDefinition = (val != null) ? Utils.findTable(qualifiedNameConverter, artifacts,
                scopeProvider.getScope(artifacts, ProcessorDslPackage.Literals.ARTIFACTS__TABLES), val) : null;
        if (tableDefinition == null) {
            tableDefinition = getTableDefinition(artifacts, statement, null, prefix);
        }
        String tableName = tableDefinition != null ? tableDefinition.getTable() : null;
        if (tableName == null || !dbResolver.checkColumn(databaseColumn, tableName, columnName)) {
            error("Cannot find column in DB : " + databaseColumn.getName() + "[" + tableName + "]",
                    ProcessorDslPackage.Literals.DATABASE_COLUMN__NAME);
        }
    }

    @Check
    public void checkDatabaseTable(DatabaseTable databaseTable) {
        if (!isResolveDb(databaseTable))
            return;
        String tableName = databaseTable.getName();
        MetaStatement statement = EcoreUtil2.getContainerOfType(databaseTable, MetaStatement.class);
        Artifacts artifacts = EcoreUtil2.getContainerOfType(statement, Artifacts.class);

        TableDefinition tableDefinition = null;
        List<String> vals = Utils.getTokensFromFilter(statement, TABLE_USAGE);
        for (String val : vals) {
            tableDefinition = Utils.findTable(qualifiedNameConverter, artifacts,
                    scopeProvider.getScope(artifacts, ProcessorDslPackage.Literals.ARTIFACTS__TABLES), val);
            if (tableDefinition != null)
                break;
        }
        if (tableDefinition == null) {
            tableDefinition = getTableDefinition(artifacts, statement, tableName, null);
        }
        if (tableDefinition == null || !dbResolver.checkTable(databaseTable, tableName)) {
            error("Cannot find table in DB : " + tableName, ProcessorDslPackage.Literals.DATABASE_TABLE__NAME);
        }
    }

    protected boolean isResolvePojo(EObject model) {
        if (pojoResolverFactory.getPojoResolver() == null
                || !pojoResolverFactory.getPojoResolver().isResolvePojo(model))
            return false;
        return true;

    }

    protected boolean isResolveDb(EObject model) {
        return dbResolver.isResolveDb(model);
    }

    protected TableDefinition getTableDefinition(Artifacts artifacts, MetaStatement statement, String tableName,
            String prefix) {
        TableUsage usage = null;
        IScope scope = scopeProvider.getScope(artifacts, ProcessorDslPackage.Literals.ARTIFACTS__TABLE_USAGES);
        Iterable<IEObjectDescription> iterable = scope.getAllElements();
        for (Iterator<IEObjectDescription> iter = iterable.iterator(); iter.hasNext();) {
            IEObjectDescription description = iter.next();
            if (ProcessorDslPackage.Literals.TABLE_USAGE.getName().equals(description.getEClass().getName())) {
                TableUsage tableUsage = (TableUsage) artifacts.eResource().getResourceSet()
                        .getEObject(description.getEObjectURI(), true);
                if (tableUsage.getStatement().getName().equals(statement.getName())) {
                    if (tableName != null && tableUsage.getTable() != null
                            && tableName.equals(tableUsage.getTable().getTable())) {
                        usage = tableUsage;
                        break;
                    }
                    if (prefix == null && tableUsage.getPrefix() == null) {
                        usage = tableUsage;
                        break;
                    }
                    if (prefix != null && prefix.equals(tableUsage.getPrefix())) {
                        usage = tableUsage;
                        break;
                    }
                }
            }
        }
        if (usage != null && usage.getTable() != null && usage.getTable().getName() != null) {
            scope = scopeProvider.getScope(artifacts, ProcessorDslPackage.Literals.ARTIFACTS__TABLES);
            iterable = scope.getAllElements();
            for (Iterator<IEObjectDescription> iter = iterable.iterator(); iter.hasNext();) {
                IEObjectDescription description = iter.next();
                if (ProcessorDslPackage.Literals.TABLE_DEFINITION.getName().equals(description.getEClass().getName())) {
                    TableDefinition tableDefinition = (TableDefinition) artifacts.eResource().getResourceSet()
                            .getEObject(description.getEObjectURI(), true);
                    if (usage.getTable().getName().equals(tableDefinition.getName())) {
                        return tableDefinition;
                    }
                }
            }
        }
        return null;
    }

    @Check
    public void checkUniquePojoEntity(PojoEntity pojoEntity) {
        Artifacts artifacts;
        EObject object = EcoreUtil.getRootContainer(pojoEntity);
        if (!(object instanceof Artifacts))
            return;
        artifacts = (Artifacts) object;
        for (PackageDeclaration pkg : artifacts.getPojoPackages()) {
            if (pkg == null)
                continue;
            for (AbstractPojoEntity entity : pkg.getElements()) {
                if (entity == null || !(entity instanceof PojoEntity))
                    continue;
                PojoEntity pentity = (PojoEntity) entity;
                if (pentity == pojoEntity)
                    continue;
                if (pojoEntity.getName().equals(pentity.getName())) {
                    error("Duplicate name : " + pojoEntity.getName(), ProcessorDslPackage.Literals.POJO_ENTITY__NAME);
                    return;
                }
            }
        }
    }

    @Check
    public void checkUniquePojoProperty(PojoProperty pojoProperty) {
        PojoEntity entity = EcoreUtil2.getContainerOfType(pojoProperty, PojoEntity.class);
        for (PojoProperty property : entity.getFeatures()) {
            if (property == null || property == pojoProperty)
                continue;
            if (pojoProperty.getName().equals(property.getName())) {
                error("Duplicate name : " + pojoProperty.getName(), ProcessorDslPackage.Literals.POJO_PROPERTY__NAME);
                return;
            }
        }
    }

    @Check
    public void checkUniqueColumnUsageExt(ColumnUsageExt columnUsage) {
        Artifacts artifacts;
        EObject object = EcoreUtil.getRootContainer(columnUsage);
        if (!(object instanceof Artifacts))
            return;
        artifacts = (Artifacts) object;
        for (PojoUsage usage : artifacts.getUsages()) {
            if (usage == null || usage == columnUsage || !(usage instanceof ColumnUsage))
                continue;
            ColumnUsage column = (ColumnUsage) usage;
            if (column.getStatement() == null)
                continue;
            if (columnUsage.getStatement().getName().equals(column.getStatement().getName())) {
                error("Duplicate name : " + columnUsage.getStatement().getName() + "[col]",
                        ProcessorDslPackage.Literals.COLUMN_USAGE_EXT__STATEMENT);
                return;
            }
        }
        for (PojoUsageExt usage : artifacts.getUsagesExt()) {
            if (usage == null || usage == columnUsage || !(usage instanceof ColumnUsageExt))
                continue;
            ColumnUsageExt column = (ColumnUsageExt) usage;
            if (column.getStatement() == null)
                continue;
            if (columnUsage.getStatement().getName().equals(column.getStatement().getName())) {
                error("Duplicate name : " + columnUsage.getStatement().getName() + "[col]",
                        ProcessorDslPackage.Literals.COLUMN_USAGE_EXT__STATEMENT);
                return;
            }
        }
    }

    @Check
    public void checkUniqueIdentifierUsageExt(IdentifierUsageExt identifierUsage) {
        Artifacts artifacts;
        EObject object = EcoreUtil.getRootContainer(identifierUsage);
        if (!(object instanceof Artifacts))
            return;
        artifacts = (Artifacts) object;
        for (PojoUsage usage : artifacts.getUsages()) {
            if (usage == null || usage == identifierUsage || !(usage instanceof IdentifierUsage))
                continue;
            IdentifierUsage ident = (IdentifierUsage) usage;
            if (ident.getStatement() == null)
                continue;
            if (identifierUsage.getStatement().getName().equals(ident.getStatement().getName())) {
                error("Duplicate name : " + identifierUsage.getStatement().getName() + "[ident]",
                        ProcessorDslPackage.Literals.IDENTIFIER_USAGE_EXT__STATEMENT);
                return;
            }
        }
        for (PojoUsageExt usage : artifacts.getUsagesExt()) {
            if (usage == null || usage == identifierUsage || !(usage instanceof IdentifierUsageExt))
                continue;
            IdentifierUsageExt ident = (IdentifierUsageExt) usage;
            if (ident.getStatement() == null)
                continue;
            if (identifierUsage.getStatement().getName().equals(ident.getStatement().getName())) {
                error("Duplicate name : " + identifierUsage.getStatement().getName() + "[ident]",
                        ProcessorDslPackage.Literals.IDENTIFIER_USAGE_EXT__STATEMENT);
                return;
            }
        }
    }

    @Check
    public void checkUniqueConstantUsageExt(ConstantUsageExt constantUsage) {
        Artifacts artifacts;
        EObject object = EcoreUtil.getRootContainer(constantUsage);
        if (!(object instanceof Artifacts))
            return;
        artifacts = (Artifacts) object;
        for (PojoUsage usage : artifacts.getUsages()) {
            if (usage == null || usage == constantUsage || !(usage instanceof ConstantUsage))
                continue;
            ConstantUsage constant = (ConstantUsage) usage;
            if (constant.getStatement() == null)
                continue;
            if (constantUsage.getStatement().getName().equals(constant.getStatement().getName())) {
                error("Duplicate name : " + constantUsage.getStatement().getName() + "[const]",
                        ProcessorDslPackage.Literals.CONSTANT_USAGE__STATEMENT);
                return;
            }
        }
        for (PojoUsageExt usage : artifacts.getUsagesExt()) {
            if (usage == null || usage == constantUsage || !(usage instanceof ConstantUsageExt))
                continue;
            ConstantUsageExt constant = (ConstantUsageExt) usage;
            if (constant.getStatement() == null)
                continue;
            if (constantUsage.getStatement().getName().equals(constant.getStatement().getName())) {
                error("Duplicate name : " + constantUsage.getStatement().getName() + "[const]",
                        ProcessorDslPackage.Literals.CONSTANT_USAGE_EXT__STATEMENT);
                return;
            }
        }
    }

    @Check
    public void checkUniqueMappingUsageExt(MappingUsageExt mappingUsage) {
        Artifacts artifacts;
        EObject object = EcoreUtil.getRootContainer(mappingUsage);
        if (!(object instanceof Artifacts))
            return;
        artifacts = (Artifacts) object;
        for (PojoUsage usage : artifacts.getUsages()) {
            if (usage == null || usage == mappingUsage || !(usage instanceof MappingUsage))
                continue;
            MappingUsage mapping = (MappingUsage) usage;
            if (mapping.getStatement() == null)
                continue;
            if (mappingUsage.getStatement().getName().equals(mapping.getStatement().getName())) {
                error("Duplicate name : " + mappingUsage.getStatement().getName() + "[col]",
                        ProcessorDslPackage.Literals.MAPPING_USAGE__STATEMENT);
                return;
            }
        }
        for (PojoUsageExt usage : artifacts.getUsagesExt()) {
            if (usage == null || usage == mappingUsage || !(usage instanceof MappingUsageExt))
                continue;
            MappingUsageExt mapping = (MappingUsageExt) usage;
            if (mapping.getStatement() == null)
                continue;
            if (mappingUsage.getStatement().getName().equals(mapping.getStatement().getName())) {
                error("Duplicate name : " + mappingUsage.getStatement().getName() + "[col]",
                        ProcessorDslPackage.Literals.MAPPING_USAGE_EXT__STATEMENT);
                return;
            }
        }
    }
}
