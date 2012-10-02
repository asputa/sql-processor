/**
 */
package org.sqlproc.dsl.processorDsl;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Pojo Property Modifier</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.sqlproc.dsl.processorDsl.PojoPropertyModifier#isRequired <em>Required</em>}</li>
 *   <li>{@link org.sqlproc.dsl.processorDsl.PojoPropertyModifier#isDiscriminator <em>Discriminator</em>}</li>
 *   <li>{@link org.sqlproc.dsl.processorDsl.PojoPropertyModifier#isPrimaryKey <em>Primary Key</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.sqlproc.dsl.processorDsl.ProcessorDslPackage#getPojoPropertyModifier()
 * @model
 * @generated
 */
public interface PojoPropertyModifier extends EObject
{
  /**
   * Returns the value of the '<em><b>Required</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Required</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Required</em>' attribute.
   * @see #setRequired(boolean)
   * @see org.sqlproc.dsl.processorDsl.ProcessorDslPackage#getPojoPropertyModifier_Required()
   * @model
   * @generated
   */
  boolean isRequired();

  /**
   * Sets the value of the '{@link org.sqlproc.dsl.processorDsl.PojoPropertyModifier#isRequired <em>Required</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Required</em>' attribute.
   * @see #isRequired()
   * @generated
   */
  void setRequired(boolean value);

  /**
   * Returns the value of the '<em><b>Discriminator</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Discriminator</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Discriminator</em>' attribute.
   * @see #setDiscriminator(boolean)
   * @see org.sqlproc.dsl.processorDsl.ProcessorDslPackage#getPojoPropertyModifier_Discriminator()
   * @model
   * @generated
   */
  boolean isDiscriminator();

  /**
   * Sets the value of the '{@link org.sqlproc.dsl.processorDsl.PojoPropertyModifier#isDiscriminator <em>Discriminator</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Discriminator</em>' attribute.
   * @see #isDiscriminator()
   * @generated
   */
  void setDiscriminator(boolean value);

  /**
   * Returns the value of the '<em><b>Primary Key</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Primary Key</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Primary Key</em>' attribute.
   * @see #setPrimaryKey(boolean)
   * @see org.sqlproc.dsl.processorDsl.ProcessorDslPackage#getPojoPropertyModifier_PrimaryKey()
   * @model
   * @generated
   */
  boolean isPrimaryKey();

  /**
   * Sets the value of the '{@link org.sqlproc.dsl.processorDsl.PojoPropertyModifier#isPrimaryKey <em>Primary Key</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Primary Key</em>' attribute.
   * @see #isPrimaryKey()
   * @generated
   */
  void setPrimaryKey(boolean value);

} // PojoPropertyModifier