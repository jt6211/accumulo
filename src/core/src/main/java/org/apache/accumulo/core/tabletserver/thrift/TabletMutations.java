/**
 * Autogenerated by Thrift
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 */
package org.apache.accumulo.core.tabletserver.thrift;



import org.apache.thrift.*;
import org.apache.thrift.meta_data.*;
import org.apache.thrift.protocol.*;

@SuppressWarnings("serial")
public class TabletMutations implements TBase<TabletMutations, TabletMutations._Fields>, java.io.Serializable, Cloneable {
  private static final TStruct STRUCT_DESC = new TStruct("TabletMutations");

  private static final TField TABLET_ID_FIELD_DESC = new TField("tabletID", TType.I32, (short)1);
  private static final TField SEQ_FIELD_DESC = new TField("seq", TType.I64, (short)2);
  private static final TField MUTATIONS_FIELD_DESC = new TField("mutations", TType.LIST, (short)3);

  public int tabletID;
  public long seq;
  public java.util.List<org.apache.accumulo.core.data.thrift.TMutation> mutations;

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements TFieldIdEnum {
    TABLET_ID((short)1, "tabletID"),
    SEQ((short)2, "seq"),
    MUTATIONS((short)3, "mutations");

    private static final java.util.Map<String, _Fields> byName = new java.util.HashMap<String, _Fields>();

    static {
      for (_Fields field : java.util.EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // TABLET_ID
          return TABLET_ID;
        case 2: // SEQ
          return SEQ;
        case 3: // MUTATIONS
          return MUTATIONS;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
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
  private static final int __TABLETID_ISSET_ID = 0;
  private static final int __SEQ_ISSET_ID = 1;
  private java.util.BitSet __isset_bit_vector = new java.util.BitSet(2);

  public static final java.util.Map<_Fields, FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields, FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.TABLET_ID, new FieldMetaData("tabletID", TFieldRequirementType.DEFAULT, 
        new FieldValueMetaData(TType.I32        , "TabletID")));
    tmpMap.put(_Fields.SEQ, new FieldMetaData("seq", TFieldRequirementType.DEFAULT, 
        new FieldValueMetaData(TType.I64)));
    tmpMap.put(_Fields.MUTATIONS, new FieldMetaData("mutations", TFieldRequirementType.DEFAULT, 
        new ListMetaData(TType.LIST, 
            new StructMetaData(TType.STRUCT, org.apache.accumulo.core.data.thrift.TMutation.class))));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    FieldMetaData.addStructMetaDataMap(TabletMutations.class, metaDataMap);
  }

  public TabletMutations() {
  }

  public TabletMutations(
    int tabletID,
    long seq,
    java.util.List<org.apache.accumulo.core.data.thrift.TMutation> mutations)
  {
    this();
    this.tabletID = tabletID;
    setTabletIDIsSet(true);
    this.seq = seq;
    setSeqIsSet(true);
    this.mutations = mutations;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public TabletMutations(TabletMutations other) {
    __isset_bit_vector.clear();
    __isset_bit_vector.or(other.__isset_bit_vector);
    this.tabletID = other.tabletID;
    this.seq = other.seq;
    if (other.isSetMutations()) {
      java.util.List<org.apache.accumulo.core.data.thrift.TMutation> __this__mutations = new java.util.ArrayList<org.apache.accumulo.core.data.thrift.TMutation>();
      for (org.apache.accumulo.core.data.thrift.TMutation other_element : other.mutations) {
        __this__mutations.add(new org.apache.accumulo.core.data.thrift.TMutation(other_element));
      }
      this.mutations = __this__mutations;
    }
  }

  public TabletMutations deepCopy() {
    return new TabletMutations(this);
  }

  @Deprecated
  public TabletMutations clone() {
    return new TabletMutations(this);
  }

  public int getTabletID() {
    return this.tabletID;
  }

  public TabletMutations setTabletID(int tabletID) {
    this.tabletID = tabletID;
    setTabletIDIsSet(true);
    return this;
  }

  public void unsetTabletID() {
    __isset_bit_vector.clear(__TABLETID_ISSET_ID);
  }

  /** Returns true if field tabletID is set (has been asigned a value) and false otherwise */
  public boolean isSetTabletID() {
    return __isset_bit_vector.get(__TABLETID_ISSET_ID);
  }

  public void setTabletIDIsSet(boolean value) {
    __isset_bit_vector.set(__TABLETID_ISSET_ID, value);
  }

  public long getSeq() {
    return this.seq;
  }

  public TabletMutations setSeq(long seq) {
    this.seq = seq;
    setSeqIsSet(true);
    return this;
  }

  public void unsetSeq() {
    __isset_bit_vector.clear(__SEQ_ISSET_ID);
  }

  /** Returns true if field seq is set (has been asigned a value) and false otherwise */
  public boolean isSetSeq() {
    return __isset_bit_vector.get(__SEQ_ISSET_ID);
  }

  public void setSeqIsSet(boolean value) {
    __isset_bit_vector.set(__SEQ_ISSET_ID, value);
  }

  public int getMutationsSize() {
    return (this.mutations == null) ? 0 : this.mutations.size();
  }

  public java.util.Iterator<org.apache.accumulo.core.data.thrift.TMutation> getMutationsIterator() {
    return (this.mutations == null) ? null : this.mutations.iterator();
  }

  public void addToMutations(org.apache.accumulo.core.data.thrift.TMutation elem) {
    if (this.mutations == null) {
      this.mutations = new java.util.ArrayList<org.apache.accumulo.core.data.thrift.TMutation>();
    }
    this.mutations.add(elem);
  }

  public java.util.List<org.apache.accumulo.core.data.thrift.TMutation> getMutations() {
    return this.mutations;
  }

  public TabletMutations setMutations(java.util.List<org.apache.accumulo.core.data.thrift.TMutation> mutations) {
    this.mutations = mutations;
    return this;
  }

  public void unsetMutations() {
    this.mutations = null;
  }

  /** Returns true if field mutations is set (has been asigned a value) and false otherwise */
  public boolean isSetMutations() {
    return this.mutations != null;
  }

  public void setMutationsIsSet(boolean value) {
    if (!value) {
      this.mutations = null;
    }
  }

  @SuppressWarnings("unchecked")
  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case TABLET_ID:
      if (value == null) {
        unsetTabletID();
      } else {
        setTabletID((Integer)value);
      }
      break;

    case SEQ:
      if (value == null) {
        unsetSeq();
      } else {
        setSeq((Long)value);
      }
      break;

    case MUTATIONS:
      if (value == null) {
        unsetMutations();
      } else {
        setMutations((java.util.List<org.apache.accumulo.core.data.thrift.TMutation>)value);
      }
      break;

    }
  }

