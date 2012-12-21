package org.sqlproc.sample.simple.model;
  
import java.util.List;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.beanutils.MethodUtils;

public class Subscriber implements Serializable {
  
  private static final long serialVersionUID = 1L;
	
  public Subscriber() {
  }
  
  public Subscriber(Library library, String name) {
    this.library = library;
    this.name = name;
  }
  
  private Long id;
    
  public Long getId() {
    return id;
  }
    
  public void setId(Long id) {
    this.id = id;
  }
    
  public Subscriber _setId(Long id) {
    this.id = id;
    return this;
  }
  
  private Library library;
    
  public Library getLibrary() {
    return library;
  }
    
  public void setLibrary(Library library) {
    this.library = library;
  }
    
  public Subscriber _setLibrary(Library library) {
    this.library = library;
    return this;
  }
  
  private Contact contact;
    
  public Contact getContact() {
    return contact;
  }
    
  public void setContact(Contact contact) {
    this.contact = contact;
  }
    
  public Subscriber _setContact(Contact contact) {
    this.contact = contact;
    return this;
  }
  
  private String name;
    
  public String getName() {
    return name;
  }
    
  public void setName(String name) {
    this.name = name;
  }
    
  public Subscriber _setName(String name) {
    this.name = name;
    return this;
  }
  
  private List<BillingDetails> billingDetails = new ArrayList<BillingDetails>();
    
  public List<BillingDetails> getBillingDetails() {
    return billingDetails;
  }
    
  public void setBillingDetails(List<BillingDetails> billingDetails) {
    this.billingDetails = billingDetails;
  }
    
  public Subscriber _setBillingDetails(List<BillingDetails> billingDetails) {
    this.billingDetails = billingDetails;
    return this;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Subscriber other = (Subscriber) obj;
    if (id != other.id)
      return false;
    if (library != other.library)
      return false;
    return true;
  }  
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (id ^ (id >>> 32));
    result = prime * result + (int) (library ^ (library >>> 32));
    return result;
  }  
  
  private Set<String> initAssociations = new HashSet<String>();
  
  public enum Association {
    library, billingDetails, contact
  }
  
  public void setInit(Association... associations) {
    if (associations == null)
      throw new IllegalArgumentException();
    for (Association association : associations)
      initAssociations.add(association.name());
  }
  
  public void clearInit(Association... associations) {
    if (associations == null)
      throw new IllegalArgumentException();
    for (Association association : associations)
      initAssociations.remove(association.name());
  }
  
  public Boolean toInit(String attrName) {
    if (attrName == null)
      throw new IllegalArgumentException();
    return initAssociations.contains(attrName);
  }
  
  public Boolean toInit(Association association) {
    if (association == null)
      throw new IllegalArgumentException();
    return initAssociations.contains(association.name());
  }
  
  public void clearAllInit() {
    initAssociations = new HashSet<String>();
  }
  
  private Set<String> nullValues = new HashSet<String>();
  
  public enum Attribute {
    contact
  }
  
  public void setNull(Attribute... attributes) {
    if (attributes == null)
      throw new IllegalArgumentException();
    for (Attribute attribute : attributes)
      nullValues.add(attribute.name());
  }
  
  public void clearNull(Attribute... attributes) {
    if (attributes == null)
      throw new IllegalArgumentException();
    for (Attribute attribute : attributes)
      nullValues.remove(attribute.name());
  }
  
  public Boolean isNull(String attrName) {
    if (attrName == null)
      throw new IllegalArgumentException();
    return nullValues.contains(attrName);
  }
  
  public Boolean isNull(Attribute attribute) {
    if (attribute == null)
      throw new IllegalArgumentException();
    return nullValues.contains(attribute.name());
  }
  
  public Boolean isDef(String attrName) {
    if (attrName == null)
      throw new IllegalArgumentException();
    if (nullValues.contains(attrName))
      return true;
    try {
      Object result = MethodUtils.invokeMethod(this, "get" + attrName.substring(0, 1).toUpperCase() + attrName.substring(1, attrName.length()), null);
      return (result != null) ? true : false;
    } catch (NoSuchMethodException e) {
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    }
    try {
      Object result = MethodUtils.invokeMethod(this, "is" + attrName.substring(0, 1).toUpperCase() + attrName.substring(1, attrName.length()), null);
      return (result != null) ? true : false;
    } catch (NoSuchMethodException e) {
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    }
    return false;
  }
  
  public void clearAllNull() {
    nullValues = new HashSet<String>();
  }
  
  @Override
  public String toString() {
    return "Subscriber [id=" + id + ", name=" + name + ", library=" + library + "]";
  }
  
  public String toStringFull() {
    return "Subscriber [id=" + id + ", name=" + name + ", library=" + library + ", contact=" + contact + "]";
  }
}
