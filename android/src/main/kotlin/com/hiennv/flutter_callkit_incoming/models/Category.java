package com.hiennv.flutter_callkit_incoming.models;

public class Category {
  private float id;
  private String name;
  private String status;
  private float parent_id;


 // Getter Methods 

  public float getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getStatus() {
    return status;
  }

  public float getParent_id() {
    return parent_id;
  }

 // Setter Methods 

  public void setId( float id ) {
    this.id = id;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public void setStatus( String status ) {
    this.status = status;
  }

  public void setParent_id( float parent_id ) {
    this.parent_id = parent_id;
  }
}