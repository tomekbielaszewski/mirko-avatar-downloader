package org.grizz;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Voter {
  @SerializedName("author")
  private String username;
}
