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

public  class ITIScodesAndText {
    private Integer code;   // use when holding an ITIS code
    private String  text;   // use when holding an ITIS text phrase

    public ITIScodesAndText() {
        // empty by default
    }

    public static ITIScodesAndText ofCode(int code) {
        ITIScodesAndText x = new ITIScodesAndText();
        x.setCode(code);
        return x;
    }

    public static ITIScodesAndText ofText(String text) {
        ITIScodesAndText x = new ITIScodesAndText();
        x.setText(text);
        return x;
    }

    // Setters: setting one clears the other (simple CHOICE behavior)
    public void setCode(int code) {
        this.code = code;
        this.text = null;
    }

    public void setText(String text) {
        this.text = text;
        this.code = null;
    }

    // Getters
    public Integer getCode() { return code; }
    public String getText() { return text; }

    public boolean isCode() { return code != null; }
    public boolean isText() { return text != null; }

    @Override
    public String toString() {
        if (isCode()) {
            return "ITISTextCode{code='" + code + "'}";
        } else if (isText()) {
            return "ITISTextCode{text='" + text + "'}";
        } else {
            return "ITISTextCode{}";
        }
    }
}
