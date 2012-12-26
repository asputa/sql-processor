package org.sqlproc.dsl.ui.templates;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.scoping.IScopeProvider;
import org.sqlproc.dsl.processorDsl.Artifacts;
import org.sqlproc.dsl.property.ModelProperty;
import org.sqlproc.dsl.property.PojoAttribute;

public class TableDaoConverter extends TableMetaConverter {

    private boolean debug = false;

    protected Set<String> finalDaos;
    protected Set<String> daoIgnoreTables = new HashSet<String>();
    protected Set<String> daoOnlyTables = new HashSet<String>();
    protected Map<String, String> daoImplementsInterfaces = new HashMap<String, String>();
    protected JvmType daoControlClass;
    protected Map<String, JvmType> daoToImplements = new HashMap<String, JvmType>();
    protected JvmType daoToExtends = null;

    public TableDaoConverter() {
        super();
    }

    public TableDaoConverter(ModelProperty modelProperty, Artifacts artifacts, IScopeProvider scopeProvider,
            Set<String> finalDaos) {
        super(modelProperty, artifacts, null);

        this.finalDaos = finalDaos;

        Set<String> daoIgnoreTables = modelProperty.getDaoIgnoreTables(artifacts);
        if (daoIgnoreTables != null) {
            this.daoIgnoreTables.addAll(daoIgnoreTables);
        }
        Set<String> daoOnlyTables = modelProperty.getDaoOnlyTables(artifacts);
        if (daoOnlyTables != null) {
            this.daoOnlyTables.addAll(daoOnlyTables);
        }
        Map<String, String> daoImplementsInterfaces = modelProperty.getDaoImplementsInterfaces(artifacts);
        if (daoImplementsInterfaces != null) {
            this.daoImplementsInterfaces.putAll(daoImplementsInterfaces);
        }
        this.daoControlClass = modelProperty.getDaoControlParameter(artifacts);
        Map<String, JvmType> daoToImplements = modelProperty.getDaoToImplements(artifacts);
        if (daoToImplements != null) {
            this.daoToImplements.putAll(daoToImplements);
        }
        this.daoToExtends = modelProperty.getDaoToExtends(artifacts);

        if (debug) {
            System.out.println("daoIgnoreTables " + this.daoIgnoreTables);
            System.out.println("daoOnlyTables " + this.daoOnlyTables);
            System.out.println("daoImplementsInterfaces " + this.daoImplementsInterfaces);
            System.out.println("daoControlClass " + this.daoControlClass);
        }
    }

    public String getDaoDefinitions() {
        try {
            if (debug) {
                System.out.println("pojos " + this.pojos);
                System.out.println("pojoExtends " + this.pojoExtends);
                System.out.println("pojoInheritanceDiscriminator " + this.pojoInheritanceDiscriminator);
                System.out.println("pojoInheritanceSimple " + this.pojoInheritanceSimple);
                System.out.println("pojoDiscriminators " + this.pojoDiscriminators);
            }

            StringBuilder buffer = new StringBuilder();
            boolean isSerializable = false;
            if (!daoToImplements.isEmpty()) {
                for (JvmType type : daoToImplements.values()) {
                    if (type.getIdentifier().endsWith("Serializable")) {
                        isSerializable = true;
                        continue;
                    }
                    buffer.append("\n  implements ").append(type.getIdentifier());
                }
            }
            if (daoToExtends != null) {
                buffer.append("\n  extends ").append(daoToExtends.getIdentifier());
            }
            if (!daoToImplements.isEmpty() || daoToExtends != null) {
                buffer.append("\n");
            }
            for (String pojo : pojos.keySet()) {
                // System.out.println("QQQQQ " + pojo);
                if (!daoOnlyTables.isEmpty() && !daoOnlyTables.contains(pojo))
                    continue;
                if (daoIgnoreTables.contains(pojo))
                    continue;
                if (finalDaos.contains(tableToCamelCase(pojo)))
                    continue;
                String pojoName = tableNames.get(pojo);
                if (pojoName == null)
                    pojoName = pojo;
                if (pojoInheritanceDiscriminator.containsKey(pojo) || pojoInheritanceSimple.containsKey(pojo)) {
                    if (!notAbstractTables.contains(pojo))
                        continue;
                }
                buffer.append("\n  ");
                buffer.append("dao ");
                buffer.append(tableToCamelCase(pojoName)).append("Dao");
                buffer.append(" :: ").append(tableToCamelCase(pojoName));
                if (daoImplementsInterfaces.containsKey(pojo))
                    buffer.append(" implementation package ").append(daoImplementsInterfaces.get(pojo));
                if (isSerializable)
                    buffer.append(" serializable 1 ");
                buffer.append(" {");
                Map<String, String> toInit = new LinkedHashMap<String, String>();
                toInits(pojo, toInit);
                for (Entry<String, String> entry : toInit.entrySet()) {
                    buffer.append("\n    ").append(entry.getKey()).append(" :::");
                    // pojoExtends {BANK_ACCOUNT=BILLING_DETAILS, MOVIE=MEDIA, CREDIT_CARD=BILLING_DETAILS, BOOK=MEDIA}
                    // pojoInheritanceDiscriminator {BILLING_DETAILS=[BANK_ACCOUNT, CREDIT_CARD]}
                    // pojoInheritanceSimple {MEDIA=[MOVIE, BOOK]}
                    // pojoDiscriminators {BANK_ACCOUNT=BA, CREDIT_CARD=CC}
                    if (pojoInheritanceSimple.containsKey(entry.getValue())) {
                        for (String pojo2 : pojoInheritanceSimple.get(entry.getValue())) {
                            buffer.append(" ").append(columnToCamelCase(pojo2));
                            String pojoName2 = tableNames.get(pojo2);
                            if (pojoName2 == null)
                                pojoName2 = pojo2;
                            buffer.append(" ::").append(tableToCamelCase(pojoName2));
                        }
                    } else {
                        for (String pojo2 : pojoInheritanceDiscriminator.get(entry.getValue())) {
                            buffer.append(" ").append(pojoDiscriminators.get(pojo2));
                            String pojoName2 = tableNames.get(pojo2);
                            if (pojoName2 == null)
                                pojoName2 = pojo2;
                            buffer.append(" ::").append(tableToCamelCase(pojoName2));
                        }
                    }
                }
                buffer.append("\n  }\n");
            }
            return buffer.toString();
        } catch (RuntimeException ex) {
            Writer writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            ex.printStackTrace(printWriter);
            String s = writer.toString();
            return s;
        }
    }

    protected void toInits(String pojo, Map<String, String> toInit) {
        for (Map.Entry<String, PojoAttribute> pentry : pojos.get(pojo).entrySet()) {
            if (ignoreColumns.containsKey(pojo) && ignoreColumns.get(pojo).contains(pentry.getKey()))
                continue;
            PojoAttribute attribute = pentry.getValue();
            String name = (columnNames.containsKey(pojo)) ? columnNames.get(pojo).get(pentry.getKey()) : null;
            if (name == null)
                name = attribute.getName();
            else
                name = columnToCamelCase(name);
            if (attribute.toInit()) {
                if (attribute.getRef() != null) {
                    if (pojoInheritanceDiscriminator.containsKey(attribute.getRef())
                            || pojoInheritanceSimple.containsKey(attribute.getRef())) {
                        toInit.put(name, attribute.getRef());
                        // System.out.println("AAAAAAAAA " + " " + pojo + " " + attribute.getRef() + " " + name);
                    }
                }
            }
        }
        if (pojoExtends.containsKey(pojo)) {
            toInits(pojoExtends.get(pojo), toInit);
        }
    }
}