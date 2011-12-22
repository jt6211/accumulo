/**
 * Autogenerated by Thrift
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 */
package org.apache.accumulo.core.data.thrift;

import org.apache.thrift.*;
import org.apache.thrift.meta_data.*;
import org.apache.thrift.protocol.*;

@SuppressWarnings("serial")
public class UpdateErrors implements TBase<UpdateErrors,UpdateErrors._Fields>, java.io.Serializable, Cloneable {
  private static final TStruct STRUCT_DESC = new TStruct("UpdateErrors");
  
  private static final TField FAILED_EXTENTS_FIELD_DESC = new TField("failedExtents", TType.MAP, (short) 1);
  private static final TField VIOLATION_SUMMARIES_FIELD_DESC = new TField("violationSummaries", TType.LIST, (short) 2);
  private static final TField AUTHORIZATION_FAILURES_FIELD_DESC = new TField("authorizationFailures", TType.LIST, (short) 3);
  
  public java.util.Map<TKeyExtent,Long> failedExtents;
  public java.util.List<TConstraintViolationSummary> violationSummaries;
  public java.util.List<TKeyExtent> authorizationFailures;
  
  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements TFieldIdEnum {
    FAILED_EXTENTS((short) 1, "failedExtents"),
    VIOLATION_SUMMARIES((short) 2, "violationSummaries"),
    AUTHORIZATION_FAILURES((short) 3, "authorizationFailures");
    
    private static final java.util.Map<String,_Fields> byName = new java.util.HashMap<String,_Fields>();
    
    static {
      for (_Fields field : java.util.EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }
    
    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch (fieldId) {
        case 1: // FAILED_EXTENTS
          return FAILED_EXTENTS;
        case 2: // VIOLATION_SUMMARIES
          return VIOLATION_SUMMARIES;
        case 3: // AUTHORIZATION_FAILURES
          return AUTHORIZATION_FAILURES;
        default:
          return null;
      }
    }
    
    /**
     * Find the _Fields constant that matches fieldId, throwing an exception if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null)
        throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }
    
    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }
    
    private final short _thriftId;
    private final String _fieldName;
    
    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }
    
    public short getThriftFieldId() {
      return _thriftId;
    }
    
    public String getFieldName() {
      return _fieldName;
    }
  }
  
  // isset id assignments
  
  public static final java.util.Map<_Fields,FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields,FieldMetaData> tmpMap = new java.util.EnumMap<_Fields,FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.FAILED_EXTENTS, new FieldMetaData("failedExtents", TFieldRequirementType.DEFAULT, new MapMetaData(TType.MAP, new StructMetaData(
        TType.STRUCT, TKeyExtent.class), new FieldValueMetaData(TType.I64))));
    tmpMap.put(_Fields.VIOLATION_SUMMARIES, new FieldMetaData("violationSummaries", TFieldRequirementType.DEFAULT, new ListMetaData(TType.LIST,
        new StructMetaData(TType.STRUCT, TConstraintViolationSummary.class))));
    tmpMap.put(_Fields.AUTHORIZATION_FAILURES, new FieldMetaData("authorizationFailures", TFieldRequirementType.DEFAULT, new ListMetaData(TType.LIST,
        new StructMetaData(TType.STRUCT, TKeyExtent.class))));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    FieldMetaData.addStructMetaDataMap(UpdateErrors.class, metaDataMap);
  }
  
  public UpdateErrors() {}
  
  public UpdateErrors(java.util.Map<TKeyExtent,Long> failedExtents, java.util.List<TConstraintViolationSummary> violationSummaries,
      java.util.List<TKeyExtent> authorizationFailures) {
    this();
    this.failedExtents = failedExtents;
    this.violationSummaries = violationSummaries;
    this.authorizationFailures = authorizationFailures;
  }
  
  /**
   * Performs a deep copy on <i>other</i>.
   */
  public UpdateErrors(UpdateErrors other) {
    if (other.isSetFailedExtents()) {
      java.util.Map<TKeyExtent,Long> __this__failedExtents = new java.util.HashMap<TKeyExtent,Long>();
      for (java.util.Map.Entry<TKeyExtent,Long> other_element : other.failedExtents.entrySet()) {
        
        TKeyExtent other_element_key = other_element.getKey();
        Long other_element_value = other_element.getValue();
        
        TKeyExtent __this__failedExtents_copy_key = new TKeyExtent(other_element_key);
        
        Long __this__failedExtents_copy_value = other_element_value;
        
        __this__failedExtents.put(__this__failedExtents_copy_key, __this__failedExtents_copy_value);
      }
      this.failedExtents = __this__failedExtents;
    }
    if (other.isSetViolationSummaries()) {
      java.util.List<TConstraintViolationSummary> __this__violationSummaries = new java.util.ArrayList<TConstraintViolationSummary>();
      for (TConstraintViolationSummary other_element : other.violationSummaries) {
        __this__violationSummaries.add(new TConstraintViolationSummary(other_element));
      }
      this.violationSummaries = __this__violationSummaries;
    }
    if (other.isSetAuthorizationFailures()) {
      java.util.List<TKeyExtent> __this__authorizationFailures = new java.util.ArrayList<TKeyExtent>();
      for (TKeyExtent other_element : other.authorizationFailures) {
        __this__authorizationFailures.add(new TKeyExtent(other_element));
      }
      this.authorizationFailures = __this__authorizationFailures;
    }
  }
  
  public UpdateErrors deepCopy() {
    return new UpdateErrors(this);
  }
  
  @Deprecated
  public UpdateErrors clone() {
    return new UpdateErrors(this);
  }
  
  public int getFailedExtentsSize() {
    return (this.failedExtents == null) ? 0 : this.failedExtents.size();
  }
  
  public void putToFailedExtents(TKeyExtent key, long val) {
    if (this.failedExtents == null) {
      this.failedExtents = new java.util.HashMap<TKeyExtent,Long>();
    }
    this.failedExtents.put(key, val);
  }
  
  public java.util.Map<TKeyExtent,Long> getFailedExtents() {
    return this.failedExtents;
  }
  
  public UpdateErrors setFailedExtents(java.util.Map<TKeyExtent,Long> failedExtents) {
    this.failedExtents = failedExtents;
    return this;
  }
  
  public void unsetFailedExtents() {
    this.failedExtents = null;
  }
  
  /** Returns true if field failedExtents is set (has been asigned a value) and false otherwise */
  public boolean isSetFailedExtents() {
    return this.failedExtents != null;
  }
  
  public void setFailedExtentsIsSet(boolean value) {
    if (!value) {
      this.failedExtents = null;
    }
  }
  
  public int getViolationSummariesSize() {
    return (this.violationSummaries == null) ? 0 : this.violationSummaries.size();
  }
  
  public java.util.Iterator<TConstraintViolationSummary> getViolationSummariesIterator() {
    return (this.violationSummaries == null) ? null : this.violationSummaries.iterator();
  }
  
  public void addToViolationSummaries(TConstraintViolationSummary elem) {
    if (this.violationSummaries == null) {
      this.violationSummaries = new java.util.ArrayList<TConstraintViolationSummary>();
    }
    this.violationSummaries.add(elem);
  }
  
  public java.util.List<TConstraintViolationSummary> getViolationSummaries() {
    return this.violationSummaries;
  }
  
  public UpdateErrors setViolationSummaries(java.util.List<TConstraintViolationSummary> violationSummaries) {
    this.violationSummaries = violationSummaries;
    return this;
  }
  
  public void unsetViolationSummaries() {
    this.violationSummaries = null;
  }
  
  /** Returns true if field violationSummaries is set (has been asigned a value) and false otherwise */
  public boolean isSetViolationSummaries() {
    return this.violationSummaries != null;
  }
  
  public void setViolationSummariesIsSet(boolean value) {
    if (!value) {
      this.violationSummaries = null;
    }
  }
  
  public int getAuthorizationFailuresSize() {
    return (this.authorizationFailures == null) ? 0 : this.authorizationFailures.size();
  }
  
  public java.util.Iterator<TKeyExtent> getAuthorizationFailuresIterator() {
    return (this.authorizationFailures == null) ? null : this.authorizationFailures.iterator();
  }
  
  public void addToAuthorizationFailures(TKeyExtent elem) {
    if (this.authorizationFailures == null) {
      this.authorizationFailures = new java.util.ArrayList<TKeyExtent>();
    }
    this.authorizationFailures.add(elem);
  }
  
  public java.util.List<TKeyExtent> getAuthorizationFailures() {
    return this.authorizationFailures;
  }
  
  public UpdateErrors setAuthorizationFailures(java.util.List<TKeyExtent> authorizationFailures) {
    this.authorizationFailures = authorizationFailures;
    return this;
  }
  
  public void unsetAuthorizationFailures() {
    this.authorizationFailures = null;
  }
  
  /** Returns true if field authorizationFailures is set (has been asigned a value) and false otherwise */
  public boolean isSetAuthorizationFailures() {
    return this.authorizationFailures != null;
  }
  
  public void setAuthorizationFailuresIsSet(boolean value) {
    if (!value) {
      this.authorizationFailures = null;
    }
  }
  
  @SuppressWarnings("unchecked")
  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
      case FAILED_EXTENTS:
        if (value == null) {
          unsetFailedExtents();
        } else {
          setFailedExtents((java.util.Map<TKeyExtent,Long>) value);
        }
        break;
      
      case VIOLATION_SUMMARIES:
        if (value == null) {
          unsetViolationSummaries();
        } else {
          setViolationSummaries((java.util.List<TConstraintViolationSummary>) value);
        }
        break;
      
      case AUTHORIZATION_FAILURES:
        if (value == null) {
          unsetAuthorizationFailures();
        } else {
          setAuthorizationFailures((java.util.List<TKeyExtent>) value);
        }
        break;
    
    }
  }
  
  public void setFieldValue(int fieldID, Object value) {
    setFieldValue(_Fields.findByThriftIdOrThrow(fieldID), value);
  }
  
  public Object getFieldValue(_Fields field) {
    switch (field) {
      case FAILED_EXTENTS:
        return getFailedExtents();
        
      case VIOLATION_SUMMARIES:
        return getViolationSummaries();
        
      case AUTHORIZATION_FAILURES:
        return getAuthorizationFailures();
        
    }
    throw new IllegalStateException();
  }
  
  public Object getFieldValue(int fieldId) {
    return getFieldValue(_Fields.findByThriftIdOrThrow(fieldId));
  }
  
  /** Returns true if field corresponding to fieldID is set (has been asigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    switch (field) {
      case FAILED_EXTENTS:
        return isSetFailedExtents();
      case VIOLATION_SUMMARIES:
        return isSetViolationSummaries();
      case AUTHORIZATION_FAILURES:
        return isSetAuthorizationFailures();
    }
    throw new IllegalStateException();
  }
  
  public boolean isSet(int fieldID) {
    return isSet(_Fields.findByThriftIdOrThrow(fieldID));
  }
  
  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof UpdateErrors)
      return this.equals((UpdateErrors) that);
    return false;
  }
  
  public boolean equals(UpdateErrors that) {
    if (that == null)
      return false;
    
    boolean this_present_failedExtents = true && this.isSetFailedExtents();
    boolean that_present_failedExtents = true && that.isSetFailedExtents();
    if (this_present_failedExtents || that_present_failedExtents) {
      if (!(this_present_failedExtents && that_present_failedExtents))
        return false;
      if (!this.failedExtents.equals(that.failedExtents))
        return false;
    }
    
    boolean this_present_violationSummaries = true && this.isSetViolationSummaries();
    boolean that_present_violationSummaries = true && that.isSetViolationSummaries();
    if (this_present_violationSummaries || that_present_violationSummaries) {
      if (!(this_present_violationSummaries && that_present_violationSummaries))
        return false;
      if (!this.violationSummaries.equals(that.violationSummaries))
        return false;
    }
    
    boolean this_present_authorizationFailures = true && this.isSetAuthorizationFailures();
    boolean that_present_authorizationFailures = true && that.isSetAuthorizationFailures();
    if (this_present_authorizationFailures || that_present_authorizationFailures) {
      if (!(this_present_authorizationFailures && that_present_authorizationFailures))
        return false;
      if (!this.authorizationFailures.equals(that.authorizationFailures))
        return false;
    }
    
    return true;
  }
  
  @Override
  public int hashCode() {
    return 0;
  }
  
  public int compareTo(UpdateErrors other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }
    
    int lastComparison = 0;
    UpdateErrors typedOther = (UpdateErrors) other;
    
    lastComparison = Boolean.valueOf(isSetFailedExtents()).compareTo(typedOther.isSetFailedExtents());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetFailedExtents()) {
      lastComparison = TBaseHelper.compareTo(this.failedExtents, typedOther.failedExtents);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetViolationSummaries()).compareTo(typedOther.isSetViolationSummaries());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetViolationSummaries()) {
      lastComparison = TBaseHelper.compareTo(this.violationSummaries, typedOther.violationSummaries);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetAuthorizationFailures()).compareTo(typedOther.isSetAuthorizationFailures());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetAuthorizationFailures()) {
      lastComparison = TBaseHelper.compareTo(this.authorizationFailures, typedOther.authorizationFailures);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }
  
  public void read(TProtocol iprot) throws TException {
    TField field;
    iprot.readStructBegin();
    while (true) {
      field = iprot.readFieldBegin();
      if (field.type == TType.STOP) {
        break;
      }
      switch (field.id) {
        case 1: // FAILED_EXTENTS
          if (field.type == TType.MAP) {
            {
              TMap _map25 = iprot.readMapBegin();
              this.failedExtents = new java.util.HashMap<TKeyExtent,Long>(2 * _map25.size);
              for (int _i26 = 0; _i26 < _map25.size; ++_i26) {
                TKeyExtent _key27;
                long _val28;
                _key27 = new TKeyExtent();
                _key27.read(iprot);
                _val28 = iprot.readI64();
                this.failedExtents.put(_key27, _val28);
              }
              iprot.readMapEnd();
            }
          } else {
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 2: // VIOLATION_SUMMARIES
          if (field.type == TType.LIST) {
            {
              TList _list29 = iprot.readListBegin();
              this.violationSummaries = new java.util.ArrayList<TConstraintViolationSummary>(_list29.size);
              for (int _i30 = 0; _i30 < _list29.size; ++_i30) {
                TConstraintViolationSummary _elem31;
                _elem31 = new TConstraintViolationSummary();
                _elem31.read(iprot);
                this.violationSummaries.add(_elem31);
              }
              iprot.readListEnd();
            }
          } else {
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 3: // AUTHORIZATION_FAILURES
          if (field.type == TType.LIST) {
            {
              TList _list32 = iprot.readListBegin();
              this.authorizationFailures = new java.util.ArrayList<TKeyExtent>(_list32.size);
              for (int _i33 = 0; _i33 < _list32.size; ++_i33) {
                TKeyExtent _elem34;
                _elem34 = new TKeyExtent();
                _elem34.read(iprot);
                this.authorizationFailures.add(_elem34);
              }
              iprot.readListEnd();
            }
          } else {
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        default:
          TProtocolUtil.skip(iprot, field.type);
      }
      iprot.readFieldEnd();
    }
    iprot.readStructEnd();
    
    // check for required fields of primitive type, which can't be checked in the validate method
    validate();
  }
  
  public void write(TProtocol oprot) throws TException {
    validate();
    
    oprot.writeStructBegin(STRUCT_DESC);
    if (this.failedExtents != null) {
      oprot.writeFieldBegin(FAILED_EXTENTS_FIELD_DESC);
      {
        oprot.writeMapBegin(new TMap(TType.STRUCT, TType.I64, this.failedExtents.size()));
        for (java.util.Map.Entry<TKeyExtent,Long> _iter35 : this.failedExtents.entrySet()) {
          _iter35.getKey().write(oprot);
          oprot.writeI64(_iter35.getValue());
        }
        oprot.writeMapEnd();
      }
      oprot.writeFieldEnd();
    }
    if (this.violationSummaries != null) {
      oprot.writeFieldBegin(VIOLATION_SUMMARIES_FIELD_DESC);
      {
        oprot.writeListBegin(new TList(TType.STRUCT, this.violationSummaries.size()));
        for (TConstraintViolationSummary _iter36 : this.violationSummaries) {
          _iter36.write(oprot);
        }
        oprot.writeListEnd();
      }
      oprot.writeFieldEnd();
    }
    if (this.authorizationFailures != null) {
      oprot.writeFieldBegin(AUTHORIZATION_FAILURES_FIELD_DESC);
      {
        oprot.writeListBegin(new TList(TType.STRUCT, this.authorizationFailures.size()));
        for (TKeyExtent _iter37 : this.authorizationFailures) {
          _iter37.write(oprot);
        }
        oprot.writeListEnd();
      }
      oprot.writeFieldEnd();
    }
    oprot.writeFieldStop();
    oprot.writeStructEnd();
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("UpdateErrors(");
    sb.append("failedExtents:");
    if (this.failedExtents == null) {
      sb.append("null");
    } else {
      sb.append(this.failedExtents);
    }
    sb.append(", ");
    sb.append("violationSummaries:");
    if (this.violationSummaries == null) {
      sb.append("null");
    } else {
      sb.append(this.violationSummaries);
    }
    sb.append(", ");
    sb.append("authorizationFailures:");
    if (this.authorizationFailures == null) {
      sb.append("null");
    } else {
      sb.append(this.authorizationFailures);
    }
    sb.append(")");
    return sb.toString();
  }
  
  public void validate() throws TException {
    // check for required fields
  }
  
}
