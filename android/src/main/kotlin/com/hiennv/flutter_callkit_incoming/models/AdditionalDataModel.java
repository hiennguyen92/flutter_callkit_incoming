package com.hiennv.flutter_callkit_incoming.models;

public class AdditionalDataModel {
  private float id;
  private String custom_id;
  private String accident_description;
  private String status;
  private String created_at;
  Category CategoryObject;
  Sub_category Sub_categoryObject;
  Caller CallerObject;


 // Getter Methods 

  public float getId() {
    return id;
  }

  public String getCustom_id() {
    return custom_id;
  }

  public String getAccident_description() {
    return accident_description;
  }

  public String getStatus() {
    return status;
  }

  public String getCreated_at() {
    return created_at;
  }

  public Category getCategory() {
    return CategoryObject;
  }

  public Sub_category getSub_category() {
    return Sub_categoryObject;
  }

  public Caller getCaller() {
    return CallerObject;
  }

 // Setter Methods 

  public void setId( float id ) {
    this.id = id;
  }

  public void setCustom_id( String custom_id ) {
    this.custom_id = custom_id;
  }

  public void setAccident_description( String accident_description ) {
    this.accident_description = accident_description;
  }

  public void setStatus( String status ) {
    this.status = status;
  }

  public void setCreated_at( String created_at ) {
    this.created_at = created_at;
  }

  public void setCategory( Category categoryObject ) {
    this.CategoryObject = categoryObject;
  }

  public void setSub_category( Sub_category sub_categoryObject ) {
    this.Sub_categoryObject = sub_categoryObject;
  }

  public void setCaller( Caller callerObject ) {
    this.CallerObject = callerObject;
  }
}
