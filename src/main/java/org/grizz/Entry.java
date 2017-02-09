package org.grizz;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Entry {
  private List<Voter> voters;
}
