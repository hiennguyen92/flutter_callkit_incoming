package com.hiennv.flutter_callkit_incoming.models;

public class AdditionalDataModel {private String address;
  private String sub_category;
  private String custom_id;
  private String accident_description;
  private String created_at;
  private String subscriber_full_name;
  private float id;
  private String category;
  private String status;

  // Getter Methods

  public String getAddress() {
    return address;
  }

  public String getSub_category() {
    return sub_category;
  }

  public String getCustom_id() {
    return custom_id;
  }

  public String getAccident_description() {
    return accident_description;
  }

  public String getCreated_at() {
    return created_at;
  }

  public String getSubscriber_full_name() {
    return subscriber_full_name;
  }

  public float getId() {
    return id;
  }

  public String getCategory() {
    return category;
  }

  public String getStatus() {
    return status;
  }

  // Setter Methods

  public void setAddress( String address ) {
    this.address = address;
  }

  public void setSub_category( String sub_category ) {
    this.sub_category = sub_category;
  }

  public void setCustom_id( String custom_id ) {
    this.custom_id = custom_id;
  }

  public void setAccident_description( String accident_description ) {
    this.accident_description = accident_description;
  }

  public void setCreated_at( String created_at ) {
    this.created_at = created_at;
  }

  public void setSubscriber_full_name( String subscriber_full_name ) {
    this.subscriber_full_name = subscriber_full_name;
  }

  public void setId( float id ) {
    this.id = id;
  }

  public void setCategory( String category ) {
    this.category = category;
  }

  public void setStatus( String status ) {
    this.status = status;
  }
}
