package org.grizz;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class User {
  @SerializedName("avatar_big")
  private String avatar;
  private String login;
}