  public void setFieldValue(int fieldID, Object value) {
    setFieldValue(_Fields.findByThriftIdOrThrow(fieldID), value);
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case TABLET_ID:
      return new Integer(getTabletID());

    case SEQ:
      return new Long(getSeq());

    case MUTATIONS:
      return getMutations();

    }
    throw new IllegalStateException();
  }

  public Object getFieldValue(int fieldId) {
    return getFieldValue(_Fields.findByThriftIdOrThrow(fieldId));
  }

  /** Returns true if field corresponding to fieldID is set (has been asigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    switch (field) {
    case TABLET_ID:
      return isSetTabletID();
    case SEQ:
      return isSetSeq();
    case MUTATIONS:
      return isSetMutations();
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
    if (that instanceof TabletMutations)
      return this.equals((TabletMutations)that);
    return false;
  }

  public boolean equals(TabletMutations that) {
    if (that == null)
      return false;

    boolean this_present_tabletID = true;
    boolean that_present_tabletID = true;
    if (this_present_tabletID || that_present_tabletID) {
      if (!(this_present_tabletID && that_present_tabletID))
        return false;
      if (this.tabletID != that.tabletID)
        return false;
    }

    boolean this_present_seq = true;
    boolean that_present_seq = true;
    if (this_present_seq || that_present_seq) {
      if (!(this_present_seq && that_present_seq))
        return false;
      if (this.seq != that.seq)
        return false;
    }

    boolean this_present_mutations = true && this.isSetMutations();
    boolean that_present_mutations = true && that.isSetMutations();
    if (this_present_mutations || that_present_mutations) {
      if (!(this_present_mutations && that_present_mutations))
        return false;
      if (!this.mutations.equals(that.mutations))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  public int compareTo(TabletMutations other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;
    TabletMutations typedOther = (TabletMutations)other;

    lastComparison = Boolean.valueOf(isSetTabletID()).compareTo(typedOther.isSetTabletID());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetTabletID()) {      lastComparison = TBaseHelper.compareTo(this.tabletID, typedOther.tabletID);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetSeq()).compareTo(typedOther.isSetSeq());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetSeq()) {      lastComparison = TBaseHelper.compareTo(this.seq, typedOther.seq);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetMutations()).compareTo(typedOther.isSetMutations());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetMutations()) {      lastComparison = TBaseHelper.compareTo(this.mutations, typedOther.mutations);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public void read(TProtocol iprot) throws TException {
    TField field;
    iprot.readStructBegin();
    while (true)
    {
      field = iprot.readFieldBegin();
      if (field.type == TType.STOP) { 
        break;
      }
      switch (field.id) {
        case 1: // TABLET_ID
          if (field.type == TType.I32) {
            this.tabletID = iprot.readI32();
            setTabletIDIsSet(true);
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 2: // SEQ
          if (field.type == TType.I64) {
            this.seq = iprot.readI64();
            setSeqIsSet(true);
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 3: // MUTATIONS
          if (field.type == TType.LIST) {
            {
              TList _list22 = iprot.readListBegin();
              this.mutations = new java.util.ArrayList<org.apache.accumulo.core.data.thrift.TMutation>(_list22.size);
              for (int _i23 = 0; _i23 < _list22.size; ++_i23)
              {
                org.apache.accumulo.core.data.thrift.TMutation _elem24;
                _elem24 = new org.apache.accumulo.core.data.thrift.TMutation();
                _elem24.read(iprot);
                this.mutations.add(_elem24);
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
    oprot.writeFieldBegin(TABLET_ID_FIELD_DESC);
    oprot.writeI32(this.tabletID);
    oprot.writeFieldEnd();
    oprot.writeFieldBegin(SEQ_FIELD_DESC);
    oprot.writeI64(this.seq);
    oprot.writeFieldEnd();
    if (this.mutations != null) {
      oprot.writeFieldBegin(MUTATIONS_FIELD_DESC);
      {
        oprot.writeListBegin(new TList(TType.STRUCT, this.mutations.size()));
        for (org.apache.accumulo.core.data.thrift.TMutation _iter25 : this.mutations)
        {
          _iter25.write(oprot);
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
    StringBuilder sb = new StringBuilder("TabletMutations(");
    sb.append("tabletID:");
    sb.append(this.tabletID);
    sb.append(", ");
    sb.append("seq:");
    sb.append(this.seq);
    sb.append(", ");
    sb.append("mutations:");
    if (this.mutations == null) {
      sb.append("null");
    } else {
      sb.append(this.mutations);
    }
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws TException {
    // check for required fields
  }

}
