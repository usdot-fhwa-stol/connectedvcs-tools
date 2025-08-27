/*
* Copyright (C) 2025 LEIDOS.
*
* Licensed under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License. You may obtain a copy of
* the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
* License for the specific language governing permissions and limitations under
* the License.
*/

package gov.usdot.cv.timencoder;

public final class HeadingSlice {
  private int mask; // 16 bits

  public HeadingSlice() { this.mask = 0; }
  public HeadingSlice(int mask) { this.mask = mask & 0xFFFF; }

  private boolean get(int i) { return ((mask >>> i) & 1) == 1; }
  private void set(int i, boolean v) {
    if (v) mask |= (1 << i); else mask &= ~(1 << i);
    mask &= 0xFFFF;
  }

  public int intValue() { return mask & 0xFFFF; }
  public void setValue(int mask) { this.mask = mask & 0xFFFF; }

  public boolean isFrom000_0to022_5degrees() { return get(0); }
  public void setFrom000_0to022_5degrees(boolean v) { set(0, v); }

  public boolean isFrom022_5to045_0degrees() { return get(1); }
  public void setFrom022_5to045_0degrees(boolean v) { set(1, v); }

  public boolean isFrom045_0to067_5degrees() { return get(2); }
  public void setFrom045_0to067_5degrees(boolean v) { set(2, v); }

  public boolean isFrom067_5to090_0degrees() { return get(3); }
  public void setFrom067_5to090_0degrees(boolean v) { set(3, v); }

  public boolean isFrom090_0to112_5degrees() { return get(4); }
  public void setFrom090_0to112_5degrees(boolean v) { set(4, v); }

  public boolean isFrom112_5to135_0degrees() { return get(5); }
  public void setFrom112_5to135_0degrees(boolean v) { set(5, v); }

  public boolean isFrom135_0to157_5degrees() { return get(6); }
  public void setFrom135_0to157_5degrees(boolean v) { set(6, v); }

  public boolean isFrom157_5to180_0degrees() { return get(7); }
  public void setFrom157_5to180_0degrees(boolean v) { set(7, v); }

  public boolean isFrom180_0to202_5degrees() { return get(8); }
  public void setFrom180_0to202_5degrees(boolean v) { set(8, v); }

  public boolean isFrom202_5to225_0degrees() { return get(9); }
  public void setFrom202_5to225_0degrees(boolean v) { set(9, v); }

  public boolean isFrom225_0to247_5degrees() { return get(10); }
  public void setFrom225_0to247_5degrees(boolean v) { set(10, v); }

  public boolean isFrom247_5to270_0degrees() { return get(11); }
  public void setFrom247_5to270_0degrees(boolean v) { set(11, v); }

  public boolean isFrom270_0to292_5degrees() { return get(12); }
  public void setFrom270_0to292_5degrees(boolean v) { set(12, v); }

  public boolean isFrom292_5to315_0degrees() { return get(13); }
  public void setFrom292_5to315_0degrees(boolean v) { set(13, v); }

  public boolean isFrom315_0to337_5degrees() { return get(14); }
  public void setFrom315_0to337_5degrees(boolean v) { set(14, v); }

  public boolean isFrom337_5to360_0degrees() { return get(15); }
  public void setFrom337_5to360_0degrees(boolean v) { set(15, v); }
}
