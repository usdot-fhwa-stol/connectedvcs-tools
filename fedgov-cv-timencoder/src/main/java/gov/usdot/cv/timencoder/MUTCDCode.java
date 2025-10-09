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

public enum MUTCDCode {

    none(0),
    regulatory(1),
    warning(2),
    maintenance(3),
    motoristService(4),
    guide(5),
    rec(6);

    private final long value;

    MUTCDCode(long value) {
        this.value = value;
    }    

    public long getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "MUTCDCode{" +
                "value=" + value +
                '}';
    }
}
