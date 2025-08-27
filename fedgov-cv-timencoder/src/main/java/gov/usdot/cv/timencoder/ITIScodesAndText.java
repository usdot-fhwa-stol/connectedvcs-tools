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

public class ITIScodesAndText {
    private ITIScodes code; 
    private String text;    

    public ITIScodes getCode() {
        return code;
    }

    public void setCode(ITIScodes code) {
        this.code = code;
        this.text = null; 
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        this.code = null; 
    }

    @Override
    public String toString() {
        if (code != null) {
            return "ITIScodesAndText{code=" + code.intValue() + "}";
        } else if (text != null) {
            return "ITIScodesAndText{text='" + text + "'}";
        } else {
            return "ITIScodesAndText{}";
        }
    }
}
