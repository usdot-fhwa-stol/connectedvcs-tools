/*
 * Copyright (C) 2024 LEIDOS.
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
package gov.usdot.cv.googlemap.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleSearchTextResponse {
    private String formattedAddress;
    private GoogleLocation location;

    public GoogleSearchTextResponse() {
    }

    public GoogleSearchTextResponse(String formattedAddress, GoogleLocation location) {
        this.formattedAddress = formattedAddress;
        this.location = location;
    }

    public String getFormattedAddress() {
        return this.formattedAddress;
    }

    public void setFormattedAddress(String formattedAddress) {
        this.formattedAddress = formattedAddress;
    }

    public GoogleLocation getLocation() {
        return this.location;
    }

    public void setLocation(GoogleLocation location) {
        this.location = location;
    }

    public GoogleSearchTextResponse formattedAddress(String formattedAddress) {
        setFormattedAddress(formattedAddress);
        return this;
    }

    public GoogleSearchTextResponse location(GoogleLocation location) {
        setLocation(location);
        return this;
    }

    @Override
    public String toString() {
        return "{" +
            " formattedAddress='" + getFormattedAddress() + "'" +
            ", location='" + getLocation() + "'" +
            "}";
    }
    
}
