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
package gov.usdot.cv.esrimap.models;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EsriSpatialReference {
    @JsonProperty("wkid")
    @JsonAlias("wkid")
    private double wkid;

    @JsonProperty("vcsWkid")
    @JsonAlias("vcsWkid")
    private double vcsWkid;

    public EsriSpatialReference() {}

    public EsriSpatialReference(double wkid, double vcsWkid) {
        this.wkid = wkid;
        this.vcsWkid = vcsWkid;
    }

    public double getWkid() {
        return this.wkid;

    }public double getVcsWkid() {
        return this.vcsWkid;
    }

    public void setWkid(double wkid) {
        this.wkid = wkid;
    }

    public void setVcsWkid(double vcsWkid) {
        this.vcsWkid= vcsWkid;
    }

    @Override
    public String toString() {
        return "{" +
            " wkid='" + getWkid() + "'" +
            " wkid='" + getVcsWkid() + "'" +
            "}";
    }
}
