package com.zhangzhao.app;

import com.zhangzhao.annotation.Getter;
import com.zhangzhao.annotation.PrimaryKey;

@PrimaryKey
@Getter
public class Dummy {

  private String name;

  public Dummy(String name) {
    this.name = name;
  }
}
