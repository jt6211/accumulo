/**
 * Autogenerated by Thrift
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 */
package org.apache.accumulo.core.master.thrift;

import org.apache.thrift.TEnum;

public enum MasterGoalState implements TEnum {
  CLEAN_STOP(0), SAFE_MODE(1), NORMAL(2);
  
  private final int value;
  
  private MasterGoalState(int value) {
    this.value = value;
  }
  
  /**
   * Get the integer value of this enum value, as defined in the Thrift IDL.
   */
  public int getValue() {
    return value;
  }
  
  /**
   * Find a the enum type by its integer value, as defined in the Thrift IDL.
   * 
   * @return null if the value is not found.
   */
  public static MasterGoalState findByValue(int value) {
    switch (value) {
      case 0:
        return CLEAN_STOP;
      case 1:
        return SAFE_MODE;
      case 2:
        return NORMAL;
      default:
        return null;
    }
  }
}
